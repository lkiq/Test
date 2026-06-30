package com.xuelian.career.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

/**
 * Resilience4j 熔断器状态监听配置
 * 在熔断器状态切换时输出 INFO 日志
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ResilienceConfig {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    /**
     * 注册熔断器事件监听器
     * 监听状态切换、调用被拒绝、调用失败事件并输出日志
     */
    @PostConstruct
    public void registerCircuitBreakerListener() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("ai-call");
        circuitBreaker.getEventPublisher()
            .onStateTransition(event -> log.info("熔断器状态切换: {} -> {}",
                event.getStateTransition().getFromState(),
                event.getStateTransition().getToState()))
            .onCallNotPermitted(event -> log.warn("熔断器 OPEN，AI 调用被拒绝（直接走兜底）"))
            .onError(event -> log.warn("熔断器记录失败: duration={}ms, exception={}",
                event.getElapsedDuration().toMillis(),
                event.getThrowable().getClass().getSimpleName()));
    }
}
