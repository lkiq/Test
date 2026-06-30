package com.xuelian.career.service;

import com.xuelian.career.dto.request.LoginRequest;
import com.xuelian.career.dto.request.RegisterRequest;
import com.xuelian.career.dto.response.LoginResponse;

/**
 * 认证服务接口 - 处理用户注册与登录
 */
public interface AuthService {

    /**
     * 用户注册
     * @param request 注册请求
     * @return 登录响应（含 Token）
     */
    LoginResponse register(RegisterRequest request);

    /**
     * 用户登录
     * @param request 登录请求
     * @return 登录响应（含 Token）
     */
    LoginResponse login(LoginRequest request);
}
