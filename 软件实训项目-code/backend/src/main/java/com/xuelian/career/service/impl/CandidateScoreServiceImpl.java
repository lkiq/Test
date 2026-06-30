package com.xuelian.career.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuelian.career.config.RecommendConfig;
import com.xuelian.career.dto.response.EnterpriseRecommendResponse.SkillRequirement;
import com.xuelian.career.entity.*;
import com.xuelian.career.mapper.*;
import com.xuelian.career.service.CandidateScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 候选人评分引擎实现 - 五维度加权评分
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CandidateScoreServiceImpl implements CandidateScoreService {

    private final CareerProfileMapper profileMapper;
    private final AssessmentResultMapper assessmentResultMapper;
    private final LearningTaskMapper learningTaskMapper;
    private final LearningResultMapper learningResultMapper;
    private final UserMapper userMapper;
    private final SkillMapper skillMapper;
    private final RecommendConfig config;
    private final ObjectMapper objectMapper;

    /** 技能等级 → 数值映射 */
    static final Map<String, Integer> LEVEL_MAP = Map.of(
        "了解", 1, "掌握", 2, "熟练", 3, "精通", 4
    );

    @Override
    public CandidateScore compute(Long userId, List<SkillRequirement> requiredSkills,
                                   String positionTitle, FilterInfo filters) {
        User user = userMapper.selectById(userId);
        CareerProfile profile = profileMapper.selectOne(
            new LambdaQueryWrapper<CareerProfile>().eq(CareerProfile::getUserId, userId));
        // 捞最近5次测评用于趋势分析
        List<AssessmentResult> assessmentHistory = assessmentResultMapper.selectList(
            new LambdaQueryWrapper<AssessmentResult>().eq(AssessmentResult::getUserId, userId)
                .orderByDesc(AssessmentResult::getCreatedAt).last("LIMIT 5"));
        AssessmentResult assessment = assessmentHistory.isEmpty() ? null : assessmentHistory.get(0);
        List<LearningTask> tasks = learningTaskMapper.selectList(
            new LambdaQueryWrapper<LearningTask>().eq(LearningTask::getUserId, userId));

        // 五个维度评分
        SkillMatchResult skillResult = calcSkillMatch(profile, requiredSkills);
        double assessmentScore = calcAssessmentMatch(assessment, assessmentHistory, positionTitle);
        double learningScore = calcLearningProgress(tasks);
        double learningResultScore = calcLearningResultScore(userId);
        double basicScore = calcBasicMatch(profile, user, filters);

        // 综合加权（五维度）
        double total = config.getScoring().getWeightSkill() * skillResult.score
                     + config.getScoring().getWeightAssessment() * assessmentScore
                     + config.getScoring().getWeightLearning() * learningScore
                     + config.getScoring().getWeightLearningResult() * learningResultScore
                     + config.getScoring().getWeightBasic() * basicScore;

        CandidateScore result = new CandidateScore();
        result.setUserId(userId);
        result.setUsername(user.getUsername() != null ? user.getUsername() : user.getRealName());
        result.setEducation(profile != null ? profile.getEducation() : (user.getEducation() != null ? user.getEducation() : "未知"));
        result.setSchool(profile != null ? profile.getSchool() : (user.getSchool() != null ? user.getSchool() : ""));
        result.setMajor(profile != null ? profile.getMajor() : (user.getMajor() != null ? user.getMajor() : ""));
        result.setMatchScore(round(total));
        result.setMatchLevel(getLevel(total));
        result.setSkillScore(round(skillResult.score));
        result.setAssessmentScore(round(assessmentScore));
        result.setLearningScore(round(learningScore));
        result.setLearningResultScore(round(learningResultScore));
        result.setBasicScore(round(basicScore));
        result.setMatchedSkills(skillResult.matched);
        result.setGapSkills(skillResult.gaps);
        result.setRecommendReason(buildFallbackReason(skillResult, assessment, learningScore));

        return result;
    }

    // ==================== 技能匹配（50%） ====================

    private SkillMatchResult calcSkillMatch(CareerProfile profile, List<SkillRequirement> requiredSkills) {
        if (requiredSkills == null || requiredSkills.isEmpty()) {
            return new SkillMatchResult(50.0, List.of(), requiredSkills.stream()
                .map(SkillRequirement::getSkillName).collect(Collectors.toList()));
        }

        // 解析学生技能标签
        Map<String, Integer> studentSkills = parseSkillTags(profile);
        if (studentSkills.isEmpty()) {
            return new SkillMatchResult(30.0, List.of(), requiredSkills.stream()
                .map(SkillRequirement::getSkillName).collect(Collectors.toList()));
        }

        double totalWeight = requiredSkills.size(); // 每个技能默认等权
        double earned = 0;
        List<String> matched = new ArrayList<>();
        List<String> gaps = new ArrayList<>();

        for (SkillRequirement req : requiredSkills) {
            String name = req.getSkillName();
            int reqLevel = levelValue(req.getRequiredLevel());

            if (studentSkills.containsKey(name)) {
                int stuLevel = studentSkills.get(name);
                double contribution;
                if (stuLevel >= reqLevel) {
                    contribution = 1.0;       // 完全满足
                    matched.add(name);
                } else if (stuLevel == reqLevel - 1) {
                    contribution = 0.6;       // 基本满足（差1级）
                    matched.add(name);
                } else {
                    contribution = 0.3;       // 有差距
                    gaps.add(name);
                }
                earned += contribution;
            } else {
                // 模糊匹配（技能名包含关系）
                String fuzzy = fuzzyMatch(name, studentSkills.keySet());
                if (fuzzy != null) {
                    int stuLevel = studentSkills.get(fuzzy);
                    int levelGap = reqLevel - stuLevel;
                    if (levelGap <= 0) {
                        earned += 1.0;
                        matched.add(name);
                    } else if (levelGap == 1) {
                        earned += 0.6;
                        matched.add(name);
                    } else {
                        earned += 0.3;
                        gaps.add(name);
                    }
                } else {
                    gaps.add(name);
                }
            }
        }

        double score = totalWeight > 0 ? (earned / totalWeight) * 100 : 50;
        return new SkillMatchResult(score, matched, gaps);
    }

    /** 解析 skill_tags JSON → 技能名→等级映射 */
    private Map<String, Integer> parseSkillTags(CareerProfile profile) {
        if (profile == null || profile.getSkillTags() == null || profile.getSkillTags().isBlank()) {
            return Map.of();
        }
        try {
            List<Map<String, Object>> tags = objectMapper.readValue(profile.getSkillTags(),
                new TypeReference<List<Map<String, Object>>>() {});
            Map<String, Integer> result = new LinkedHashMap<>();
            for (Map<String, Object> tag : tags) {
                String name = (String) tag.get("name");
                if (name == null) name = (String) tag.get("skillName");
                String level = (String) tag.getOrDefault("level", "掌握");
                if (name != null) {
                    result.put(name, levelValue(level));
                }
            }
            return result;
        } catch (Exception e) {
            log.debug("解析 skill_tags 失败: profileId={}, error={}", profile.getId(), e.getMessage());
            return Map.of();
        }
    }

    private int levelValue(String level) {
        return LEVEL_MAP.getOrDefault(level, 2);
    }

    private String fuzzyMatch(String target, Set<String> studentSkills) {
        String lower = target.toLowerCase();
        for (String s : studentSkills) {
            if (s.toLowerCase().contains(lower) || lower.contains(s.toLowerCase())) {
                return s;
            }
        }
        return null;
    }

    // ==================== 测评适配（25%） ====================

    /** 计算测评适配得分（含趋势分析） */
    private double calcAssessmentMatch(AssessmentResult latest, List<AssessmentResult> history, String positionTitle) {
        if (latest == null) return 40.0; // 无测评数据给低分

        Map<String, Double> weights = config.getDimensionWeights(positionTitle);
        if (weights == null || weights.isEmpty()) return 50;

        // 最新成绩（60%）
        double latestScore = nvl(latest.getProgrammingScore()) * weights.getOrDefault("programming", 0.2)
                           + nvl(latest.getLogicScore()) * weights.getOrDefault("logic", 0.2)
                           + nvl(latest.getProductScore()) * weights.getOrDefault("product", 0.2)
                           + nvl(latest.getTechScore()) * weights.getOrDefault("tech", 0.2)
                           + nvl(latest.getCommunicationScore()) * weights.getOrDefault("communication", 0.2);

        // 趋势分（20%）：多次测评总分趋势
        double trendScore = calcTrendScore(history);

        // 等级分（20%）：根据最新总分转等级分
        double levelScore = 50;
        if (latest.getTotalScore() != null) {
            if (latest.getTotalScore() >= 90) levelScore = 95;
            else if (latest.getTotalScore() >= 80) levelScore = 80;
            else if (latest.getTotalScore() >= 70) levelScore = 65;
            else if (latest.getTotalScore() >= 60) levelScore = 50;
            else levelScore = 35;
        }

        return Math.min(100, latestScore * 0.60 + trendScore * 0.20 + levelScore * 0.20);
    }

    /** 计算测评趋势分 */
    private double calcTrendScore(List<AssessmentResult> history) {
        if (history == null || history.size() < 2) return 50; // 不够2次无法算趋势
        // 按时间升序排列，取最近5次
        List<AssessmentResult> sorted = history.stream()
            .sorted(Comparator.comparing(AssessmentResult::getCreatedAt))
            .collect(Collectors.toList());
        if (sorted.size() < 2) return 50;

        double trend = 0;
        int pairs = 0;
        for (int i = 1; i < sorted.size(); i++) {
            Double prev = sorted.get(i - 1).getTotalScore();
            Double curr = sorted.get(i).getTotalScore();
            if (prev != null && curr != null) {
                trend += (curr - prev);
                pairs++;
            }
        }
        if (pairs == 0) return 50;
        double avgChange = trend / pairs;
        // 上升趋势加分，下降扣分
        return Math.min(100, Math.max(20, 50 + avgChange * 2));
    }

    private double nvl(Double v) { return v != null ? v : 50; }

    // ==================== 学习完成（15%） ====================

    /** 计算学习进度（阶段加权+时效衰减） */
    private double calcLearningProgress(List<LearningTask> tasks) {
        if (tasks == null || tasks.isEmpty()) return 30.0;

        Map<String, Double> stageWeights = Map.of(
            "INTERVIEW", 1.5, "PROJECT", 1.3, "FRAMEWORK", 1.0, "BASIC", 0.7
        );
        java.time.LocalDate today = java.time.LocalDate.now();
        double weightedSum = 0, totalWeight = 0;

        for (LearningTask t : tasks) {
            double stageW = stageWeights.getOrDefault(t.getStage(), 1.0);
            totalWeight += stageW;
            if ("COMPLETED".equals(t.getStatus())) {
                weightedSum += stageW;
            } else if ("IN_PROGRESS".equals(t.getStatus())) {
                boolean overdue = t.getDueDate() != null && t.getDueDate().isBefore(today);
                weightedSum += stageW * (overdue ? 0.25 : 0.50);
            }
            // 未开始的任务 weight 0
        }

        double completionRatio = totalWeight > 0 ? (weightedSum / totalWeight) : 0;
        return Math.min(100, Math.max(10, completionRatio * 100));
    }

    // ==================== 学习成果测评（20%） ====================

    /** 计算学习成果测评得分 */
    private double calcLearningResultScore(Long userId) {
        List<LearningResult> results = learningResultMapper.selectList(
            new LambdaQueryWrapper<LearningResult>()
                .eq(LearningResult::getUserId, userId)
                .orderByDesc(LearningResult::getCreatedAt));

        if (results == null || results.isEmpty()) return 25.0; // 未测评给低分

        // 各阶段权重
        Map<String, Double> stageWeights = Map.of(
            "FINAL", 1.4, "PROJECT", 1.3, "INTERVIEW", 1.2, "FRAMEWORK", 1.0, "BASIC", 0.8
        );
        double totalWeight = 0, weightedSum = 0;
        for (LearningResult r : results) {
            double w = stageWeights.getOrDefault(r.getStage(), 1.0);
            totalWeight += w;
            weightedSum += w * nvl(r.getTotalScore());
        }
        double stageWeightedScore = totalWeight > 0 ? weightedSum / totalWeight : 50;

        // 最新一次成绩（30%）
        LearningResult latest = results.get(0);
        double latestScore = nvl(latest.getTotalScore());

        // 通过率（20%）
        long passed = results.stream().filter(r -> r.getPassed() != null && r.getPassed() == 1).count();
        double passRate = results.size() > 0 ? (double) passed / results.size() * 100 : 0;

        return Math.min(100, stageWeightedScore * 0.50 + latestScore * 0.30 + passRate * 0.20);
    }

    // ==================== 基础匹配（10%） ====================

    private double calcBasicMatch(CareerProfile profile, User user, FilterInfo filters) {
        double eduScore = calcEducationMatch(profile, user, filters);
        double cityScore = calcCityMatch(profile, filters);
        double majorScore = calcMajorMatch(profile, user);

        return eduScore * 0.3 + cityScore * 0.3 + 100 * 0.2 + majorScore * 0.2;
    }

    private double calcEducationMatch(CareerProfile profile, User user, FilterInfo filters) {
        String reqEdu = filters != null ? filters.getExpectedEducation() : null;
        String stuEdu = (profile != null && profile.getEducation() != null)
            ? profile.getEducation() : (user.getEducation() != null ? user.getEducation() : "");
        if (reqEdu == null || reqEdu.isBlank()) return 100;
        if (stuEdu.isBlank()) return 70;

        int reqLvl = eduLevel(reqEdu);
        int stuLvl = eduLevel(stuEdu);
        int gap = Math.abs(reqLvl - stuLvl);

        if (gap == 0) return 100;
        if (gap == 1) return 70;
        return 30;
    }

    private int eduLevel(String edu) {
        String e = edu.toLowerCase();
        if (e.contains("博士")) return 5;
        if (e.contains("硕士")) return 4;
        if (e.contains("本科") || e.contains("bachelor")) return 3;
        if (e.contains("大专") || e.contains("专科")) return 2;
        return 1;
    }

    private double calcCityMatch(CareerProfile profile, FilterInfo filters) {
        String reqCity = filters != null ? filters.getExpectedCity() : null;
        if (reqCity == null || reqCity.isBlank()) return 100;
        String stuCity = profile != null ? profile.getExpectedCity() : "";
        if (stuCity == null || stuCity.isBlank()) return 70;

        if (stuCity.contains(reqCity) || reqCity.contains(stuCity)) return 100;
        return 30;
    }

    private double calcMajorMatch(CareerProfile profile, User user) {
        String major = (profile != null && profile.getMajor() != null)
            ? profile.getMajor() : (user.getMajor() != null ? user.getMajor() : "");
        if (major.isBlank()) return 60;

        String lower = major.toLowerCase();
        if (containsAny(lower, "计算机", "软件工程", "人工智能", "computer", "software")) return 100;
        if (containsAny(lower, "电子", "通信", "信息", "electronic")) return 80;
        if (containsAny(lower, "数学", "统计", "math")) return 75;
        if (containsAny(lower, "产品", "设计", "design", "交互")) return 70;
        return 40;
    }

    // ==================== 辅助方法 ====================

    /** 兜底推荐理由生成 */
    private String buildFallbackReason(SkillMatchResult skillResult, AssessmentResult assessment, double learningScore) {
        StringBuilder sb = new StringBuilder();
        if (!skillResult.matched.isEmpty()) {
            sb.append("该候选人在").append(String.join("、", skillResult.matched.stream().limit(3).collect(Collectors.toList())));
            sb.append("方面与岗位要求较匹配");
        }
        if (!skillResult.gaps.isEmpty()) {
            if (sb.length() > 0) sb.append("，");
            sb.append("建议关注").append(String.join("、", skillResult.gaps.stream().limit(2).collect(Collectors.toList())));
            sb.append("方面的培养");
        }
        if (assessment != null && assessment.getTotalScore() != null && assessment.getTotalScore() >= 80) {
            sb.append("，综合测评表现优秀");
        }
        if (sb.length() == 0) sb.append("综合技能匹配度较高");
        return sb.toString();
    }

    /** 根据分数返回等级 */
    private String getLevel(double score) {
        if (score >= 85) return "非常匹配";
        if (score >= 70) return "比较匹配";
        if (score >= 60) return "一般匹配";
        if (score >= 45) return "部分匹配";
        return "不太匹配";
    }

    private double round(double v) { return Math.round(v * 10.0) / 10.0; }

    private boolean containsAny(String s, String... keywords) {
        for (String kw : keywords) {
            if (s.contains(kw)) return true;
        }
        return false;
    }

    /** 技能匹配结果 */
    private static class SkillMatchResult {
        final double score;
        final List<String> matched;
        final List<String> gaps;
        SkillMatchResult(double score, List<String> matched, List<String> gaps) {
            this.score = score;
            this.matched = matched;
            this.gaps = gaps;
        }
    }
}
