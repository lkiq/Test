package com.xuelian.career.enums;

/**
 * 面试类型枚举
 */
public enum InterviewType {
    TECHNICAL("技术面试"),
    HR("HR面试"),
    COMPREHENSIVE("综合面试");

    private final String description;

    InterviewType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
