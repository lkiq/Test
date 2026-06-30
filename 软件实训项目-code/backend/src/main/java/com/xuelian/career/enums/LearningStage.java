package com.xuelian.career.enums;

/**
 * 学习阶段枚举
 */
public enum LearningStage {
    BASIC("基础入门"),
    FRAMEWORK("框架进阶"),
    PROJECT("项目实战"),
    INTERVIEW("面试冲刺");

    private final String description;

    LearningStage(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
