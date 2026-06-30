package com.xuelian.career.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 测评报告响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentReportResponse {
    private Long id;
    private String type;
    /** 各维度得分 */
    private Map<String, Double> dimensionScores;
    /** 综合总分 */
    private Double totalScore;
    /** 等级评定 */
    private String level;
    /** 各维度等级 */
    private Map<String, String> dimensionLevels;
    /** 优势维度（保留作为兜底） */
    private String strengths;
    /** 薄弱维度（保留作为兜底） */
    private String weaknesses;
    /** AI生成的详细分析报告 */
    private Map<String, Object> aiAnalysis;
    /** 创建时间 */
    private LocalDateTime createdAt;
}
