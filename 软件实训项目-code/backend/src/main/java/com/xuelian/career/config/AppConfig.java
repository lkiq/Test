package com.xuelian.career.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.Executors;

/**
 * 通用配置 - REST 客户端等
 */
@Configuration
public class AppConfig {

    /**
     * RestTemplate Bean - 使用 Java 17 原生 HttpClient，自带连接复用
     * 全局唯一实例，业务层通过 @Autowired 注入，禁止 new
     *
     * 超时策略（2026-06-28 调整）：
     * - connectTimeout = 6s：curl 实测 connect 仅 100ms，但 Java 首次 TLS 握手偶发 3-4s，6s 给足余量
     * - readTimeout = 12s：略大于最大业务超时 8s（职业探索），作为底层兜底
     * - 业务层超时通过 CompletableFuture.future.get(timeoutMs) 精确控制（5s/6s/8s 按场景）
     * - 底层异步线程池 10 线程，与 AI 调用线程池隔离
     */
    @Bean
    public RestTemplate restTemplate() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(6))      // 连接超时 6 秒（Java 首次 TLS 握手余量）
                .executor(Executors.newFixedThreadPool(10)) // 底层异步线程池
                .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(12));     // 读取超时 12 秒（底层兜底，业务层 future.get 先触发）

        return new RestTemplate(factory);
    }
}
