package com.xuelian.career.service;

import com.xuelian.career.dto.response.GrowthReportResponse;
import com.xuelian.career.dto.response.ProgressOverviewResponse;
import com.xuelian.career.dto.response.SkillProgressResponse;

/**
 * 学习进度服务接口
 */
public interface ProgressService {
    ProgressOverviewResponse getOverview(Long userId);
    SkillProgressResponse getSkillProgress(Long userId);
    GrowthReportResponse getGrowthReport(Long userId);
}
