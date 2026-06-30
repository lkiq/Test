package com.xuelian.career.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuelian.career.common.PageResult;
import com.xuelian.career.common.Result;
import com.xuelian.career.dto.response.DashboardResponse;
import com.xuelian.career.entity.JobPosition;
import com.xuelian.career.entity.JobSkillRequirement;
import com.xuelian.career.entity.Skill;
import com.xuelian.career.entity.User;
import com.xuelian.career.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理后台控制器
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ---- 用户管理 ----
    /** GET /api/admin/users */
    @GetMapping("/users")
    public Result<PageResult<User>> listUsers(@RequestParam(defaultValue = "1") long page,
                                               @RequestParam(defaultValue = "10") long size,
                                               @RequestParam(required = false) String role,
                                               @RequestParam(required = false) String keyword) {
        Page<User> result = adminService.listUsers(page, size, role, keyword);
        return Result.success(PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords()));
    }

    /** GET /api/admin/users/{id} */
    @GetMapping("/users/{id}")
    public Result<User> getUserDetail(@PathVariable Long id) {
        return Result.success(adminService.getUserDetail(id));
    }

    /** PUT /api/admin/users/{id}/status */
    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        adminService.updateUserStatus(id, body.get("status"));
        return Result.success();
    }

    /** PUT /api/admin/users/{id}/reset-password */
    @PutMapping("/users/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        adminService.resetPassword(id, body.get("newPassword"));
        return Result.success(null);
    }

    // ---- 技能管理 ----
    /** GET /api/admin/skills */
    @GetMapping("/skills")
    public Result<PageResult<Skill>> listSkills(@RequestParam(defaultValue = "1") long page,
                                                  @RequestParam(defaultValue = "10") long size,
                                                  @RequestParam(required = false) String keyword) {
        Page<Skill> result = adminService.listSkills(page, size, keyword);
        return Result.success(PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords()));
    }

    /** POST /api/admin/skills */
    @PostMapping("/skills")
    public Result<Void> addSkill(@RequestBody Skill skill) {
        adminService.addSkill(skill);
        return Result.success();
    }

    /** PUT /api/admin/skills/{id} */
    @PutMapping("/skills/{id}")
    public Result<Void> updateSkill(@PathVariable Long id, @RequestBody Skill skill) {
        adminService.updateSkill(id, skill);
        return Result.success();
    }

    /** DELETE /api/admin/skills/{id} */
    @DeleteMapping("/skills/{id}")
    public Result<Void> deleteSkill(@PathVariable Long id) {
        adminService.deleteSkill(id);
        return Result.success();
    }

    // ---- 岗位管理 ----
    /** GET /api/admin/jobs/{id}/skills */
    @GetMapping("/jobs/{id}/skills")
    public Result<List<JobSkillRequirement>> listJobSkills(@PathVariable Long id) {
        return Result.success(adminService.listJobSkills(id));
    }

    /** PUT /api/admin/jobs/{id}/skills */
    @PutMapping("/jobs/{id}/skills")
    public Result<Void> updateJobSkills(@PathVariable Long id, @RequestBody List<JobSkillRequirement> requirements) {
        adminService.updateJobSkills(id, requirements);
        return Result.success();
    }

    /** GET /api/admin/positions */
    @GetMapping("/positions")
    public Result<PageResult<JobPosition>> listPositions(@RequestParam(defaultValue = "1") long page,
                                                           @RequestParam(defaultValue = "10") long size) {
        Page<JobPosition> result = adminService.listPositions(page, size);
        return Result.success(PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords()));
    }

    /** GET /api/admin/dashboard */
    @GetMapping("/dashboard")
    public Result<DashboardResponse> getDashboard() {
        return Result.success(adminService.getDashboard());
    }
}
