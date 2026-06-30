package com.xuelian.career.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * DeepSeek API 配置属性 - 从 application.yml 读取 deepseek.* 配置
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "deepseek")
public class DeepSeekConfig {
    /** API 密钥 */
    private String apiKey;
    /** API 地址 */
    private String apiUrl = "https://api.deepseek.com/v1/chat/completions";
    /** 模型名称 */
    private String model = "deepseek-chat";
    /** 超时时间（秒） */
    private int timeoutSeconds = 60;
    /** 最大重试次数 */
    private int maxRetries = 1;
    /** 缓存 TTL（秒） */
    private long cacheTtl = 3600;

    @PostConstruct
    public void init() {
        if (apiKey != null && !apiKey.isBlank() && !apiKey.contains("xxx")) {
            log.info("DeepSeek API Key 已配置: {}...{} (长度={})",
                    apiKey.substring(0, 5), apiKey.substring(apiKey.length() - 4), apiKey.length());
        } else {
            log.warn("DeepSeek API Key 未正确配置！当前值: {}, 将使用本地兜底算法",
                    apiKey == null ? "null" : apiKey.substring(0, Math.min(6, apiKey.length())) + "...");
        }
    }
}
