package com.xuelian.career.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录/注册成功响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 用户角色 */
    private String role;

    /** JWT Token */
    private String token;

    /** Token 过期时间（Unix 时间戳，秒） */
    private Long expireTime;
}
