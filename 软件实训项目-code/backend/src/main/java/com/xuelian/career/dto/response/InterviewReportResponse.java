package com.xuelian.career.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 面试报告响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewReportResponse {
    private Long id;
    private String interviewType;
    private Double totalScore;
    private Map<String, Double> dimensionScores;
    private List<String> highlights;
    private List<String> improvements;
    private String summary;
    private LocalDateTime createdAt;
}
