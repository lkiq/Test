package com.xuelian.career.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuelian.career.dto.request.EnterpriseRecommendRequest;
import com.xuelian.career.dto.response.EnterpriseRecommendResponse;
import com.xuelian.career.dto.response.EnterpriseRecommendResponse.*;
import com.xuelian.career.entity.RecommendationRecord;
import com.xuelian.career.entity.User;
import com.xuelian.career.mapper.RecommendationRecordMapper;
import com.xuelian.career.mapper.UserMapper;
import com.xuelian.career.service.CandidateScoreService;
import com.xuelian.career.service.CandidateScoreService.*;
import com.xuelian.career.service.EnterpriseService;
import com.xuelian.career.service.ProjectParseService;
import com.xuelian.career.service.ProjectParseService.*;
import com.xuelian.career.service.DeepSeekService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 企业推荐服务实现 - AI 项目解析 + 候选人评分 + 推荐理由
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnterpriseServiceImpl implements EnterpriseService {

    private final ProjectParseService projectParseService;
    private final CandidateScoreService candidateScoreService;
    private final DeepSeekService deepSeekService;
    private final UserMapper userMapper;
    private final RecommendationRecordMapper recordMapper;
    private final ObjectMapper objectMapper;

    @Override
    public EnterpriseRecommendResponse recommend(Long userId, EnterpriseRecommendRequest request) {
        String projectDesc = request.getProjectDescription();

        // 步骤①：解析项目需求
        ParseResult parseResult = projectParseService.parseProject(projectDesc);
        boolean aiUsed = !parseResult.getPositions().isEmpty();

        // 步骤②：为每个岗位的第一个岗位（主岗位）筛选候选人
        List<PositionSuggestion> positions = parseResult.getPositions();
        PositionSuggestion primaryPosition = positions.isEmpty() ? null : positions.get(0);

        // 准备筛选条件
        FilterInfo filterInfo = null;
        if (request.getFilters() != null) {
            EnterpriseRecommendRequest.RecommendFilters f = request.getFilters();
            filterInfo = new FilterInfo(f.getEducation(), f.getCity(), f.getSkillPreference());
        }

        // 步骤③④：召回所有学生并评分
        List<User> students = userMapper.selectList(
            new LambdaQueryWrapper<User>().eq(User::getRole, "STUDENT")
                .eq(User::getIsDeleted, 0)
                .last("LIMIT 50"));

        final FilterInfo _filters = filterInfo;
        List<CandidateScore> scoredCandidates = students.stream()
            .map(student -> candidateScoreService.compute(
                student.getId(),
                primaryPosition != null ? primaryPosition.getSkillRequirements() : List.of(),
                primaryPosition != null ? primaryPosition.getPositionTitle() : "后端开发",
                _filters))
            .sorted(Comparator.comparingDouble(CandidateScore::getMatchScore).reversed())
            .limit(20)
            .collect(Collectors.toList());

        // 步骤⑤：为 Top 3-5 生成 AI 推荐理由
        List<CandidateScore> topCandidates = scoredCandidates.stream()
            .filter(c -> c.getMatchScore() >= 40).limit(5).collect(Collectors.toList());

        if (!topCandidates.isEmpty()) {
            enrichReasons(topCandidates, primaryPosition);
        }

        // 步骤⑥：构建响应并持久化
        List<CandidateItem> candidateItems = scoredCandidates.stream()
            .map(this::toCandidateItem).collect(Collectors.toList());

        EnterpriseRecommendResponse response = EnterpriseRecommendResponse.builder()
            .projectSummary(parseResult.getProjectSummary())
            .positions(positions)
            .candidates(candidateItems)
            .source(aiUsed ? "AI" : "FALLBACK")
            .createdAt(LocalDateTime.now())
            .build();

        // 持久化推荐记录
        saveRecord(userId, projectDesc, response);

        return response;
    }

    /**
     * 为 Top 候选人通过 DeepSeek 生成个性化推荐理由
     */
    private void enrichReasons(List<CandidateScore> candidates, PositionSuggestion position) {
        try {
            if (!deepSeekService.isAvailable()) return;

            String requiredSkills = position.getSkillRequirements().stream()
                .map(s -> s.getSkillName() + "(" + s.getRequiredLevel() + ")")
                .collect(Collectors.joining("、"));

            for (CandidateScore c : candidates) {
                String prompt = String.format(
                    "你是招聘辅助助手。根据以下信息，为候选人生成简洁的推荐理由。\n\n" +
                    "岗位：%s\n" +
                    "岗位需求技能：%s\n" +
                    "候选人：%s\n" +
                    "匹配分：%.1f\n" +
                    "已匹配技能：%s\n" +
                    "技能缺口：%s\n" +
                    "学历：%s，学校：%s，专业：%s\n\n" +
                    "请生成1-2句话的推荐理由，突出优势和不足，字数50-80字。直接返回推荐理由文本，不要JSON。",
                    position.getPositionTitle(), requiredSkills,
                    c.getUsername(), c.getMatchScore(),
                    c.getMatchedSkills() != null ? String.join("、", c.getMatchedSkills()) : "无",
                    c.getGapSkills() != null ? String.join("、", c.getGapSkills()) : "无",
                    c.getEducation(), c.getSchool(), c.getMajor()
                );

                String reason = deepSeekService.callAPI(
                    "你是招聘辅助助手，生成候选人推荐理由。只返回简要推荐文本。", prompt);
                if (reason != null && !reason.isBlank()) {
                    // 清理可能的多余内容
                    reason = reason.replaceAll("^[\\s\"'`]+|[\\s\"'`]+$", "");
                    c.setRecommendReason(reason.length() > 150 ? reason.substring(0, 150) : reason);
                }
            }
        } catch (Exception e) {
            log.warn("AI 推荐理由生成失败，使用兜底理由: {}", e.getMessage());
        }
    }

    private CandidateItem toCandidateItem(CandidateScore score) {
        return CandidateItem.builder()
            .userId(score.getUserId())
            .username(score.getUsername())
            .avatar(score.getAvatar())
            .education(score.getEducation())
            .school(score.getSchool())
            .major(score.getMajor())
            .matchScore(score.getMatchScore())
            .matchLevel(score.getMatchLevel())
            .skillScore(score.getSkillScore())
            .assessmentScore(score.getAssessmentScore())
            .learningScore(score.getLearningScore())
            .learningResultScore(score.getLearningResultScore())
            .basicScore(score.getBasicScore())
            .matchedSkills(score.getMatchedSkills())
            .gapSkills(score.getGapSkills())
            .recommendReason(score.getRecommendReason() != null ? score.getRecommendReason() : "综合技能匹配度较高")
            .build();
    }

    private void saveRecord(Long userId, String inputText, EnterpriseRecommendResponse response) {
        try {
            RecommendationRecord record = new RecommendationRecord();
            record.setUserId(userId);
            record.setType("ENTERPRISE_REC");
            record.setInputText(inputText);
            record.setResultJson(objectMapper.writeValueAsString(response));
            record.setSource(response.getSource());
            recordMapper.insert(record);
            log.info("推荐记录已保存: userId={}, source={}", userId, response.getSource());
        } catch (Exception e) {
            log.warn("保存推荐记录失败: {}", e.getMessage());
        }
    }

    @Override
    public List<EnterpriseRecommendResponse> getHistory(Long userId) {
        List<RecommendationRecord> records = recordMapper.selectList(
            new LambdaQueryWrapper<RecommendationRecord>()
                .eq(RecommendationRecord::getUserId, userId)
                .eq(RecommendationRecord::getType, "ENTERPRISE_REC")
                .orderByDesc(RecommendationRecord::getCreatedAt)
                .last("LIMIT 20"));

        return records.stream().map(r -> {
            try {
                return objectMapper.readValue(r.getResultJson(), EnterpriseRecommendResponse.class);
            } catch (Exception e) {
                log.warn("解析历史推荐记录失败: id={}", r.getId());
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
