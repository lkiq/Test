package com.xuelian.career.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

/**
 * 管理后台数据看板响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private Long totalUsers;
    private Long totalAssessments;
    private Long totalMatches;
    private Long totalResumeAnalysis;
    private Long totalInterviews;
    private Long totalEnterpriseRecommendations;
    private List<Map<String, Object>> weeklyTrend;
}
