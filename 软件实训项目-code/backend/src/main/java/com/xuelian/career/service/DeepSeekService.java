package com.xuelian.career.service;

import java.util.Map;

/**
 * DeepSeek AI 服务接口 - 封装 API 调用和缓存/兜底机制
 */
public interface DeepSeekService {

    /**
     * 调用 DeepSeek API
     * @param systemPrompt 系统提示词
     * @param userPrompt   用户提示词
     * @return API 返回的原始文本
     */
    String callAPI(String systemPrompt, String userPrompt);

    /**
     * 带缓存的 API 调用
     * @param cacheKey     缓存键
     * @param systemPrompt 系统提示词
     * @param userPrompt   用户提示词
     * @param ttlSeconds   缓存秒数（null 则使用默认值）
     * @return API 返回的原始文本
     */
    String callAPIWithCache(String cacheKey, String systemPrompt, String userPrompt, Long ttlSeconds);

    /**
     * 调用 DeepSeek API（带超时和 max_tokens 配置）
     * @param systemPrompt 系统提示词
     * @param userPrompt   用户提示词
     * @param timeoutMs    超时时间（毫秒）
     * @param maxTokens    最大生成 token 数
     * @return API 返回的原始文本
     */
    String callAPI(String systemPrompt, String userPrompt, long timeoutMs, int maxTokens);

    /**
     * 调用 DeepSeek API（带超时、max_tokens 和 temperature 配置）
     * 用于差异化生成策略：结构化 JSON 用 0.3，自由问答用 0.7
     * @param systemPrompt 系统提示词
     * @param userPrompt   用户提示词
     * @param timeoutMs    超时时间（毫秒）
     * @param maxTokens    最大生成 token 数
     * @param temperature  采样温度（0.0-2.0，结构化输出建议 0.3，自由问答建议 0.7）
     * @return API 返回的原始文本
     */
    String callAPI(String systemPrompt, String userPrompt, long timeoutMs, int maxTokens, double temperature);

    /**
     * 带缓存的 API 调用（带超时和 max_tokens 配置）
     * @param cacheKey     缓存键
     * @param systemPrompt 系统提示词
     * @param userPrompt   用户提示词
     * @param ttlSeconds   缓存秒数（兼作超时控制的 TTL）
     * @param maxTokens    最大生成 token 数
     * @return API 返回的原始文本
     */
    String callAPIWithCache(String cacheKey, String systemPrompt, String userPrompt, long ttlSeconds, int maxTokens);

    /**
     * 带缓存的 API 调用（完整参数：超时 + max_tokens）
     * @param cacheKey     缓存键
     * @param systemPrompt 系统提示词
     * @param userPrompt   用户提示词
     * @param ttlSeconds   缓存秒数
     * @param timeoutMs    业务超时时间（毫秒）
     * @param maxTokens    最大生成 token 数
     * @return API 返回的原始文本
     */
    String callAPIWithCache(String cacheKey, String systemPrompt, String userPrompt, long ttlSeconds, long timeoutMs, int maxTokens);

    /**
     * 解析 API 返回的 JSON 字符串为 Map
     * @param response API 原始响应文本
     * @return 解析后的 Map
     */
    Map<String, Object> parseJSONResponse(String response);

    /**
     * 检查 DeepSeek API 是否可用
     * @return true 可用 / false 不可用
     */
    boolean isAvailable();
}
