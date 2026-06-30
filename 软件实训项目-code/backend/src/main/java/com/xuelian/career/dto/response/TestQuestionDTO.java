package com.xuelian.career.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 测试题目 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestQuestionDTO {
    private String question;
    private List<String> options;       // ["A. 选项1", "B. 选项2", "C. 选项3", "D. 选项4"]
    private String correctAnswer;       // "A"
}
