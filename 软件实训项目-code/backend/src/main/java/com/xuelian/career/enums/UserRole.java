package com.xuelian.career.enums;

/**
 * 用户角色枚举
 */
public enum UserRole {
    STUDENT("学生"),
    HR("企业HR"),
    ADMIN("管理员");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
