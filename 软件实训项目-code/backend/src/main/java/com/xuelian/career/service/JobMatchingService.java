package com.xuelian.career.service;

import com.xuelian.career.dto.response.JobMatchResponse;
import com.xuelian.career.entity.AssessmentResult;
import com.xuelian.career.entity.JobPosition;
import java.util.List;

/**
 * 岗位匹配推荐服务接口
 */
public interface JobMatchingService {
    /** 基于用户画像的岗位匹配推荐（原有方法，保持兼容） */
    List<JobMatchResponse> recommendJobs(Long userId);

    /**
     * 支持外部传入实时兴趣/城市的岗位匹配推荐（智能合并：override 优先，画像兜底）
     * @param userId 用户 ID
     * @param overrideInterest 对话中提取的最新兴趣方向（可为 null 表示沿用画像）
     * @param overrideCity 对话中提取的最新城市（可为 null 表示沿用画像）
     */
    List<JobMatchResponse> recommendJobs(Long userId, String overrideInterest, String overrideCity);

    /**
     * 支持外部传入显式测评结果的岗位匹配推荐（用于测评直达推荐场景，避免重复查库）
     * @param userId 用户 ID
     * @param overrideInterest 兴趣方向（可为 null 表示沿用画像）
     * @param overrideCity 期望城市（可为 null 表示沿用画像）
     * @param explicitResult 显式传入的测评结果（可为 null，为 null 时走原查库逻辑）
     */
    List<JobMatchResponse> recommendJobs(Long userId, String overrideInterest, String overrideCity,
                                          AssessmentResult explicitResult);

    JobPosition getJobDetail(Long jobId);
    List<JobPosition> searchJobs(String keyword, String city);
}
