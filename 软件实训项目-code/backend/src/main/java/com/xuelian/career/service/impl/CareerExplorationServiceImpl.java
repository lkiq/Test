package com.xuelian.career.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuelian.career.dto.request.CareerExplorationRequest;
import com.xuelian.career.dto.response.CareerDirectionResponse;
import com.xuelian.career.dto.response.JobMatchResponse;
import com.xuelian.career.entity.*;
import com.xuelian.career.mapper.*;
import com.xuelian.career.service.CareerExplorationService;
import com.xuelian.career.service.DeepSeekService;
import com.xuelian.career.service.JobMatchingService;
import com.xuelian.career.util.PromptTemplateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * AI职业方向探索服务实现 - 支持意图分流：职业方向推荐 / 通用行业咨询
 * 集成 JobMatchingService 真实匹配度，解决推荐方向失准问题
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CareerExplorationServiceImpl implements CareerExplorationService {

    private final CareerProfileMapper profileMapper;
    private final AssessmentResultMapper assessmentResultMapper;
    private final JobPositionMapper jobPositionMapper;
    private final RecommendationRecordMapper recordMapper;
    private final DeepSeekService deepSeekService;
    private final PromptTemplateUtil promptUtil;
    private final ObjectMapper objectMapper;
    private final JobMatchingService jobMatchingService;

    /** 意图：职业方向推荐 */
    private static final String INTENT_RECOMMENDATION = "RECOMMENDATION";
    /** 意图：通用行业咨询 */
    private static final String INTENT_GENERAL = "GENERAL";

    /** Prompt 中可推荐岗位最大数量，防止 prompt 过长导致 AI 超时 */
    private static final int MAX_PROMPT_JOBS = 10;
    /** 对话历史保留最大条数 */
    private static final int MAX_HISTORY_MESSAGES = 6;
    /** 单条对话历史最大长度 */
    private static final int MAX_HISTORY_LENGTH = 300;
    /** 兜底推荐最大数量 */
    private static final int MAX_FALLBACK_DIRECTIONS = 5;

    @Override
    public CareerDirectionResponse explore(Long userId, CareerExplorationRequest req) {
        try {
            // 获取用户画像和测评结果
            CareerProfile profile = profileMapper.selectOne(
                    new LambdaQueryWrapper<CareerProfile>().eq(CareerProfile::getUserId, userId));
            AssessmentResult latestResult = assessmentResultMapper.selectOne(
                    new LambdaQueryWrapper<AssessmentResult>().eq(AssessmentResult::getUserId, userId)
                            .orderByDesc(AssessmentResult::getCreatedAt).last("LIMIT 1"));
            List<JobPosition> positions = jobPositionMapper.selectList(
                    new LambdaQueryWrapper<JobPosition>().eq(JobPosition::getIsDeleted, 0));

            String question = req.getPreferences() != null ? req.getPreferences() : "";
            boolean isGeneralQuestion = isLikelyGeneralQuestion(question);
            boolean fromAssessment = "ASSESSMENT".equals(req.getSource());

            // 新增：信息不足时先主动追问，帮助用户明确职业方向
            if (!isGeneralQuestion && isInformationInsufficient(profile, latestResult, req, fromAssessment)) {
                return askForClarification(userId, req, profile, latestResult);
            }

            // API 不可用时直接兜底
            if (!deepSeekService.isAvailable()) {
                return fallbackRecommend(userId, req, profile, latestResult, positions, isGeneralQuestion);
            }

            // 1. 轻量意图识别：判断用户是在做岗位推荐还是通用咨询
            // 本地规则优先：
            // - 只要用户明确表达个人职业兴趣/目标，直接按 RECOMMENDATION 处理，避免 AI 把 "我想做 Java 后端" 误判为 GENERAL
            // - 只要问题包含明显通用咨询词（趋势/薪资/行业等），优先按 GENERAL 处理，避免无关问题也推荐岗位
            String intent;
            if (isLikelyGeneralQuestion(question)) {
                // 先尝试通用问答；若 AI 调用失败，再降级到职业推荐（此时仍按通用问题兜底）
                CareerDirectionResponse generalResp = answerGeneralQuestion(userId, req, profile, latestResult, positions);
                if (generalResp != null) {
                    return generalResp;
                }
                intent = INTENT_GENERAL;
                isGeneralQuestion = true;
                log.info("本地规则命中 GENERAL，通用问答失败降级到推荐兜底: question={}", question);
            } else {
                intent = INTENT_RECOMMENDATION;
                log.info("本地规则命中 RECOMMENDATION: question={}", question);
            }
            if (INTENT_GENERAL.equals(intent)) {
                // 已通过 answerGeneralQuestion 处理，若走到这里说明通用问答失败
                // 直接使用通用问题兜底，禁止再走职业推荐链路，避免通用问题被误推荐岗位
                isGeneralQuestion = true;
                log.warn("通用问答 AI 调用失败，直接使用通用问题兜底，防止误推荐岗位: question={}", question);
                return fallbackRecommend(userId, req, profile, latestResult, positions, true);
            }

            // 3. 职业方向推荐：无画像用户也走职业推荐链路
            // career_exploration 模板已含"无画像兜底"指令，doCareerRecommendation/fallbackRecommend 均可处理无画像场景
            // 不应在此处降级到通用咨询，否则明确请求职业推荐的用户只会收到文本回复而无岗位
            if (profile == null) {
                log.info("用户 {} 无画像数据，但用户明确请求职业推荐，直接进入推荐链路（AI+兜底可处理空画像）", userId);
            }
            return doCareerRecommendation(userId, req, profile, latestResult, positions, isGeneralQuestion);

        } catch (Exception e) {
            log.error("职业方向探索异常", e);
            return fallbackSimple();
        }
    }

    /**
     * 职业方向推荐核心逻辑
     * - 基于 JobMatchingService 构建真实匹配度
     * - 仅向 AI 提供 Top10 高匹配岗位，缩短 prompt
     * - AI 返回后用真实匹配度覆盖分数，并过滤未命中岗位
     * - 最终构建双队列：primaryDirections（意向方向）+ fallbackDirections（稳妥备选）
     */
    private CareerDirectionResponse doCareerRecommendation(Long userId, CareerExplorationRequest req,
                                                           CareerProfile profile, AssessmentResult latestResult,
                                                           List<JobPosition> positions, boolean isGeneralQuestion) {
        try {
            boolean fromAssessment = "ASSESSMENT".equals(req.getSource());

            // 解析用户当前表达的兴趣和城市
            InterestCity resolved = extractInterestCity(req.getPreferences(), profile, fromAssessment);

            // 测评场景且未提取到兴趣时，用测评优势维度作为临时兴趣方向
            String effectiveInterest = resolved.interest;
            if ((effectiveInterest == null || effectiveInterest.isBlank()) && fromAssessment && latestResult != null) {
                String advantageDim = extractAssessmentAdvantageDimension(latestResult);
                effectiveInterest = mapDimensionToInterest(advantageDim);
                log.info("测评场景未提取到兴趣，使用测评优势维度: dimension={}, interest={}", advantageDim, effectiveInterest);
            }

            // 构建真实匹配度：基于画像 + 当前兴趣/城市 + 测评结果
            Map<String, JobMatchResponse> realScoreMap = buildRealScoreMap(
                    userId, effectiveInterest, resolved.city, latestResult);

            // 仅取 Top10 岗位进入 prompt，避免过长导致 AI 超时
            List<JobPosition> topPositions = selectTopPositionsForPrompt(realScoreMap, positions);

            String template = promptUtil.loadTemplate("career_exploration");
            Map<String, String> params = buildCareerParams(req, profile, latestResult, topPositions);
            String prompt = promptUtil.renderTemplate(template, params);

            // 对话式场景不使用缓存：用户每次输入不同问题，应实时生成不同回复
            String response = deepSeekService.callAPI("你是一位资深的职业规划导师", prompt, 12000L, 768, 0.3);
            Map<String, Object> result = deepSeekService.parseJSONResponse(response);
            if (result != null && result.containsKey("directions")) {
                CareerDirectionResponse aiResp = objectMapper.convertValue(result, CareerDirectionResponse.class);
                aiResp.setSource("AI");
                aiResp.setCreatedAt(LocalDateTime.now());

                // 用真实匹配度覆盖 AI 生成的 matchScore 并过滤未命中岗位
                CareerDirectionResponse processedResp = postProcessRecommendations(
                        aiResp, realScoreMap, effectiveInterest, latestResult);
                saveRecord(userId, "CAREER_EXPLORATION", req.getPreferences(),
                        Map.of("directions", processedResp.getDirections(),
                                "primaryDirections", processedResp.getPrimaryDirections(),
                                "fallbackDirections", processedResp.getFallbackDirections(),
                                "overallAnalysis", processedResp.getOverallAnalysis()), "AI");
                return processedResp;
            }
            log.warn("职业推荐 AI 返回结果缺少 directions 字段，使用兜底方案");
        } catch (Exception e) {
            log.warn("AI 职业探索失败，使用兜底方案: {}", e.getMessage());
        }
        return fallbackRecommend(userId, req, profile, latestResult, positions, isGeneralQuestion);
    }

    /**
     * 通用行业咨询回答
     * 返回 CareerDirectionResponse：answer 放入 overallAnalysis，directions 为空
     */
    private CareerDirectionResponse answerGeneralQuestion(Long userId, CareerExplorationRequest req,
                                                          CareerProfile profile, AssessmentResult latestResult,
                                                          List<JobPosition> positions) {
        try {
            String question = req.getPreferences() != null ? req.getPreferences() : "";
            if (question.isBlank()) {
                log.warn("通用咨询问题为空，降级到职业推荐");
                return null;
            }
            // 通用咨询不强制要求岗位匹配，不传入岗位列表，避免 AI 混淆为推荐任务
            String template = promptUtil.loadTemplate("career_general_qa");
            Map<String, String> params = buildCareerParams(req, profile, latestResult, Collections.emptyList());
            params.put("question", question);
            String prompt = promptUtil.renderTemplate(template, params);

            String response = deepSeekService.callAPI("你是一位以职业规划见长的AI助手", prompt, 8000L, 768, 0.7);
            Map<String, Object> result = deepSeekService.parseJSONResponse(response);
            String answer = null;
            if (result != null) {
                if (result.get("answer") instanceof String a) {
                    answer = a;
                } else if (result.get("overallAnalysis") instanceof String o) {
                    // AI 误返回了职业推荐格式，提取 overallAnalysis 作为文本回答
                    answer = o;
                    log.warn("通用咨询 AI 误返回职业推荐格式，已提取 overallAnalysis 作为文本回答");
                }
            }
            // AI 未按 JSON 返回时，直接将原始响应作为文本回答
            if (answer == null && response != null && !response.isBlank()) {
                answer = response.trim();
                log.warn("通用咨询 AI 未按 JSON 返回，已使用原始响应作为文本回答");
            }
            if (answer != null && !answer.isBlank()) {
                CareerDirectionResponse resp = CareerDirectionResponse.builder()
                        .overallAnalysis(answer)
                        .directions(new ArrayList<>())
                        .primaryDirections(new ArrayList<>())
                        .fallbackDirections(new ArrayList<>())
                        .source("AI")
                        .createdAt(LocalDateTime.now())
                        .build();
                saveRecord(userId, "CAREER_EXPLORATION", req.getPreferences(),
                        Map.of("answer", answer, "intent", INTENT_GENERAL), "AI");
                return resp;
            }
            log.warn("通用咨询 AI 返回结果缺少有效文本字段，降级到职业推荐");
        } catch (Exception e) {
            log.warn("通用咨询 AI 调用失败，降级到职业推荐: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 构建职业探索相关 prompt 参数（职业推荐与通用咨询共用）
     * 精简原则：与测评集成后避免 prompt 过长导致 AI 调用失败
     * - 删除完整 assessment_json，仅保留 assessment_summary
     * - 从测评页进入时 preferences 已含测评摘要，assessment_summary 进一步简化
     */
    private Map<String, String> buildCareerParams(CareerExplorationRequest req,
                                                  CareerProfile profile, AssessmentResult latestResult,
                                                  List<JobPosition> positions) {
        Map<String, String> params = new HashMap<>();
        try {
            params.put("profile_json", objectMapper.writeValueAsString(profile));
        } catch (Exception e) {
            params.put("profile_json", "（未填写）");
        }
        boolean fromAssessment = "ASSESSMENT".equals(req.getSource());
        params.put("assessment_summary", fromAssessment
                ? buildBriefAssessmentSummary(latestResult)
                : buildAssessmentSummary(latestResult));
        params.put("preferences", req.getPreferences() != null ? req.getPreferences() : "");
        try {
            params.put("job_positions", objectMapper.writeValueAsString(positions.stream()
                    .map(p -> Map.of("id", p.getId(), "title", p.getTitle(), "direction", p.getDirection()))
                    .collect(Collectors.toList())));
        } catch (Exception e) {
            params.put("job_positions", "[]");
        }
        params.put("conversation_history", buildConversationHistory(req.getHistory()));
        return params;
    }

    /**
     * 简化版测评摘要（用于 from-assessment 入口，preferences 已含详细测评信息）
     */
    private String buildBriefAssessmentSummary(AssessmentResult result) {
        if (result == null) return "（暂无测评数据）";
        return String.format("综合总分：%.0f 分。", result.getTotalScore());
    }

    /**
     * 完整版测评摘要（用于普通探索入口）
     */
    private String buildAssessmentSummary(AssessmentResult result) {
        if (result == null) return "（暂无测评数据）";
        Map<String, Double> scores = new LinkedHashMap<>();
        scores.put("编程能力", result.getProgrammingScore());
        scores.put("逻辑推理", result.getLogicScore());
        scores.put("产品思维", result.getProductScore());
        scores.put("技术素养", result.getTechScore());
        scores.put("沟通表达", result.getCommunicationScore());
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("综合总分：%.0f 分；五维得分：", result.getTotalScore()));
        scores.forEach((k, v) -> sb.append(String.format("%s=%.0f ", k, v == null ? 0 : v)));
        sb.append("。");
        return sb.toString();
    }

    /**
     * 构建真实匹配度映射：岗位标题 -> JobMatchResponse
     * 调用 JobMatchingService 动态权重算法，结合画像、当前兴趣/城市、测评结果
     */
    private Map<String, JobMatchResponse> buildRealScoreMap(Long userId, String interest, String city,
                                                            AssessmentResult latestResult) {
        try {
            List<JobMatchResponse> matches = jobMatchingService.recommendJobs(userId, interest, city, latestResult);
            return matches.stream()
                    .filter(m -> m.getTitle() != null && !m.getTitle().isBlank())
                    .collect(Collectors.toMap(JobMatchResponse::getTitle, m -> m, (a, b) -> a, LinkedHashMap::new));
        } catch (Exception e) {
            log.warn("构建真实匹配度失败: {}", e.getMessage());
            return new LinkedHashMap<>();
        }
    }

    /**
     * 从真实匹配度结果中选取 Top10 岗位进入 AI prompt
     * 若匹配度列表为空，则回退到全部岗位前 10 个，保证 prompt 不空
     */
    private List<JobPosition> selectTopPositionsForPrompt(Map<String, JobMatchResponse> realScoreMap,
                                                          List<JobPosition> allPositions) {
        if (realScoreMap == null || realScoreMap.isEmpty()) {
            return allPositions.stream().limit(MAX_PROMPT_JOBS).collect(Collectors.toList());
        }
        List<JobPosition> selected = new ArrayList<>();
        for (JobMatchResponse match : realScoreMap.values()) {
            allPositions.stream()
                    .filter(p -> match.getTitle().equals(p.getTitle()))
                    .findFirst()
                    .ifPresent(selected::add);
            if (selected.size() >= MAX_PROMPT_JOBS) break;
        }
        // 若按匹配度未选够，用全部岗位补齐
        if (selected.size() < MAX_PROMPT_JOBS) {
            for (JobPosition p : allPositions) {
                if (!selected.contains(p)) {
                    selected.add(p);
                    if (selected.size() >= MAX_PROMPT_JOBS) break;
                }
            }
        }
        return selected;
    }

    /**
     * 判断岗位是否与用户兴趣方向强相关
     * 兴趣为空时返回 false，避免所有岗位都进入 primary 队列
     */
    private boolean isInterestMatched(JobPosition job, String interest) {
        if (interest == null || interest.isBlank()) return false;
        String i = interest.toLowerCase();
        String title = (job.getTitle() != null ? job.getTitle() : "").toLowerCase();
        String direction = (job.getDirection() != null ? job.getDirection() : "").toLowerCase();
        return title.contains(i) || direction.contains(i)
                || i.contains(title) || i.contains(direction);
    }

    /**
     * 后处理 AI 推荐结果：
     * 1. 用真实匹配度覆盖 AI 生成的 matchScore
     * 2. 丢弃未在真实匹配度中命中的岗位，避免 AI 编造岗位
     * 3. 若结果不足，从真实匹配度中按兴趣补充
     * 4. 构建双队列：primaryDirections（与用户当前兴趣相关）+ fallbackDirections（其他高分岗位）
     */
    private CareerDirectionResponse postProcessRecommendations(CareerDirectionResponse aiResp,
                                                               Map<String, JobMatchResponse> realScoreMap,
                                                               String interest, AssessmentResult latestResult) {
        List<CareerDirectionResponse.DirectionItem> aiDirections = aiResp.getDirections();
        if (aiDirections == null) aiDirections = new ArrayList<>();

        // AI 未返回有效 directions 时，直接用真实匹配度兜底填充，避免空结果
        if (aiDirections.isEmpty() && realScoreMap != null && !realScoreMap.isEmpty()) {
            log.warn("AI 返回空 directions，使用真实匹配度兜底填充，interest={}", interest);
            List<CareerDirectionResponse.DirectionItem> fallbackItems = realScoreMap.values().stream()
                    .limit(5)
                    .map(this::mapMatchToDirectionItem)
                    .collect(Collectors.toList());
            aiResp.setDirections(fallbackItems);
            aiResp.setNeedClarification(false);
            aiResp.setMissingDimensions(new ArrayList<>());
            return buildDualQueueDirections(aiResp, realScoreMap, interest);
        }

        List<CareerDirectionResponse.DirectionItem> validItems = new ArrayList<>();
        for (CareerDirectionResponse.DirectionItem item : aiDirections) {
            if (item.getJobTitle() == null) continue;
            JobMatchResponse match = realScoreMap.get(item.getJobTitle());
            if (match != null) {
                item.setMatchScore(match.getMatchScore().intValue());
                item.setDirection(match.getDirection());
                validItems.add(item);
            } else {
                log.warn("AI 推荐岗位未在真实匹配度中命中，已过滤: {}", item.getJobTitle());
            }
        }

        // 若有效结果不足 3 个，从真实匹配度中补充相关岗位
        if (validItems.size() < 3) {
            for (JobMatchResponse match : realScoreMap.values()) {
                if (validItems.stream().anyMatch(i -> match.getTitle().equals(i.getJobTitle()))) continue;
                validItems.add(mapMatchToDirectionItem(match));
                if (validItems.size() >= 5) break;
            }
        }

        aiResp.setDirections(validItems);
        // 服务层已判断信息充足并进入推荐分支，强制关闭 AI 可能误返回的追问标记
        aiResp.setNeedClarification(false);
        aiResp.setMissingDimensions(new ArrayList<>());
        return buildDualQueueDirections(aiResp, realScoreMap, interest);
    }

    /**
     * 将 JobMatchResponse 转换为 DirectionItem
     */
    private CareerDirectionResponse.DirectionItem mapMatchToDirectionItem(JobMatchResponse match) {
        return CareerDirectionResponse.DirectionItem.builder()
                .jobTitle(match.getTitle())
                .direction(match.getDirection())
                .matchScore(match.getMatchScore().intValue())
                .reason("基于你的画像、兴趣与测评结果匹配推荐")
                .learningPriority("高")
                .growthPath("建议从核心技能入手，逐步积累项目经验")
                .build();
    }

    /**
     * 构建双队列推荐结果
     * - primaryDirections：与用户当前兴趣强相关的方向
     * - fallbackDirections：其他高分方向（稳妥备选）
     * 当 primary 为空时，从真实匹配度中按兴趣补充 Top3，保证前端双队列 UI 正常展示
     */
    private CareerDirectionResponse buildDualQueueDirections(CareerDirectionResponse resp,
                                                             Map<String, JobMatchResponse> realScoreMap,
                                                             String interest) {
        List<CareerDirectionResponse.DirectionItem> all = resp.getDirections();
        if (all == null) all = new ArrayList<>();

        List<CareerDirectionResponse.DirectionItem> primary = new ArrayList<>();
        List<CareerDirectionResponse.DirectionItem> fallback = new ArrayList<>();

        for (CareerDirectionResponse.DirectionItem item : all) {
            JobPosition temp = new JobPosition();
            temp.setTitle(item.getJobTitle());
            temp.setDirection(item.getDirection());
            if (isInterestMatched(temp, interest)) {
                primary.add(item);
            } else {
                fallback.add(item);
            }
        }

        // primary 不足时从真实匹配度中按兴趣补充
        if (primary.isEmpty() && interest != null && !interest.isBlank()) {
            for (JobMatchResponse match : realScoreMap.values()) {
                JobPosition temp = new JobPosition();
                temp.setTitle(match.getTitle());
                temp.setDirection(match.getDirection());
                if (isInterestMatched(temp, interest)
                        && primary.stream().noneMatch(i -> match.getTitle().equals(i.getJobTitle()))) {
                    primary.add(mapMatchToDirectionItem(match));
                    if (primary.size() >= 3) break;
                }
            }
        }

        // fallback 不足时从真实匹配度中取其他高分岗位补充
        if (fallback.isEmpty()) {
            for (JobMatchResponse match : realScoreMap.values()) {
                JobPosition temp = new JobPosition();
                temp.setTitle(match.getTitle());
                temp.setDirection(match.getDirection());
                if (!isInterestMatched(temp, interest)
                        && fallback.stream().noneMatch(i -> match.getTitle().equals(i.getJobTitle()))
                        && primary.stream().noneMatch(i -> match.getTitle().equals(i.getJobTitle()))) {
                    fallback.add(mapMatchToDirectionItem(match));
                    if (fallback.size() >= 2) break;
                }
            }
        }

        // 队列长度限制
        primary = primary.stream().limit(5).collect(Collectors.toList());
        fallback = fallback.stream().limit(5).collect(Collectors.toList());

        resp.setPrimaryDirections(primary);
        resp.setFallbackDirections(fallback);

        // 如果 overallAnalysis 为空，补充一段说明
        if (resp.getOverallAnalysis() == null || resp.getOverallAnalysis().isBlank()) {
            resp.setOverallAnalysis("基于你的画像与测评结果，AI 为你生成以下职业方向建议。");
        }

        return resp;
    }

    /**
     * 规则推荐兜底：基于 JobMatchingService 真实匹配度
     * 通用问题兜底时不返回岗位列表，避免无关问题也被强行推荐
     */
    private CareerDirectionResponse fallbackRecommend(Long userId, CareerExplorationRequest req,
                                                        CareerProfile profile, AssessmentResult result,
                                                        List<JobPosition> positions, boolean isGeneralQuestion) {
        // 通用问题兜底：只返回文字说明，不生成岗位推荐
        if (isGeneralQuestion) {
            String overallAnalysis = "当前无法获取 AI 分析，建议换个方式提问或稍后重试。";
            CareerDirectionResponse resp = CareerDirectionResponse.builder()
                    .directions(new ArrayList<>())
                    .primaryDirections(new ArrayList<>())
                    .fallbackDirections(new ArrayList<>())
                    .overallAnalysis(overallAnalysis)
                    .source("FALLBACK")
                    .createdAt(LocalDateTime.now())
                    .build();
            saveRecord(userId, "CAREER_EXPLORATION", req.getPreferences(),
                    Map.of("overallAnalysis", overallAnalysis), "FALLBACK");
            return resp;
        }

        try {
            boolean fromAssessment = "ASSESSMENT".equals(req.getSource());
            InterestCity resolved = extractInterestCity(req.getPreferences(), profile, fromAssessment);

            // 测评场景且未提取到兴趣时，用测评优势维度作为临时兴趣方向
            String effectiveInterest = resolved.interest;
            if ((effectiveInterest == null || effectiveInterest.isBlank()) && fromAssessment && result != null) {
                String advantageDim = extractAssessmentAdvantageDimension(result);
                effectiveInterest = mapDimensionToInterest(advantageDim);
                log.info("兜底推荐-测评场景使用优势维度: dimension={}, interest={}", advantageDim, effectiveInterest);
            }

            Map<String, JobMatchResponse> realScoreMap = buildRealScoreMap(
                    userId, effectiveInterest, resolved.city, result);

            if (realScoreMap == null || realScoreMap.isEmpty()) {
                log.warn("兜底推荐无有效匹配度，触发追问: userId={}, fromAssessment={}", userId, fromAssessment);
                return askForClarification(userId, req, profile, result);
            }

            List<CareerDirectionResponse.DirectionItem> items = realScoreMap.values().stream()
                    .limit(MAX_FALLBACK_DIRECTIONS)
                    .map(this::mapMatchToDirectionItem)
                    .collect(Collectors.toList());

            CareerDirectionResponse resp = CareerDirectionResponse.builder()
                    .directions(items)
                    .overallAnalysis("当前 AI 服务不稳定，系统基于你的画像与测评结果给出以下兜底推荐。")
                    .source("FALLBACK")
                    .createdAt(LocalDateTime.now())
                    .build();
            CareerDirectionResponse dualResp = buildDualQueueDirections(resp, realScoreMap, effectiveInterest);
            saveRecord(userId, "CAREER_EXPLORATION", req.getPreferences(),
                    Map.of("directions", dualResp.getDirections(),
                            "primaryDirections", dualResp.getPrimaryDirections(),
                            "fallbackDirections", dualResp.getFallbackDirections(),
                            "overallAnalysis", dualResp.getOverallAnalysis()), "FALLBACK");
            return dualResp;
        } catch (Exception e) {
            log.warn("兜底推荐构建失败: {}", e.getMessage());
        }

        // 最终兜底：取全部岗位前 5 个
        List<CareerDirectionResponse.DirectionItem> items = new ArrayList<>();
        for (int i = 0; i < Math.min(MAX_FALLBACK_DIRECTIONS, positions.size()); i++) {
            JobPosition p = positions.get(i);
            items.add(CareerDirectionResponse.DirectionItem.builder()
                    .jobTitle(p.getTitle())
                    .direction(p.getDirection())
                    .matchScore(70 + (5 - i) * 5)
                    .reason("根据你的测评结果和岗位需求匹配")
                    .learningPriority(i < 2 ? "高" : "中")
                    .growthPath("建议从基础技能开始，逐步深入到项目实战")
                    .build());
        }

        CareerDirectionResponse resp = CareerDirectionResponse.builder()
                .directions(items)
                .overallAnalysis("基于你的能力测评结果，系统为你推荐以下职业方向（当前为兜底推荐）")
                .source("FALLBACK")
                .createdAt(LocalDateTime.now())
                .build();
        saveRecord(userId, "CAREER_EXPLORATION", req.getPreferences(),
                Map.of("directions", items, "overallAnalysis", resp.getOverallAnalysis()), "FALLBACK");
        return resp;
    }

    /**
     * 判断用户问题是否更像通用咨询而非职业推荐
     */
    private boolean isLikelyGeneralQuestion(String question) {
        if (question == null || question.isBlank()) return false;
        String q = question.toLowerCase();
        // 明确表达个人职业兴趣/目标的，即使包含某些通用词，也应按职业推荐处理
        String[] recommendationKeywords = {"我想做", "我想从事", "我对", "感兴趣", "帮我推荐", "推荐岗位"};
        for (String kw : recommendationKeywords) {
            if (q.contains(kw)) return false;
        }
        String[] generalKeywords = {"趋势", "发展", "前景", "薪资", "工资", "面试", "简历",
                "如何准备", "行业", "公司", "企业", "offer", "跳槽", "转行", "加班", "福利",
                "今天", "明天", "昨天", "星期", "日期", "时间", "天气", "你好", "谢谢", "再见"};
        for (String kw : generalKeywords) {
            if (q.contains(kw)) return true;
        }
        return false;
    }

    /**
     * 从用户输入和画像中提取兴趣方向与期望城市
     * 输入优先：命中常见城市名或技术方向关键词时直接使用；否则回退到画像
     *
     * @param preferAssessment 为 true 时（来自测评页入口），禁止回退到 profile.targetRoles，
     *                         避免测评推荐被历史画像覆盖
     */
    private InterestCity extractInterestCity(String preferences, CareerProfile profile, boolean preferAssessment) {
        String interest = null;
        String city = null;
        if (preferences != null && !preferences.isBlank()) {
            String p = preferences;
            // 兴趣关键词：后端/前端/全栈/算法/数据/产品/测试/运维/UI/UX/运营/AI/人工智能/大数据/云计算/Java/Python
            Pattern interestPattern = Pattern.compile("(?:我想做|我想从事|我对| interested in |想|做|从事|学习)?(Java|Python|C\\+\\+|Go|前端|后端|全栈|算法|数据|大数据|AI|人工智能|机器学习|深度学习|云计算|运维|测试|产品|UI|UX|设计|运营|新媒体|人力资源|财务|市场|销售)[开发工程师方向师]?",
                    Pattern.CASE_INSENSITIVE);
            Matcher m = interestPattern.matcher(p);
            if (m.find()) {
                interest = m.group(1);
            }
            // 城市关键词：国内主要城市
            Pattern cityPattern = Pattern.compile("(?:在|去|回|到|城市|期望城市|工作地点|base|Base)?(北京|上海|广州|深圳|杭州|成都|武汉|长沙|南京|西安|重庆|天津|苏州|郑州|青岛|大连|厦门|宁波|无锡|佛山|东莞|合肥|济南|福州|昆明|哈尔滨|长春|沈阳|石家庄|太原|南昌|贵阳|南宁|海口|兰州|银川|西宁|乌鲁木齐|拉萨|呼和浩特|港澳台|香港|澳门|台湾)",
                    Pattern.CASE_INSENSITIVE);
            Matcher cm = cityPattern.matcher(p);
            if (cm.find()) {
                city = cm.group(1);
            }
        }
        // 测评页入口：只回退城市，不回退兴趣方向，确保推荐基于测评结果
        if (preferAssessment) {
            if ((city == null || city.isBlank()) && profile != null) {
                city = profile.getExpectedCity();
            }
            return new InterestCity(interest, city);
        }

        // 普通探索入口：画像兜底
        if ((interest == null || interest.isBlank()) && profile != null) {
            interest = parseFirstTargetRole(profile.getTargetRoles());
        }
        if ((city == null || city.isBlank()) && profile != null) {
            city = profile.getExpectedCity();
        }
        return new InterestCity(interest, city);
    }

    /**
     * 解析 targetRoles JSON 数组字符串，取第一个元素
     */
    @SuppressWarnings("unchecked")
    private String parseFirstTargetRole(String targetRolesJson) {
        if (targetRolesJson == null || targetRolesJson.isBlank()) return null;
        try {
            List<String> roles = objectMapper.readValue(targetRolesJson, List.class);
            if (roles != null && !roles.isEmpty()) return roles.get(0);
        } catch (Exception e) {
            return targetRolesJson;
        }
        return null;
    }

    /**
     * 服务异常兜底：返回简短提示
     */
    private CareerDirectionResponse fallbackSimple() {
        return CareerDirectionResponse.builder()
                .directions(new ArrayList<>())
                .primaryDirections(new ArrayList<>())
                .fallbackDirections(new ArrayList<>())
                .overallAnalysis("AI服务暂时不可用，请稍后重试")
                .source("FALLBACK")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Override
    public List<CareerDirectionResponse> getHistory(Long userId) {
        return recordMapper.selectList(new LambdaQueryWrapper<RecommendationRecord>()
                        .eq(RecommendationRecord::getUserId, userId)
                        .eq(RecommendationRecord::getType, "CAREER_EXPLORATION")
                        .orderByDesc(RecommendationRecord::getCreatedAt))
                .stream().map(r -> {
                    try {
                        CareerDirectionResponse resp = objectMapper.readValue(r.getResultJson(), CareerDirectionResponse.class);
                        resp.setRecordId(r.getId());
                        return resp;
                    } catch (Exception e) { return null; }
                }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 保存推荐记录
     */
    private void saveRecord(Long userId, String type, String input, Map<String, Object> result, String source) {
        try {
            RecommendationRecord record = new RecommendationRecord();
            record.setUserId(userId);
            record.setType(type);
            record.setInputText(input);
            record.setResultJson(objectMapper.writeValueAsString(result));
            record.setSource(source);
            record.setCreatedAt(LocalDateTime.now());
            recordMapper.insert(record);
        } catch (Exception e) {
            log.warn("保存推荐记录失败: {}", e.getMessage());
        }
    }

    /**
     * 将对话历史列表格式化为文本，供 AI prompt 使用
     * 限制：只保留最近 6 条消息（约 3 轮对话），单条内容超过 300 字截断
     */
    private String buildConversationHistory(List<Map<String, String>> history) {
        if (history == null || history.isEmpty()) return "（无）";
        List<Map<String, String>> recent = history;
        if (history.size() > MAX_HISTORY_MESSAGES) {
            recent = history.subList(history.size() - MAX_HISTORY_MESSAGES, history.size());
        }
        StringBuilder sb = new StringBuilder();
        for (Map<String, String> msg : recent) {
            String role = msg.getOrDefault("role", "user");
            String content = msg.getOrDefault("content", "");
            if (content == null) content = "";
            if (content.length() > MAX_HISTORY_LENGTH) {
                content = content.substring(0, MAX_HISTORY_LENGTH) + "...";
            }
            String label = "user".equals(role) ? "用户" : "AI导师";
            sb.append(label).append("：").append(content).append("\n");
        }
        return sb.toString();
    }

    /**
     * 从测评结果中提取相对优势维度（得分最高维度）
     */
    private String extractAssessmentAdvantageDimension(AssessmentResult result) {
        if (result == null) return null;
        Map<String, Double> scores = new LinkedHashMap<>();
        scores.put("编程", result.getProgrammingScore());
        scores.put("逻辑", result.getLogicScore());
        scores.put("产品", result.getProductScore());
        scores.put("技术", result.getTechScore());
        scores.put("沟通", result.getCommunicationScore());

        return scores.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * 将测评优势维度映射为兴趣关键词，用于岗位匹配
     */
    private String mapDimensionToInterest(String dimension) {
        if (dimension == null) return null;
        return switch (dimension) {
            case "编程" -> "后端";
            case "逻辑" -> "算法";
            case "产品" -> "产品";
            case "技术" -> "运维";
            case "沟通" -> "产品";
            default -> dimension;
        };
    }

    /**
     * 判断用户信息是否不足以进行职业推荐
     * 规则触发：比 AI 意图识别更稳定，避免信息不足时给出固定推荐
     *
     * @param fromAssessment 是否来自测评页入口；若为 true 且有测评结果，视为信息充足
     */
    private boolean isInformationInsufficient(CareerProfile profile, AssessmentResult latestResult,
                                              CareerExplorationRequest req, boolean fromAssessment) {
        // 测评页入口：有测评结果即视为信息充足，由测评优势维度驱动推荐
        if (fromAssessment && latestResult != null) {
            return false;
        }

        // 用户已明确表达兴趣，信息充足
        InterestCity resolved = extractInterestCity(req.getPreferences(), profile, false);
        if (resolved.interest != null && !resolved.interest.isBlank()) {
            return false;
        }

        // 画像关键维度缺失
        boolean profileIncomplete = profile == null
                || isBlank(profile.getTargetRoles())
                || isBlank(profile.getExpectedCity())
                || isBlank(profile.getSkillTags());
        boolean noAssessment = latestResult == null;
        boolean noPreferences = isBlank(req.getPreferences());

        return profileIncomplete && noAssessment && noPreferences;
    }

    /**
     * 生成 AI 追问文案，引导用户补充职业方向信息
     */
    private CareerDirectionResponse askForClarification(Long userId, CareerExplorationRequest req,
                                                        CareerProfile profile, AssessmentResult latestResult) {
        try {
            String template = promptUtil.loadTemplate("career_clarification");
            Map<String, String> params = buildCareerParams(req, profile, latestResult, Collections.emptyList());
            String prompt = promptUtil.renderTemplate(template, params);

            String response = deepSeekService.callAPI("你是一位资深的职业规划导师", prompt, 8000L, 512, 0.7);
            String clarifyText = response;
            Map<String, Object> result = deepSeekService.parseJSONResponse(response);
            if (result != null && result.get("overallAnalysis") instanceof String text && !text.isBlank()) {
                clarifyText = text;
            } else if (result != null && result.get("answer") instanceof String text && !text.isBlank()) {
                clarifyText = text;
            }

            List<String> missingDimensions = new ArrayList<>();
            if (profile == null || isBlank(profile.getTargetRoles())) missingDimensions.add("interest");
            if (profile == null || isBlank(profile.getExpectedCity())) missingDimensions.add("city");
            if (latestResult == null) missingDimensions.add("assessment");
            if (isBlank(req.getPreferences())) missingDimensions.add("preference");

            CareerDirectionResponse resp = CareerDirectionResponse.builder()
                    .overallAnalysis(clarifyText)
                    .needClarification(true)
                    .missingDimensions(missingDimensions)
                    .directions(new ArrayList<>())
                    .primaryDirections(new ArrayList<>())
                    .fallbackDirections(new ArrayList<>())
                    .source("AI")
                    .createdAt(LocalDateTime.now())
                    .build();
            saveRecord(userId, "CAREER_EXPLORATION", req.getPreferences(),
                    Map.of("needClarification", true,
                            "overallAnalysis", clarifyText,
                            "missingDimensions", missingDimensions), "AI");
            return resp;
        } catch (Exception e) {
            log.warn("生成 AI 追问文案失败，使用默认追问: {}", e.getMessage());
            return defaultClarificationResponse(userId, req, profile, latestResult);
        }
    }

    /**
     * 默认追问文案兜底
     */
    private CareerDirectionResponse defaultClarificationResponse(Long userId, CareerExplorationRequest req,
                                                                  CareerProfile profile, AssessmentResult latestResult) {
        StringBuilder sb = new StringBuilder("为了给你推荐更合适的职业方向，我还需要了解：\n");
        List<String> missingDimensions = new ArrayList<>();
        if (profile == null || isBlank(profile.getTargetRoles())) {
            sb.append("1. 你对哪些岗位或技术方向感兴趣？（如后端开发、AI、产品设计等）\n");
            missingDimensions.add("interest");
        }
        if (profile == null || isBlank(profile.getExpectedCity())) {
            sb.append("2. 你期望工作的城市是哪里？\n");
            missingDimensions.add("city");
        }
        if (latestResult == null) {
            sb.append("3. 可以先完成一次能力测评，我会结合测评结果给你更精准的建议。\n");
            missingDimensions.add("assessment");
        }

        CareerDirectionResponse resp = CareerDirectionResponse.builder()
                .overallAnalysis(sb.toString())
                .needClarification(true)
                .missingDimensions(missingDimensions)
                .directions(new ArrayList<>())
                .primaryDirections(new ArrayList<>())
                .fallbackDirections(new ArrayList<>())
                .source("FALLBACK")
                .createdAt(LocalDateTime.now())
                .build();
        saveRecord(userId, "CAREER_EXPLORATION", req.getPreferences(),
                Map.of("needClarification", true,
                        "overallAnalysis", sb.toString(),
                        "missingDimensions", missingDimensions), "FALLBACK");
        return resp;
    }

    /**
     * 判断字符串是否为空或空白
     */
    private boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

    /**
     * 兴趣与城市提取结果内部类
     */
    private record InterestCity(String interest, String city) {}
}
