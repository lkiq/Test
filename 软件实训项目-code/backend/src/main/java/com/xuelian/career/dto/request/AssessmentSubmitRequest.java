package com.xuelian.career.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

/**
 * 提交测评答案请求 DTO
 */
@Data
public class AssessmentSubmitRequest {
    /** 测评类型 */
    private String type;
    /** 答案列表（key: 题目ID, value: 选择答案） */
    @NotEmpty(message = "答案不能为空")
    private List<AnswerItem> answers;

    @Data
    public static class AnswerItem {
        private Long questionId;
        private String answer;
    }
}
