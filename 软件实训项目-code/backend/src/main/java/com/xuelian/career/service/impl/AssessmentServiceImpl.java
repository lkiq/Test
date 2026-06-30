package com.xuelian.career.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuelian.career.dto.request.AssessmentSubmitRequest;
import com.xuelian.career.dto.response.AssessmentReportResponse;
import com.xuelian.career.entity.AssessmentDimensionConfig;
import com.xuelian.career.entity.AssessmentQuestion;
import com.xuelian.career.entity.AssessmentResult;
import com.xuelian.career.entity.AssessmentSummaryConfig;
import com.xuelian.career.mapper.AssessmentQuestionMapper;
import com.xuelian.career.mapper.AssessmentResultMapper;
import com.xuelian.career.service.AssessmentConfigService;
import com.xuelian.career.service.AssessmentService;
import com.xuelian.career.service.DeepSeekService;
import com.xuelian.career.util.PromptTemplateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * 能力测评服务实现 - 题目生成、评分计算、AI分析报告生成
 * 本地兜底分析文案、任务清单、综合评语模板均由 assessment_dimension_config / assessment_summary_config
 * 数据库表动态匹配，按维度+分数段输出个性化内容。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentServiceImpl implements AssessmentService {

    private final AssessmentQuestionMapper questionMapper;
    private final AssessmentResultMapper resultMapper;
    private final DeepSeekService deepSeekService;
    private final PromptTemplateUtil promptUtil;
    private final ObjectMapper objectMapper;
    private final AssessmentConfigService configService;

    /** 五个测评维度 */
    private static final String[] DIMENSIONS = {"PROGRAMMING", "LOGIC", "PRODUCT", "TECH", "COMMUNICATION"};
    /** 每个维度抽取题目数 */
    private static final int QUESTIONS_PER_DIMENSION = 5;

    /** 维度中文名映射 */
    private static final Map<String, String> DIM_CN = Map.of(
            "PROGRAMMING", "编程能力",
            "LOGIC", "逻辑推理",
            "PRODUCT", "产品思维",
            "TECH", "技术素养",
            "COMMUNICATION", "沟通表达"
    );

    @Override
    public List<AssessmentQuestion> getQuestions(String type) {
        List<AssessmentQuestion> questions = new ArrayList<>();
        for (String dimension : DIMENSIONS) {
            List<AssessmentQuestion> dimQuestions = questionMapper.selectRandomByDimension(dimension, QUESTIONS_PER_DIMENSION);
            questions.addAll(dimQuestions);
        }
        return questions;
    }

    @Override
    public AssessmentReportResponse submitAssessment(Long userId, AssessmentSubmitRequest request) {
        // 1. 计算各维度得分
        Map<String, Double> dimensionScores = new LinkedHashMap<>();
        Map<String, Integer> dimensionCounts = new LinkedHashMap<>();

        List<Long> questionIds = request.getAnswers().stream()
                .map(AssessmentSubmitRequest.AnswerItem::getQuestionId)
                .collect(Collectors.toList());
        List<AssessmentQuestion> questions = questionMapper.selectBatchIds(questionIds);
        Map<Long, AssessmentQuestion> questionMap = questions.stream()
                .collect(Collectors.toMap(AssessmentQuestion::getId, q -> q));

        for (AssessmentSubmitRequest.AnswerItem item : request.getAnswers()) {
            AssessmentQuestion question = questionMap.get(item.getQuestionId());
            if (question == null) continue;

            String dimension = question.getDimension();
            boolean isCorrect = question.getAnswer().equals(item.getAnswer());
            double score = isCorrect ? question.getScore() : 0;

            dimensionScores.merge(dimension, score, Double::sum);
            dimensionCounts.merge(dimension, question.getScore(), Integer::sum);
        }

        // 2. 计算各维度百分比得分
        Map<String, Double> percentageScores = new LinkedHashMap<>();
        for (String dim : DIMENSIONS) {
            double total = dimensionScores.getOrDefault(dim, 0.0);
            int maxPossible = dimensionCounts.getOrDefault(dim, 25);
            percentageScores.put(dim, maxPossible > 0 ? Math.round(total / maxPossible * 100.0) : 0.0);
        }

        // 3. 计算综合总分
        double totalScore = percentageScores.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        // 4. 调用 AI 生成分析（或使用兜底算法）
        Map<String, Object> aiAnalysis = generateAnalysis(percentageScores);

        // 5. 构建测评结果并持久化
        AssessmentResult result = new AssessmentResult();
        result.setUserId(userId);
        result.setType(request.getType() != null ? request.getType() : "COMPREHENSIVE");
        result.setProgrammingScore(percentageScores.get("PROGRAMMING"));
        result.setLogicScore(percentageScores.get("LOGIC"));
        result.setProductScore(percentageScores.get("PRODUCT"));
        result.setTechScore(percentageScores.get("TECH"));
        result.setCommunicationScore(percentageScores.get("COMMUNICATION"));
        result.setTotalScore(totalScore);
        result.setCreatedAt(LocalDateTime.now());

        // 将 AI 分析结果存入 resultJson
        try {
            result.setResultJson(objectMapper.writeValueAsString(aiAnalysis));
        } catch (JsonProcessingException e) {
            log.warn("序列化 AI 分析结果失败", e);
        }

        resultMapper.insert(result);

        // 6. 构建响应
        return buildReport(result, percentageScores, aiAnalysis);
    }

    @Override
    public AssessmentReportResponse getResult(Long resultId) {
        AssessmentResult result = resultMapper.selectById(resultId);
        if (result == null) return null;

        Map<String, Double> percentageScores = buildScoreMap(result);
        Map<String, Object> aiAnalysis = parseResultJson(result.getResultJson());
        return buildReport(result, percentageScores, aiAnalysis);
    }

    @Override
    public List<AssessmentReportResponse> getHistory(Long userId) {
        LambdaQueryWrapper<AssessmentResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssessmentResult::getUserId, userId)
                .orderByDesc(AssessmentResult::getCreatedAt);
        List<AssessmentResult> results = resultMapper.selectList(wrapper);

        return results.stream()
                .map(r -> buildReport(r, buildScoreMap(r), parseResultJson(r.getResultJson())))
                .collect(Collectors.toList());
    }

    @Override
    public AssessmentReportResponse getLatestResult(Long userId) {
        LambdaQueryWrapper<AssessmentResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssessmentResult::getUserId, userId)
                .orderByDesc(AssessmentResult::getCreatedAt)
                .last("LIMIT 1");
        AssessmentResult result = resultMapper.selectOne(wrapper);
        if (result == null) return null;

        Map<String, Double> scores = buildScoreMap(result);
        Map<String, Object> aiAnalysis = parseResultJson(result.getResultJson());
        return buildReport(result, scores, aiAnalysis);
    }

    /**
     * 从实体构建分数映射
     */
    private Map<String, Double> buildScoreMap(AssessmentResult result) {
        Map<String, Double> scores = new LinkedHashMap<>();
        scores.put("PROGRAMMING", result.getProgrammingScore());
        scores.put("LOGIC", result.getLogicScore());
        scores.put("PRODUCT", result.getProductScore());
        scores.put("TECH", result.getTechScore());
        scores.put("COMMUNICATION", result.getCommunicationScore());
        return scores;
    }

    /**
     * 解析 resultJson 中的 AI 分析结果
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseResultJson(String resultJson) {
        if (resultJson == null || resultJson.isEmpty()) return null;
        try {
            return objectMapper.readValue(resultJson, Map.class);
        } catch (Exception e) {
            log.warn("解析 resultJson 失败", e);
            return null;
        }
    }

    /**
     * 构建测评报告响应
     * 措辞软化：strengths/weaknesses 为空时使用正向描述
     */
    private AssessmentReportResponse buildReport(AssessmentResult result, Map<String, Double> scores,
                                                  Map<String, Object> aiAnalysis) {
        String level = getLevel(result.getTotalScore());
        Map<String, String> dimensionLevels = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            dimensionLevels.put(DIM_CN.getOrDefault(entry.getKey(), entry.getKey()), getLevel(entry.getValue()));
        }

        // 保留原有 strengths/weaknesses 作为兜底（措辞软化）
        String strengths = scores.entrySet().stream()
                .filter(e -> e.getValue() >= 85)
                .map(e -> DIM_CN.getOrDefault(e.getKey(), e.getKey()))
                .collect(Collectors.joining("、"));
        if (strengths.isEmpty()) strengths = "各维度均有成长空间";

        String weaknesses = scores.entrySet().stream()
                .filter(e -> e.getValue() < 55)
                .map(e -> DIM_CN.getOrDefault(e.getKey(), e.getKey()))
                .collect(Collectors.joining("、"));
        if (weaknesses.isEmpty()) weaknesses = "各维度发展较均衡";

        // 维度名称映射
        Map<String, Double> namedScores = new LinkedHashMap<>();
        scores.forEach((k, v) -> namedScores.put(DIM_CN.getOrDefault(k, k), v));

        return AssessmentReportResponse.builder()
                .id(result.getId())
                .type(result.getType())
                .dimensionScores(namedScores)
                .totalScore(result.getTotalScore())
                .level(level)
                .dimensionLevels(dimensionLevels)
                .strengths(strengths)
                .weaknesses(weaknesses)
                .aiAnalysis(aiAnalysis)
                .createdAt(result.getCreatedAt())
                .build();
    }

    /**
     * 生成分析报告：优先尝试 AI，失败则使用本地兜底算法
     * 策略：
     * 1. 即使 isAvailable() 检查异常（如 Redis 不可用），仍尝试调用一次 AI
     * 2. 区分超时与其他异常，记录不同日志便于排查
     * 3. AI 不可用/超时/异常时，降级到本地算法
     */
    private Map<String, Object> generateAnalysis(Map<String, Double> scores) {
        // 优先尝试 AI，即使 isAvailable 判断异常也尝试一次
        boolean aiAvailable = false;
        try {
            aiAvailable = deepSeekService.isAvailable();
        } catch (Exception e) {
            log.warn("AI 可用性检查异常，仍尝试调用 AI: {}", e.getMessage());
            aiAvailable = true;
        }

        // 尝试 AI 分析
        if (aiAvailable) {
            try {
                Map<String, Object> aiResult = callAIForAnalysis(scores);
                if (aiResult != null) {
                    log.info("AI 测评分析生成成功");
                    return aiResult;
                }
                log.warn("AI 测评分析返回空结果，降级到本地算法");
            } catch (Exception e) {
                if (isTimeoutException(e)) {
                    log.warn("AI 测评分析超时，降级到本地算法: timeout=15000ms, msg={}", e.getMessage());
                } else {
                    log.warn("AI 测评分析失败，降级到本地算法: {}", e.getMessage());
                }
            }
        }

        // 兜底：本地算法生成结构化分析
        log.info("使用本地兜底算法生成测评分析");
        return fallbackAnalysis(scores);
    }

    /**
     * 判断异常链中是否包含 TimeoutException
     * DeepSeekServiceImpl.doCallAPI 将 TimeoutException 包装为 RestClientException，
     * 需遍历 cause 链识别真实超时
     */
    private boolean isTimeoutException(Throwable t) {
        if (t == null) return false;
        Throwable cur = t;
        while (cur != null) {
            if (cur instanceof TimeoutException) return true;
            if (cur.getMessage() != null && cur.getMessage().contains("超时")) return true;
            cur = cur.getCause();
        }
        return false;
    }

    /**
     * 调用 DeepSeek AI 生成测评分析
     * 超时设为 15s（测评分析为非实时交互报告，DeepSeek 生成 5 维度结构化长 JSON 实测需 8-12s）
     * max_tokens 设为 1024（5维度×3层提升计划内容较长，512/768 会被截断导致 JSON 解析失败）
     * 异常向上抛出，由 generateAnalysis 统一捕获并识别超时
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> callAIForAnalysis(Map<String, Double> scores) {
        // 构建测评数据文本
        StringBuilder scoreData = new StringBuilder();
        for (String dim : DIMENSIONS) {
            String cnName = DIM_CN.get(dim);
            Double score = scores.getOrDefault(dim, 0.0);
            String level = getLevel(score);
            scoreData.append(String.format("- %s（%s）：%.0f分，等级：%s\n", dim, cnName, score, level));
        }

        Map<String, String> params = new HashMap<>();
        params.put("score_data", scoreData.toString());

        String prompt = promptUtil.loadAndRender("assessment_analysis", params);
        String response = deepSeekService.callAPI("你是一位专业的职业能力测评分析师", prompt, 15000L, 1024);
        Map<String, Object> result = deepSeekService.parseJSONResponse(response);

        if (result != null && result.containsKey("strengthAnalysis")) {
            result.put("source", "AI");
            return result;
        }
        return null;
    }

    /**
     * 本地兜底算法：基于分数生成结构化的深度分析报告
     * 优势维度 → 生成落地应用方法 + 弥补短板策略
     * 薄弱维度 → 生成三层递进式提升计划（入门→进阶→实战）
     * 文案/任务/周期均从 assessment_dimension_config 数据库表按分数段动态匹配
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> fallbackAnalysis(Map<String, Double> scores) {
        List<Map<String, Object>> strengthAnalysis = new ArrayList<>();
        List<Map<String, Object>> weaknessAnalysis = new ArrayList<>();
        List<Map<String, Object>> normalAnalysis = new ArrayList<>();

        for (String dim : DIMENSIONS) {
            double score = scores.getOrDefault(dim, 0.0);
            String cnName = DIM_CN.get(dim);
            String level = getLevel(score);

            if (score >= 85) {
                strengthAnalysis.add(buildStrengthItem(dim, cnName, score, level));
            } else if (score < 55) {
                weaknessAnalysis.add(buildWeaknessItem(dim, cnName, score, level));
            } else {
                normalAnalysis.add(buildNormalItem(dim, cnName, score, level));
            }
        }

        // 生成综合评语（数据库模板优先，未命中则使用默认逻辑兜底）
        String overallSummary = buildOverallSummary(scores, strengthAnalysis, weaknessAnalysis);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("source", "FALLBACK");
        result.put("strengthAnalysis", strengthAnalysis);
        result.put("weaknessAnalysis", weaknessAnalysis);
        result.put("overallSummary", overallSummary);
        return result;
    }

    /**
     * 构建优势维度分析项 - 落地应用 + 以长补短
     * 文案、应用方法、补偿策略均来自数据库配置
     */
    private Map<String, Object> buildStrengthItem(String dim, String cnName, double score, String level) {
        AssessmentDimensionConfig config = configService.getConfig(dim, (int) score);

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("dimension", cnName);
        item.put("score", score);
        item.put("level", level);
        item.put("analysis", config != null && config.getAnalysis() != null
                ? config.getAnalysis()
                : String.format("%s表现突出，可作为求职核心优势。", cnName));

        // application_templates 为 JSON 数组字符串
        List<Map<String, String>> applications = new ArrayList<>();
        if (config != null && config.getApplicationTemplates() != null) {
            try {
                applications = objectMapper.readValue(config.getApplicationTemplates(),
                        new TypeReference<List<Map<String, String>>>() {});
            } catch (Exception e) {
                log.warn("解析 applicationTemplates 失败 [dim={}]: {}", dim, e.getMessage());
            }
        }
        if (applications.isEmpty()) {
            applications.add(Map.of("title", "持续深化优势",
                    "desc", String.format("将 %s 优势与求职目标深度结合，形成差异化竞争力", cnName)));
        }
        item.put("applications", applications);
        item.put("compensateWeakness", config != null && config.getCompensateStrategy() != null
                ? config.getCompensateStrategy()
                : String.format("利用 %s 优势带动其他维度同步提升。", cnName));

        return item;
    }

    /**
     * 构建薄弱维度分析项 - 三层递进式提升计划
     * 文案、任务、周期均来自数据库配置（按分数段匹配，编程 0 分与 40 分文案不同）
     */
    private Map<String, Object> buildWeaknessItem(String dim, String cnName, double score, String level) {
        AssessmentDimensionConfig config = configService.getConfig(dim, (int) score);

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("dimension", cnName);
        item.put("score", score);
        item.put("level", level);
        item.put("analysis", config != null && config.getAnalysis() != null
                ? config.getAnalysis()
                : String.format("%s尚有成长空间，建议系统性补强。", cnName));

        List<Map<String, Object>> plan = new ArrayList<>();

        // 入门夯实
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("level", "入门夯实");
        entry.put("duration", config != null && config.getEntryDuration() != null
                ? config.getEntryDuration() : "2-3周");
        entry.put("tasks", parseJsonStringList(config != null ? config.getEntryTasks() : null));
        plan.add(entry);

        // 进阶提升
        Map<String, Object> advance = new LinkedHashMap<>();
        advance.put("level", "进阶提升");
        advance.put("duration", config != null && config.getAdvanceDuration() != null
                ? config.getAdvanceDuration() : "3-4周");
        advance.put("tasks", parseJsonStringList(config != null ? config.getAdvanceTasks() : null));
        plan.add(advance);

        // 实战应用
        Map<String, Object> practice = new LinkedHashMap<>();
        practice.put("level", "实战应用");
        practice.put("duration", config != null && config.getPracticeDuration() != null
                ? config.getPracticeDuration() : "持续");
        practice.put("tasks", parseJsonStringList(config != null ? config.getPracticeTasks() : null));
        plan.add(practice);

        item.put("improvementPlan", plan);
        return item;
    }

    /**
     * 构建中等维度分析项
     * 文案、应用方法、提升任务均来自数据库配置
     */
    private Map<String, Object> buildNormalItem(String dim, String cnName, double score, String level) {
        AssessmentDimensionConfig config = configService.getConfig(dim, (int) score);

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("dimension", cnName);
        item.put("score", score);
        item.put("level", level);
        item.put("analysis", config != null && config.getAnalysis() != null
                ? config.getAnalysis()
                : String.format("%s处于中等水平，仍有提升空间。", cnName));

        // 中等维度使用 application_templates 作为应用方法（若有）
        List<Map<String, String>> applications = new ArrayList<>();
        if (config != null && config.getApplicationTemplates() != null) {
            try {
                applications = objectMapper.readValue(config.getApplicationTemplates(),
                        new TypeReference<List<Map<String, String>>>() {});
            } catch (Exception e) {
                log.warn("解析 applicationTemplates 失败 [dim={}]: {}", dim, e.getMessage());
            }
        }
        // 中等维度额外给出基于 advance_tasks 的专项提升建议
        List<String> advanceTasks = parseJsonStringList(config != null ? config.getAdvanceTasks() : null);
        if (applications.isEmpty()) {
            applications.add(Map.of("title", "针对性强化",
                    "desc", String.format("当前%s处于中等水平，建议聚焦以下方向突破：%s",
                            cnName, advanceTasks.isEmpty() ? "巩固基础概念" : String.join("；", advanceTasks))));
        }
        item.put("applications", applications);

        // 中等维度使用 advance + practice 两层作为专项提升计划
        List<Map<String, Object>> plan = new ArrayList<>();
        Map<String, Object> advancePlan = new LinkedHashMap<>();
        advancePlan.put("level", "专项提升");
        advancePlan.put("duration", config != null && config.getAdvanceDuration() != null
                ? config.getAdvanceDuration() : "3-4周");
        advancePlan.put("tasks", advanceTasks.isEmpty()
                ? List.of("针对性补强该维度的核心概念") : advanceTasks);
        plan.add(advancePlan);

        Map<String, Object> practicePlan = new LinkedHashMap<>();
        practicePlan.put("level", "实战检验");
        practicePlan.put("duration", config != null && config.getPracticeDuration() != null
                ? config.getPracticeDuration() : "持续");
        practicePlan.put("tasks", parseJsonStringList(config != null ? config.getPracticeTasks() : null));
        plan.add(practicePlan);

        item.put("improvementPlan", plan);
        return item;
    }

    /**
     * 生成综合评语
     * 优先使用数据库模板（assessment_summary_config），按总分+优势/薄弱数匹配
     * 模板支持占位符：{totalScore} {level} {strengthDims} {weaknessDims} {relativeStrength} {topWeakness}
     * 未命中则使用默认逻辑兜底（措辞已软化）
     */
    private String buildOverallSummary(Map<String, Double> scores,
                                        List<Map<String, Object>> strengths,
                                        List<Map<String, Object>> weaknesses) {
        double totalScore = scores.values().stream().mapToDouble(Double::doubleValue).average().orElse(0);
        String overallLevel = getLevel(totalScore);
        int strengthCount = strengths.size();
        int weaknessCount = weaknesses.size();

        // 优先匹配数据库模板
        AssessmentSummaryConfig tplConfig = configService.getSummaryConfig(
                (int) Math.round(totalScore), strengthCount, weaknessCount);
        if (tplConfig != null && tplConfig.getSummaryTemplate() != null) {
            String strengthDims = strengths.stream()
                    .map(s -> (String) s.get("dimension"))
                    .collect(Collectors.joining("、"));
            String weaknessDims = weaknesses.stream()
                    .map(s -> (String) s.get("dimension"))
                    .collect(Collectors.joining("、"));
            String relativeStrength = strengths.isEmpty() ? "暂无明显优势" : (String) strengths.get(0).get("dimension");
            String topWeakness = weaknesses.isEmpty() ? "暂无明显待提升项" : (String) weaknesses.get(0).get("dimension");
            if (strengthDims.isEmpty()) strengthDims = "（暂无明显优势维度）";
            if (weaknessDims.isEmpty()) weaknessDims = "（暂无明显待提升维度）";
            return tplConfig.getSummaryTemplate()
                    .replace("{totalScore}", String.format("%.0f", totalScore))
                    .replace("{level}", overallLevel)
                    .replace("{strengthDims}", strengthDims)
                    .replace("{weaknessDims}", weaknessDims)
                    .replace("{relativeStrength}", relativeStrength)
                    .replace("{topWeakness}", topWeakness);
        }

        // 数据库未命中，使用默认逻辑兜底（措辞软化）
        log.warn("未命中综合评语模板 [total={},strength={},weakness={}]，使用默认兜底",
                (int) totalScore, strengthCount, weaknessCount);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("综合测评总分%.0f分，等级%s。", totalScore, overallLevel));

        if (!strengths.isEmpty()) {
            List<String> strengthDims = strengths.stream()
                    .map(s -> (String) s.get("dimension"))
                    .collect(Collectors.toList());
            sb.append(String.format("优势领域为%s，建议将这些能力深度结合到实际工作中，形成差异化竞争力。",
                    String.join("、", strengthDims)));
        }

        if (!weaknesses.isEmpty()) {
            List<String> weaknessDims = weaknesses.stream()
                    .map(s -> (String) s.get("dimension"))
                    .collect(Collectors.toList());
            sb.append(String.format("待提升领域为%s，按上述分层计划坚持执行，预计8-12周可见明显进步。",
                    String.join("、", weaknessDims)));
        }

        if (!strengths.isEmpty() && !weaknesses.isEmpty()) {
            String topStrength = (String) strengths.get(0).get("dimension");
            String topWeakness = (String) weaknesses.get(0).get("dimension");
            sb.append(String.format("可以利用%s的优势来驱动%s的学习，在实践中同步成长。",
                    topStrength, topWeakness));
        }

        if (strengths.isEmpty() && weaknesses.isEmpty()) {
            sb.append("各维度发展较为均衡，建议选择1-2个方向进行深度突破，形成自己的核心竞争力。");
        }

        return sb.toString();
    }

    /**
     * 根据分数计算等级
     */
    private String getLevel(double score) {
        if (score >= 85) return "优秀";
        if (score >= 70) return "良好";
        if (score >= 55) return "一般";
        return "待提升";
    }

    /**
     * 解析 JSON 字符串数组为 List<String>，失败返回默认列表
     */
    private List<String> parseJsonStringList(String json) {
        if (json == null || json.isBlank()) {
            return List.of("针对性补强该维度的核心概念");
        }
        try {
            List<String> list = objectMapper.readValue(json, new TypeReference<List<String>>() {});
            return list.isEmpty() ? List.of("针对性补强该维度的核心概念") : list;
        } catch (Exception e) {
            log.warn("解析 JSON 任务列表失败: {}, json={}", e.getMessage(), json);
            return List.of("针对性补强该维度的核心概念");
        }
    }
}
