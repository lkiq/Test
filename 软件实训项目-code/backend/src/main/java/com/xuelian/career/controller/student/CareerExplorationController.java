package com.xuelian.career.controller.student;

import com.xuelian.career.common.Result;
import com.xuelian.career.dto.request.CareerExplorationRequest;
import com.xuelian.career.dto.response.CareerDirectionResponse;
import com.xuelian.career.entity.AssessmentResult;
import com.xuelian.career.mapper.AssessmentResultMapper;
import com.xuelian.career.service.CareerExplorationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI职业方向探索控制器（学生端）
 */
@Slf4j
@RestController
@RequestMapping("/api/student/career")
@RequiredArgsConstructor
public class CareerExplorationController {

    private final CareerExplorationService explorationService;
    private final AssessmentResultMapper assessmentResultMapper;

    /** POST /api/student/career/explore */
    @PostMapping("/explore")
    public Result<CareerDirectionResponse> explore(@RequestBody CareerExplorationRequest request,
                                                    HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(explorationService.explore(userId, request));
    }

    /**
     * 基于测评结果直达职业探索推荐
     * 根据 resultId 查询测评结果，将测评摘要作为 preferences 触发双队列推荐
     * 前端测评结果页「基于测评结果探索职业方向」按钮调用此接口
     * @param resultId 测评结果ID
     * @param body 请求体，可选字段 expressedInterest：用户在对话中已表达的兴趣方向
     *             用于保持兴趣一致性，避免测评入口覆盖用户已表达的真实兴趣
     */
    @PostMapping("/explore/from-assessment/{resultId}")
    public Result<CareerDirectionResponse> exploreFromAssessment(@PathVariable Long resultId,
                                                                  @RequestBody(required = false) Map<String, String> body,
                                                                  HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        AssessmentResult result = assessmentResultMapper.selectById(resultId);
        if (result == null) {
            log.warn("测评结果不存在: resultId={}", resultId);
            return Result.notFound("测评结果不存在");
        }
        // 提取前端传入的已表达兴趣方向（可为空）
        String expressedInterest = body != null ? body.get("expressedInterest") : null;
        if (expressedInterest != null) {
            expressedInterest = expressedInterest.trim();
            if (expressedInterest.isEmpty()) expressedInterest = null;
        }
        log.info("测评页入口推荐: resultId={}, expressedInterest={}", resultId, expressedInterest);

        // 构造 preferences：包含测评摘要 + 明确表达"基于测评推荐岗位"意图
        // 触发职业探索服务内部的双队列推荐逻辑
        StringBuilder prefs = new StringBuilder();
        prefs.append("我刚完成能力测评，请基于我的测评结果推荐合适的职业方向。");
        prefs.append(String.format("我的综合总分 %.0f 分（%s），", result.getTotalScore(), getLevel(result.getTotalScore())));
        prefs.append("五维得分：");
        prefs.append(String.format("编程能力 %.0f 分、", nz(result.getProgrammingScore())));
        prefs.append(String.format("逻辑推理 %.0f 分、", nz(result.getLogicScore())));
        prefs.append(String.format("产品思维 %.0f 分、", nz(result.getProductScore())));
        prefs.append(String.format("技术素养 %.0f 分、", nz(result.getTechScore())));
        prefs.append(String.format("沟通表达 %.0f 分。", nz(result.getCommunicationScore())));
        // 关键：若用户在对话中已表达兴趣，拼入 preferences，避免 extractInterestCity 提取失败导致兴趣被覆盖
        if (expressedInterest != null && !expressedInterest.isBlank()) {
            prefs.append(String.format("此外，我之前表示对【%s】方向感兴趣，请重点考虑该方向的岗位推荐。", expressedInterest));
        }
        prefs.append("请结合我的测评相对优势维度推荐岗位，对低分维度给出可落地的过渡岗位建议。");

        CareerExplorationRequest req = new CareerExplorationRequest();
        req.setPreferences(prefs.toString());
        req.setSource("ASSESSMENT");
        req.setExpressedInterest(expressedInterest);
        return Result.success(explorationService.explore(userId, req));
    }

    /** GET /api/student/career/explore/history */
    @GetMapping("/explore/history")
    public Result<List<CareerDirectionResponse>> getHistory(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(explorationService.getHistory(userId));
    }

    private double nz(Double v) {
        return v == null ? 0 : v;
    }

    private String getLevel(double score) {
        if (score >= 85) return "优秀";
        if (score >= 70) return "良好";
        if (score >= 55) return "一般";
        return "待提升";
    }
}
