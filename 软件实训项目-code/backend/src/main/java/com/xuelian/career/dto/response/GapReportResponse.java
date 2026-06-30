package com.xuelian.career.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

/**
 * 能力差距分析报告响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GapReportResponse {
    private Long recordId;
    private Long jobId;
    private String jobTitle;
    /** 综合匹配度 */
    private Double overallMatch;
    /** 用户技能→等级 */
    private Map<String, String> userSkills;
    /** 岗位要求技能→等级 */
    private Map<String, String> requiredSkills;
    /** 差距分析列表 */
    private List<GapItem> gaps;
    /** 雷达图数据（用户、岗位两条线） */
    private RadarChartData radarChart;
    /** 行动建议 */
    private String suggestions;
    /** 分析模式：single / multi */
    private String mode;
    /** 多岗位模式下，技能→来源岗位列表 */
    private Map<String, List<String>> sourceJobs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GapItem {
        private String skillName;
        private String userLevel;
        private String requiredLevel;
        private String gapDegree; // 严重不足/需要提升/基本达标/完全达标
        private Integer priority;
        /** 该技能要求的来源岗位 */
        private List<String> sourceJobs;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RadarChartData {
        private List<String> dimensions;
        private List<Double> userValues;
        private List<Double> requiredValues;
    }
}
