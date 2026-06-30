package com.xuelian.career.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuelian.career.common.BusinessException;
import com.xuelian.career.entity.JobApplication;
import com.xuelian.career.entity.JobPosition;
import com.xuelian.career.entity.ResumeFile;
import com.xuelian.career.entity.User;
import com.xuelian.career.mapper.JobApplicationMapper;
import com.xuelian.career.mapper.JobPositionMapper;
import com.xuelian.career.mapper.ResumeFileMapper;
import com.xuelian.career.mapper.UserMapper;
import com.xuelian.career.service.JobApplicationService;
import com.xuelian.career.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 岗位投递服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobApplicationServiceImpl implements JobApplicationService {

    private final JobApplicationMapper applicationMapper;
    private final JobPositionMapper jobPositionMapper;
    private final UserMapper userMapper;
    private final ResumeFileMapper resumeFileMapper;
    private final FileUtil fileUtil;

    @Override
    @Transactional
    public JobApplication apply(Long userId, Long jobId, Long resumeId) {
        // 1. 检查岗位是否存在且已发布
        JobPosition job = jobPositionMapper.selectById(jobId);
        if (job == null) {
            throw new BusinessException(404, "岗位不存在");
        }
        if (job.getPublishStatus() == null || job.getPublishStatus() != 1) {
            throw new BusinessException("该岗位已下架或未发布，无法投递");
        }

        // 2. 检查是否已投递
        long count = applicationMapper.selectCount(
                new LambdaQueryWrapper<JobApplication>()
                        .eq(JobApplication::getUserId, userId)
                        .eq(JobApplication::getJobId, jobId));
        if (count > 0) {
            throw new BusinessException("您已投递过该岗位，请勿重复投递");
        }

        // 3. 保存投递记录
        JobApplication app = new JobApplication();
        app.setJobId(jobId);
        app.setUserId(userId);
        app.setResumeId(resumeId);
        app.setStatus("PENDING");
        app.setCreatedAt(LocalDateTime.now());
        applicationMapper.insert(app);

        log.info("简历投递成功: userId={}, jobId={}, appId={}", userId, jobId, app.getId());
        return app;
    }

    @Override
    public List<Map<String, Object>> listMyApplications(Long userId) {
        List<JobApplication> apps = applicationMapper.selectList(
                new LambdaQueryWrapper<JobApplication>()
                        .eq(JobApplication::getUserId, userId)
                        .orderByDesc(JobApplication::getCreatedAt));

        List<Map<String, Object>> result = new ArrayList<>();
        for (JobApplication app : apps) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", app.getId());
            item.put("jobId", app.getJobId());
            item.put("userId", app.getUserId());
            item.put("resumeId", app.getResumeId());
            item.put("status", app.getStatus());
            item.put("createdAt", app.getCreatedAt());

            // 关联岗位信息
            JobPosition job = jobPositionMapper.selectById(app.getJobId());
            if (job != null) {
                item.put("jobTitle", job.getTitle());
                item.put("companyName", job.getCompanyName());
            } else {
                item.put("jobTitle", "岗位 #" + app.getJobId());
                item.put("companyName", "未知公司");
            }

            result.add(item);
        }
        return result;
    }

    @Override
    @Transactional
    public void cancelApplication(Long userId, Long applicationId) {
        JobApplication app = applicationMapper.selectById(applicationId);
        if (app == null) {
            throw new BusinessException(404, "投递记录不存在");
        }
        if (!app.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此投递记录");
        }
        app.setStatus("CANCELLED");
        applicationMapper.updateById(app);
        log.info("投递已取消: userId={}, appId={}", userId, applicationId);
    }

    @Override
    public List<Map<String, Object>> listJobApplications(Long hrUserId, Long jobId) {
        // 1. 校验该岗位是否属于此HR
        JobPosition job = jobPositionMapper.selectById(jobId);
        if (job == null) {
            throw new BusinessException(404, "岗位不存在");
        }
        if (job.getHrUserId() != null && !job.getHrUserId().equals(hrUserId)) {
            throw new BusinessException(403, "无权查看该岗位的投递记录");
        }

        // 2. 查询投递列表
        List<JobApplication> apps = applicationMapper.selectList(
                new LambdaQueryWrapper<JobApplication>()
                        .eq(JobApplication::getJobId, jobId)
                        .orderByDesc(JobApplication::getCreatedAt));

        // 3. 组装返回数据，关联用户表和简历表
        List<Map<String, Object>> result = new ArrayList<>();
        for (JobApplication app : apps) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", app.getId());
            item.put("jobId", app.getJobId());
            item.put("userId", app.getUserId());
            item.put("resumeId", app.getResumeId());
            item.put("status", app.getStatus());
            item.put("jobTitle", job.getTitle());
            item.put("companyName", job.getCompanyName());
            item.put("createdAt", app.getCreatedAt());

            // 关联用户信息
            User user = userMapper.selectById(app.getUserId());
            if (user != null) {
                item.put("realName", user.getRealName());
                item.put("phone", user.getPhone());
                item.put("email", user.getEmail());
                item.put("education", user.getEducation());
                item.put("school", user.getSchool());
                item.put("major", user.getMajor());
            }

            // 关联简历文件信息
            if (app.getResumeId() != null) {
                ResumeFile resume = resumeFileMapper.selectById(app.getResumeId());
                if (resume != null) {
                    item.put("resumeFileName", resume.getFileName());
                    item.put("resumeFileUrl", fileUtil.getFileUrl(resume.getFileUrl()));
                    item.put("resumeFileSize", resume.getFileSize());
                    item.put("resumeFileType", resume.getFileType());
                }
            }

            result.add(item);
        }
        return result;
    }

    @Override
    @Transactional
    public JobApplication updateStatus(Long hrUserId, Long applicationId, String status) {
        JobApplication app = applicationMapper.selectById(applicationId);
        if (app == null) {
            throw new BusinessException(404, "投递记录不存在");
        }

        // 校验岗位归属
        JobPosition job = jobPositionMapper.selectById(app.getJobId());
        if (job == null) {
            throw new BusinessException(404, "岗位不存在");
        }
        if (job.getHrUserId() != null && !job.getHrUserId().equals(hrUserId)) {
            throw new BusinessException(403, "无权操作该投递记录");
        }

        // 校验状态合法性
        Set<String> validStatuses = Set.of("INTERVIEW", "REJECT");
        if (!validStatuses.contains(status)) {
            throw new BusinessException("无效的状态值，支持: INTERVIEW, REJECT");
        }

        app.setStatus(status);
        applicationMapper.updateById(app);
        log.info("投递状态已更新: appId={}, status={}, hrUserId={}", applicationId, status, hrUserId);
        return app;
    }

    @Override
    @Transactional
    public void deleteApplication(Long hrUserId, Long applicationId) {
        JobApplication app = applicationMapper.selectById(applicationId);
        if (app == null) {
            throw new BusinessException(404, "投递记录不存在");
        }

        // 校验岗位归属
        JobPosition job = jobPositionMapper.selectById(app.getJobId());
        if (job == null) {
            throw new BusinessException(404, "岗位不存在");
        }
        if (job.getHrUserId() != null && !job.getHrUserId().equals(hrUserId)) {
            throw new BusinessException(403, "无权操作该投递记录");
        }

        applicationMapper.deleteById(applicationId);
        log.info("投递记录已删除: hrUserId={}, appId={}", hrUserId, applicationId);
    }
}
