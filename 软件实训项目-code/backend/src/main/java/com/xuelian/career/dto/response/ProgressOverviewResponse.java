package com.xuelian.career.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学习进度总览响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressOverviewResponse {
    private Integer completedTasks;
    private Integer totalTasks;
    private Double completionRate;
    private Integer totalAssessmentCount;
    private Integer totalInterviewCount;
    private Integer resumeAnalysisCount;
    private Double averageAssessmentScore;
}
