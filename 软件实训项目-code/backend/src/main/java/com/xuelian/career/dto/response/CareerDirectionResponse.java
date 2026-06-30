package com.xuelian.career.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 职业方向探索响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CareerDirectionResponse {
    private Long recordId;
    /** 综合分析 */
    private String overallAnalysis;
    /** 推荐方向列表（兼容字段：= primaryDirections + fallbackDirections） */
    private List<DirectionItem> directions;
    /** 结果来源 */
    private String source;
    private LocalDateTime createdAt;
    /** 是否需要追问补充信息（信息不足时为 true，前端应将 overallAnalysis 作为追问消息渲染，不展示推荐卡片） */
    private Boolean needClarification = false;
    /** 本轮缺失的信息维度（如 interest/city），用于前端提示 */
    private List<String> missingDimensions;
    /** 用户意向发展方向（兴趣权重上浮后的推荐，尊重用户主观诉求） */
    private List<DirectionItem> primaryDirections;
    /** 系统判断的更稳妥备选方向（基于画像/测评的客观适配结论） */
    private List<DirectionItem> fallbackDirections;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DirectionItem {
        private String jobTitle;
        private String direction;
        private Integer matchScore;
        private String reason;
        private String learningPriority;
        private String growthPath;
    }
}
