package com.xuelian.career.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuelian.career.dto.request.ProfileRequest;
import com.xuelian.career.dto.response.ProfileResponse;
import com.xuelian.career.entity.CareerProfile;
import com.xuelian.career.mapper.CareerProfileMapper;
import com.xuelian.career.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 求职画像服务实现 - 处理求职画像的保存与查询
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final CareerProfileMapper careerProfileMapper;

    @Override
    public ProfileResponse saveProfile(Long userId, ProfileRequest request) {
        // 查询是否已有画像记录
        LambdaQueryWrapper<CareerProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CareerProfile::getUserId, userId);
        CareerProfile existing = careerProfileMapper.selectOne(wrapper);

        CareerProfile profile;
        if (existing != null) {
            // 更新已有画像
            profile = existing;
        } else {
            // 新建画像
            profile = new CareerProfile();
            profile.setUserId(userId);
            profile.setCreatedAt(LocalDateTime.now());
        }

        // 复制请求属性到实体
        profile.setSchool(request.getSchool());
        profile.setMajor(request.getMajor());
        profile.setEducation(request.getEducation());
        profile.setGrade(request.getGrade());
        profile.setSkillTags(request.getSkillTags());
        profile.setTargetRoles(request.getTargetRoles());
        profile.setExpectedCity(request.getExpectedCity());
        profile.setExpectedSalary(request.getExpectedSalary());
        profile.setJobStatus(request.getJobStatus());
        profile.setSummary(request.getSummary());
        profile.setUpdatedAt(LocalDateTime.now());

        if (existing != null) {
            careerProfileMapper.updateById(profile);
            log.info("求职画像更新成功: userId={}", userId);
        } else {
            careerProfileMapper.insert(profile);
            log.info("求职画像创建成功: userId={}", userId);
        }

        return convertToResponse(profile);
    }

    @Override
    public ProfileResponse getProfile(Long userId) {
        LambdaQueryWrapper<CareerProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CareerProfile::getUserId, userId);
        CareerProfile profile = careerProfileMapper.selectOne(wrapper);

        if (profile == null) {
            return null;
        }
        return convertToResponse(profile);
    }

    /**
     * 实体转响应 DTO
     */
    private ProfileResponse convertToResponse(CareerProfile profile) {
        ProfileResponse response = new ProfileResponse();
        BeanUtils.copyProperties(profile, response);
        return response;
    }
}
