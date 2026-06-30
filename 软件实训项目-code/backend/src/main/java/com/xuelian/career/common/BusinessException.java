package com.xuelian.career.common;

import lombok.Getter;

/**
 * 业务异常 - 用于在 Service 层抛出自定义业务异常，
 * 由 GlobalExceptionHandler 统一捕获并返回友好提示
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 业务错误码 */
    private final int code;

    /**
     * @param code    错误码
     * @param message 错误信息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * @param message 错误信息（默认 400）
     */
    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }
}
