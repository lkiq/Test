package com.xuelian.career.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuelian.career.dto.response.GrowthReportResponse;
import com.xuelian.career.dto.response.ProgressOverviewResponse;
import com.xuelian.career.dto.response.SkillProgressResponse;
import com.xuelian.career.entity.*;
import com.xuelian.career.mapper.*;
import com.xuelian.career.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 学习进度服务实现 - 聚合学习任务、测评、面试等数据
 */
@Service
@RequiredArgsConstructor
public class ProgressServiceImpl implements ProgressService {

    private final LearningTaskMapper taskMapper;
    private final AssessmentResultMapper assessmentResultMapper;
    private final InterviewRecordMapper interviewRecordMapper;
    private final ResumeAnalysisMapper resumeAnalysisMapper;

    @Override
    public ProgressOverviewResponse getOverview(Long userId) {
        List<LearningTask> tasks = taskMapper.selectList(
                new LambdaQueryWrapper<LearningTask>().eq(LearningTask::getUserId, userId));
        long completed = tasks.stream().filter(t -> "LEARNING_COMPLETED".equals(t.getStatus())).count();
        double completionRate = tasks.isEmpty() ? 0 : Math.round(100.0 * completed / tasks.size());

        int assessmentCount = assessmentResultMapper.selectCount(
                new LambdaQueryWrapper<AssessmentResult>().eq(AssessmentResult::getUserId, userId)).intValue();
        int interviewCount = interviewRecordMapper.selectCount(
                new LambdaQueryWrapper<InterviewRecord>().eq(InterviewRecord::getUserId, userId)).intValue();
        int resumeCount = resumeAnalysisMapper.selectCount(
                new LambdaQueryWrapper<ResumeAnalysis>().eq(ResumeAnalysis::getUserId, userId)).intValue();

        // 平均测评分
        List<AssessmentResult> results = assessmentResultMapper.selectList(
                new LambdaQueryWrapper<AssessmentResult>().eq(AssessmentResult::getUserId, userId));
        double avgScore = results.stream().mapToDouble(AssessmentResult::getTotalScore).average().orElse(0);

        return ProgressOverviewResponse.builder()
                .completedTasks((int) completed).totalTasks(tasks.size()).completionRate(completionRate)
                .totalAssessmentCount(assessmentCount).totalInterviewCount(interviewCount)
                .resumeAnalysisCount(resumeCount).averageAssessmentScore(Math.round(avgScore * 10.0) / 10.0)
                .build();
    }

    @Override
    public SkillProgressResponse getSkillProgress(Long userId) {
        List<LearningTask> tasks = taskMapper.selectList(
                new LambdaQueryWrapper<LearningTask>().eq(LearningTask::getUserId, userId));
        // 按技能统计完成率
        Map<String, List<LearningTask>> bySkill = tasks.stream()
                .collect(Collectors.groupingBy(t -> t.getSkillId() != null ? String.valueOf(t.getSkillId()) : "未分类"));
        Map<String, Double> skillScores = new LinkedHashMap<>();
        Map<String, String> skillLevels = new LinkedHashMap<>();
        bySkill.forEach((skill, tlist) -> {
            long done = tlist.stream().filter(t -> "LEARNING_COMPLETED".equals(t.getStatus())).count();
            double rate = tlist.isEmpty() ? 0 : Math.round(100.0 * done / tlist.size());
            skillScores.put(skill, rate);
            skillLevels.put(skill, rate >= 80 ? "熟练" : rate >= 50 ? "掌握" : "学习");
        });
        return SkillProgressResponse.builder().skillScores(skillScores).skillLevels(skillLevels).build();
    }

    @Override
    public GrowthReportResponse getGrowthReport(Long userId) {
        List<Map<String, Object>> timeline = new ArrayList<>();
        // 测评趋势
        assessmentResultMapper.selectList(new LambdaQueryWrapper<AssessmentResult>()
                        .eq(AssessmentResult::getUserId, userId).orderByAsc(AssessmentResult::getCreatedAt))
                .forEach(r -> timeline.add(Map.of("date", r.getCreatedAt().toLocalDate().toString(),
                        "type", "测评", "score", r.getTotalScore())));

        // 面试趋势
        interviewRecordMapper.selectList(new LambdaQueryWrapper<InterviewRecord>()
                        .eq(InterviewRecord::getUserId, userId).orderByAsc(InterviewRecord::getCreatedAt))
                .forEach(r -> timeline.add(Map.of("date", r.getCreatedAt().toLocalDate().toString(),
                        "type", "面试", "score", r.getScore())));

        timeline.sort((a, b) -> ((String) a.get("date")).compareTo((String) b.get("date")));

        return GrowthReportResponse.builder()
                .timeline(timeline)
                .summary(Map.of("message", "持续学习，稳步提升"))
                .build();
    }
}
