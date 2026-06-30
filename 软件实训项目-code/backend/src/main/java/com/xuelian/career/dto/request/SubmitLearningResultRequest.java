package com.xuelian.career.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * 学习成果测评提交请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitLearningResultRequest {

    /** 学习路径ID（可选） */
    private Long pathId;

    /** 技能ID（可选） */
    private Long skillId;

    /** 测评阶段：BASIC/FRAMEWORK/PROJECT/INTERVIEW/FINAL */
    @NotNull
    private String stage;

    /** 用户答案 Map<题号, 答案> */
    @NotNull
    private Map<String, String> answers;
}
