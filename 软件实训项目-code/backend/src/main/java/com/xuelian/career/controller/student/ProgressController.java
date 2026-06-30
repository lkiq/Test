package com.xuelian.career.controller.student;

import com.xuelian.career.common.Result;
import com.xuelian.career.dto.response.GrowthReportResponse;
import com.xuelian.career.dto.response.ProgressOverviewResponse;
import com.xuelian.career.dto.response.SkillProgressResponse;
import com.xuelian.career.service.ProgressService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 学习进度控制器（学生端）
 */
@RestController
@RequestMapping("/api/student/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    /** GET /api/student/progress/overview */
    @GetMapping("/overview")
    public Result<ProgressOverviewResponse> getOverview(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(progressService.getOverview(userId));
    }

    /** GET /api/student/progress/skills */
    @GetMapping("/skills")
    public Result<SkillProgressResponse> getSkillProgress(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(progressService.getSkillProgress(userId));
    }

    /** GET /api/student/progress/report */
    @GetMapping("/report")
    public Result<GrowthReportResponse> getGrowthReport(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(progressService.getGrowthReport(userId));
    }
}
