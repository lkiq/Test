package com.xuelian.career.service;

import com.xuelian.career.dto.response.InterviewReportResponse;
import com.xuelian.career.dto.response.InterviewSession;
import java.util.List;

/**
 * 模拟面试服务接口
 */
public interface InterviewService {
    InterviewSession startInterview(Long userId, Long targetJobId, String interviewType);
    InterviewSession submitAnswer(Long userId, String sessionId, String answer);
    InterviewReportResponse endInterview(Long userId, String sessionId);
    InterviewReportResponse getReport(Long recordId);
    List<InterviewReportResponse> getHistory(Long userId);
}
