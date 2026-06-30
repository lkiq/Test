package com.xuelian.career.controller.student;

import com.xuelian.career.common.Result;
import com.xuelian.career.entity.JobApplication;
import com.xuelian.career.service.JobApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 岗位投递控制器（学生端）
 */
@RestController
@RequestMapping("/api/student/application")
@RequiredArgsConstructor
public class ApplicationController {

    private final JobApplicationService applicationService;

    /** POST /api/student/application — 投递简历到岗位 */
    @PostMapping
    public Result<JobApplication> apply(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Long userId = (Long) request.getAttribute("userId");
        Long jobId = Long.valueOf(body.get("jobId").toString());
        Long resumeId = body.get("resumeId") != null
                ? Long.valueOf(body.get("resumeId").toString()) : null;
        return Result.success("投递成功", applicationService.apply(userId, jobId, resumeId));
    }

    /** GET /api/student/application/list — 我的投递列表（含岗位名称和公司名称） */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(applicationService.listMyApplications(userId));
    }

    /** DELETE /api/student/application/{id} — 取消投递 */
    @DeleteMapping("/{id}")
    public Result<String> cancel(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        applicationService.cancelApplication(userId, id);
        return Result.success("已取消投递");
    }
}
