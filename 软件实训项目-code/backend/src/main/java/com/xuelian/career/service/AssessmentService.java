package com.xuelian.career.service;

import com.xuelian.career.dto.request.AssessmentSubmitRequest;
import com.xuelian.career.dto.response.AssessmentReportResponse;
import com.xuelian.career.entity.AssessmentQuestion;

import java.util.List;

/**
 * 能力测评服务接口
 */
public interface AssessmentService {

    /**
     * 生成测评题目（按测评类型，每个维度随机抽取5题）
     * @param type 测评类型
     * @return 题目列表（25题）
     */
    List<AssessmentQuestion> getQuestions(String type);

    /**
     * 提交测评答案，计算得分
     * @param userId  用户ID
     * @param request 答案请求
     * @return 测评报告
     */
    AssessmentReportResponse submitAssessment(Long userId, AssessmentSubmitRequest request);

    /**
     * 获取指定测评结果
     * @param resultId 结果ID
     * @return 测评报告
     */
    AssessmentReportResponse getResult(Long resultId);

    /**
     * 获取当前用户的测评历史
     * @param userId 用户ID
     * @return 报告列表
     */
    List<AssessmentReportResponse> getHistory(Long userId);

    /**
     * 获取当前用户最近一次测评结果（用于页面重新进入展示）
     * @param userId 用户ID
     * @return 最新测评报告，无记录时返回null
     */
    AssessmentReportResponse getLatestResult(Long userId);
}
