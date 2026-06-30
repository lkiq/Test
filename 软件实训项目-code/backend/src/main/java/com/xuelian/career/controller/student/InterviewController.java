package com.xuelian.career.controller.student;

import com.xuelian.career.common.Result;
import com.xuelian.career.dto.response.InterviewReportResponse;
import com.xuelian.career.dto.response.InterviewSession;
import com.xuelian.career.service.InterviewService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 模拟面试控制器（学生端）
 */
@RestController
@RequestMapping("/api/student/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    /** POST /api/student/interview/start */
    @PostMapping("/start")
    public Result<InterviewSession> start(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long targetJobId = body.get("targetJobId") != null ? Long.valueOf(body.get("targetJobId").toString()) : null;
        String interviewType = (String) body.getOrDefault("interviewType", "COMPREHENSIVE");
        return Result.success(interviewService.startInterview(userId, targetJobId, interviewType));
    }

    /** POST /api/student/interview/{sid}/answer */
    @PostMapping("/{sid}/answer")
    public Result<InterviewSession> submitAnswer(@PathVariable String sid, @RequestBody Map<String, String> body,
                                                  HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        InterviewSession session = interviewService.submitAnswer(userId, sid, body.get("answer"));
        return session != null ? Result.success(session) : Result.notFound("面试会话不存在或已过期");
    }

    /** POST /api/student/interview/{sid}/end */
    @PostMapping("/{sid}/end")
    public Result<InterviewReportResponse> endInterview(@PathVariable String sid, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        InterviewReportResponse report = interviewService.endInterview(userId, sid);
        return report != null ? Result.success(report) : Result.notFound("面试会话不存在");
    }

    /** GET /api/student/interview/report/{id} */
    @GetMapping("/report/{id}")
    public Result<InterviewReportResponse> getReport(@PathVariable Long id) {
        InterviewReportResponse report = interviewService.getReport(id);
        return report != null ? Result.success(report) : Result.notFound("报告不存在");
    }

    /** GET /api/student/interview/history */
    @GetMapping("/history")
    public Result<List<InterviewReportResponse>> getHistory(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(interviewService.getHistory(userId));
    }
}
