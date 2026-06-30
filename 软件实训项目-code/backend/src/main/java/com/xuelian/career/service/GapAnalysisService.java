package com.xuelian.career.service;

import com.xuelian.career.dto.response.GapReportResponse;

import java.util.List;
import java.util.Map;

/**
 * 能力差距分析服务接口
 * V6.1: 新增双模式分析、技能等级计算
 */
public interface GapAnalysisService {
    /** 单岗位差距分析 */
    GapReportResponse analyze(Long userId, Long jobId);

    /** 多岗位合并差距分析 */
    GapReportResponse analyzeMultiple(Long userId, List<Long> jobIds);

    GapReportResponse getReport(Long recordId);

    /**
     * 基于用户测评结果推荐匹配岗位
     * @param userId 用户ID
     * @return 推荐岗位列表（含岗位ID、名称、匹配度）
     */
    List<Map<String, Object>> getRecommendedJobs(Long userId);

    /**
     * 计算用户对某技能的实际掌握等级（基于学习任务状态）
     * @return 0-4: NOT_MASTERED/BASIC/MASTER/PROFICIENT/EXPERT
     */
    Integer calculateUserSkillLevel(Long userId, Long skillId);
}
