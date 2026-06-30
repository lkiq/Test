package com.xuelian.career.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuelian.career.entity.LearningTask;
import com.xuelian.career.entity.Skill;
import com.xuelian.career.entity.UserSkillMastery;
import com.xuelian.career.mapper.LearningTaskMapper;
import com.xuelian.career.mapper.SkillMapper;
import com.xuelian.career.mapper.UserSkillMasteryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 历史数据迁移服务 - 将已有的 LEARNING_COMPLETED 任务同步到 user_skill_mastery 表
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MasteryMigrationService {

    private static final int BATCH_SIZE = 500;

    private final LearningTaskMapper taskMapper;
    private final UserSkillMasteryMapper masteryMapper;
    private final SkillMapper skillMapper;

    /**
     * 分页迁移：将所有 LEARNING_COMPLETED 任务同步到 mastery 表
     */
    @Transactional
    public void migrateCompletedTasks() {
        int page = 0;
        int totalProcessed = 0;
        Set<String> processedKeys = new HashSet<>();  // 全局去重

        while (true) {
            List<LearningTask> batch = taskMapper.selectList(
                    new LambdaQueryWrapper<LearningTask>()
                            .eq(LearningTask::getStatus, "LEARNING_COMPLETED")
                            .isNotNull(LearningTask::getSkillId)
                            .last("LIMIT " + BATCH_SIZE + " OFFSET " + (page * BATCH_SIZE)));
            if (batch.isEmpty()) break;

            for (LearningTask task : batch) {
                String key = task.getUserId() + "_" + task.getSkillId();
                if (!processedKeys.contains(key)) {
                    processedKeys.add(key);
                    UserSkillMastery existing = masteryMapper.selectOne(
                            new LambdaQueryWrapper<UserSkillMastery>()
                                    .eq(UserSkillMastery::getUserId, task.getUserId())
                                    .eq(UserSkillMastery::getSkillId, task.getSkillId()));
                    if (existing == null) {
                        Skill skill = skillMapper.selectById(task.getSkillId());
                        UserSkillMastery mastery = new UserSkillMastery();
                        mastery.setUserId(task.getUserId());
                        mastery.setSkillId(task.getSkillId());
                        mastery.setSkillName(skill != null ? skill.getName() : "未知技能");
                        mastery.setLevel("BASIC");
                        mastery.setFirstMasteredAt(task.getCreatedAt());
                        mastery.setReviewCount(0);
                        mastery.setSource("LEARNING");
                        mastery.setCreatedAt(LocalDateTime.now());
                        masteryMapper.insert(mastery);
                        totalProcessed++;
                    }
                }
            }
            page++;
        }
        log.info("历史数据迁移完成：共处理 {} 条唯一 (userId, skillId) 组合", totalProcessed);
    }
}
