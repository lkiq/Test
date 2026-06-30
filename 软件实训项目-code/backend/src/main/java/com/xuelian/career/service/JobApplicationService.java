package com.xuelian.career.service;

import com.xuelian.career.entity.JobApplication;

import java.util.List;
import java.util.Map;

/**
 * 岗位投递服务接口
 */
public interface JobApplicationService {

    /** 投递简历到岗位 */
    JobApplication apply(Long userId, Long jobId, Long resumeId);

    /** 获取我的投递列表（含岗位名称和公司名称） */
    List<Map<String, Object>> listMyApplications(Long userId);

    /** 取消投递 */
    void cancelApplication(Long userId, Long applicationId);

    /** HR查看某岗位的所有投递记录（含用户信息） */
    List<Map<String, Object>> listJobApplications(Long hrUserId, Long jobId);

    /** HR更新投递状态 */
    JobApplication updateStatus(Long hrUserId, Long applicationId, String status);

    /** HR删除投递记录 */
    void deleteApplication(Long hrUserId, Long applicationId);
}
