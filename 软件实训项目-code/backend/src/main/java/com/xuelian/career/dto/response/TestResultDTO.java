package com.xuelian.career.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 测试结果 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestResultDTO {
    private int score;           // 0-100
    private int correctCount;    // 正确数量
    private int totalCount;      // 总题数
    private boolean passed;      // 是否通过 (score >= 60)
    private String newLevel;     // 更新后的 mastery level

    public TestResultDTO(int score, int correctCount, int totalCount) {
        this.score = score;
        this.correctCount = correctCount;
        this.totalCount = totalCount;
        this.passed = score >= 60;
        this.newLevel = score >= 80 ? "ADVANCED" : (score >= 60 ? "INTERMEDIATE" : "BASIC");
    }
}
