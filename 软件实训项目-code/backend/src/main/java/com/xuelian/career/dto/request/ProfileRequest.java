package com.xuelian.career.dto.request;

import lombok.Data;

/**
 * 保存求职画像请求 DTO
 */
@Data
public class ProfileRequest {
    /** 学校名称 */
    private String school;
    /** 专业 */
    private String major;
    /** 学历 */
    private String education;
    /** 年级 */
    private String grade;
    /** 技能标签列表（JSON字符串） */
    private String skillTags;
    /** 目标岗位列表（JSON字符串） */
    private String targetRoles;
    /** 期望城市 */
    private String expectedCity;
    /** 期望薪资 */
    private String expectedSalary;
    /** 求职状态 */
    private String jobStatus;
    /** 个人总结 */
    private String summary;
}
