package com.xuelian.career.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuelian.career.common.BusinessException;
import com.xuelian.career.dto.response.DashboardResponse;
import com.xuelian.career.entity.*;
import com.xuelian.career.mapper.*;
import com.xuelian.career.service.AdminService;
import com.xuelian.career.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 管理后台服务实现
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final SkillMapper skillMapper;
    private final JobPositionMapper jobPositionMapper;
    private final JobSkillRequirementMapper requirementMapper;
    private final AssessmentResultMapper assessmentResultMapper;
    private final RecommendationRecordMapper recommendationRecordMapper;
    private final ResumeAnalysisMapper resumeAnalysisMapper;
    private final InterviewRecordMapper interviewRecordMapper;
    private final PasswordUtil passwordUtil;

    @Override
    public Page<User> listUsers(long page, long size, String role, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (role != null && !role.isEmpty()) wrapper.eq(User::getRole, role);
        if (keyword != null && !keyword.isEmpty()) wrapper.like(User::getUsername, keyword);
        wrapper.orderByDesc(User::getCreatedAt);
        return userMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public User getUserDetail(Long id) { return userMapper.selectById(id); }

    @Override
    public void updateUserStatus(Long id, String status) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        user.setPasswordHash(passwordUtil.encode(newPassword));
        userMapper.updateById(user);
    }

    @Override
    public Page<Skill> listSkills(long page, long size, String keyword) {
        LambdaQueryWrapper<Skill> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) wrapper.like(Skill::getName, keyword);
        wrapper.orderByAsc(Skill::getCategory).orderByAsc(Skill::getId);
        return skillMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public void addSkill(Skill skill) { skillMapper.insert(skill); }

    @Override
    public void updateSkill(Long id, Skill skill) {
        skill.setId(id);
        skillMapper.updateById(skill);
    }

    @Override
    public void deleteSkill(Long id) { skillMapper.deleteById(id); }

    @Override
    public List<JobSkillRequirement> listJobSkills(Long jobId) {
        return requirementMapper.selectList(
                new LambdaQueryWrapper<JobSkillRequirement>().eq(JobSkillRequirement::getJobId, jobId));
    }

    @Override
    public void updateJobSkills(Long jobId, List<JobSkillRequirement> requirements) {
        requirementMapper.delete(new LambdaQueryWrapper<JobSkillRequirement>().eq(JobSkillRequirement::getJobId, jobId));
        requirements.forEach(r -> { r.setJobId(jobId); requirementMapper.insert(r); });
    }

    @Override
    public Page<JobPosition> listPositions(long page, long size) {
        return jobPositionMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<JobPosition>().eq(JobPosition::getIsDeleted, 0));
    }

    @Override
    public DashboardResponse getDashboard() {
        return DashboardResponse.builder()
                .totalUsers(userMapper.selectCount(null))
                .totalAssessments(assessmentResultMapper.selectCount(null))
                .totalMatches(recommendationRecordMapper.selectCount(
                        new LambdaQueryWrapper<RecommendationRecord>().eq(RecommendationRecord::getType, "JOB_MATCH")))
                .totalResumeAnalysis(resumeAnalysisMapper.selectCount(null))
                .totalInterviews(interviewRecordMapper.selectCount(null))
                .totalEnterpriseRecommendations(recommendationRecordMapper.selectCount(
                        new LambdaQueryWrapper<RecommendationRecord>().eq(RecommendationRecord::getType, "ENTERPRISE_REC")))
                .weeklyTrend(new ArrayList<>())
                .build();
    }
}
