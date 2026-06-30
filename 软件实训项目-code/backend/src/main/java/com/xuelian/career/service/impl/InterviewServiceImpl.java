package com.xuelian.career.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuelian.career.dto.response.InterviewReportResponse;
import com.xuelian.career.dto.response.InterviewSession;
import com.xuelian.career.entity.InterviewRecord;
import com.xuelian.career.entity.JobPosition;
import com.xuelian.career.mapper.InterviewRecordMapper;
import com.xuelian.career.mapper.JobPositionMapper;
import com.xuelian.career.service.DeepSeekService;
import com.xuelian.career.service.InterviewService;
import com.xuelian.career.util.PromptTemplateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 模拟面试服务实现 - DeepSeek AI 生成题目与评估 + 本地题库兜底
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final InterviewRecordMapper recordMapper;
    private final JobPositionMapper jobPositionMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final DeepSeekService deepSeekService;
    private final PromptTemplateUtil promptUtil;
    private final ObjectMapper objectMapper;

    private static final String SESSION_PREFIX = "interview:session:";
    private static final int TOTAL_QUESTIONS = 5;

    /** 本地题库 — AI 不可用时兜底 */
    private static final List<Map<String, String>> DEFAULT_QUESTIONS = List.of(
            Map.of("q", "请做一个简单的自我介绍", "type", "行为面试"),
            Map.of("q", "请描述一个你参与过的最有挑战性的项目", "type", "项目经验"),
            Map.of("q", "你对面向对象设计中的SOLID原则有什么理解？", "type", "技术基础"),
            Map.of("q", "假设你发现线上系统响应变慢，你会怎么排查？", "type", "情景模拟"),
            Map.of("q", "你未来3-5年的职业规划是什么？", "type", "行为面试")
    );

    /** 本地题库每道题的关键词列表（用于兜底评分的关键词匹配） */
    private static final Map<Integer, List<String>> QUESTION_KEYWORDS = Map.of(
            0, List.of("项目", "经验", "技术", "能力", "方向", "专业", "学习", "团队", "实习", "开发", "编程", "框架"),
            1, List.of("挑战", "方案", "解决", "技术", "团队", "成果", "难点", "优化", "架构", "设计", "沟通", "交付"),
            2, List.of("封装", "继承", "多态", "设计", "模式", "接口", "复用", "耦合", "单一职责", "开闭原则", "依赖倒置", "重构"),
            3, List.of("排查", "日志", "监控", "性能", "负载", "缓存", "数据库", "优化", "CPU", "内存", "网络", "慢查询", "索引", "连接池"),
            4, List.of("规划", "目标", "发展", "技术", "管理", "方向", "成长", "学习", "深耕", "架构", "全栈", "团队", "业务")
    );

    /** 每道题到评估维度的权重映射 */
    private static final Map<Integer, Map<String, Double>> QUESTION_DIMENSION_WEIGHTS = Map.of(
            0, Map.of("logic", 0.1, "professionalism", 0.1, "communication", 0.5, "adaptability", 0.15, "jobFit", 0.15),   // 自我介绍
            1, Map.of("logic", 0.25, "professionalism", 0.35, "communication", 0.15, "adaptability", 0.1, "jobFit", 0.15),   // 项目经验
            2, Map.of("logic", 0.25, "professionalism", 0.45, "communication", 0.1, "adaptability", 0.1, "jobFit", 0.1),     // 技术基础
            3, Map.of("logic", 0.3, "professionalism", 0.2, "communication", 0.1, "adaptability", 0.3, "jobFit", 0.1),       // 情景模拟
            4, Map.of("logic", 0.1, "professionalism", 0.1, "communication", 0.15, "adaptability", 0.15, "jobFit", 0.5)      // 职业规划
    );

    @Override
    public InterviewSession startInterview(Long userId, Long targetJobId, String interviewType) {
        String sessionId = UUID.randomUUID().toString();
        String key = SESSION_PREFIX + sessionId;
        Map<String, Object> session = new HashMap<>();
        session.put("userId", userId);
        session.put("targetJobId", targetJobId);
        session.put("interviewType", interviewType);
        session.put("questionIndex", 0);
        session.put("totalQuestions", TOTAL_QUESTIONS);
        session.put("answers", new ArrayList<Map<String, String>>());
        session.put("stage", "FIRST_QUESTION");
        session.put("questionStartTime", System.currentTimeMillis());
        redisTemplate.opsForValue().set(key, session, 30, TimeUnit.MINUTES);

        // 尝试用 DeepSeek 生成第一道题
        try {
            if (deepSeekService.isAvailable()) {
                QuestionResult qr = callAIForQuestion(session, null, null);
                if (qr != null) {
                    return InterviewSession.builder()
                            .sessionId(sessionId)
                            .question(qr.question)
                            .questionType(qr.questionType)
                            .questionIndex(1)
                            .totalQuestions(TOTAL_QUESTIONS)
                            .finished(false)
                            .build();
                }
            }
        } catch (Exception e) {
            log.warn("AI 出题失败，使用本地题库: {}", e.getMessage());
        }

        return InterviewSession.builder()
                .sessionId(sessionId)
                .question(DEFAULT_QUESTIONS.get(0).get("q"))
                .questionType(DEFAULT_QUESTIONS.get(0).get("type"))
                .questionIndex(1)
                .totalQuestions(TOTAL_QUESTIONS)
                .finished(false)
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public InterviewSession submitAnswer(Long userId, String sessionId, String answer) {
        String key = SESSION_PREFIX + sessionId;
        Map<String, Object> session = (Map<String, Object>) redisTemplate.opsForValue().get(key);
        if (session == null) return null;

        // 获取当前题目信息
        String currentQuestion = (String) session.getOrDefault("aiCurrentQuestion",
                getLocalQuestion((Integer) session.get("questionIndex")));

        // 获取答题开始时间，计算耗时
        Long questionStartTime = (Long) session.get("questionStartTime");
        long elapsedMs = (questionStartTime != null) ? System.currentTimeMillis() - questionStartTime : 0;

        List<Map<String, String>> answers = (List<Map<String, String>>) session.get("answers");
        int currentIndex = ((Number) session.get("questionIndex")).intValue();
        // 记录答案和耗时
        Map<String, String> answerRecord = new LinkedHashMap<>();
        answerRecord.put("question", currentQuestion);
        answerRecord.put("answer", answer);
        answerRecord.put("elapsedMs", String.valueOf(elapsedMs));
        answers.add(answerRecord);
        session.put("answers", answers);

        int nextIndex = currentIndex + 1;
        session.put("questionIndex", nextIndex);

        // 如果还有题，用 AI 出下一道
        if (nextIndex < TOTAL_QUESTIONS) {
            try {
                if (deepSeekService.isAvailable()) {
                    QuestionResult qr = callAIForQuestion(session, answer, currentQuestion);
                    if (qr != null) {
                        session.put("aiCurrentQuestion", qr.question);
                        session.put("questionStartTime", System.currentTimeMillis());
                        redisTemplate.opsForValue().set(key, session, 30, TimeUnit.MINUTES);
                        return InterviewSession.builder()
                                .sessionId(sessionId)
                                .question(qr.question)
                                .questionType(qr.questionType)
                                .questionIndex(nextIndex + 1)
                                .totalQuestions(TOTAL_QUESTIONS)
                                .finished(false)
                                .build();
                    }
                }
            } catch (Exception e) {
                log.warn("AI 出题失败，使用本地题库: {}", e.getMessage());
            }

            session.put("questionStartTime", System.currentTimeMillis());
            redisTemplate.opsForValue().set(key, session, 30, TimeUnit.MINUTES);
            Map<String, String> localQ = DEFAULT_QUESTIONS.get(nextIndex);
            return InterviewSession.builder()
                    .sessionId(sessionId)
                    .question(localQ.get("q"))
                    .questionType(localQ.get("type"))
                    .questionIndex(nextIndex + 1)
                    .totalQuestions(TOTAL_QUESTIONS)
                    .finished(false)
                    .build();
        }

        // 全部答完
        session.put("stage", "EVALUATION");
        redisTemplate.opsForValue().set(key, session, 30, TimeUnit.MINUTES);
        return InterviewSession.builder()
                .sessionId(sessionId).question(null)
                .questionIndex(TOTAL_QUESTIONS).totalQuestions(TOTAL_QUESTIONS)
                .finished(true).build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public InterviewReportResponse endInterview(Long userId, String sessionId) {
        String key = SESSION_PREFIX + sessionId;
        Map<String, Object> session = (Map<String, Object>) redisTemplate.opsForValue().get(key);
        if (session == null) return null;

        // 尝试 DeepSeek 综合评估
        InterviewReportResponse report = null;
        try {
            if (deepSeekService.isAvailable()) {
                report = callAIForEvaluation(session);
            }
        } catch (Exception e) {
            log.warn("AI 评估失败，使用本地评分: {}", e.getMessage());
        }

        // AI 不可用时的本地评分兜底
        if (report == null) {
            report = localEvaluation(session);
        }

        // 持久化
        InterviewRecord record = new InterviewRecord();
        record.setUserId(userId);
        record.setTargetJobId((Long) session.get("targetJobId"));
        record.setInterviewType((String) session.get("interviewType"));
        record.setScore(report.getTotalScore());
        record.setCreatedAt(LocalDateTime.now());

        try {
            record.setQuestionJson(objectMapper.writeValueAsString(session.get("answers")));
            record.setReportJson(objectMapper.writeValueAsString(report));
        } catch (Exception e) {
            log.warn("序列化面试记录失败", e);
        }
        recordMapper.insert(record);

        redisTemplate.delete(key);

        report.setId(record.getId());
        report.setInterviewType(record.getInterviewType());
        report.setCreatedAt(record.getCreatedAt());
        return report;
    }

    // ==================== AI 调用方法 ====================

    /**
     * 调用 DeepSeek 生成一道面试题目
     */
    @SuppressWarnings("unchecked")
    private QuestionResult callAIForQuestion(Map<String, Object> session,
                                              String lastAnswer, String lastQuestion) {
        try {
            JobPosition job = getJob(session);

            // 构建历史问答
            List<Map<String, String>> history = (List<Map<String, String>>) session.get("answers");
            StringBuilder historyStr = new StringBuilder();
            if (history != null) {
                for (Map<String, String> h : history) {
                    historyStr.append("- 问题: ").append(h.get("question")).append("\n");
                    historyStr.append("  回答: ").append(h.get("answer")).append("\n");
                }
            }

            int nextIdx = ((Number) session.get("questionIndex")).intValue() + 1;

            Map<String, String> params = new HashMap<>();
            params.put("job_title", job != null ? job.getTitle() : "通用岗位");
            params.put("job_jd", job != null ? job.getJd() : "请根据候选人背景提问");
            params.put("interview_type", (String) session.getOrDefault("interviewType", "COMPREHENSIVE"));
            params.put("current_stage", "FIRST_QUESTION");
            params.put("current_task", "这是第 " + nextIdx + " / " + TOTAL_QUESTIONS
                    + " 题。请根据岗位要求和面试类型，出一道与之前不重复的题目。");
            params.put("history_qa", historyStr.length() > 0 ? historyStr.toString() : "（暂无历史问答）");

            String prompt = promptUtil.loadAndRender("mock_interview", params);
            String response = deepSeekService.callAPI("你是一位资深技术面试官", prompt, 6000L, 256);
            Map<String, Object> result = deepSeekService.parseJSONResponse(response);

            if (result != null && result.containsKey("question")) {
                return new QuestionResult(
                        (String) result.get("question"),
                        (String) result.getOrDefault("questionType", "综合面试")
                );
            }
        } catch (Exception e) {
            log.warn("DeepSeek 出题异常: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 调用 DeepSeek 生成面试综合评估报告
     */
    @SuppressWarnings("unchecked")
    private InterviewReportResponse callAIForEvaluation(Map<String, Object> session) {
        try {
            JobPosition job = getJob(session);

            List<Map<String, String>> history = (List<Map<String, String>>) session.get("answers");
            StringBuilder historyStr = new StringBuilder();
            if (history != null) {
                for (int i = 0; i < history.size(); i++) {
                    Map<String, String> h = history.get(i);
                    historyStr.append("第").append(i + 1).append("题: ").append(h.get("question")).append("\n");
                    historyStr.append("回答: ").append(h.get("answer")).append("\n\n");
                }
            }

            Map<String, String> params = new HashMap<>();
            params.put("job_title", job != null ? job.getTitle() : "通用岗位");
            params.put("job_jd", job != null ? job.getJd() : "");
            params.put("interview_type", (String) session.getOrDefault("interviewType", "COMPREHENSIVE"));
            params.put("current_stage", "EVALUATION");
            params.put("current_task", "请根据以上完整的问答记录进行综合评估，给出评分和改进建议。");
            params.put("history_qa", historyStr.toString());

            String prompt = promptUtil.loadAndRender("mock_interview", params);
            String response = deepSeekService.callAPI("你是一位资深技术面试官", prompt, 6000L, 768);
            Map<String, Object> result = deepSeekService.parseJSONResponse(response);

            if (result != null && result.containsKey("totalScore")) {
                Map<String, Object> dimMap = (Map<String, Object>) result.get("dimensionScores");
                Map<String, Double> dimensionScores = new LinkedHashMap<>();
                if (dimMap != null) {
                    dimMap.forEach((k, v) -> dimensionScores.put(k, ((Number) v).doubleValue()));
                }

                List<String> highlights = (List<String>) result.getOrDefault("highlights", List.of());
                List<String> improvements = (List<String>) result.getOrDefault("improvements", List.of());

                return InterviewReportResponse.builder()
                        .totalScore(((Number) result.get("totalScore")).doubleValue())
                        .dimensionScores(dimensionScores)
                        .highlights(highlights)
                        .improvements(improvements)
                        .summary((String) result.getOrDefault("summary", "面试完成"))
                        .build();
            }
        } catch (Exception e) {
            log.warn("DeepSeek 评估异常: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 本地评分兜底 — 基于字数/关键词/耗时的加权评分算法
     * 每题满分100分，三个维度加权求和后映射到各评估维度
     */
    @SuppressWarnings("unchecked")
    private InterviewReportResponse localEvaluation(Map<String, Object> session) {
        List<Map<String, String>> answers = (List<Map<String, String>>) session.get("answers");
        if (answers == null || answers.isEmpty()) {
            return InterviewReportResponse.builder()
                    .totalScore(50.0)
                    .dimensionScores(Map.of("logic", 50.0, "professionalism", 50.0,
                            "communication", 50.0, "adaptability", 50.0, "jobFit", 50.0))
                    .highlights(List.of())
                    .improvements(List.of("本次面试未记录有效回答，建议重新参与面试"))
                    .summary("面试记录为空")
                    .build();
        }

        // 1. 逐题评分
        List<double[]> questionScores = new ArrayList<>(); // 每题 [lengthScore, keywordScore, timeScore, weightedTotal]
        List<String> allHighlights = new ArrayList<>();
        List<String> allImprovements = new ArrayList<>();

        for (int i = 0; i < answers.size(); i++) {
            Map<String, String> ans = answers.get(i);
            String answerText = ans.getOrDefault("answer", "");
            long elapsedMs = 0;
            try { elapsedMs = Long.parseLong(ans.getOrDefault("elapsedMs", "0")); } catch (NumberFormatException ignored) {}

            double lengthScore = evaluateLength(answerText);        // 权重 30%
            double keywordScore = evaluateKeywords(answerText, i);   // 权重 40%
            double timeScore = evaluateTime(elapsedMs);              // 权重 30%
            double weightedTotal = lengthScore * 0.3 + keywordScore * 0.4 + timeScore * 0.3;

            questionScores.add(new double[]{lengthScore, keywordScore, timeScore, weightedTotal});

            // 生成每题级别的评语
            if (keywordScore >= 30) {
                allHighlights.add(String.format("第%d题：回答涵盖了多个关键概念，专业度较高（关键词匹配%.0f%%）",
                        i + 1, keywordScore / 0.4));
            }
            if (lengthScore < 10) {
                allImprovements.add(String.format("第%d题：回答过于简短（%d字），建议展开论述并补充具体案例",
                        i + 1, countChineseChars(answerText)));
            }
            if (timeScore < 10) {
                allImprovements.add(String.format("第%d题：作答耗时过短（%.1f秒），建议更深入思考后再回答",
                        i + 1, elapsedMs / 1000.0));
            }
        }

        // 2. 分维度汇总（每题按维度权重贡献）
        String[] dims = {"logic", "professionalism", "communication", "adaptability", "jobFit"};
        Map<String, Double> dimWeightedSum = new LinkedHashMap<>();
        Map<String, Double> dimWeightTotal = new LinkedHashMap<>();
        for (String d : dims) {
            dimWeightedSum.put(d, 0.0);
            dimWeightTotal.put(d, 0.0);
        }

        for (int i = 0; i < questionScores.size(); i++) {
            double total = questionScores.get(i)[3];
            Map<String, Double> weights = QUESTION_DIMENSION_WEIGHTS.getOrDefault(i, Map.of(
                    "logic", 0.2, "professionalism", 0.2, "communication", 0.2, "adaptability", 0.2, "jobFit", 0.2));
            for (String d : dims) {
                double w = weights.getOrDefault(d, 0.2);
                dimWeightedSum.merge(d, total * w, Double::sum);
                dimWeightTotal.merge(d, w, Double::sum);
            }
        }

        // 计算各维度归一化得分（0-100）
        Map<String, Double> dimensionScores = new LinkedHashMap<>();
        for (String d : dims) {
            double wSum = dimWeightedSum.getOrDefault(d, 0.0);
            double wTotal = dimWeightTotal.getOrDefault(d, 1.0);
            dimensionScores.put(d, Math.min(100, Math.round(wSum / wTotal * 10.0) / 10.0));
        }

        // 3. 综合总分（五维度平均）
        double totalScore = dimensionScores.values().stream()
                .mapToDouble(Double::doubleValue).average().orElse(0);

        // 4. 生成动态亮点和改进建议
        // 亮点：取最高分维度
        String topDim = dimensionScores.entrySet().stream()
                .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("logic");
        String dimLabel = getDimensionLabel(topDim);
        if (dimensionScores.get(topDim) >= 70) {
            allHighlights.add(0, String.format("「%s」维度表现最佳（%.0f分），展现了该领域的扎实基础",
                    dimLabel, dimensionScores.get(topDim)));
        }

        // 改进：取最低分维度
        String lowDim = dimensionScores.entrySet().stream()
                .min(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("adaptability");
        String lowLabel = getDimensionLabel(lowDim);
        allImprovements.add(0, String.format("「%s」维度建议重点提升（%.0f分），推荐针对性训练",
                lowLabel, dimensionScores.get(lowDim)));

        // 限制数量
        if (allHighlights.size() > 5) allHighlights = allHighlights.subList(0, 5);
        if (allImprovements.size() > 5) allImprovements = allImprovements.subList(0, 5);
        if (allHighlights.isEmpty()) allHighlights.add("面试流程完整，建议在后续面试中更充分地展现技术深度");
        if (allImprovements.isEmpty()) allImprovements.add("建议在回答中增加具体案例和量化成果");

        // 5. 生成综合评语
        String summary = buildInterviewSummary(totalScore, dimensionScores, topDim, lowDim);

        return InterviewReportResponse.builder()
                .totalScore(Math.round(totalScore * 10.0) / 10.0)
                .dimensionScores(dimensionScores)
                .highlights(allHighlights)
                .improvements(allImprovements)
                .summary(summary)
                .build();
    }

    /**
     * 评估回答长度得分（权重30%，满分30）
     */
    private double evaluateLength(String answer) {
        int charCount = countChineseChars(answer);
        if (charCount >= 150) return 30;
        if (charCount >= 100) return 22;
        if (charCount >= 50) return 15;
        if (charCount >= 20) return 8;
        return 3;
    }

    /**
     * 评估关键词匹配得分（权重40%，满分40）
     */
    private double evaluateKeywords(String answer, int questionIndex) {
        List<String> keywords = QUESTION_KEYWORDS.getOrDefault(questionIndex, List.of());
        if (keywords.isEmpty()) return 20; // 无关键词定义时给均分

        int hitCount = 0;
        for (String keyword : keywords) {
            if (answer.contains(keyword)) {
                hitCount++;
            }
        }
        double hitRate = (double) hitCount / keywords.size();
        return hitRate * 40;
    }

    /**
     * 评估作答耗时合理性得分（权重30%，满分30）
     */
    private double evaluateTime(long elapsedMs) {
        double seconds = elapsedMs / 1000.0;
        if (seconds < 10) return 5;        // 敷衍：<10秒
        if (seconds < 30) return 15;       // 偏短：10-30秒
        if (seconds <= 180) return 30;     // 合理：30-180秒
        return 20;                          // 过长：>180秒
    }

    /**
     * 统计中文字符数（去空白）
     */
    private int countChineseChars(String text) {
        if (text == null || text.isEmpty()) return 0;
        int count = 0;
        for (char c : text.toCharArray()) {
            if (!Character.isWhitespace(c)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 生成面试综合评语
     */
    private String buildInterviewSummary(double totalScore, Map<String, Double> dimScores,
                                          String topDim, String lowDim) {
        String level;
        if (totalScore >= 85) level = "表现优秀";
        else if (totalScore >= 70) level = "表现良好";
        else if (totalScore >= 55) level = "表现一般";
        else level = "需要提升";

        String topLabel = getDimensionLabel(topDim);
        String lowLabel = getDimensionLabel(lowDim);

        return String.format("面试综合评分%.0f分，%s。在「%s」方面表现突出（%.0f分），" +
                        "建议继续保持优势。在「%s」方面有待加强（%.0f分），" +
                        "建议通过系统学习和专项练习提升该领域能力。",
                totalScore, level,
                topLabel, dimScores.getOrDefault(topDim, 0.0),
                lowLabel, dimScores.getOrDefault(lowDim, 0.0));
    }

    /**
     * 维度英文名 → 中文标签
     */
    private String getDimensionLabel(String dimKey) {
        return switch (dimKey) {
            case "logic" -> "逻辑思维";
            case "professionalism" -> "专业能力";
            case "communication" -> "沟通表达";
            case "adaptability" -> "适应能力";
            case "jobFit" -> "岗位匹配";
            default -> dimKey;
        };
    }

    /**
     * 获取岗位信息
     */
    private JobPosition getJob(Map<String, Object> session) {
        Long jobId = (Long) session.get("targetJobId");
        if (jobId != null) {
            return jobPositionMapper.selectById(jobId);
        }
        return null;
    }

    /**
     * 获取本地题库的第 index 道题
     */
    private String getLocalQuestion(int index) {
        if (index >= 0 && index < DEFAULT_QUESTIONS.size()) {
            return DEFAULT_QUESTIONS.get(index).get("q");
        }
        return "";
    }

    /**
     * AI 返回的题目
     */
    private static class QuestionResult {
        final String question;
        final String questionType;
        QuestionResult(String question, String questionType) {
            this.question = question;
            this.questionType = questionType;
        }
    }

    @Override
    public InterviewReportResponse getReport(Long recordId) {
        InterviewRecord record = recordMapper.selectById(recordId);
        if (record == null) return null;
        return InterviewReportResponse.builder()
                .id(record.getId()).interviewType(record.getInterviewType())
                .totalScore(record.getScore()).createdAt(record.getCreatedAt())
                .highlights(List.of("已完成面试")).improvements(List.of())
                .summary("面试记录").build();
    }

    @Override
    public List<InterviewReportResponse> getHistory(Long userId) {
        return recordMapper.selectList(new LambdaQueryWrapper<InterviewRecord>()
                        .eq(InterviewRecord::getUserId, userId)
                        .orderByDesc(InterviewRecord::getCreatedAt))
                .stream().map(r -> InterviewReportResponse.builder()
                        .id(r.getId()).interviewType(r.getInterviewType())
                        .totalScore(r.getScore()).createdAt(r.getCreatedAt())
                        .build()).collect(Collectors.toList());
    }
}
