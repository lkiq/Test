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
import java.util.concurrent.ConcurrentHashMap;
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
    /** 重复请求缓存时间窗口（毫秒）：30 秒内相同输入直接返回上一次结果 */
    private static final long DUPLICATE_CACHE_TTL_MS = 30_000;
    /** 从数据库读取历史推荐记录的最大条数 */
    private static final int MAX_DB_HISTORY_RECORDS = 20;

    /** 用户最近一次推荐结果缓存，用于快速去重重复请求 */
    private final Map<Long, CacheEntry> lastRecommendationCache = new ConcurrentHashMap<>();

    /**
     * 推荐结果缓存条目
     */
    private static class CacheEntry {
        /** 用户输入文本 */
        String input;
        /** 缓存的推荐响应 */
        CareerDirectionResponse response;
        /** 缓存时间戳 */
        long timestamp;

        CacheEntry(String input, CareerDirectionResponse response, long timestamp) {
            this.input = input;
            this.response = response;
            this.timestamp = timestamp;
        }
    }

    /** 追问轮次上限：连续追问超过该次数仍未补充信息时，直接给出推荐 */
    private static final int MAX_CLARIFICATION_ROUNDS = 2;

    @Override
    public CareerDirectionResponse explore(Long userId, CareerExplorationRequest req) {
        // 1. 重复请求检测：30 秒内相同输入直接返回上一次结果，避免无效调用
        String question = req.getPreferences() != null ? req.getPreferences() : "";
        CareerDirectionResponse cachedResponse = getCachedRecommendation(userId, question);
        if (cachedResponse != null) {
            return cachedResponse;
        }

        // 2. 执行推荐逻辑，并将结果缓存
        CareerDirectionResponse response = doExploreInternal(userId, req);
        updateCache(userId, question, response);
        return response;
    }

    /**
     * 职业方向探索内部实现，处理所有推荐/追问/兜底逻辑
     * 由 explore 方法统一进行缓存包装
     */
    private CareerDirectionResponse doExploreInternal(Long userId, CareerExplorationRequest req) {
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

            // 信息充足性检查：当用户画像不清晰且当前输入未表达明确兴趣时，
            // 应主动追问补充信息，体现 AI 职业方向探索的引导功能，而非直接推荐稳妥选择
            // 测评页入口(ASSESSMENT)已有测评数据支撑，无需追问
            if (!"ASSESSMENT".equals(req.getSource())) {
                List<String> missing = checkInfoSufficiency(profile, question);
                if (!missing.isEmpty()) {
                    // 若用户已明确表达推荐请求，不再追问，直接推荐
                    if (isRecommendationIntentOnly(question != null ? question.toLowerCase() : "")) {
                        log.info("用户 {} 明确请求推荐，即使缺少画像和兴趣也直接给出推荐: question={}", userId, question);
                    } else {
                        // 连续追问次数达到上限仍未补充信息，直接给出推荐，避免无限追问
                        int rounds = countClarificationRounds(req.getHistory());
                        if (rounds >= MAX_CLARIFICATION_ROUNDS) {
                            log.info("用户 {} 已追问 {} 轮仍未补充信息，直接给出推荐", userId, rounds);
                        } else {
                            log.info("用户 {} 信息不足，触发追问补充: missing={}", userId, missing);
                            return doClarification(userId, req, profile, latestResult, missing);
                        }
                    }
                }
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
            // 解析用户当前表达的兴趣和城市（优先使用输入关键词，其次 expressedInterest，最后画像兜底）
            InterestCity resolved = extractInterestCity(req.getPreferences(), profile,
                    req.getExpressedInterest(), latestResult);

            // 构建真实匹配度：基于画像 + 当前兴趣/城市 + 测评结果
            Map<String, JobMatchResponse> realScoreMap = buildRealScoreMap(
                    userId, resolved.interest, resolved.city, latestResult);

            // 提取历史已推荐岗位，用于去重约束（双重保险：Prompt 约束 + 后端过滤）
            Set<String> excludedJobs = extractRecommendedJobTitles(
                    req.getHistory(), realScoreMap.keySet(), userId);
            if (!excludedJobs.isEmpty()) {
                log.info("用户 {} 历史已推荐岗位，本次将去重: {}", userId, excludedJobs);
            }

            // 仅取 Top10 岗位进入 prompt，避免过长导致 AI 超时
            List<JobPosition> topPositions = selectTopPositionsForPrompt(realScoreMap, positions);

            String template = promptUtil.loadTemplate("career_exploration");
            Map<String, String> params = buildCareerParams(req, profile, latestResult, topPositions,
                    realScoreMap, excludedJobs);
            String prompt = promptUtil.renderTemplate(template, params);

            // 对话式场景不使用缓存：用户每次输入不同问题，应实时生成不同回复
            String response = deepSeekService.callAPI("你是一位资深的职业规划导师", prompt, 12000L, 768, 0.3);
            Map<String, Object> result = deepSeekService.parseJSONResponse(response);
            if (result != null && result.containsKey("directions")) {
                // 预处理：AI 返回的 matchScore 可能是字符串小数（如 "52.2"），需规范化为整数
                normalizeMatchScores(result);
                CareerDirectionResponse aiResp = objectMapper.convertValue(result, CareerDirectionResponse.class);
                aiResp.setSource("AI");
                aiResp.setCreatedAt(LocalDateTime.now());

                // 解析用户条件（如"匹配度高于80""Java相关"等自然语言条件）
                RecommendationCondition condition = parseUserCondition(
                        req.getPreferences(), realScoreMap.keySet());
                if (condition.hasAnyCondition()) {
                    log.info("用户 {} 提出推荐条件: minScore={}, maxScore={}, keywords={}",
                            userId, condition.minScore(), condition.maxScore(), condition.requiredKeywords());
                }

                // 用真实匹配度覆盖 AI 分数，应用分数过滤与历史去重
                CareerDirectionResponse processedResp = postProcessRecommendations(
                        aiResp, realScoreMap, resolved.interest, latestResult, condition, excludedJobs);
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
     * 规范化 AI 返回结果中的 matchScore 字段
     * AI 可能返回字符串小数（如 "52.2"）或浮点数（如 52.2），而 DTO 字段为 Integer
     * 本方法遍历 directions/primaryDirections/fallbackDirections 列表，将 matchScore 统一转为 int
     * @param result AI 返回的 JSON Map
     */
    @SuppressWarnings("unchecked")
    private void normalizeMatchScores(Map<String, Object> result) {
        if (result == null) return;
        normalizeMatchScoresInList((List<Object>) result.get("directions"));
        normalizeMatchScoresInList((List<Object>) result.get("primaryDirections"));
        normalizeMatchScoresInList((List<Object>) result.get("fallbackDirections"));
    }

    /**
     * 规范化单个方向列表中的 matchScore 字段
     * @param items 方向列表（每个元素为 Map）
     */
    @SuppressWarnings("unchecked")
    private void normalizeMatchScoresInList(List<Object> items) {
        if (items == null || items.isEmpty()) return;
        for (Object item : items) {
            if (!(item instanceof Map)) continue;
            Map<String, Object> map = (Map<String, Object>) item;
            Object score = map.get("matchScore");
            if (score == null) continue;
            if (score instanceof Number) {
                map.put("matchScore", ((Number) score).intValue());
            } else if (score instanceof String) {
                try {
                    // 去除百分号、空格等非数字字符后解析
                    String cleaned = ((String) score).replaceAll("[^0-9.\\-]", "");
                    if (cleaned.isEmpty()) {
                        log.warn("matchScore 清理后为空，保持原值: {}", score);
                        continue;
                    }
                    map.put("matchScore", (int) Double.parseDouble(cleaned));
                } catch (NumberFormatException e) {
                    log.warn("matchScore 无法解析为整数，保持原值: {}", score);
                }
            }
        }
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
            Map<String, String> params = buildCareerParams(req, profile, latestResult,
                    Collections.emptyList(), null, Collections.emptySet());
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
     * 统计对话历史中 AI 已追问的轮次
     * 通过识别 assistant 消息中的追问特征词来判断
     * @param history 对话历史
     * @return 已追问轮次
     */
    private int countClarificationRounds(List<Map<String, String>> history) {
        if (history == null || history.isEmpty()) return 0;
        String[] clarifyMarkers = {
                "为了给你推荐", "想先问问", "想了解一下", "想先了解", "还想了解",
                "你更倾向于", "你更偏向", "你期望在哪个城市", "你期望在哪个城市工作",
                "请告诉我", "想再了解", "想先问", "想更精准地推荐"
        };
        int rounds = 0;
        for (Map<String, String> msg : history) {
            if ("assistant".equals(msg.getOrDefault("role", ""))) {
                String content = msg.getOrDefault("content", "").toLowerCase();
                if (content.isBlank()) continue;
                for (String marker : clarifyMarkers) {
                    if (content.contains(marker.toLowerCase())) {
                        rounds++;
                        break;
                    }
                }
            }
        }
        return rounds;
    }

    /**
     * 检查用户信息是否足以进行精准推荐
     * 判断维度：画像是否有目标岗位、当前输入是否含明确兴趣关键词
     * 城市缺失优先级较低（可缺城市但不能缺兴趣），不作为强制追问条件
     * 当用户明确请求推荐（如"推荐岗位"）时，不再强制追问，直接允许推荐
     * @return 信息不足时返回缺失维度列表（如 ["interest"]），充足返回空列表
     */
    private List<String> checkInfoSufficiency(CareerProfile profile, String preferences) {
        List<String> missing = new ArrayList<>();
        String q = preferences != null ? preferences.toLowerCase() : "";

        // 1. 兴趣方向检查：画像无目标岗位 且 当前输入不含明确兴趣关键词 → 缺兴趣
        // 特别处理：用户明确请求推荐（如"推荐岗位""帮我推荐"）时，不再强制追问，
        // 允许 AI 基于画像或热门岗位直接推荐，提升灵活性
        boolean hasProfileTarget = profile != null
                && profile.getTargetRoles() != null
                && !profile.getTargetRoles().isBlank()
                && !"[]".equals(profile.getTargetRoles().trim());
        boolean hasInterestKeywordInInput = containsInterestKeyword(q);

        if (!hasProfileTarget && !hasInterestKeywordInInput) {
            // 用户明确请求推荐，不再追问
            if (!isRecommendationIntentOnly(q)) {
                missing.add("interest");
            }
        }

        // 2. 城市检查（仅当画像也无城市且输入也无城市时才记录，但不强制追问）
        // 城市缺失不影响推荐质量过多，作为辅助提示
        boolean hasProfileCity = profile != null
                && profile.getExpectedCity() != null
                && !profile.getExpectedCity().isBlank();
        if (!hasProfileCity) {
            // 检查输入中是否含城市关键词
            boolean hasCityInInput = containsCityKeyword(q);
            if (!hasCityInInput) {
                missing.add("city");
            }
        }

        // 仅当缺兴趣时才真正触发追问；仅缺城市不触发（避免过度打扰）
        if (missing.contains("interest")) {
            return missing;
        }
        return new ArrayList<>();
    }

    /**
     * 判断文本中是否包含明确的兴趣表达关键词
     * 注意：仅表达推荐请求意图的词（如"推荐岗位""帮我推荐"）不算作兴趣方向
     * 必须有具体方向或技术栈关键词才算表达兴趣
     */
    private boolean containsInterestKeyword(String text) {
        if (text == null || text.isBlank()) return false;
        String[] interestKeywords = {
                // 明确方向/技术栈关键词
                "Java", "Python", "C++", "Go", "前端", "后端", "全栈",
                "算法", "数据", "大数据", "AI", "人工智能", "机器学习", "深度学习",
                "云计算", "运维", "测试", "产品", "UI", "UX", "设计", "运营", "新媒体",
                "人力资源", "财务", "市场", "销售", "开发", "工程师",
                // 专业背景关键词（用于"我是学XX的能做什么"类基于背景的推荐）
                "计算机", "软件工程", "信息安全", "信息管理", "电子商务",
                // 明确表达兴趣的句式（需包含"我"字，确保是用户自身兴趣）
                "我想做", "我想从事", "我对", "倾向于", "我喜欢", "我想做",
                "从事", "学习", "感兴趣"};
        for (String kw : interestKeywords) {
            if (text.contains(kw.toLowerCase())) return true;
        }
        return false;
    }

    /**
     * 判断文本是否仅表达推荐请求意图，但未提供具体兴趣方向
     * 例如："推荐岗位""帮我推荐""有什么推荐""适合什么岗位"
     */
    private boolean isRecommendationIntentOnly(String text) {
        if (text == null || text.isBlank()) return false;
        String[] intentKeywords = {"推荐岗位", "帮我推荐", "有什么推荐", "推荐方向", "推荐工作",
                "适合什么岗位", "适合做什么", "能做什么", "找什么工作"};
        for (String kw : intentKeywords) {
            if (text.contains(kw.toLowerCase())) return true;
        }
        return false;
    }

    /**
     * 判断文本中是否包含城市关键词
     */
    private boolean containsCityKeyword(String text) {
        if (text == null || text.isBlank()) return false;
        String[] cities = {"北京", "上海", "广州", "深圳", "杭州", "成都", "武汉", "长沙",
                "南京", "西安", "重庆", "天津", "苏州", "郑州", "青岛", "大连"};
        for (String city : cities) {
            if (text.contains(city)) return true;
        }
        return false;
    }

    /**
     * 主动追问：当用户信息不足时，调用 career_clarification 模板生成友好提问
     * 返回 needClarification=true，前端清空推荐卡片并将 overallAnalysis 作为追问消息展示
     */
    private CareerDirectionResponse doClarification(Long userId, CareerExplorationRequest req,
                                                     CareerProfile profile, AssessmentResult latestResult,
                                                     List<String> missing) {
        try {
            String question = req.getPreferences() != null ? req.getPreferences() : "";
            String template = promptUtil.loadTemplate("career_clarification");
            Map<String, String> params = new HashMap<>();
            params.put("user_question", question);
            params.put("conversation_history", buildConversationHistory(req.getHistory()));

            // 构建缺失信息描述
            StringBuilder missingInfo = new StringBuilder();
            if (missing.contains("interest")) {
                missingInfo.append("兴趣方向（用户未明确表达想从事哪个方向，如前端/后端/数据/产品等）；");
            }
            if (missing.contains("city")) {
                missingInfo.append("期望工作城市；");
            }
            if (missingInfo.length() == 0) {
                missingInfo.append("兴趣方向和期望城市等基本信息");
            }
            params.put("missing_info", missingInfo.toString());

            String prompt = promptUtil.renderTemplate(template, params);
            // 追问使用较高 temperature 增加自然度
            String response = deepSeekService.callAPI("你是一位耐心的职业规划导师", prompt, 6000L, 256, 0.7);

            String clarifyText;
            if (response != null && !response.isBlank()) {
                // 尝试解析 JSON，失败则直接使用原始文本
                Map<String, Object> result = deepSeekService.parseJSONResponse(response);
                if (result != null && result.get("answer") instanceof String a) {
                    clarifyText = a;
                } else if (result != null && result.get("overallAnalysis") instanceof String o) {
                    clarifyText = o;
                } else {
                    clarifyText = response.trim();
                }
            } else {
                // AI 无响应时使用兜底追问文本
                clarifyText = buildFallbackClarifyText(missing);
            }

            CareerDirectionResponse resp = CareerDirectionResponse.builder()
                    .overallAnalysis(clarifyText)
                    .directions(new ArrayList<>())
                    .primaryDirections(new ArrayList<>())
                    .fallbackDirections(new ArrayList<>())
                    .needClarification(true)
                    .missingDimensions(missing)
                    .source("AI")
                    .createdAt(LocalDateTime.now())
                    .build();
            saveRecord(userId, "CAREER_EXPLORATION", req.getPreferences(),
                    Map.of("needClarification", true, "missing", missing, "clarifyText", clarifyText), "AI");
            return resp;
        } catch (Exception e) {
            log.warn("追问生成失败，使用兜底追问文本: {}", e.getMessage());
            String clarifyText = buildFallbackClarifyText(missing);
            CareerDirectionResponse resp = CareerDirectionResponse.builder()
                    .overallAnalysis(clarifyText)
                    .directions(new ArrayList<>())
                    .primaryDirections(new ArrayList<>())
                    .fallbackDirections(new ArrayList<>())
                    .needClarification(true)
                    .missingDimensions(missing)
                    .source("FALLBACK")
                    .createdAt(LocalDateTime.now())
                    .build();
            return resp;
        }
    }

    /**
     * 构建兜底追问文本（AI 不可用时使用）
     */
    private String buildFallbackClarifyText(List<String> missing) {
        StringBuilder sb = new StringBuilder();
        if (missing.contains("interest")) {
            sb.append("为了给你推荐更合适的职业方向，想了解一下：你更倾向于哪个方向？");
            sb.append("比如前端开发、后端开发、数据分析、AI算法、产品经理、测试运维等。");
        }
        if (missing.contains("city")) {
            if (sb.length() > 0) sb.append(" 另外，");
            sb.append("你期望在哪个城市工作呢？这样我能给你推荐当地更对口的岗位。");
        }
        if (sb.length() == 0) {
            sb.append("请告诉我你感兴趣的职业方向和期望工作城市，我会为你精准推荐。");
        }
        return sb.toString();
    }

    /**
     * 用户推荐条件（解析自自然语言输入）
     * @param minScore 最低匹配度阈值（如 80），null 表示无下限约束
     * @param maxScore 最高匹配度阈值（低优先级），null 表示无上限约束
     * @param requiredKeywords 用户提及的方向/技术栈关键词
     */
    private record RecommendationCondition(Integer minScore, Integer maxScore, List<String> requiredKeywords) {
        /**
         * 是否存在任何过滤条件
         */
        public boolean hasAnyCondition() {
            return minScore != null || maxScore != null
                    || (requiredKeywords != null && !requiredKeywords.isEmpty());
        }
    }

    /**
     * 从用户输入解析推荐条件
     * 支持："高于80""大于80""超过80""80分以上""80以上"等阈值表达
     * 支持：岗位方向/技能关键词（从已知岗位列表与通用技术栈中匹配）
     * @param question 用户输入文本
     * @param allJobTitles 已知岗位标题集合（用于关键词匹配）
     * @return 解析出的推荐条件，无匹配返回空条件
     */
    private RecommendationCondition parseUserCondition(String question, Set<String> allJobTitles) {
        if (question == null || question.isBlank()) {
            return new RecommendationCondition(null, null, Collections.emptyList());
        }
        String q = question.toLowerCase();

        Integer minScore = null;
        Integer maxScore = null;

        // 1. 正向阈值：高于/大于/超过/>=/≥ + 数字
        Pattern minPattern1 = Pattern.compile("(?:高于|大于|超过|不少于|不低于|>=|≥)\\s*(\\d{2,3})");
        Matcher m1 = minPattern1.matcher(q);
        if (m1.find()) {
            minScore = Integer.parseInt(m1.group(1));
        }
        // "80分以上""80以上""80分及以上"
        if (minScore == null) {
            Pattern minPattern2 = Pattern.compile("(\\d{2,3})\\s*分?\\s*(?:以上|及以上|起步|起)");
            Matcher m2 = minPattern2.matcher(q);
            if (m2.find()) {
                minScore = Integer.parseInt(m2.group(1));
            }
        }
        // 2. 负向阈值：低于/小于/<=/≤ + 数字（低优先级）
        Pattern maxPattern = Pattern.compile("(?:低于|小于|不超过|至多|<=|≤)\\s*(\\d{2,3})");
        Matcher mx = maxPattern.matcher(q);
        if (mx.find()) {
            maxScore = Integer.parseInt(mx.group(1));
        }

        // 3. 关键词匹配：从已知岗位标题中提取用户提及的关键词
        List<String> requiredKeywords = new ArrayList<>();
        if (allJobTitles != null) {
            for (String title : allJobTitles) {
                if (title != null && q.contains(title.toLowerCase())) {
                    requiredKeywords.add(title);
                }
            }
        }
        // 通用技术栈关键词
        String[] techKeywords = {"Java", "Python", "前端", "后端", "全栈", "算法", "数据",
                "AI", "人工智能", "大数据", "云计算", "运维", "测试", "产品", "UI", "设计"};
        for (String kw : techKeywords) {
            if (q.contains(kw.toLowerCase()) && !requiredKeywords.contains(kw)) {
                requiredKeywords.add(kw);
            }
        }

        return new RecommendationCondition(minScore, maxScore, requiredKeywords);
    }

    /**
     * 从对话历史及数据库推荐记录中提取已推荐过的岗位标题
     * 双源合并：1) 扫描 history 中 assistant 消息文本；2) 读取当前用户最近 N 条推荐记录中的岗位名
     * 兜底推荐时 assistant 文本不含岗位名，数据库读取可保证历史去重真正生效
     * @param history 对话历史
     * @param allJobTitles 已知岗位标题集合
     * @param userId 用户ID（用于数据库历史读取，可为 null）
     * @return 已推荐过的岗位标题集合（保持插入顺序）
     */
    private Set<String> extractRecommendedJobTitles(List<Map<String, String>> history, Set<String> allJobTitles, Long userId) {
        Set<String> recommended = new LinkedHashSet<>();

        // 1. 从对话历史文本扫描（兼容原有逻辑）
        if (history != null && !history.isEmpty() && allJobTitles != null && !allJobTitles.isEmpty()) {
            StringBuilder aiText = new StringBuilder();
            for (Map<String, String> msg : history) {
                if ("assistant".equals(msg.getOrDefault("role", ""))) {
                    String content = msg.getOrDefault("content", "");
                    if (content != null) {
                        aiText.append(content).append("\n");
                    }
                }
            }
            String text = aiText.toString().toLowerCase();
            if (!text.isBlank()) {
                for (String title : allJobTitles) {
                    if (title != null && text.contains(title.toLowerCase())) {
                        recommended.add(title);
                    }
                }
            }
        }

        // 2. 从数据库推荐记录读取历史岗位名（兜底/缓存场景 history 文本无法覆盖时至关重要）
        if (userId != null) {
            try {
                List<RecommendationRecord> records = recordMapper.selectList(
                        new LambdaQueryWrapper<RecommendationRecord>()
                                .eq(RecommendationRecord::getUserId, userId)
                                .eq(RecommendationRecord::getType, "CAREER_EXPLORATION")
                                .orderByDesc(RecommendationRecord::getCreatedAt)
                                .last("LIMIT " + MAX_DB_HISTORY_RECORDS));
                for (RecommendationRecord record : records) {
                    if (record == null || record.getResultJson() == null) continue;
                    try {
                        CareerDirectionResponse resp = objectMapper.readValue(
                                record.getResultJson(), CareerDirectionResponse.class);
                        collectJobTitles(resp, recommended);
                    } catch (Exception e) {
                        log.warn("解析历史推荐记录失败，跳过: recordId={}, error={}", record.getId(), e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.warn("读取历史推荐记录失败: userId={}, error={}", userId, e.getMessage());
            }
        }

        return recommended;
    }

    /**
     * 从 CareerDirectionResponse 各方向列表中提取岗位标题
     */
    private void collectJobTitles(CareerDirectionResponse resp, Set<String> target) {
        if (resp == null) return;
        collectJobTitlesFromList(resp.getDirections(), target);
        collectJobTitlesFromList(resp.getPrimaryDirections(), target);
        collectJobTitlesFromList(resp.getFallbackDirections(), target);
    }

    /**
     * 从单个方向列表中提取岗位标题
     */
    private void collectJobTitlesFromList(List<CareerDirectionResponse.DirectionItem> items, Set<String> target) {
        if (items == null || target == null) return;
        for (CareerDirectionResponse.DirectionItem item : items) {
            if (item != null && item.getJobTitle() != null && !item.getJobTitle().isBlank()) {
                target.add(item.getJobTitle());
            }
        }
    }

    /**
     * 轻量意图识别：调用 DeepSeek 判断用户问题属于 RECOMMENDATION 还是 GENERAL
     * 分类失败时默认返回 RECOMMENDATION，兼容存量行为
     */
    private String classifyIntent(String question) {
        try {
            String q = question != null ? question : "";
            Map<String, String> params = new HashMap<>();
            params.put("question", q);
            String prompt = promptUtil.loadAndRender("career_intent_classifier", params);

            // 轻量调用：快速分类，max_tokens 128 防止中文输出被截断
            String response = deepSeekService.callAPI("你是一位意图分类专家", prompt, 3000L, 128);
            Map<String, Object> result = deepSeekService.parseJSONResponse(response);
            log.info("意图识别结果: question={}, result={}", q, result);
            if (result != null && result.get("intent") instanceof String intent) {
                if (INTENT_GENERAL.equalsIgnoreCase(intent)) {
                    return INTENT_GENERAL;
                }
            }
        } catch (Exception e) {
            log.warn("意图识别失败，默认按职业推荐处理: {}", e.getMessage());
        }
        return INTENT_RECOMMENDATION;
    }

    /**
     * 构建职业探索相关 prompt 参数（职业推荐与通用咨询共用）
     * 精简原则：与测评集成后避免 prompt 过长导致 AI 调用失败
     * - 删除完整 assessment_json，仅保留 assessment_summary
     * - 从测评页进入时 preferences 已含测评摘要，assessment_summary 进一步简化
     */
    private Map<String, String> buildCareerParams(CareerExplorationRequest req,
                                                  CareerProfile profile, AssessmentResult latestResult,
                                                  List<JobPosition> positions,
                                                  Map<String, JobMatchResponse> realScoreMap,
                                                  Set<String> excludedJobs) {
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
        // 真实匹配度映射：岗位标题 -> 匹配分数，供 AI 参考并约束分数一致
        try {
            if (realScoreMap != null && !realScoreMap.isEmpty()) {
                Map<String, Object> scoreMap = new LinkedHashMap<>();
                for (Map.Entry<String, JobMatchResponse> entry : realScoreMap.entrySet()) {
                    scoreMap.put(entry.getKey(), entry.getValue().getMatchScore());
                }
                params.put("job_match_scores", objectMapper.writeValueAsString(scoreMap));
            } else {
                params.put("job_match_scores", "{}");
            }
        } catch (Exception e) {
            params.put("job_match_scores", "{}");
        }
        // 已推荐过的岗位列表，约束 AI 不再重复推荐
        if (excludedJobs != null && !excludedJobs.isEmpty()) {
            params.put("excluded_jobs", String.join("、", excludedJobs));
        } else {
            params.put("excluded_jobs", "（无）");
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
     */
    private boolean isInterestMatched(JobPosition job, String interest) {
        if (interest == null || interest.isBlank()) return true;
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
     * 3. 去重：移除历史已推荐岗位（excludedJobs），不足时从真实匹配度补充未推荐过的高分岗位
     * 4. 分数过滤：根据用户口头条件（如"高于80"）过滤，无满足项时返回最接近 Top3 并附加说明
     * 5. 构建双队列：primaryDirections（与用户当前兴趣相关）+ fallbackDirections（其他高分岗位）
     * @param condition 用户解析出的推荐条件（minScore/maxScore/keywords），可为 null
     * @param excludedJobs 历史已推荐岗位标题集合，可为空
     */
    private CareerDirectionResponse postProcessRecommendations(CareerDirectionResponse aiResp,
                                                               Map<String, JobMatchResponse> realScoreMap,
                                                               String interest, AssessmentResult latestResult,
                                                               RecommendationCondition condition,
                                                               Set<String> excludedJobs) {
        List<CareerDirectionResponse.DirectionItem> aiDirections = aiResp.getDirections();
        if (aiDirections == null) aiDirections = new ArrayList<>();

        // 标记是否因分数过滤为空而触发了兜底
        boolean scoreFilteredEmpty = false;

        // AI 未返回有效 directions 时，直接用真实匹配度兜底填充，避免空结果
        if (aiDirections.isEmpty() && realScoreMap != null && !realScoreMap.isEmpty()) {
            log.warn("AI 返回空 directions，使用真实匹配度兜底填充，interest={}", interest);
            List<CareerDirectionResponse.DirectionItem> fallbackItems = realScoreMap.values().stream()
                    .filter(m -> excludedJobs == null || !excludedJobs.contains(m.getTitle()))
                    .limit(5)
                    .map(this::mapMatchToDirectionItem)
                    .collect(Collectors.toList());
            // 去重后不足时，补充已推荐过的岗位（总比空好）
            if (fallbackItems.size() < 3) {
                for (JobMatchResponse match : realScoreMap.values()) {
                    if (fallbackItems.stream().anyMatch(i -> match.getTitle().equals(i.getJobTitle()))) continue;
                    fallbackItems.add(mapMatchToDirectionItem(match));
                    if (fallbackItems.size() >= 5) break;
                }
            }
            // 应用分数过滤
            fallbackItems = applyScoreFilter(fallbackItems, condition);
            if (fallbackItems.isEmpty() && condition != null && condition.minScore() != null) {
                scoreFilteredEmpty = true;
                fallbackItems = realScoreMap.values().stream()
                        .limit(3)
                        .map(this::mapMatchToDirectionItem)
                        .collect(Collectors.toList());
            }
            aiResp.setDirections(fallbackItems);
            CareerDirectionResponse resp = buildDualQueueDirections(aiResp, realScoreMap, interest);
            if (scoreFilteredEmpty) {
                appendFilterNotice(resp, condition);
            }
            return resp;
        }

        // 1. 覆盖真实分数，丢弃 AI 编造的岗位
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

        // 2. 去重：移除历史已推荐岗位
        if (excludedJobs != null && !excludedJobs.isEmpty()) {
            List<CareerDirectionResponse.DirectionItem> deduped = new ArrayList<>();
            for (CareerDirectionResponse.DirectionItem item : validItems) {
                if (!excludedJobs.contains(item.getJobTitle())) {
                    deduped.add(item);
                } else {
                    log.info("岗位 {} 已在历史中推荐，本次去重移除", item.getJobTitle());
                }
            }
            validItems = deduped;
        }

        // 3. 从真实匹配度中补充未推荐过的高分岗位，保证数量
        if (validItems.size() < 3 && realScoreMap != null) {
            for (JobMatchResponse match : realScoreMap.values()) {
                if (validItems.stream().anyMatch(i -> match.getTitle().equals(i.getJobTitle()))) continue;
                if (excludedJobs != null && excludedJobs.contains(match.getTitle())) continue;
                validItems.add(mapMatchToDirectionItem(match));
                if (validItems.size() >= 5) break;
            }
        }
        // 仍不足时，即使已推荐过也补充（避免结果过少）
        if (validItems.size() < 3 && realScoreMap != null) {
            for (JobMatchResponse match : realScoreMap.values()) {
                if (validItems.stream().anyMatch(i -> match.getTitle().equals(i.getJobTitle()))) continue;
                validItems.add(mapMatchToDirectionItem(match));
                if (validItems.size() >= 3) break;
            }
        }

        // 4. 应用分数过滤
        validItems = applyScoreFilter(validItems, condition);
        if (validItems.isEmpty() && condition != null && condition.minScore() != null) {
            scoreFilteredEmpty = true;
            // 兜底：取真实匹配度 Top 3（不应用分数过滤），优先未推荐过的
            validItems = realScoreMap.values().stream()
                    .filter(m -> excludedJobs == null || !excludedJobs.contains(m.getTitle()))
                    .limit(3)
                    .map(this::mapMatchToDirectionItem)
                    .collect(Collectors.toList());
            // 仍不足，取 Top 3 不去重
            if (validItems.size() < 3) {
                for (JobMatchResponse match : realScoreMap.values()) {
                    if (validItems.stream().anyMatch(i -> match.getTitle().equals(i.getJobTitle()))) continue;
                    validItems.add(mapMatchToDirectionItem(match));
                    if (validItems.size() >= 3) break;
                }
            }
        }

        aiResp.setDirections(validItems);
        CareerDirectionResponse resp = buildDualQueueDirections(aiResp, realScoreMap, interest);
        if (scoreFilteredEmpty) {
            appendFilterNotice(resp, condition);
        }
        return resp;
    }

    /**
     * 应用分数过滤条件
     * 根据用户口头阈值（minScore/maxScore）过滤岗位列表
     * @param items 待过滤的岗位列表
     * @param condition 用户推荐条件，可为 null
     * @return 过滤后的列表
     */
    private List<CareerDirectionResponse.DirectionItem> applyScoreFilter(
            List<CareerDirectionResponse.DirectionItem> items, RecommendationCondition condition) {
        if (condition == null || (condition.minScore() == null && condition.maxScore() == null)) {
            return items;
        }
        return items.stream()
                .filter(item -> {
                    int score = item.getMatchScore() != null ? item.getMatchScore() : 0;
                    if (condition.minScore() != null && score < condition.minScore()) return false;
                    if (condition.maxScore() != null && score > condition.maxScore()) return false;
                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * 在 overallAnalysis 中追加"未满足条件"提示
     * 当分数过滤后无结果、返回最接近推荐时调用
     * @param resp 推荐响应
     * @param condition 用户推荐条件
     */
    private void appendFilterNotice(CareerDirectionResponse resp, RecommendationCondition condition) {
        if (condition == null || condition.minScore() == null) return;
        String notice = String.format(
                "按你的条件（匹配度≥%d）暂未筛选到满足要求的岗位，以下是目前与你最匹配的几个方向供参考。",
                condition.minScore());
        String existing = resp.getOverallAnalysis();
        if (existing == null || existing.isBlank()) {
            resp.setOverallAnalysis(notice);
        } else if (!existing.contains("暂未筛选到满足要求")) {
            resp.setOverallAnalysis(notice + existing);
        }
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
     * 根据兴趣从全量岗位中构建兜底推荐项
     * 优先选择 title/direction 与兴趣匹配的岗位，按匹配强度降序排列
     * @param positions 全量岗位列表
     * @param interest 用户兴趣方向
     * @param limit 最大数量
     */
    private List<CareerDirectionResponse.DirectionItem> buildInterestFallbackItems(
            List<JobPosition> positions, String interest, int limit) {
        List<CareerDirectionResponse.DirectionItem> items = new ArrayList<>();
        if (positions == null || positions.isEmpty() || interest == null || interest.isBlank()) {
            return items;
        }
        String i = interest.toLowerCase();
        for (JobPosition p : positions) {
            String title = (p.getTitle() != null ? p.getTitle() : "").toLowerCase();
            String direction = (p.getDirection() != null ? p.getDirection() : "").toLowerCase();
            // 仅推荐与兴趣明确相关的岗位
            if (title.contains(i) || direction.contains(i) || i.contains(title) || i.contains(direction)) {
                int score = 70;
                // 完全匹配时额外加分
                if (title.contains(i) || direction.contains(i)) {
                    score += 8;
                }
                items.add(CareerDirectionResponse.DirectionItem.builder()
                        .jobTitle(p.getTitle())
                        .direction(p.getDirection())
                        .matchScore(Math.min(score, 99))
                        .reason("根据你当前提到的" + interest + "方向，从岗位库中匹配推荐")
                        .learningPriority("高")
                        .growthPath("建议从" + interest + "核心技能入手，逐步积累项目经验")
                        .build());
                if (items.size() >= limit) break;
            }
        }
        return items;
    }

    /**
     * 默认兜底推荐项：当无法提取兴趣时，按数据库顺序取前 N 个岗位
     */
    private List<CareerDirectionResponse.DirectionItem> buildDefaultFallbackItems(
            List<JobPosition> positions, int limit) {
        List<CareerDirectionResponse.DirectionItem> items = new ArrayList<>();
        if (positions == null) return items;
        for (int i = 0; i < Math.min(limit, positions.size()); i++) {
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
        return items;
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
     * 修复点：当 AI 不可用时，仍根据用户当前输入的兴趣重新计算匹配结果，避免结果一成不变
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
            InterestCity resolved = extractInterestCity(req.getPreferences(), profile,
                    req.getExpressedInterest(), result);
            Map<String, JobMatchResponse> realScoreMap = buildRealScoreMap(
                    userId, resolved.interest, resolved.city, result);
            List<CareerDirectionResponse.DirectionItem> items = realScoreMap.values().stream()
                    .limit(MAX_FALLBACK_DIRECTIONS)
                    .map(this::mapMatchToDirectionItem)
                    .collect(Collectors.toList());

            // 当 JobMatchingService 返回为空或岗位不足时，按当前兴趣从全量岗位中筛选兜底
            // 避免 AI 失败后推荐结果固定不变
            if (items.isEmpty() && resolved.interest != null && !resolved.interest.isBlank()) {
                items = buildInterestFallbackItems(positions, resolved.interest, MAX_FALLBACK_DIRECTIONS);
            }

            CareerDirectionResponse resp = CareerDirectionResponse.builder()
                    .directions(items)
                    .overallAnalysis("当前 AI 服务不稳定，系统基于你的画像与测评结果给出以下兜底推荐。")
                    .source("FALLBACK")
                    .createdAt(LocalDateTime.now())
                    .build();
            CareerDirectionResponse dualResp = buildDualQueueDirections(resp, realScoreMap, resolved.interest);
            saveRecord(userId, "CAREER_EXPLORATION", req.getPreferences(),
                    Map.of("directions", dualResp.getDirections(),
                            "primaryDirections", dualResp.getPrimaryDirections(),
                            "fallbackDirections", dualResp.getFallbackDirections(),
                            "overallAnalysis", dualResp.getOverallAnalysis()), "FALLBACK");
            return dualResp;
        } catch (Exception e) {
            log.warn("兜底推荐构建失败: {}", e.getMessage());
        }

        // 最终兜底：尝试按兴趣取相关岗位，无兴趣时取全部岗位前 5 个
        try {
            InterestCity resolved = extractInterestCity(req.getPreferences(), profile,
                    req.getExpressedInterest(), result);
            List<CareerDirectionResponse.DirectionItem> items;
            if (resolved.interest != null && !resolved.interest.isBlank()) {
                items = buildInterestFallbackItems(positions, resolved.interest, MAX_FALLBACK_DIRECTIONS);
            } else {
                items = buildDefaultFallbackItems(positions, MAX_FALLBACK_DIRECTIONS);
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
        } catch (Exception e) {
            log.warn("最终兜底推荐构建失败: {}", e.getMessage());
        }

        // 极限兜底：只返回空列表 + 提示，避免报错
        return CareerDirectionResponse.builder()
                .directions(new ArrayList<>())
                .primaryDirections(new ArrayList<>())
                .fallbackDirections(new ArrayList<>())
                .overallAnalysis("当前 AI 服务不稳定，且暂无匹配岗位，请稍后重试或补充更多信息。")
                .source("FALLBACK")
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * 判断用户问题是否更像通用咨询而非职业推荐
     * 改进策略：区分"问知识"与"问个人推荐"
     * - 含"我适合/帮我选/我做XX行不行"等个人指向 → 推荐
     * - 含"需要掌握哪些/怎么选/用什么/平时干啥/有前途吗"等知识询问 → 通用
     * - 含"岗位"但问技术/工具/架构知识 → 通用（避免见岗位就推荐）
     */
    private boolean isLikelyGeneralQuestion(String question) {
        if (question == null || question.isBlank()) return false;
        String q = question.toLowerCase();

        // 1. 纯闲聊/无关问题关键词 → 通用
        // 注意：避免"时间"误匹配"打发时间"这类闲聊开场白，使用更精确的时间询问词
        String[] chitchatKeywords = {"今天", "明天", "昨天", "星期", "日期", "几点", "什么时候", "当前时间",
                "你好", "谢谢", "再见", "吃了吗", "笑话", "故事", "唱歌", "颜色"};
        for (String kw : chitchatKeywords) {
            if (q.contains(kw)) return true;
        }

        // 2. 明确的个人推荐请求关键词 → 非通用（走推荐）
        // 必须含"我"或"帮我"等个人指向，且是请求推荐/判断/选择
        String[] personalRecommendKeywords = {
                "我适合", "帮我推荐", "帮我选", "帮我看看", "我做", "我搞", "我是学",
                "我该选", "适合我", "我想做", "我想从事", "我感兴趣", "倾向于",
                "行不行", "怎么样"};
        boolean hasPersonalRequest = false;
        for (String kw : personalRecommendKeywords) {
            if (q.contains(kw)) {
                hasPersonalRequest = true;
                break;
            }
        }
        // 若含个人推荐请求，直接判定为非通用（走推荐链路）
        if (hasPersonalRequest) {
            return false;
        }

        // 3. 知识询问关键词 → 通用（问知识而非个人推荐）
        String[] knowledgeKeywords = {"趋势", "前景", "前途", "薪资", "工资", "面试", "简历",
                "如何准备", "行业", "公司", "企业", "offer", "跳槽", "转行", "加班", "福利",
                "需要掌握", "哪些技术", "怎么选", "用什么", "平时", "干啥", "干什么",
                "一般用", "怎么拆分", "哪个更", "更值得学", "框架", "技术栈", "架构",
                "聊聊", "讨论", "介绍", "日常", "职责", "工作内容"};
        for (String kw : knowledgeKeywords) {
            if (q.contains(kw)) return true;
        }

        // 4. 兜底：默认走推荐（兼容存量行为，避免误判漏推荐）
        return false;
    }

    /**
     * 从用户输入和画像中提取兴趣方向与期望城市
     * 提取优先级：
     * 1. 当前输入 preferences 中的兴趣关键词（最高优先级）
     * 2. 测评页入口传入的 expressedInterest（用户对话中已表达的兴趣，保持一致性）
     * 3. 画像 targetRoles，但结合测评最高维度选择最合适的角色（而非永远取第一个）
     * 城市优先级：当前输入 > 画像 expectedCity
     */
    private InterestCity extractInterestCity(String preferences, CareerProfile profile,
                                               String expressedInterest, AssessmentResult latestResult) {
        String interest = null;
        String city = null;
        if (preferences != null && !preferences.isBlank()) {
            String p = preferences;
            // 兴趣关键词：后端/前端/全栈/算法/数据/产品/测试/运维/UI/UX/运营/AI/人工智能/大数据/云计算/Java/Python
            // 注意：复合词（如 AI算法）需放在简单词（AI、算法）之前，保证优先最长匹配
            Pattern interestPattern = Pattern.compile("(?:我想做|我想从事|我对| interested in |想|做|从事|学习)?(Java|Python|C\\+\\+|Go|前端|后端|全栈|AI算法|算法|数据|大数据|AI|人工智能|机器学习|深度学习|云计算|运维|测试|产品|UI|UX|设计|运营|新媒体|人力资源|财务|市场|销售)[开发工程师方向师]?",
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
        // 优先级2：测评页入口传入的 expressedInterest（用户对话中已表达的兴趣）
        if ((interest == null || interest.isBlank()) && expressedInterest != null && !expressedInterest.isBlank()) {
            interest = expressedInterest;
            log.info("使用测评页入口传入的已表达兴趣: {}", interest);
        }
        // 优先级3：画像兜底，但基于测评维度选择最合适的角色
        if ((interest == null || interest.isBlank()) && profile != null) {
            interest = selectBestTargetRoleByAssessment(profile.getTargetRoles(), latestResult);
            if (interest != null) {
                log.info("基于测评维度从画像中选择最佳目标角色: {}", interest);
            }
        }
        if ((city == null || city.isBlank()) && profile != null) {
            city = profile.getExpectedCity();
        }
        return new InterestCity(interest, city);
    }

    /**
     * 基于测评维度优势从画像 targetRoles 中选择最合适的角色
     * 替代原来固定取第一个的逻辑，避免意向方向永远是画像第一个角色（如"产品经理"）
     * 策略：找到测评最高分维度，匹配 targetRoles 中含对应关键词的角色
     * 若无匹配或无测评数据，回退到取第一个
     */
    @SuppressWarnings("unchecked")
    private String selectBestTargetRoleByAssessment(String targetRolesJson, AssessmentResult result) {
        if (targetRolesJson == null || targetRolesJson.isBlank()) return null;
        List<String> roles;
        try {
            roles = objectMapper.readValue(targetRolesJson, List.class);
        } catch (Exception e) {
            return targetRolesJson;
        }
        if (roles == null || roles.isEmpty()) return null;

        // 无测评数据时回退到第一个
        if (result == null) return roles.get(0);

        // 找到测评最高分维度
        Map<String, Double> scores = new LinkedHashMap<>();
        scores.put("编程", nz(result.getProgrammingScore()));
        scores.put("逻辑", nz(result.getLogicScore()));
        scores.put("产品", nz(result.getProductScore()));
        scores.put("技术", nz(result.getTechScore()));
        scores.put("沟通", nz(result.getCommunicationScore()));

        String topDimension = scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("产品");

        // 维度到角色关键词的映射
        Map<String, List<String>> dimensionToRoleKeywords = new LinkedHashMap<>();
        dimensionToRoleKeywords.put("编程", List.of("开发", "后端", "前端", "全栈", "Java", "Python", "工程师", "算法"));
        dimensionToRoleKeywords.put("逻辑", List.of("算法", "数据", "分析", "后端", "开发"));
        dimensionToRoleKeywords.put("产品", List.of("产品", "运营", "策划"));
        dimensionToRoleKeywords.put("技术", List.of("运维", "测试", "开发", "工程师", "技术"));
        dimensionToRoleKeywords.put("沟通", List.of("运营", "市场", "销售", "产品", "新媒体"));

        List<String> preferredKeywords = dimensionToRoleKeywords.getOrDefault(topDimension, List.of());
        // 遍历 targetRoles，找到第一个含偏好关键词的角色
        for (String role : roles) {
            if (role == null) continue;
            for (String kw : preferredKeywords) {
                if (role.contains(kw)) return role;
            }
        }
        // 无匹配时回退到第一个
        return roles.get(0);
    }

    /**
     * 安全获取 Double 值，null 返回 0
     */
    private double nz(Double v) {
        return v == null ? 0 : v;
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
     * 构建对话历史文本，供 AI prompt 使用
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
     * 检查是否命中重复请求缓存
     * 当 userId + 输入文本（去除首尾空白）与最近一次缓存相同，且未超过 30 秒时间窗时，
     * 返回缓存结果的深拷贝，source 标记为 CACHE
     * @return 命中时返回缓存响应，未命中返回 null
     */
    private CareerDirectionResponse getCachedRecommendation(Long userId, String input) {
        if (userId == null || input == null) return null;
        CacheEntry cached = lastRecommendationCache.get(userId);
        if (cached == null) return null;
        long now = System.currentTimeMillis();
        if (now - cached.timestamp > DUPLICATE_CACHE_TTL_MS) {
            lastRecommendationCache.remove(userId);
            return null;
        }
        if (!isDuplicateInput(input, cached.input)) {
            return null;
        }
        log.info("用户 {} 重复请求命中缓存，直接返回上一次结果: input={}", userId, input);
        CareerDirectionResponse copy = copyResponse(cached.response);
        copy.setSource("CACHE");
        copy.setCreatedAt(LocalDateTime.now());
        return copy;
    }

    /**
     * 更新用户最近一次推荐结果缓存
     * 追问场景（needClarification=true）也缓存，避免用户重复触发追问
     */
    private void updateCache(Long userId, String input, CareerDirectionResponse response) {
        if (userId == null || response == null) return;
        lastRecommendationCache.put(userId, new CacheEntry(input, copyResponse(response), System.currentTimeMillis()));
    }

    /**
     * 判断两次输入是否视为重复
     * 去除首尾空白后比较，忽略空字符串差异
     */
    private boolean isDuplicateInput(String current, String cached) {
        if (current == null) current = "";
        if (cached == null) cached = "";
        return current.trim().equals(cached.trim());
    }

    /**
     * 深拷贝 CareerDirectionResponse，避免缓存对象被后续修改污染
     */
    private CareerDirectionResponse copyResponse(CareerDirectionResponse source) {
        if (source == null) return null;
        CareerDirectionResponse copy = new CareerDirectionResponse();
        copy.setRecordId(source.getRecordId());
        copy.setOverallAnalysis(source.getOverallAnalysis());
        copy.setSource(source.getSource());
        copy.setCreatedAt(source.getCreatedAt());
        copy.setNeedClarification(source.getNeedClarification());
        copy.setMissingDimensions(source.getMissingDimensions() == null ? null : new ArrayList<>(source.getMissingDimensions()));
        copy.setDirections(copyDirectionItems(source.getDirections()));
        copy.setPrimaryDirections(copyDirectionItems(source.getPrimaryDirections()));
        copy.setFallbackDirections(copyDirectionItems(source.getFallbackDirections()));
        return copy;
    }

    /**
     * 深拷贝方向列表
     */
    private List<CareerDirectionResponse.DirectionItem> copyDirectionItems(
            List<CareerDirectionResponse.DirectionItem> source) {
        if (source == null) return null;
        List<CareerDirectionResponse.DirectionItem> copy = new ArrayList<>(source.size());
        for (CareerDirectionResponse.DirectionItem item : source) {
            if (item == null) continue;
            CareerDirectionResponse.DirectionItem cloned = new CareerDirectionResponse.DirectionItem();
            cloned.setJobTitle(item.getJobTitle());
            cloned.setDirection(item.getDirection());
            cloned.setMatchScore(item.getMatchScore());
            cloned.setReason(item.getReason());
            cloned.setLearningPriority(item.getLearningPriority());
            cloned.setGrowthPath(item.getGrowthPath());
            copy.add(cloned);
        }
        return copy;
    }

    /**
     * 兴趣与城市提取结果内部类
     */
    private record InterestCity(String interest, String city) {}
}
