package com.xuelian.career.service;

import com.xuelian.career.dto.request.SubmitLearningResultRequest;
import com.xuelian.career.dto.response.LearningResultDetailResponse;
import com.xuelian.career.dto.response.TestQuestionDTO;
import com.xuelian.career.entity.LearningResult;

import java.util.List;

/**
 * 学习成果测评服务
 */
public interface LearningResultService {

    /**
     * 开始测评 - 获取题目
     * @param userId 用户ID
     * @param stage 测评阶段
     * @param skillId 技能ID（可选）
     * @return 题目列表（不含答案）
     */
    List<TestQuestionDTO> startTest(Long userId, String stage, Long skillId);

    /**
     * 提交测评
     */
    LearningResultDetailResponse submitTest(Long userId, SubmitLearningResultRequest request);

    /**
     * 获取测评详情
     */
    LearningResultDetailResponse getDetail(Long userId, Long resultId);

    /**
     * 获取测评历史
     */
    List<LearningResult> getHistory(Long userId);
}
