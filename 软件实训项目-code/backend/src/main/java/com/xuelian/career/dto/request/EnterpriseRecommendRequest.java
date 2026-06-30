package com.xuelian.career.dto.request;

import lombok.Data;

/**
 * 企业推荐请求
 */
@Data
public class EnterpriseRecommendRequest {
    /** 项目描述文本（≥20字） */
    private String projectDescription;
    /** 筛选条件 */
    private RecommendFilters filters;

    @Data
    public static class RecommendFilters {
        /** 学历要求 */
        private String education;
        /** 期望城市 */
        private String city;
        /** 偏好技能（逗号分隔） */
        private String skillPreference;
    }
}
