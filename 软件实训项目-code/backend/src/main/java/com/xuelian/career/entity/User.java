package com.xuelian.career.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 用户账号表 - 存储学生、HR、管理员三种角色的用户信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名（唯一） */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** BCrypt 加密密码哈希 */
    private String passwordHash;

    /** 用户角色：STUDENT / HR / ADMIN */
    private String role;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 学历：BACHELOR/MASTER/PHD/JUNIOR */
    private String education;

    /** 毕业院校 */
    private String school;

    /** 专业 */
    private String major;

    /** 账号状态：ACTIVE / DISABLED */
    private String status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除（0正常/1删除） */
    @TableLogic
    private Integer isDeleted;
}
