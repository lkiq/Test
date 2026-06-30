package com.xuelian.career.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 简历优化分析响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeOptimizeResponse {
    private Long id;
    private Double score;
    private Map<String, Double> dimensionScores;
    private List<IssueItem> issues;
    private List<String> optimizedSnippets;
    private String summary;
    /** AI 生成的完整优化简历全文 */
    private String optimizedResume;
    private String source;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IssueItem {
        private String severity;
        private String category;
        private String description;
        private String suggestion;
        private String exampleRewrite;
    }
}
