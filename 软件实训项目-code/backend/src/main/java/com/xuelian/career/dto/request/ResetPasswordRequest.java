package com.xuelian.career.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 重置密码请求 DTO
 */
@Data
public class ResetPasswordRequest {

    /** 注册邮箱 */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /** 邮箱验证码 */
    @NotBlank(message = "验证码不能为空")
    @Size(min = 6, max = 6, message = "验证码为6位")
    private String code;

    /** 新密码（6-20位） */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度为6-20个字符")
    private String newPassword;
}
