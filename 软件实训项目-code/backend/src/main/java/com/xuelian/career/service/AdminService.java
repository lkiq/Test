package com.xuelian.career.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuelian.career.dto.response.DashboardResponse;
import com.xuelian.career.entity.*;

/**
 * 管理后台服务接口
 */
public interface AdminService {
    Page<User> listUsers(long page, long size, String role, String keyword);
    User getUserDetail(Long id);
    void updateUserStatus(Long id, String status);
    void resetPassword(Long id, String newPassword);
    Page<Skill> listSkills(long page, long size, String keyword);
    void addSkill(Skill skill);
    void updateSkill(Long id, Skill skill);
    void deleteSkill(Long id);
    java.util.List<JobSkillRequirement> listJobSkills(Long jobId);
    void updateJobSkills(Long jobId, java.util.List<JobSkillRequirement> requirements);
    Page<JobPosition> listPositions(long page, long size);
    DashboardResponse getDashboard();
}
