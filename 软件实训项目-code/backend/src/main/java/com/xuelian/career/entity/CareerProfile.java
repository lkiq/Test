package com.xuelian.career.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 求职画像表 - 存储学生的教育背景、技能标签、求职偏好等信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("career_profile")
public class CareerProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联用户ID（唯一） */
    private Long userId;

    /** 学校名称 */
    private String school;

    /** 专业 */
    private String major;

    /** 学历：专科/本科/硕士/博士 */
    private String education;

    /** 年级 */
    private String grade;

    /** 技能标签列表（JSON数组） */
    private String skillTags;

    /** 目标岗位列表（JSON数组） */
    private String targetRoles;

    /** 期望城市 */
    private String expectedCity;

    /** 期望薪资范围 */
    private String expectedSalary;

    /** 求职状态：在校/应届/已毕业 */
    private String jobStatus;

    /** 个人总结 */
    private String summary;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
