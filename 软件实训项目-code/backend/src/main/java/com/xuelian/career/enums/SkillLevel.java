package com.xuelian.career.enums;

/**
 * 技能掌握等级枚举
 */
public enum SkillLevel {
    NOT_MASTERED("未掌握"),
    BASIC("了解"),
    MASTER("掌握"),
    PROFICIENT("熟练"),
    EXPERT("精通");

    private final String description;

    SkillLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
