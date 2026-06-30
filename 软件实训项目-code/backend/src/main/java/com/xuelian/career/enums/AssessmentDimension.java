package com.xuelian.career.enums;

/**
 * 测评维度枚举
 */
public enum AssessmentDimension {
    PROGRAMMING("编程能力"),
    LOGIC("逻辑推理"),
    PRODUCT("产品思维"),
    TECH("技术素养"),
    COMMUNICATION("沟通表达");

    private final String description;

    AssessmentDimension(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
