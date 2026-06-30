package com.xuelian.career.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * AI 调用日志表 - 记录每次 DeepSeek API 调用的详细信息，用于监控与降级分析
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_call_log")
public class AiCallLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 调用场景：CAREER_EXPLORATION/RESUME_OPTIMIZE/MOCK_INTERVIEW/PROJECT_PARSE/CUSTOMER_SERVICE */
    private String scene;

    /** Prompt 摘要 */
    private String promptSummary;

    /** 请求哈希（用于缓存去重） */
    private String requestHash;

    /** 响应来源：AI/CACHE/FALLBACK/RULE */
    private String responseSource;

    /** 调用状态：SUCCESS/FAILED/FALLBACK */
    private String status;

    /** 调用耗时（毫秒） */
    private Long durationMs;

    /** 错误信息 */
    private String errorMessage;

    /** 降级原因 */
    private String fallbackReason;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
