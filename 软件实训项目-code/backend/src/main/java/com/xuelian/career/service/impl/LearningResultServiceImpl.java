package com.xuelian.career.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuelian.career.dto.request.SubmitLearningResultRequest;
import com.xuelian.career.dto.response.LearningResultDetailResponse;
import com.xuelian.career.dto.response.LearningResultDetailResponse.QuestionDetail;
import com.xuelian.career.dto.response.TestQuestionDTO;
import com.xuelian.career.entity.LearningQuestion;
import com.xuelian.career.entity.LearningResult;
import com.xuelian.career.mapper.LearningQuestionMapper;
import com.xuelian.career.mapper.LearningResultMapper;
import com.xuelian.career.service.LearningResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 学习成果测评服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LearningResultServiceImpl implements LearningResultService {

    private final LearningQuestionMapper questionMapper;
    private final LearningResultMapper resultMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    /** Redis 不可用时的内存兜底 */
    private final Map<String, String> testSessionFallback = new ConcurrentHashMap<>();

    private static final String REDIS_PREFIX = "lr:session:";
    private static final long SESSION_TTL_MINUTES = 30;
    private static final int QUESTION_COUNT = 10;

    @Override
    public List<TestQuestionDTO> startTest(Long userId, String stage, Long skillId) {
        // 从题库随机抽取10题
        LambdaQueryWrapper<LearningQuestion> wrapper = new LambdaQueryWrapper<LearningQuestion>()
                .eq(LearningQuestion::getStage, stage)
                .orderByAsc(LearningQuestion::getId);
        if (skillId != null) {
            wrapper.eq(LearningQuestion::getSkillId, skillId);
        }
        List<LearningQuestion> allQuestions = questionMapper.selectList(wrapper);

        if (allQuestions.isEmpty()) {
            log.warn("题库无题目: stage={}, skillId={}", stage, skillId);
            return List.of();
        }

        // 随机打乱并取前10题
        Collections.shuffle(allQuestions);
        List<LearningQuestion> selected = allQuestions.subList(0, Math.min(QUESTION_COUNT, allQuestions.size()));

        // 转为 TestQuestionDTO（返回给前端，不含答案）
        List<TestQuestionDTO> dtos = new ArrayList<>();
        for (LearningQuestion q : selected) {
            List<String> options = parseOptions(q.getOptions());
            dtos.add(new TestQuestionDTO(q.getContent(), options, ""));
        }

        // 缓存答案到 Redis（前端只拿到不带答案的题目）
        String sessionKey = REDIS_PREFIX + userId + ":" + stage;
        Map<String, String> answerMap = new LinkedHashMap<>();
        for (int i = 0; i < selected.size(); i++) {
            answerMap.put(String.valueOf(i), selected.get(i).getAnswer());
            answerMap.put("content_" + i, selected.get(i).getContent());
            answerMap.put("type_" + i, selected.get(i).getType());
            answerMap.put("explanation_" + i, selected.get(i).getExplanation() != null ? selected.get(i).getExplanation() : "");
            answerMap.put("options_" + i, selected.get(i).getOptions());
            answerMap.put("knowledge_" + i, selected.get(i).getKnowledgePoint() != null ? selected.get(i).getKnowledgePoint() : "");
            answerMap.put("score_" + i, String.valueOf(selected.get(i).getScore() != null ? selected.get(i).getScore() : 5));
        }

        try {
            String json = objectMapper.writeValueAsString(answerMap);
            redisTemplate.opsForValue().set(sessionKey, json, SESSION_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis存储测评session失败，使用内存兜底", e);
            try {
                testSessionFallback.put(sessionKey, objectMapper.writeValueAsString(answerMap));
            } catch (Exception ex) {
                log.error("内存兜底也失败", ex);
            }
        }

        return dtos;
    }

    @Override
    public LearningResultDetailResponse submitTest(Long userId, SubmitLearningResultRequest request) {
        String stage = request.getStage();
        String sessionKey = REDIS_PREFIX + userId + ":" + stage;

        // 从 Redis 或内存获取缓存的答案
        Map<String, String> answerMap = null;
        try {
            Object cached = redisTemplate.opsForValue().get(sessionKey);
            if (cached != null) {
                answerMap = objectMapper.convertValue(cached, new TypeReference<Map<String, String>>() {});
            }
        } catch (Exception e) {
            log.warn("Redis读取测评session失败", e);
        }
        if (answerMap == null) {
            String fallback = testSessionFallback.get(sessionKey);
            if (fallback != null) {
                try {
                    answerMap = objectMapper.readValue(fallback, new TypeReference<Map<String, String>>() {});
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        if (answerMap == null) {
            throw new RuntimeException("测评会话已过期，请重新开始");
        }

        // 计算得分
        Map<String, String> userAnswers = request.getAnswers();
        int correctCount = 0;
        int totalScore = 0;
        int maxScore = 0;
        List<QuestionDetail> details = new ArrayList<>();
        List<String> wrongKnowledge = new ArrayList<>();

        for (int i = 0; ; i++) {
            String correctAnswer = answerMap.get(String.valueOf(i));
            if (correctAnswer == null) break;

            String contentType = answerMap.getOrDefault("type_" + i, "SELECT");
            String userAnswer = userAnswers != null ? userAnswers.get(String.valueOf(i)) : null;
            String explanation = answerMap.getOrDefault("explanation_" + i, "");
            String knowledge = answerMap.getOrDefault("knowledge_" + i, "");
            int qScore = Integer.parseInt(answerMap.getOrDefault("score_" + i, "5"));
            maxScore += qScore;

            boolean isCorrect = userAnswer != null && userAnswer.trim().equalsIgnoreCase(correctAnswer.trim());
            if (isCorrect) {
                correctCount++;
                totalScore += qScore;
            } else {
                if (!knowledge.isEmpty()) wrongKnowledge.add(knowledge);
            }

            List<String> options = parseOptions(answerMap.get("options_" + i));
            QuestionDetail detail = QuestionDetail.builder()
                    .content(answerMap.get("content_" + i))
                    .type(contentType)
                    .userAnswer(userAnswer)
                    .correctAnswer(correctAnswer)
                    .isCorrect(isCorrect)
                    .explanation(explanation)
                    .options(options)
                    .build();
            details.add(detail);
        }

        int totalQuestions = details.size();
        double scorePercent = totalQuestions > 0 ? (double) correctCount / totalQuestions * 100 : 0;

        // 清除缓存
        try { redisTemplate.delete(sessionKey); } catch (Exception e) { /* silent */ }
        testSessionFallback.remove(sessionKey);

        // 保存结果
        LearningResult result = new LearningResult();
        result.setUserId(userId);
        result.setPathId(request.getPathId());
        result.setSkillId(request.getSkillId());
        result.setType("STAGE");
        result.setStage(stage);
        result.setTotalScore(round(scorePercent));
        result.setKnowledgeScore(round(scorePercent));
        result.setPracticeScore(round(scorePercent));
        result.setLevel(getLevel(scorePercent));
        result.setPassed(scorePercent >= 60 ? 1 : 0);
        result.setCorrectCount(correctCount);
        result.setTotalCount(totalQuestions);
        result.setStartTime(LocalDateTime.now().minusMinutes(5)); // 近似
        result.setSubmitTime(LocalDateTime.now());
        result.setDurationMinutes(5);

        // 薄弱环节
        if (!wrongKnowledge.isEmpty()) {
            result.setWeaknesses("建议加强: " + String.join("、", wrongKnowledge.stream().distinct().limit(5).collect(Collectors.toList())));
        }
        if (scorePercent >= 80) {
            result.setStrengths("理论基础扎实，综合表现优秀");
        } else if (scorePercent >= 60) {
            result.setStrengths("基本掌握核心知识点，仍有提升空间");
        }

        resultMapper.insert(result);

        // 构建响应
        return buildDetailResponse(result, details);
    }

    @Override
    public LearningResultDetailResponse getDetail(Long userId, Long resultId) {
        LearningResult result = resultMapper.selectOne(
                new LambdaQueryWrapper<LearningResult>()
                        .eq(LearningResult::getId, resultId)
                        .eq(LearningResult::getUserId, userId));
        if (result == null) return null;

        // 历史记录没有题目详情，返回基本信息
        return LearningResultDetailResponse.builder()
                .id(result.getId())
                .type(result.getType())
                .stage(result.getStage())
                .totalScore(result.getTotalScore())
                .level(result.getLevel())
                .passed(result.getPassed() == 1)
                .correctCount(result.getCorrectCount())
                .totalCount(result.getTotalCount())
                .knowledgeScore(result.getKnowledgeScore())
                .practiceScore(result.getPracticeScore())
                .strengths(result.getStrengths())
                .weaknesses(result.getWeaknesses())
                .durationMinutes(result.getDurationMinutes())
                .startTime(result.getStartTime())
                .submitTime(result.getSubmitTime())
                .createdAt(result.getCreatedAt())
                .questions(List.of())
                .build();
    }

    @Override
    public List<LearningResult> getHistory(Long userId) {
        return resultMapper.selectList(
                new LambdaQueryWrapper<LearningResult>()
                        .eq(LearningResult::getUserId, userId)
                        .orderByDesc(LearningResult::getCreatedAt)
                        .last("LIMIT 20"));
    }

    // ==================== 辅助方法 ====================

    private List<String> parseOptions(String optionsJson) {
        if (optionsJson == null || optionsJson.isBlank()) return List.of();
        try {
            return objectMapper.readValue(optionsJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private LearningResultDetailResponse buildDetailResponse(LearningResult result, List<QuestionDetail> details) {
        return LearningResultDetailResponse.builder()
                .id(result.getId())
                .type(result.getType())
                .stage(result.getStage())
                .totalScore(result.getTotalScore())
                .level(result.getLevel())
                .passed(result.getPassed() == 1)
                .correctCount(result.getCorrectCount())
                .totalCount(result.getTotalCount())
                .knowledgeScore(result.getKnowledgeScore())
                .practiceScore(result.getPracticeScore())
                .strengths(result.getStrengths())
                .weaknesses(result.getWeaknesses())
                .durationMinutes(result.getDurationMinutes())
                .startTime(result.getStartTime())
                .submitTime(result.getSubmitTime())
                .createdAt(result.getCreatedAt())
                .questions(details)
                .build();
    }

    private String getLevel(double score) {
        if (score >= 90) return "优秀";
        if (score >= 75) return "良好";
        if (score >= 60) return "一般";
        return "待提升";
    }

    private double round(double v) { return Math.round(v * 10.0) / 10.0; }
}
