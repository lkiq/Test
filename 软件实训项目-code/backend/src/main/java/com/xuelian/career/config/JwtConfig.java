package com.xuelian.career.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT 配置属性 - 从 application.yml 读取 jwt.* 配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    /** JWT 签名密钥 */
    private String secret;
    /** Token 过期时间（秒），默认 604800（7天） */
    private long expiration = 604800L;
}
