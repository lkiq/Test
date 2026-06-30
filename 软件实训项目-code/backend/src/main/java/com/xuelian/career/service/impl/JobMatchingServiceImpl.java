package com.xuelian.career.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuelian.career.dto.response.JobMatchResponse;
import com.xuelian.career.entity.*;
import com.xuelian.career.mapper.*;
import com.xuelian.career.service.JobMatchingService;
import com.xuelian.career.util.AssessmentWeightAdjuster;
import com.xuelian.career.vo.SkillGapVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 岗位匹配推荐服务实现 - 动态权重算法
 * 分支A（画像完整+测评有效）：技能40% + 兴趣30% + 城市20% + 测评10%
 * 分支B（画像不足/低测评）：技能20% + 兴趣50% + 城市20% + 测评10%
 * 基础分50 + 加权满分50，确保最低50分避免信任崩塌；区分度因子打破同分
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobMatchingServiceImpl implements JobMatchingService {

    private final JobPositionMapper jobPositionMapper;
    private final JobSkillRequirementMapper jobSkillRequirementMapper;
    private final SkillMapper skillMapper;
    private final CareerProfileMapper profileMapper;
    private final AssessmentResultMapper assessmentResultMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<JobMatchResponse> recommendJobs(Long userId) {
        return recommendJobs(userId, null, null);
    }

    @Override
    public List<JobMatchResponse> recommendJobs(Long userId, String overrideInterest, String overrideCity) {
        return recommendJobs(userId, overrideInterest, overrideCity, null);
    }

    /**
     * 支持外部传入实时兴趣/城市/显式测评结果的岗位匹配推荐
     * 智能合并：override 优先，画像兜底
     * 当 explicitResult != null 时直接使用该测评结果，避免重复查库（用于测评直达推荐场景）
     */
    @Override
    public List<JobMatchResponse> recommendJobs(Long userId, String overrideInterest, String overrideCity,
                                                 AssessmentResult explicitResult) {
        // 智能合并：override 优先，画像兜底
        CareerProfile profile = profileMapper.selectOne(
                new LambdaQueryWrapper<CareerProfile>().eq(CareerProfile::getUserId, userId));
        List<String> userSkills = parseSkills(profile != null ? profile.getSkillTags() : null);
        // 兴趣方向：override 优先，否则取 targetRoles 第一个
        String userInterest = (overrideInterest != null && !overrideInterest.isBlank())
                ? overrideInterest
                : parseFirstTargetRole(profile != null ? profile.getTargetRoles() : null);
        // 城市：override 优先，否则取画像 expectedCity
        String userCity = (overrideCity != null && !overrideCity.isBlank())
                ? overrideCity
                : (profile != null ? profile.getExpectedCity() : null);

        // 测评结果：显式传入优先，否则查库
        AssessmentResult latestResult = explicitResult;
        if (latestResult == null) {
            latestResult = assessmentResultMapper.selectOne(
                    new LambdaQueryWrapper<AssessmentResult>().eq(AssessmentResult::getUserId, userId)
                            .orderByDesc(AssessmentResult::getCreatedAt).last("LIMIT 1"));
        }

        // 获取所有岗位
        List<JobPosition> jobs = jobPositionMapper.selectList(
                new LambdaQueryWrapper<JobPosition>().eq(JobPosition::getIsDeleted, 0));

        // 动态权重分支选择（基于生效后的 interest/city 判断）
        boolean infoSufficient = userInterest != null && !userInterest.isBlank()
                && userCity != null && !userCity.isBlank();
        boolean lowAssessment = latestResult == null || latestResult.getTotalScore() < 30;
        double skillW, interestW, cityW, assessW;
        if (infoSufficient && !lowAssessment) {
            // 分支 A：画像完整、测评有效
            skillW = 40; interestW = 30; cityW = 20; assessW = 10;
        } else {
            // 分支 B：画像不足或测评低分
            skillW = 20; interestW = 50; cityW = 20; assessW = 10;
        }
        log.debug("用户 {} 权重分支: infoSufficient={}, lowAssessment={}, weights=({},{},{},{}), interest={}, city={}",
                userId, infoSufficient, lowAssessment, skillW, interestW, cityW, assessW, userInterest, userCity);

        return recommendJobsWithWeights(userId, userSkills, userInterest, userCity, latestResult, jobs,
                skillW, interestW, cityW, assessW);
    }

    /**
     * 抽离核心岗位打分逻辑，支持自定义权重（供双队列复用）
     * 优化点：
     * - 动态基础分：画像完整 base=50，不完整 base=45（拉大差距）
     * - 兴趣命中奖励：兴趣完全命中的岗位额外 +8 分，部分相关 +3 分（拉开 primary/fallback 分差）
     * @param userSkills 用户技能列表
     * @param userInterest 兴趣方向
     * @param userCity 期望城市
     * @param latestResult 最新测评
     * @param jobs 候选岗位列表
     * @param skillW/interestW/cityW/assessW 四项权重
     */
    private List<JobMatchResponse> recommendJobsWithWeights(Long userId, List<String> userSkills,
                                                             String userInterest, String userCity,
                                                             AssessmentResult latestResult, List<JobPosition> jobs,
                                                             double skillW, double interestW, double cityW, double assessW) {
        // 动态基础分：画像完整时 base=50，不完整时 base=45
        boolean profileComplete = userInterest != null && !userInterest.isBlank()
                && userCity != null && !userCity.isBlank();
        double baseScore = profileComplete ? 50.0 : 45.0;

        List<JobMatchResponse> results = new ArrayList<>();
        int rank = 0;
        for (JobPosition job : jobs) {
            // 获取岗位技能要求
            List<JobSkillRequirement> requirements = jobSkillRequirementMapper.selectList(
                    new LambdaQueryWrapper<JobSkillRequirement>().eq(JobSkillRequirement::getJobId, job.getId()));
            List<Long> skillIds = requirements.stream().map(JobSkillRequirement::getSkillId).collect(Collectors.toList());
            List<Skill> skills = skillIds.isEmpty() ? new ArrayList<>() : skillMapper.selectBatchIds(skillIds);
            Map<Long, Skill> skillMap = skills.stream().collect(Collectors.toMap(Skill::getId, s -> s));

            // 1. 技能匹配比例（0~1）
            int matchedCount = 0;
            for (JobSkillRequirement req : requirements) {
                Skill skill = skillMap.get(req.getSkillId());
                if (skill != null && userSkills.contains(skill.getName())) {
                    matchedCount++;
                }
            }
            double skillRatio = requirements.isEmpty() ? 0.5 : (double) matchedCount / requirements.size();
            double skillScore = skillW * skillRatio;

            // 2. 兴趣方向匹配比例（0~1）
            double interestRatio = calcInterestRatio(job, userInterest);
            double interestScore = interestW * interestRatio;

            // 3. 城市匹配（1.0 完全匹配，0.5 不匹配）
            double cityRatio = (userCity != null && userCity.equals(job.getCity())) ? 1.0 : 0.5;
            double cityScore = cityW * cityRatio;

            // 4. 测评分（0~1）：使用维度加权计算，让低总分用户在适配岗位上有合理匹配度
            double assessRatio = AssessmentWeightAdjuster.assessRatioForJob(job, latestResult);
            double assessScore = assessW * assessRatio;

            // 5. 基础分 + 加权满分 50（weighted 满分100，乘0.5得50）
            double weighted = skillScore + interestScore + cityScore + assessScore;
            double total = baseScore + weighted * 0.5;

            // 6. 兴趣奖励分：兴趣完全命中额外 +8，部分相关 +3，拉开 primary 与 fallback 的分差
            double interestBonus = 0;
            if (userInterest != null && !userInterest.isBlank()) {
                if (interestRatio >= 1.0) {
                    interestBonus = 8.0;
                } else if (interestRatio >= 0.5) {
                    interestBonus = 3.0;
                }
            }
            total += interestBonus;

            // 7. 区分度因子（打破同分，基于技能匹配数和岗位排名）
            double diff = (matchedCount * 0.5) - (rank * 0.3);
            total += diff;

            // 8. 限制区间 [50, 99]
            total = Math.max(50.0, Math.min(99.0, total));

            // 技能标签和缺口
            List<JobMatchResponse.SkillTagVO> skillTags = new ArrayList<>();
            List<SkillGapVO> skillGaps = new ArrayList<>();
            for (JobSkillRequirement req : requirements) {
                Skill skill = skillMap.get(req.getSkillId());
                if (skill == null) continue;
                if (userSkills.contains(skill.getName())) {
                    skillTags.add(JobMatchResponse.SkillTagVO.builder()
                            .skillName(skill.getName()).status("mastered").build());
                } else {
                    skillTags.add(JobMatchResponse.SkillTagVO.builder()
                            .skillName(skill.getName()).status("missing").build());
                    skillGaps.add(SkillGapVO.builder()
                            .skillName(skill.getName()).requiredLevel(req.getRequiredLevel())
                            .userLevel("未掌握").gapDegree("需要学习").build());
                }
            }

            double finalTotal = total;
            results.add(JobMatchResponse.builder()
                    .jobId(job.getId()).title(job.getTitle()).direction(job.getDirection())
                    .jd(job.getJd()).city(job.getCity()).salaryRange(job.getSalaryRange())
                    .companyName(job.getCompanyName())
                    .jobType(job.getJobType())
                    .experienceRequired(job.getExperienceRequired())
                    .educationRequired(job.getEducationRequired())
                    .publishTime(job.getPublishTime() != null ? job.getPublishTime().toString() : null)
                    .matchScore(Math.round(finalTotal * 10.0) / 10.0)
                    .skillScore(Math.round(skillScore * 10.0) / 10.0)
                    .assessmentScore(Math.round(assessScore * 10.0) / 10.0)
                    .locationScore(cityScore)
                    .skillTags(skillTags).skillGaps(skillGaps)
                    .build());
            rank++;
        }

        // 按匹配度降序排列
        results.sort((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()));
        return results;
    }

    /**
     * 判断岗位是否与用户兴趣方向强相关（用于 primary 队列筛选）
     * 兴趣关键词命中岗位 direction 或 title 即视为强相关
     */
    public boolean isInterestMatched(JobPosition job, String userInterest) {
        if (userInterest == null || userInterest.isBlank()) return false;
        String interest = userInterest.toLowerCase();
        String direction = (job.getDirection() != null ? job.getDirection() : "").toLowerCase();
        String title = (job.getTitle() != null ? job.getTitle() : "").toLowerCase();
        return direction.contains(interest) || title.contains(interest)
                || interest.contains(direction) || interest.contains(title);
    }

    /**
     * 计算兴趣方向匹配比例
     * 完全命中：1.0；部分相关：0.5；不相关：0.1
     */
    private double calcInterestRatio(JobPosition job, String userInterest) {
        if (userInterest == null || userInterest.isBlank()) return 0.3; // 默认中等
        String interest = userInterest.toLowerCase();
        String direction = (job.getDirection() != null ? job.getDirection() : "").toLowerCase();
        String title = (job.getTitle() != null ? job.getTitle() : "").toLowerCase();
        if (direction.contains(interest) || title.contains(interest)) return 1.0;
        // 部分相关：如 interest="后端"，岗位含"全栈"
        if (isPartialRelated(interest, direction, title)) return 0.5;
        return 0.1;
    }

    /**
     * 判断兴趣与岗位是否部分相关
     */
    private boolean isPartialRelated(String interest, String direction, String title) {
        // 后端/前端/全栈 互为部分相关
        if (interest.equals("后端") || interest.equals("前端")) {
            return direction.contains("全栈") || title.contains("全栈")
                    || direction.contains(interest) || title.contains(interest);
        }
        // 数据/算法 互为部分相关
        if (interest.equals("数据")) {
            return direction.contains("算法") || title.contains("算法") || direction.contains("数据");
        }
        if (interest.equals("算法")) {
            return direction.contains("数据") || title.contains("数据") || direction.contains("算法");
        }
        return false;
    }

    /**
     * 解析 targetRoles JSON 数组字符串，取第一个元素
     */
    @SuppressWarnings("unchecked")
    private String parseFirstTargetRole(String targetRolesJson) {
        if (targetRolesJson == null || targetRolesJson.isBlank()) return null;
        try {
            List<String> roles = objectMapper.readValue(targetRolesJson, List.class);
            if (!roles.isEmpty()) return roles.get(0);
        } catch (Exception e) {
            return targetRolesJson;
        }
        return null;
    }

    @Override
    public JobPosition getJobDetail(Long jobId) {
        return jobPositionMapper.selectById(jobId);
    }

    @Override
    public List<JobPosition> searchJobs(String keyword, String city) {
        LambdaQueryWrapper<JobPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobPosition::getIsDeleted, 0);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(JobPosition::getTitle, keyword)
                    .or().like(JobPosition::getDirection, keyword)
                    .or().like(JobPosition::getJd, keyword)
                    .or().like(JobPosition::getCompanyName, keyword));
        }
        if (city != null && !city.isEmpty()) {
            wrapper.eq(JobPosition::getCity, city);
        }
        return jobPositionMapper.selectList(wrapper);
    }

    /**
     * 解析技能标签 JSON 为列表
     */
    @SuppressWarnings("unchecked")
    private List<String> parseSkills(String skillTagsJson) {
        try {
            if (skillTagsJson == null || skillTagsJson.isEmpty()) return new ArrayList<>();
            return objectMapper.readValue(skillTagsJson, List.class);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
