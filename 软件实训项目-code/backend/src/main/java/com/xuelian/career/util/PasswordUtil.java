package com.xuelian.career.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 密码工具类 - 基于 BCrypt 实现密码加密与校验
 */
@Component
public class PasswordUtil {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 对原始密码进行 BCrypt 加密
     * @param rawPassword 明文密码
     * @return 加密后的哈希值
     */
    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * 校验原始密码与加密后的哈希值是否匹配
     * @param rawPassword     明文密码
     * @param encodedPassword 加密后的哈希值
     * @return 是否匹配
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
