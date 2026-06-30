package com.xuelian.career.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求 DTO
 */
@Data
public class RegisterRequest {

    /** 用户名（3-20位，字母数字下划线） */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度为3-20个字符")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    /** 真实姓名 */
    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过50个字符")
    private String realName;

    /** 密码（6-20位） */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度为6-20个字符")
    private String password;

    /** 手机号（必填） */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 邮箱（必填） */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /** 学历（选填）：BACHELOR/MASTER/PHD/JUNIOR */
    private String education;

    /** 毕业院校（选填） */
    private String school;

    /** 专业（选填） */
    private String major;

    /** 用户角色：STUDENT / HR */
    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "^(STUDENT|HR)$", message = "角色只能是 STUDENT 或 HR")
    private String role;

    // ===== HR 企业信息（role=HR 时必填） =====

    /** 公司名称 */
    private String companyName;

    /** 所属行业 */
    private String companyIndustry;

    /** 公司规模 */
    private String companySize;

    /** 公司地址 */
    private String companyAddress;

    /** 联系人职位 */
    private String contactPosition;
}
