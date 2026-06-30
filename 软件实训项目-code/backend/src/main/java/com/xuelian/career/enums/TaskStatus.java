package com.xuelian.career.enums;

/**
 * 任务状态枚举
 * V6.1: PENDING→NOT_STARTED, COMPLETED→LEARNING_COMPLETED, 预留 TEST_PASSED
 */
public enum TaskStatus {
    NOT_STARTED("未开始"),
    IN_PROGRESS("学习中"),
    LEARNING_COMPLETED("已完成未测试"),
    TEST_PASSED("已完成通过测试");  // P1 启用,预留

    private final String description;

    TaskStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
