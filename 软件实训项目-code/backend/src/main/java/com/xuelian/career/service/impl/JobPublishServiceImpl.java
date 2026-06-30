package com.xuelian.career.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuelian.career.common.BusinessException;
import com.xuelian.career.entity.JobPosition;
import com.xuelian.career.mapper.JobPositionMapper;
import com.xuelian.career.service.JobPublishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 岗位发布管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobPublishServiceImpl implements JobPublishService {

    private final JobPositionMapper jobPositionMapper;

    @Override
    public JobPosition publishJob(Long hrUserId, JobPosition job) {
        // 补全字段
        job.setHrUserId(hrUserId);
        job.setPublishStatus(1); // 直接发布
        job.setPublishTime(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());

        // 默认值处理
        if (job.getJobType() == null || job.getJobType().isEmpty()) {
            job.setJobType("全职");
        }
        if (job.getExperienceRequired() == null || job.getExperienceRequired().isEmpty()) {
            job.setExperienceRequired("经验不限");
        }
        if (job.getEducationRequired() == null || job.getEducationRequired().isEmpty()) {
            job.setEducationRequired("本科");
        }

        jobPositionMapper.insert(job);
        log.info("岗位发布成功: id={}, title={}, hrUserId={}", job.getId(), job.getTitle(), hrUserId);
        return job;
    }

    @Override
    public List<JobPosition> listMyJobs(Long hrUserId) {
        return jobPositionMapper.selectList(
                new LambdaQueryWrapper<JobPosition>()
                        .eq(JobPosition::getHrUserId, hrUserId)
                        .eq(JobPosition::getIsDeleted, 0)
                        .orderByDesc(JobPosition::getPublishTime)
        );
    }

    @Override
    public JobPosition updateJob(Long hrUserId, Long jobId, JobPosition job) {
        JobPosition existing = jobPositionMapper.selectById(jobId);
        if (existing == null) {
            throw new BusinessException(404, "岗位不存在");
        }
        if (!existing.getHrUserId().equals(hrUserId)) {
            throw new BusinessException(403, "无权修改该岗位");
        }

        // 更新允许修改的字段
        existing.setTitle(job.getTitle());
        existing.setDirection(job.getDirection());
        existing.setJd(job.getJd());
        existing.setCity(job.getCity());
        existing.setSalaryRange(job.getSalaryRange());
        existing.setCompanyName(job.getCompanyName());
        existing.setJobType(job.getJobType());
        existing.setExperienceRequired(job.getExperienceRequired());
        existing.setEducationRequired(job.getEducationRequired());
        existing.setLogoUrl(job.getLogoUrl());
        existing.setUpdatedAt(LocalDateTime.now());

        jobPositionMapper.updateById(existing);
        log.info("岗位更新成功: id={}, title={}", jobId, existing.getTitle());
        return existing;
    }

    @Override
    public void offlineJob(Long hrUserId, Long jobId) {
        JobPosition existing = jobPositionMapper.selectById(jobId);
        if (existing == null) {
            throw new BusinessException(404, "岗位不存在");
        }
        if (!existing.getHrUserId().equals(hrUserId)) {
            throw new BusinessException(403, "无权操作该岗位");
        }
        if (existing.getPublishStatus() == 2) {
            throw new BusinessException("岗位已下架");
        }

        existing.setPublishStatus(2);
        existing.setUpdatedAt(LocalDateTime.now());
        jobPositionMapper.updateById(existing);
        log.info("岗位下架: id={}, title={}", jobId, existing.getTitle());
    }

    @Override
    public void republishJob(Long hrUserId, Long jobId) {
        JobPosition existing = jobPositionMapper.selectById(jobId);
        if (existing == null) {
            throw new BusinessException(404, "岗位不存在");
        }
        if (!existing.getHrUserId().equals(hrUserId)) {
            throw new BusinessException(403, "无权操作该岗位");
        }
        if (existing.getPublishStatus() != 2) {
            throw new BusinessException("岗位未下架，无需重新发布");
        }

        existing.setPublishStatus(1);
        existing.setPublishTime(LocalDateTime.now());
        existing.setUpdatedAt(LocalDateTime.now());
        jobPositionMapper.updateById(existing);
        log.info("岗位重新发布: id={}, title={}", jobId, existing.getTitle());
    }

    @Override
    public void deleteJob(Long hrUserId, Long jobId) {
        JobPosition existing = jobPositionMapper.selectById(jobId);
        if (existing == null) {
            throw new BusinessException(404, "岗位不存在");
        }
        if (!existing.getHrUserId().equals(hrUserId)) {
            throw new BusinessException(403, "无权操作该岗位");
        }
        // 只有已下架的岗位才能删除
        if (existing.getPublishStatus() != 2) {
            throw new BusinessException("只有已下架的岗位才能删除，请先下架岗位");
        }

        jobPositionMapper.deleteById(jobId);
        log.info("岗位已删除: id={}, title={}, hrUserId={}", jobId, existing.getTitle(), hrUserId);
    }
}
