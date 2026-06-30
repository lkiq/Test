package com.xuelian.career.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 企业推荐响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnterpriseRecommendResponse {
    private String projectSummary;
    private List<PositionSuggestion> positions;
    private List<CandidateItem> candidates;
    private String source;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PositionSuggestion {
        private String positionTitle;
        private List<SkillRequirement> skillRequirements;
        private Integer headcount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillRequirement {
        private String skillName;
        private String requiredLevel;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CandidateItem {
        private Long userId;
        private String username;
        private String avatar;
        private String education;
        private String school;
        private String major;
        private Double matchScore;
        private String matchLevel;
        private Double skillScore;
        private Double assessmentScore;
        private Double learningScore;
        private Double learningResultScore;
        private Double basicScore;
        private List<String> matchedSkills;
        private List<String> gapSkills;
        private String recommendReason;
    }
}
