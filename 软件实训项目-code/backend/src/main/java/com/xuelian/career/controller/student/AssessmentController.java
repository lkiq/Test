package com.xuelian.career.controller.student;

import com.xuelian.career.common.Result;
import com.xuelian.career.dto.request.AssessmentSubmitRequest;
import com.xuelian.career.dto.response.AssessmentReportResponse;
import com.xuelian.career.entity.AssessmentQuestion;
import com.xuelian.career.service.AssessmentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 能力测评控制器（学生端）
 */
@Slf4j
@RestController
@RequestMapping("/api/student/assessment")
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentService assessmentService;

    /**
     * 获取测评题目
     * GET /api/student/assessment/questions?type=COMPREHENSIVE
     */
    @GetMapping("/questions")
    public Result<List<AssessmentQuestion>> getQuestions(@RequestParam(defaultValue = "COMPREHENSIVE") String type) {
        List<AssessmentQuestion> questions = assessmentService.getQuestions(type);
        // 隐藏答案
        questions.forEach(q -> q.setAnswer(null));
        return Result.success(questions);
    }

    /**
     * 提交测评答案
     * POST /api/student/assessment/submit
     */
    @PostMapping("/submit")
    public Result<AssessmentReportResponse> submit(@Valid @RequestBody AssessmentSubmitRequest request,
                                                    HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        AssessmentReportResponse report = assessmentService.submitAssessment(userId, request);
        return Result.success("测评完成", report);
    }

    /**
     * 获取指定测评结果
     * GET /api/student/assessment/result/{id}
     */
    @GetMapping("/result/{id}")
    public Result<AssessmentReportResponse> getResult(@PathVariable Long id) {
        AssessmentReportResponse result = assessmentService.getResult(id);
        if (result == null) {
            return Result.notFound("测评记录不存在");
        }
        return Result.success(result);
    }

    /**
     * 获取测评历史列表
     * GET /api/student/assessment/history
     */
    @GetMapping("/history")
    public Result<List<AssessmentReportResponse>> getHistory(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<AssessmentReportResponse> history = assessmentService.getHistory(userId);
        return Result.success(history);
    }

    /**
     * 获取当前用户最近一次测评结果（用于页面重新进入展示）
     * GET /api/student/assessment/latest-result
     * 无记录时返回204 No Content
     */
    @GetMapping("/latest-result")
    public Result<AssessmentReportResponse> getLatestResult(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        AssessmentReportResponse result = assessmentService.getLatestResult(userId);
        if (result == null) {
            return Result.success(null);
        }
        return Result.success(result);
    }
}
