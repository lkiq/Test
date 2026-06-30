package com.xuelian.career.service;

import com.xuelian.career.entity.JobPosition;

import java.util.List;

/**
 * 岗位发布管理服务接口（企业HR端）
 */
public interface JobPublishService {

    /** 发布岗位 */
    JobPosition publishJob(Long hrUserId, JobPosition job);

    /** 查询我的岗位列表 */
    List<JobPosition> listMyJobs(Long hrUserId);

    /** 更新岗位信息 */
    JobPosition updateJob(Long hrUserId, Long jobId, JobPosition job);

    /** 下架岗位（设置 publish_status=2） */
    void offlineJob(Long hrUserId, Long jobId);

    /** 重新发布已下架岗位 */
    void republishJob(Long hrUserId, Long jobId);

    /** 删除已下架的岗位 */
    void deleteJob(Long hrUserId, Long jobId);
}
