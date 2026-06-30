package com.xuelian.career.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器 - 统一拦截各类异常，返回 Result 结构化响应
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常 - 始终返回 HTTP 200，业务状态码放在 JSON body 中
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常（@Valid）
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", message);
        return Result.badRequest(message);
    }

    /**
     * 处理参数校验异常（MethodArgumentNotValidException）
     */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValid(org.springframework.web.bind.MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", message);
        return Result.badRequest(message);
    }

    /**
     * 处理其他未捕获异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        StringBuilder sb = new StringBuilder();
        sb.append(e.getClass().getSimpleName()).append(" - ").append(e.getMessage());
        Throwable cause = e.getCause();
        while (cause != null) {
            sb.append(" | ").append(cause.getClass().getSimpleName()).append(" - ").append(cause.getMessage());
            cause = cause.getCause();
        }
        return Result.serverError("服务器内部错误: " + sb.toString());
    }
}
