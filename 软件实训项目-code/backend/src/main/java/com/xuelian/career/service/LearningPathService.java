package com.xuelian.career.service;

import com.xuelian.career.dto.response.PathStatsDTO;
import com.xuelian.career.dto.response.SkillsMatrixDTO;
import com.xuelian.career.entity.LearningPath;
import com.xuelian.career.entity.LearningResource;
import com.xuelian.career.entity.LearningTask;

import java.util.List;
import java.util.Map;

/**
 * 学习路径服务接口
 */
public interface LearningPathService {
    LearningPath generatePath(Long userId);
    LearningPath generatePath(Long userId, Long jobId);

    /**
     * 多岗位学习路径生成（双模式）- V5 修正：复用已有路径
     * @param userId 用户ID
     * @param jobIds 岗位ID列表
     * @param mode "MERGED"合并综合路径 / "SEPARATE"各岗位独立路径
     */
    List<LearningPath> generatePath(Long userId, List<Long> jobIds, String mode);

    /**
     * V5 新增：重新生成路径（归档旧的，创建全新的）- 清空所有进度
     */
    List<LearningPath> regenerateAll(Long userId, List<Long> jobIds, String mode);

    LearningPath getPath(Long userId);
    List<LearningPath> getPaths(Long userId);

    /**
     * V5 新增：获取路径列表带统计信息
     */
    List<PathStatsDTO> getPathListWithStats(Long userId);

    void updateTaskStatus(Long taskId, String status);
    List<LearningTask> getTasks(Long userId);
    List<LearningTask> getTasks(Long userId, Long pathId);

    /**
     * V5 新增：获取单个任务（需要 userId 校验）
     */
    LearningTask getTask(Long taskId, Long userId);

    List<LearningResource> listResources(Long skillId, String stage);

    /**
     * V5 新增：获取路径的技能-岗位矩阵
     */
    List<SkillsMatrixDTO> getPathSkillsMatrix(Long pathId);

    /**
     * 获取路径元信息
     */
    Map<String, Object> getPathMeta(Long pathId);
}
