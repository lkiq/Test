package com.xuelian.career.service;

import com.xuelian.career.dto.response.EnterpriseRecommendResponse.*;

import java.util.List;

/**
 * 候选人评分引擎 - 四维度加权评分
 */
public interface CandidateScoreService {

    /**
     * 对单个学生计算综合匹配分
     * @param userId 学生用户ID
     * @param requiredSkills 岗位要求的技能列表（含名称和等级）
     * @param positionTitle 岗位名称（用于测评维度映射）
     * @param filters 学校/学历/城市筛选信息
     * @return 评分结果
     */
    CandidateScore compute(Long userId, List<SkillRequirement> requiredSkills,
                           String positionTitle, FilterInfo filters);

    /**
     * 评分结果
     */
    class CandidateScore {
        private Long userId;
        private String username;
        private String avatar;
        private String education;
        private String school;
        private String major;
        private double matchScore;
        private String matchLevel;
        private double skillScore;
        private double assessmentScore;
        private double learningScore;
        private double learningResultScore;
        private double basicScore;
        private List<String> matchedSkills;
        private List<String> gapSkills;
        private String recommendReason;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
        public String getEducation() { return education; }
        public void setEducation(String education) { this.education = education; }
        public String getSchool() { return school; }
        public void setSchool(String school) { this.school = school; }
        public String getMajor() { return major; }
        public void setMajor(String major) { this.major = major; }
        public double getMatchScore() { return matchScore; }
        public void setMatchScore(double matchScore) { this.matchScore = matchScore; }
        public String getMatchLevel() { return matchLevel; }
        public void setMatchLevel(String matchLevel) { this.matchLevel = matchLevel; }
        public double getSkillScore() { return skillScore; }
        public void setSkillScore(double skillScore) { this.skillScore = skillScore; }
        public double getAssessmentScore() { return assessmentScore; }
        public void setAssessmentScore(double assessmentScore) { this.assessmentScore = assessmentScore; }
        public double getLearningScore() { return learningScore; }
        public void setLearningScore(double learningScore) { this.learningScore = learningScore; }
        public double getLearningResultScore() { return learningResultScore; }
        public void setLearningResultScore(double learningResultScore) { this.learningResultScore = learningResultScore; }
        public double getBasicScore() { return basicScore; }
        public void setBasicScore(double basicScore) { this.basicScore = basicScore; }
        public List<String> getMatchedSkills() { return matchedSkills; }
        public void setMatchedSkills(List<String> matchedSkills) { this.matchedSkills = matchedSkills; }
        public List<String> getGapSkills() { return gapSkills; }
        public void setGapSkills(List<String> gapSkills) { this.gapSkills = gapSkills; }
        public String getRecommendReason() { return recommendReason; }
        public void setRecommendReason(String recommendReason) { this.recommendReason = recommendReason; }
    }

    /**
     * 筛选信息
     */
    class FilterInfo {
        private String expectedEducation;
        private String expectedCity;
        private String skillPreference;

        public FilterInfo() {}
        public FilterInfo(String expectedEducation, String expectedCity, String skillPreference) {
            this.expectedEducation = expectedEducation;
            this.expectedCity = expectedCity;
            this.skillPreference = skillPreference;
        }

        public String getExpectedEducation() { return expectedEducation; }
        public void setExpectedEducation(String expectedEducation) { this.expectedEducation = expectedEducation; }
        public String getExpectedCity() { return expectedCity; }
        public void setExpectedCity(String expectedCity) { this.expectedCity = expectedCity; }
        public String getSkillPreference() { return skillPreference; }
        public void setSkillPreference(String skillPreference) { this.skillPreference = skillPreference; }
    }
}
