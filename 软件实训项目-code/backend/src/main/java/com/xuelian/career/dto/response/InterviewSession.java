package com.xuelian.career.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 面试会话（每道题目的返回）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewSession {
    private String sessionId;
    private String question;
    private String questionType;
    private Integer questionIndex;
    private Integer totalQuestions;
    private Boolean finished;
}
