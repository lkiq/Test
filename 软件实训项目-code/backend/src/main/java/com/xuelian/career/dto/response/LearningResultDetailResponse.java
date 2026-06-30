package com.xuelian.career.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 学习成果测评详情响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningResultDetailResponse {

    /** 测评结果ID */
    private Long id;

    /** 测评类型 */
    private String type;

    /** 测评阶段 */
    private String stage;

    /** 总分 */
    private Double totalScore;

    /** 等级 */
    private String level;

    /** 是否通过 */
    private Boolean passed;

    /** 正确题数 */
    private Integer correctCount;

    /** 总题数 */
    private Integer totalCount;

    /** 理论得分 */
    private Double knowledgeScore;

    /** 实操得分 */
    private Double practiceScore;

    /** 优势总结 */
    private String strengths;

    /** 薄弱环节 */
    private String weaknesses;

    /** AI分析（JSON） */
    private Map<String, Object> aiAnalysis;

    /** AI建议（JSON） */
    private Map<String, Object> aiSuggestions;

    /** 耗时（分钟） */
    private Integer durationMinutes;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 提交时间 */
    private LocalDateTime submitTime;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 题目详情（含正确答案和解析） */
    private List<QuestionDetail> questions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionDetail {
        private String content;
        private String type;
        private String userAnswer;
        private String correctAnswer;
        private Boolean isCorrect;
        private String explanation;
        private List<String> options;
    }
}
