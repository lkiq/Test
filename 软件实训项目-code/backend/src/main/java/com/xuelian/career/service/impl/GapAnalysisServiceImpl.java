package com.xuelian.career.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuelian.career.dto.response.GapReportResponse;
import com.xuelian.career.entity.*;
import com.xuelian.career.mapper.*;
import com.xuelian.career.service.GapAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 能力差距分析服务实现 - 对比用户技能水平与岗位要求，计算差距
 * V6.1: 基于学习任务状态(P0单数据源)计算技能等级 + 加权匹配度 + 双模式(单/多岗位)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GapAnalysisServiceImpl implements GapAnalysisService {

    private final JobPositionMapper jobPositionMapper;
    private final JobSkillRequirementMapper requirementMapper;
    private final SkillMapper skillMapper;
    private final CareerProfileMapper profileMapper;
    private final AssessmentResultMapper assessmentResultMapper;
    private final LearningTaskMapper learningTaskMapper;
    private final RecommendationRecordMapper recordMapper;
    private final ObjectMapper objectMapper;

    /** 等级名称→数值 */
    private static final Map<String, Integer> LEVEL_TO_NUM = Map.of(
            "未掌握", 0, "了解", 1, "掌握", 2, "熟练", 3, "精通", 4
    );
    /** 数值→等级名称 */
    private static final String[] NUM_TO_LEVEL = {"未掌握", "了解", "掌握", "熟练", "精通"};

    // ======================== 公共方法 ========================

    @Override
    public Integer calculateUserSkillLevel(Long userId, Long skillId) {
        // P0: 仅查学习任务状态（V6.1单数据源）
        LearningTask task = learningTaskMapper.findLatestByUserAndSkill(userId, skillId);
        if (task != null) {
            String status = task.getStatus();
            if ("LEARNING_COMPLETED".equals(status)) return 2; // 掌握
            if ("IN_PROGRESS".equals(status)) return 1;          // 了解
        }
        // 兜底：未掌握
        return 0;
    }

    @Override
    public GapReportResponse analyze(Long userId, Long jobId) {
        JobPosition job = jobPositionMapper.selectById(jobId);
        if (job == null) return null;

        List<JobSkillRequirement> requirements = getRequirements(jobId);
        if (requirements.isEmpty()) {
            return buildEmptyReport(jobId, job.getTitle(), "single");
        }

        Map<Long, Skill> skillMap = buildSkillMap(requirements);

        Map<Long, List<String>> skillSources = new LinkedHashMap<>();
        for (JobSkillRequirement req : requirements) {
            skillSources.computeIfAbsent(req.getSkillId(), k -> new ArrayList<>()).add(job.getTitle());
        }

        GapReportResponse report = buildReport(userId, job.getTitle(), requirements, skillMap,
                "single", skillSources);
        report.setJobId(jobId);
        report.setJobTitle(job.getTitle());
        saveRecord(userId, jobId, report.getOverallMatch(), report.getGaps());
        return report;
    }

    @Override
    public GapReportResponse analyzeMultiple(Long userId, List<Long> jobIds) {
        if (jobIds == null || jobIds.isEmpty()) return null;

        // 获取所有岗位信息
        List<JobPosition> jobs = jobPositionMapper.selectBatchIds(jobIds);
        if (jobs.isEmpty()) return null;

        Map<Long, String> jobTitleMap = jobs.stream()
                .collect(Collectors.toMap(JobPosition::getId, JobPosition::getTitle));

        // 收集所有岗位的技能要求，合并去重
        Map<Long, MergedSkillReq> merged = new LinkedHashMap<>();
        for (Long jobId : jobIds) {
            List<JobSkillRequirement> reqs = getRequirements(jobId);
            String jobTitle = jobTitleMap.get(jobId);
            if (jobTitle == null) continue;

            for (JobSkillRequirement req : reqs) {
                Long skillId = req.getSkillId();
                int level = LEVEL_TO_NUM.getOrDefault(req.getRequiredLevel(), 0);
                double weight = req.getWeight() != null ? req.getWeight() : 0.1;

                MergedSkillReq existing = merged.get(skillId);
                if (existing == null) {
                    existing = new MergedSkillReq(skillId, level, req.getRequiredLevel(), weight);
                    existing.sourceJobs.add(jobTitle);
                    merged.put(skillId, existing);
                } else {
                    // 取最高要求等级
                    if (level > existing.maxLevel) {
                        existing.maxLevel = level;
                        existing.maxLevelName = req.getRequiredLevel();
                        existing.weight = weight; // 使用最高等级对应岗位的权重
                        existing.sourceJobs.clear();
                        existing.sourceJobs.add(jobTitle);
                    } else if (level == existing.maxLevel) {
                        // 同等级：累加来源
                        if (!existing.sourceJobs.contains(jobTitle)) {
                            existing.sourceJobs.add(jobTitle);
                        }
                    }
                }
            }
        }

        if (merged.isEmpty()) {
            String title = jobs.stream().map(JobPosition::getTitle).collect(Collectors.joining(" + "));
            return buildEmptyReport(null, title, "multi");
        }

        // 构建合并后的虚拟 requirements 列表
        List<JobSkillRequirement> mergedReqs = new ArrayList<>();
        Map<Long, List<String>> skillSources = new LinkedHashMap<>();
        for (Map.Entry<Long, MergedSkillReq> entry : merged.entrySet()) {
            Long skillId = entry.getKey();
            MergedSkillReq mr = entry.getValue();
            JobSkillRequirement req = new JobSkillRequirement();
            req.setSkillId(skillId);
            req.setRequiredLevel(mr.maxLevelName);
            req.setWeight(mr.weight);
            mergedReqs.add(req);
            skillSources.put(skillId, mr.sourceJobs);
        }

        Map<Long, Skill> skillMap = buildSkillMap(mergedReqs);
        String combinedTitle = jobs.stream().map(JobPosition::getTitle)
                .collect(Collectors.joining(" + "));

        GapReportResponse report = buildReport(userId, combinedTitle, mergedReqs, skillMap,
                "multi", skillSources);
        return report;
    }

    @Override
    public GapReportResponse getReport(Long recordId) {
        RecommendationRecord record = recordMapper.selectById(recordId);
        if (record == null) return null;
        try {
            return objectMapper.readValue(record.getResultJson(), GapReportResponse.class);
        } catch (Exception e) {
            log.warn("解析差距分析报告失败", e);
            return null;
        }
    }

    @Override
    public List<Map<String, Object>> getRecommendedJobs(Long userId) {
        // 获取所有发布中的岗位
        List<JobPosition> jobs = jobPositionMapper.selectList(
                new LambdaQueryWrapper<JobPosition>()
                        .eq(JobPosition::getIsDeleted, 0)
                        .last("LIMIT 20"));

        // 批量加载所有技能（避免N+1）
        Set<Long> allSkillIds = new HashSet<>();
        Map<Long, List<JobSkillRequirement>> jobReqsMap = new LinkedHashMap<>();
        for (JobPosition job : jobs) {
            List<JobSkillRequirement> reqs = getRequirements(job.getId());
            jobReqsMap.put(job.getId(), reqs);
            reqs.forEach(r -> allSkillIds.add(r.getSkillId()));
        }
        Map<Long, Skill> allSkills = allSkillIds.isEmpty() ? Map.of() :
                skillMapper.selectBatchIds(allSkillIds).stream()
                        .collect(Collectors.toMap(Skill::getId, s -> s));

        List<Map<String, Object>> result = new ArrayList<>();
        for (JobPosition job : jobs) {
            List<JobSkillRequirement> reqs = jobReqsMap.get(job.getId());
            if (reqs.isEmpty()) continue;

            double totalWeight = 0;
            double weightedMatch = 0;
            for (JobSkillRequirement req : reqs) {
                double weight = req.getWeight() != null ? req.getWeight() : 0.1;
                totalWeight += weight;

                int userLevel = calculateUserSkillLevel(userId, req.getSkillId());
                int reqLevel = LEVEL_TO_NUM.getOrDefault(req.getRequiredLevel(), 2);
                if (reqLevel <= 0) {
                    weightedMatch += weight;
                } else {
                    double ratio = Math.min(1.0, (double) userLevel / reqLevel);
                    weightedMatch += weight * ratio;
                }
            }

            double matchScore = totalWeight > 0
                    ? Math.round(weightedMatch / totalWeight * 1000.0) / 10.0
                    : 0;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("jobId", job.getId());
            item.put("jobTitle", job.getTitle());
            item.put("companyName", job.getCompanyName());
            item.put("city", job.getCity());
            item.put("matchScore", matchScore);
            result.add(item);
        }

        result.sort((a, b) -> Double.compare((Double) b.get("matchScore"), (Double) a.get("matchScore")));
        return result.size() > 3 ? result.subList(0, 3) : result;
    }

    // ======================== 内部工具方法 ========================

    /**
     * 核心报告构建方法（单/多岗位共用）
     */
    private GapReportResponse buildReport(Long userId, String jobTitles,
                                           List<JobSkillRequirement> requirements,
                                           Map<Long, Skill> skillMap,
                                           String mode, Map<Long, List<String>> skillSources) {
        Map<String, String> userSkills = new LinkedHashMap<>();
        Map<String, String> requiredSkills = new LinkedHashMap<>();
        List<GapReportResponse.GapItem> gaps = new ArrayList<>();
        Map<String, List<String>> sourceJobsMap = new LinkedHashMap<>();

        double totalWeight = 0;
        double weightedMatchSum = 0;

        for (JobSkillRequirement req : requirements) {
            Skill skill = skillMap.get(req.getSkillId());
            if (skill == null) continue;

            String skillName = skill.getName();
            int userLevel = calculateUserSkillLevel(userId, req.getSkillId());
            int requiredLevel = LEVEL_TO_NUM.getOrDefault(req.getRequiredLevel(), 2);
            double weight = req.getWeight() != null ? req.getWeight() : 0.1;
            totalWeight += weight;

            // 匹配度贡献
            if (requiredLevel <= 0) {
                weightedMatchSum += weight;
            } else {
                weightedMatchSum += weight * Math.min(1.0, (double) userLevel / requiredLevel);
            }

            int gap = requiredLevel - userLevel;
            String gapDegree;
            int priority;
            if (gap <= 0) { gapDegree = "完全达标"; priority = 0; }
            else if (gap == 1) { gapDegree = "基本达标"; priority = 1; }
            else if (gap <= 2) { gapDegree = "需要提升"; priority = 2; }
            else { gapDegree = "严重不足"; priority = 3; }

            String userLevelName = NUM_TO_LEVEL[Math.min(userLevel, 4)];
            userSkills.put(skillName, userLevelName);
            requiredSkills.put(skillName, req.getRequiredLevel());

            List<String> sources = skillSources != null ? skillSources.get(req.getSkillId()) : null;
            sourceJobsMap.put(skillName, sources != null ? sources : List.of(jobTitles));

            gaps.add(GapReportResponse.GapItem.builder()
                    .skillName(skillName)
                    .userLevel(userLevelName)
                    .requiredLevel(req.getRequiredLevel())
                    .gapDegree(gapDegree)
                    .priority(priority)
                    .sourceJobs(sources != null ? sources : new ArrayList<>())
                    .build());
        }

        // 按优先级排序（差距大的排前面）
        gaps.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));

        // 加权匹配度
        double overallMatch = totalWeight > 0
                ? Math.round(weightedMatchSum / totalWeight * 1000.0) / 10.0
                : 0;

        // 雷达图数据
        List<String> radarDims = gaps.stream().map(GapReportResponse.GapItem::getSkillName).collect(Collectors.toList());
        List<Double> userValues = gaps.stream()
                .map(g -> (double) LEVEL_TO_NUM.getOrDefault(g.getUserLevel(), 0) * 25.0)
                .collect(Collectors.toList());
        List<Double> requiredValues = gaps.stream()
                .map(g -> (double) LEVEL_TO_NUM.getOrDefault(g.getRequiredLevel(), 0) * 25.0)
                .collect(Collectors.toList());

        // 建议文案
        long severeCount = gaps.stream().filter(g -> g.getPriority() == 3).count();
        long improveCount = gaps.stream().filter(g -> g.getPriority() >= 2).count();
        String suggestions;
        if (severeCount > 0) {
            suggestions = String.format("检测到%d项严重不足，%d项需提升，建议优先从差距最大的技能开始学习", severeCount, improveCount);
        } else if (improveCount > 0) {
            suggestions = String.format("整体能力接近岗位要求，%d项技能有待提升，系统学习后可冲刺目标岗位", improveCount);
        } else {
            suggestions = "能力已基本达标，可以通过项目实战进一步巩固，建议尝试更高阶岗位";
        }

        return GapReportResponse.builder()
                .jobTitle(jobTitles)
                .overallMatch(overallMatch)
                .userSkills(userSkills)
                .requiredSkills(requiredSkills)
                .gaps(gaps)
                .radarChart(GapReportResponse.RadarChartData.builder()
                        .dimensions(radarDims)
                        .userValues(userValues)
                        .requiredValues(requiredValues)
                        .build())
                .suggestions(suggestions)
                .mode(mode)
                .sourceJobs(sourceJobsMap)
                .build();
    }

    /** 空报告（岗位无技能要求时） */
    private GapReportResponse buildEmptyReport(Long jobId, String jobTitle, String mode) {
        return GapReportResponse.builder()
                .jobId(jobId).jobTitle(jobTitle)
                .overallMatch(0.0).mode(mode)
                .userSkills(Map.of()).requiredSkills(Map.of()).gaps(List.of())
                .suggestions("该岗位暂无技能要求配置")
                .build();
    }

    /** 获取岗位技能要求 */
    private List<JobSkillRequirement> getRequirements(Long jobId) {
        return requirementMapper.selectList(
                new LambdaQueryWrapper<JobSkillRequirement>()
                        .eq(JobSkillRequirement::getJobId, jobId));
    }

    /** 根据 requirements 批量构建 skillMap */
    private Map<Long, Skill> buildSkillMap(List<JobSkillRequirement> requirements) {
        List<Long> skillIds = requirements.stream()
                .map(JobSkillRequirement::getSkillId).distinct().collect(Collectors.toList());
        if (skillIds.isEmpty()) return Map.of();
        return skillMapper.selectBatchIds(skillIds).stream()
                .collect(Collectors.toMap(Skill::getId, s -> s));
    }

    /** 保存分析记录 */
    @SuppressWarnings("unchecked")
    private void saveRecord(Long userId, Long jobId, double score, List<GapReportResponse.GapItem> gaps) {
        try {
            RecommendationRecord record = new RecommendationRecord();
            record.setUserId(userId);
            record.setType("GAP_ANALYSIS");
            record.setInputText(String.valueOf(jobId));
            record.setResultJson(objectMapper.writeValueAsString(Map.of("score", score, "gaps", gaps)));
            record.setSource("RULE");
            record.setCreatedAt(LocalDateTime.now());
            recordMapper.insert(record);
        } catch (Exception e) {
            log.warn("保存差距分析记录失败", e);
        }
    }

    // ======================== 内部数据类 ========================

    /** 多岗位合并时的中间数据结构 */
    private static class MergedSkillReq {
        Long skillId;
        int maxLevel;
        String maxLevelName;
        double weight;
        List<String> sourceJobs = new ArrayList<>();

        MergedSkillReq(Long skillId, int maxLevel, String maxLevelName, double weight) {
            this.skillId = skillId;
            this.maxLevel = maxLevel;
            this.maxLevelName = maxLevelName;
            this.weight = weight;
        }
    }
}