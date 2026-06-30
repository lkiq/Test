package com.xuelian.career.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuelian.career.common.Result;
import com.xuelian.career.entity.Enterprise;
import com.xuelian.career.entity.User;
import com.xuelian.career.mapper.EnterpriseMapper;
import com.xuelian.career.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 通用用户控制器 - 提供当前用户信息查询、更新等功能
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EnterpriseMapper enterpriseMapper;

    /**
     * GET /api/user/profile — 获取当前登录用户的个人信息
     * 学生/管理员：返回 User 表中基本信息
     * HR：额外返回企业信息
     */
    @GetMapping("/profile")
    public Result<Map<String, Object>> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        Map<String, Object> profile = new LinkedHashMap<>();
        // 基本信息（脱敏处理：不返回密码哈希）
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("realName", user.getRealName());
        profile.put("role", user.getRole());
        profile.put("phone", user.getPhone());
        profile.put("email", user.getEmail());
        profile.put("education", user.getEducation());
        profile.put("school", user.getSchool());
        profile.put("major", user.getMajor());
        profile.put("status", user.getStatus());
        profile.put("createdAt", user.getCreatedAt());

        // HR 角色：额外查询企业信息
        if ("HR".equals(user.getRole())) {
            LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Enterprise::getHrUserId, userId);
            Enterprise enterprise = enterpriseMapper.selectOne(wrapper);
            if (enterprise != null) {
                Map<String, Object> companyInfo = new LinkedHashMap<>();
                companyInfo.put("companyName", enterprise.getCompanyName());
                companyInfo.put("companyIndustry", enterprise.getCompanyIndustry());
                companyInfo.put("companySize", enterprise.getCompanySize());
                companyInfo.put("companyAddress", enterprise.getCompanyAddress());
                companyInfo.put("companyDescription", enterprise.getCompanyDescription());
                companyInfo.put("contactName", enterprise.getContactName());
                companyInfo.put("contactPosition", enterprise.getContactPosition());
                companyInfo.put("verifyStatus", enterprise.getVerifyStatus());
                profile.put("enterprise", companyInfo);
            }
        }

        return Result.success(profile);
    }

    /**
     * PUT /api/user/profile — 更新当前用户的个人信息
     */
    @PutMapping("/profile")
    public Result<String> updateProfile(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        // 只允许更新安全字段
        if (body.containsKey("realName")) user.setRealName((String) body.get("realName"));
        if (body.containsKey("phone")) user.setPhone((String) body.get("phone"));
        if (body.containsKey("email")) user.setEmail((String) body.get("email"));
        if (body.containsKey("education")) user.setEducation((String) body.get("education"));
        if (body.containsKey("school")) user.setSchool((String) body.get("school"));
        if (body.containsKey("major")) user.setMajor((String) body.get("major"));

        userService.updateUser(user);

        // 如果是 HR 且有企业信息更新
        if ("HR".equals(user.getRole()) && body.containsKey("enterprise")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> entBody = (Map<String, Object>) body.get("enterprise");
            LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Enterprise::getHrUserId, userId);
            Enterprise enterprise = enterpriseMapper.selectOne(wrapper);
            if (enterprise == null) {
                enterprise = new Enterprise();
                enterprise.setHrUserId(userId);
            }
            if (entBody.containsKey("companyName")) enterprise.setCompanyName((String) entBody.get("companyName"));
            if (entBody.containsKey("companyIndustry")) enterprise.setCompanyIndustry((String) entBody.get("companyIndustry"));
            if (entBody.containsKey("companySize")) enterprise.setCompanySize((String) entBody.get("companySize"));
            if (entBody.containsKey("companyAddress")) enterprise.setCompanyAddress((String) entBody.get("companyAddress"));
            if (entBody.containsKey("companyDescription")) enterprise.setCompanyDescription((String) entBody.get("companyDescription"));
            if (entBody.containsKey("contactName")) enterprise.setContactName((String) entBody.get("contactName"));
            if (entBody.containsKey("contactPosition")) enterprise.setContactPosition((String) entBody.get("contactPosition"));

            if (enterprise.getId() == null) {
                enterpriseMapper.insert(enterprise);
            } else {
                enterpriseMapper.updateById(enterprise);
            }
        }

        return Result.success("个人信息已更新");
    }

    /**
     * PUT /api/user/password — 修改当前用户的登录密码
     */
    @PutMapping("/password")
    public Result<String> changePassword(@RequestBody Map<String, String> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        if (oldPassword == null || oldPassword.isBlank()) {
            return Result.error(400, "旧密码不能为空");
        }
        if (newPassword == null || newPassword.isBlank()) {
            return Result.error(400, "新密码不能为空");
        }
        if (newPassword.length() < 6) {
            return Result.error(400, "新密码长度不能少于6位");
        }
        if (oldPassword.equals(newPassword)) {
            return Result.error(400, "新密码不能与旧密码相同");
        }

        boolean success = userService.changePassword(userId, oldPassword, newPassword);
        if (!success) {
            // 先查用户是否存在，区分错误类型
            if (userService.getById(userId) == null) {
                return Result.error(404, "用户不存在");
            }
            return Result.error(400, "旧密码不正确");
        }

        return Result.success("密码修改成功，请使用新密码重新登录");
    }
}
