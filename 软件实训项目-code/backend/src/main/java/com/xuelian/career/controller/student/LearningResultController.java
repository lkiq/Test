package com.xuelian.career.controller.student;

import com.xuelian.career.common.Result;
import com.xuelian.career.dto.request.SubmitLearningResultRequest;
import com.xuelian.career.dto.response.LearningResultDetailResponse;
import com.xuelian.career.dto.response.TestQuestionDTO;
import com.xuelian.career.entity.LearningResult;
import com.xuelian.career.service.LearningResultService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学习成果测评控制器（学生端）
 */
@Slf4j
@RestController
@RequestMapping("/api/student/learning-result")
@RequiredArgsConstructor
public class LearningResultController {

    private final LearningResultService learningResultService;

    /**
     * GET /api/student/learning-result/questions?stage=BASIC&skillId=1
     * 开始测评 - 获取题目（不含答案）
     */
    @GetMapping("/questions")
    public Result<List<TestQuestionDTO>> getQuestions(
            @RequestParam String stage,
            @RequestParam(required = false) Long skillId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<TestQuestionDTO> questions = learningResultService.startTest(userId, stage, skillId);
        if (questions.isEmpty()) {
            return Result.error(404, "该阶段暂无测评题目，请联系管理员添加");
        }
        return Result.success(questions);
    }

    /**
     * POST /api/student/learning-result/submit
     * 提交测评答案
     */
    @PostMapping("/submit")
    public Result<LearningResultDetailResponse> submit(
            @RequestBody @Valid SubmitLearningResultRequest req,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            LearningResultDetailResponse result = learningResultService.submitTest(userId, req);
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * GET /api/student/learning-result/{id}
     * 查看某次测评详情
     */
    @GetMapping("/{id}")
    public Result<LearningResultDetailResponse> getDetail(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        LearningResultDetailResponse detail = learningResultService.getDetail(userId, id);
        if (detail == null) {
            return Result.notFound("测评记录不存在");
        }
        return Result.success(detail);
    }

    /**
     * GET /api/student/learning-result/history
     * 查看测评历史记录
     */
    @GetMapping("/history")
    public Result<List<LearningResult>> getHistory(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<LearningResult> history = learningResultService.getHistory(userId);
        return Result.success(history);
    }
}
