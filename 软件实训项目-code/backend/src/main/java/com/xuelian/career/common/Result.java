package com.xuelian.career.common;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 统一响应结构 - 所有 API 返回结果使用此结构封装
 * @param <T> 响应数据类型
 */
@Data
@AllArgsConstructor
public class Result<T> {

    /** 状态码：200 成功 / 400 参数错误 / 401 未登录 / 403 无权限 / 404 不存在 / 500 服务端错误 */
    private int code;

    /** 提示信息 */
    private String message;

    /** 响应数据 */
    private T data;

    /**
     * 返回成功（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /**
     * 返回成功（无数据）
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    /**
     * 返回成功（自定义消息）
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    /**
     * 返回失败
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 返回参数错误
     */
    public static <T> Result<T> badRequest(String message) {
        return new Result<>(400, message, null);
    }

    /**
     * 返回未登录
     */
    public static <T> Result<T> unauthorized(String message) {
        return new Result<>(401, message, null);
    }

    /**
     * 返回无权限
     */
    public static <T> Result<T> forbidden(String message) {
        return new Result<>(403, message, null);
    }

    /**
     * 返回资源不存在
     */
    public static <T> Result<T> notFound(String message) {
        return new Result<>(404, message, null);
    }

    /**
     * 返回服务器错误
     */
    public static <T> Result<T> serverError(String message) {
        return new Result<>(500, message, null);
    }
}
