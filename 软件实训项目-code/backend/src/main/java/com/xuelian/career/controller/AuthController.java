package com.xuelian.career.controller;

import com.xuelian.career.common.Result;
import com.xuelian.career.dto.request.ForgotPasswordRequest;
import com.xuelian.career.dto.request.LoginByCodeRequest;
import com.xuelian.career.dto.request.LoginRequest;
import com.xuelian.career.dto.request.RegisterRequest;
import com.xuelian.career.dto.request.ResetPasswordRequest;
import com.xuelian.career.dto.request.SendLoginCodeRequest;
import com.xuelian.career.dto.response.LoginResponse;
import com.xuelian.career.service.AuthService;
import com.xuelian.career.service.impl.AuthServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器 - 处理用户注册、登录、密码找回请求
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthServiceImpl authServiceImpl;

    /**
     * 用户注册
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("收到注册请求: username={}, role={}", request.getUsername(), request.getRole());
        LoginResponse response = authService.register(request);
        return Result.success("注册成功", response);
    }

    /**
     * 用户登录
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("收到登录请求: username={}", request.getUsername());
        LoginResponse response = authService.login(request);
        return Result.success("登录成功", response);
    }

    /**
     * 忘记密码 - 发送验证码
     * POST /api/auth/forgot-password
     */
    @PostMapping("/forgot-password")
    public Result<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("收到密码重置请求: email={}", request.getEmail());
        authServiceImpl.sendResetCode(request.getEmail());
        return Result.success("验证码已发送至您的邮箱，5分钟内有效", null);
    }

    /**
     * 重置密码 - 验证验证码并更新密码（重置成功后返回登录页重新登录）
     * POST /api/auth/reset-password
     */
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("收到密码重置确认: email={}", request.getEmail());
        authServiceImpl.resetPassword(
                request.getEmail(), request.getCode(), request.getNewPassword());
        return Result.success("密码重置成功，请返回登录页使用新密码登录", null);
    }

    /**
     * 邮箱验证码登录 - 发送验证码
     * POST /api/auth/send-login-code
     */
    @PostMapping("/send-login-code")
    public Result<Void> sendLoginCode(@Valid @RequestBody SendLoginCodeRequest request) {
        log.info("收到邮箱登录验证码请求: email={}", request.getEmail());
        authServiceImpl.sendLoginCode(request.getEmail());
        return Result.success("验证码已发送至您的邮箱，5分钟内有效", null);
    }

    /**
     * 邮箱验证码登录
     * POST /api/auth/login-by-code
     */
    @PostMapping("/login-by-code")
    public Result<LoginResponse> loginByCode(@Valid @RequestBody LoginByCodeRequest request) {
        log.info("收到邮箱验证码登录请求: email={}", request.getEmail());
        LoginResponse response = authServiceImpl.loginByCode(request.getEmail(), request.getCode());
        return Result.success("登录成功", response);
    }
}
