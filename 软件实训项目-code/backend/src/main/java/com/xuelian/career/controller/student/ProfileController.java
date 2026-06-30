package com.xuelian.career.controller.student;

import com.xuelian.career.common.Result;
import com.xuelian.career.dto.request.ProfileRequest;
import com.xuelian.career.dto.response.ProfileResponse;
import com.xuelian.career.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 求职画像控制器（学生端）
 */
@Slf4j
@RestController
@RequestMapping("/api/student/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    /**
     * 获取当前用户的求职画像
     * GET /api/student/profile
     */
    @GetMapping
    public Result<ProfileResponse> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        ProfileResponse profile = profileService.getProfile(userId);
        if (profile == null) {
            return Result.success("尚未填写求职画像", null);
        }
        return Result.success(profile);
    }

    /**
     * 保存求职画像
     * POST /api/student/profile
     */
    @PostMapping
    public Result<ProfileResponse> saveProfile(@RequestBody ProfileRequest profileRequest, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        ProfileResponse profile = profileService.saveProfile(userId, profileRequest);
        return Result.success("保存成功", profile);
    }
}
