package com.xuelian.career.dto.request;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * AI职业方向探索请求
 */
@Data
public class CareerExplorationRequest {
    /** 用户偏好描述 */
    private String preferences;

    /** 对话历史（可选），每项包含 role 和 content */
    private List<Map<String, String>> history;

    /** 来源：ASSESSMENT（从测评页进入）、EXPLORE（单独探索） */
    private String source;

    /**
     * 用户在对话中已表达的兴趣方向（用于测评页入口保持兴趣一致性）
     * 当从测评页跳转时，前端将对话中最后一次用户表达的兴趣方向传入，
     * 避免测评入口的 preferences 文本无兴趣关键词导致兴趣被覆盖为画像默认值
     */
    private String expressedInterest;
}
