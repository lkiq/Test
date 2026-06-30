package com.xuelian.career.controller.student;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuelian.career.common.Result;
import com.xuelian.career.dto.response.*;
import com.xuelian.career.entity.*;
import com.xuelian.career.mapper.*;
import com.xuelian.career.service.LearningPathService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 学习路径控制器（学生端）
 * 支持双模式：单岗位/多岗位合并/各岗位独立
 * V5 修正：新增路径统计、技能矩阵、测试生成、regenerate、mastery 接口
 */
@Slf4j
@RestController
@RequestMapping("/api/student/learning")
@RequiredArgsConstructor
public class LearningPathController {

    // Redis不可用时的内存兜底容器（key = "test:session:" + userId + ":" + taskId）
    private final Map<String, List<TestQuestionDTO>> testSessionFallback = new ConcurrentHashMap<>();

    private final LearningPathService learningPathService;
    private final LearningPathMapper pathMapper;
    private final LearningTaskMapper taskMapper;
    private final SkillMapper skillMapper;
    private final UserSkillMasteryMapper masteryMapper;
    private final JobPositionMapper jobPositionMapper;
    private final SkillTestQuestionMapper skillTestQuestionMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    /** POST /api/student/learning/generate?jobId= */
    @PostMapping("/generate")
    public Result<LearningPath> generate(@RequestParam(required = false) Long jobId,
                                         HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        LearningPath path = jobId != null
                ? learningPathService.generatePath(userId, jobId)
                : learningPathService.generatePath(userId);
        return Result.success(path);
    }

    /**
     * POST /api/student/learning/generate/multi
     * 多岗位学习路径生成（双模式）：支持合并路径与单独路径并存
     */
    @PostMapping("/generate/multi")
    public Result<?> generateMulti(@RequestBody Map<String, Object> body,
                                   HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        @SuppressWarnings("unchecked")
        List<Integer> rawIds = (List<Integer>) body.get("jobIds");
        String mode = (String) body.getOrDefault("mode", "MERGED");

        if (rawIds == null || rawIds.isEmpty()) {
            return Result.badRequest("请选择至少一个岗位");
        }

        List<Long> jobIds = rawIds.stream().map(Integer::longValue).toList();

        List<LearningPath> paths = learningPathService.generatePath(userId, jobIds, mode);
        return Result.success(paths);
    }

    /**
     * POST /api/student/learning/regenerate/multi
     * V5 新增：重新生成路径（归档旧路径，清空进度）
     */
    @PostMapping("/regenerate/multi")
    public Result<List<LearningPath>> regenerateMulti(@RequestBody Map<String, Object> body,
                                                       HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        @SuppressWarnings("unchecked")
        List<Integer> rawIds = (List<Integer>) body.get("jobIds");
        String mode = (String) body.getOrDefault("mode", "MERGED");
        List<Long> jobIds = rawIds.stream().map(Integer::longValue).toList();

        List<LearningPath> paths = learningPathService.regenerateAll(userId, jobIds, mode);
        return Result.success(paths);
    }

    /** GET /api/student/learning/path - 获取当前活跃路径（单条） */
    @GetMapping("/path")
    public Result<LearningPath> getPath(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        LearningPath path = learningPathService.getPath(userId);
        return path != null ? Result.success(path) : Result.success("暂无学习路径", null);
    }

    /** GET /api/student/learning/paths - 获取所有活跃路径 */
    @GetMapping("/paths")
    public Result<List<LearningPath>> getPaths(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<LearningPath> paths = learningPathService.getPaths(userId);
        return Result.success(paths);
    }

    /**
     * GET /api/student/learning/path-list-with-stats
     * V5 新增：路径列表带统计信息
     */
    @GetMapping("/path-list-with-stats")
    public Result<List<PathStatsDTO>> getPathListWithStats(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<PathStatsDTO> stats = learningPathService.getPathListWithStats(userId);
        return Result.success(stats);
    }

    /** GET /api/student/learning/path/{id}/meta */
    @GetMapping("/path/{id}/meta")
    public Result<Map<String, Object>> getPathMeta(@PathVariable Long id) {
        Map<String, Object> meta = learningPathService.getPathMeta(id);
        return Result.success(meta);
    }

    /**
     * GET /api/student/learning/path/{id}/skills-matrix
     * V5 新增：获取路径的技能-岗位矩阵
     */
    @GetMapping("/path/{id}/skills-matrix")
    public Result<List<SkillsMatrixDTO>> getPathSkillsMatrix(@PathVariable Long id) {
        List<SkillsMatrixDTO> matrix = learningPathService.getPathSkillsMatrix(id);
        return Result.success(matrix);
    }

    /** PUT /api/student/learning/tasks/{id} */
    @PutMapping("/tasks/{id}")
    public Result<Void> updateTaskStatus(@PathVariable Long id, @RequestBody Map<String, String> body,
                                          HttpServletRequest request) {
        String newStatus = body.get("status");
        learningPathService.updateTaskStatus(id, newStatus);

        // 当任务标记为 LEARNING_COMPLETED 时，同步到 mastery 表
        if ("LEARNING_COMPLETED".equals(newStatus)) {
            Long userId = (Long) request.getAttribute("userId");
            syncMasteryFromCompletedTask(userId, id);
        }
        return Result.success();
    }

    /** GET /api/student/learning/tasks */
    @GetMapping("/tasks")
    public Result<List<LearningTask>> getTasks(@RequestParam(required = false) Long pathId,
                                                HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (pathId != null) {
            return Result.success(learningPathService.getTasks(userId, pathId));
        }
        return Result.success(learningPathService.getTasks(userId));
    }

    /** GET /api/student/learning/resources?skill=&stage= */
    @GetMapping("/resources")
    public Result<List<LearningResource>> listResources(@RequestParam(required = false) Long skill,
                                                         @RequestParam(required = false) String stage) {
        return Result.success(learningPathService.listResources(skill, stage));
    }

    // ==================== V5 新增：技能测试接口 ====================

    /**
     * POST /api/student/learning/tasks/{id}/test-start
     * V7 重构：纯数据库读取，不使用 AI 生成
     */
    @PostMapping("/tasks/{id}/test-start")
    public Result<List<TestQuestionDTO>> startTest(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        LearningTask task = learningPathService.getTask(id, userId);
        if (task == null) {
            return Result.notFound("任务不存在");
        }

        Skill skill = skillMapper.selectById(task.getSkillId());
        if (skill == null) {
            return Result.error(500, "关联技能不存在");
        }

        List<TestQuestionDTO> questions = new ArrayList<>();

        // ========== 从数据库题库随机获取题目 ==========
        try {
            List<SkillTestQuestion> dbQuestions = skillTestQuestionMapper
                    .selectRandomBySkillAndStage(task.getSkillId(), task.getStage(), 5);
            if (dbQuestions != null && !dbQuestions.isEmpty()) {
                questions = convertFromDbQuestions(dbQuestions);
                log.info("从数据库加载测试题: skillId={}, stage={}, count={}",
                        task.getSkillId(), task.getStage(), questions.size());
            }
        } catch (Exception e) {
            log.warn("从数据库获取测试题失败: skillId={}, stage={}",
                    task.getSkillId(), task.getStage(), e);
        }

        // ========== 数据库无题目时返回兜底题 ==========
        if (questions.isEmpty()) {
            log.warn("数据库无题目，返回兜底题: skillId={}, stage={}",
                    task.getSkillId(), task.getStage());
            questions = List.of(
                    new TestQuestionDTO(
                            "请描述你学习的核心内容",
                            List.of("A. 基础知识", "B. 进阶概念", "C. 实际应用", "D. 以上都对"),
                            "D"
                    ),
                    new TestQuestionDTO(
                            "这项技能最核心的应用场景是什么？",
                            List.of("A. 仅个人项目", "B. 企业级应用", "C. 学术研究", "D. 不太了解"),
                            "B"
                    )
            );
        }

        // ========== 缓存本次测验 session（供 submit 时验证答案） ==========
        String testSessionKey = "test:session:" + userId + ":" + id;
        try {
            redisTemplate.opsForValue().set(testSessionKey, questions, 30, TimeUnit.MINUTES);
            testSessionFallback.remove(testSessionKey);
        } catch (Exception e) {
            log.warn("Redis不可用，使用内存兜底存储测试session");
            testSessionFallback.put(testSessionKey, questions);
        }

        return Result.success(questions);
    }

    /**
     * POST /api/student/learning/tasks/{id}/test-submit
     * V5 新增：提交测试答案
     */
    @PostMapping("/tasks/{id}/test-submit")
    public Result<TestResultDTO> submitTest(@PathVariable Long id,
                                             @RequestBody Map<String, Map<String, String>> body,
                                             HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        LearningTask task = learningPathService.getTask(id, userId);
        if (task == null) {
            return Result.notFound("任务不存在");
        }

        // 从 Redis 或内存兜底获取缓存的题目
        String testSessionKey = "test:session:" + userId + ":" + id;
        List<TestQuestionDTO> questions = null;

        try {
            Object cached = redisTemplate.opsForValue().get(testSessionKey);
            if (cached != null) {
                questions = objectMapper.convertValue(cached,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, TestQuestionDTO.class));
            }
        } catch (Exception e) {
            log.warn("Redis读取测试session失败，尝试内存兜底", e);
        }

        if (questions == null) {
            questions = testSessionFallback.get(testSessionKey);
        }

        if (questions == null) {
            return Result.error(400, "测试已过期，请重新开始");
        }

        // 获取用户答案（前端传 { "0": "A", "1": "B", ... }）
        Map<String, String> answers = body.get("answers");
        if (answers == null) {
            return Result.badRequest("缺少答案");
        }

        // 计算得分
        int correctCount = 0;
        for (int i = 0; i < questions.size(); i++) {
            String userAnswer = answers.get(String.valueOf(i));
            if (userAnswer != null && userAnswer.equalsIgnoreCase(questions.get(i).getCorrectAnswer())) {
                correctCount++;
            }
        }
        int score = questions.isEmpty() ? 0 : (int) (correctCount * 100.0 / questions.size());

        TestResultDTO result = new TestResultDTO(score, correctCount, questions.size());

        // 通过则更新任务状态和技能掌握度
        if (score >= 60) {
            // 通过 service 更新任务状态（会触发进度互通同步）
            learningPathService.updateTaskStatus(task.getId(), "TEST_PASSED");

            // 更新技能掌握度
            updateOrCreateSkillMastery(userId, task.getSkillId(), result.getNewLevel(), "TEST");
        }

        // 清除本次测验缓存（Redis + 内存兜底）
        try {
            redisTemplate.delete(testSessionKey);
        } catch (Exception e) {
            // silent
        }
        testSessionFallback.remove(testSessionKey);

        return Result.success(result);
    }

    // ==================== V5 新增：技能掌握接口 ====================

    /**
     * GET /api/student/learning/mastered-skills
     * V5 新增：获取用户已掌握的技能列表
     */
    @GetMapping("/mastered-skills")
    public Result<List<UserSkillMastery>> getMasteredSkills(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<UserSkillMastery> skills = masteryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserSkillMastery>()
                        .eq(UserSkillMastery::getUserId, userId)
                        .orderByDesc(UserSkillMastery::getUpdatedAt));
        return Result.success(skills);
    }

    /**
     * POST /api/student/learning/mastered-skills/{skillId}/review
     * V5 新增：记录复习次数
     */
    @PostMapping("/mastered-skills/{skillId}/review")
    public Result<Void> recordReview(@PathVariable Long skillId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserSkillMastery mastery = masteryMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserSkillMastery>()
                        .eq(UserSkillMastery::getUserId, userId)
                        .eq(UserSkillMastery::getSkillId, skillId));
        if (mastery != null) {
            mastery.setReviewCount((mastery.getReviewCount() != null ? mastery.getReviewCount() : 0) + 1);
            mastery.setLastReviewedAt(LocalDateTime.now());
            masteryMapper.updateById(mastery);
        }
        return Result.success();
    }

    // ==================== 辅助方法 ====================

    /**
     * 当任务标记为 LEARNING_COMPLETED 时，同步到 mastery 表
     */
    private void syncMasteryFromCompletedTask(Long userId, Long taskId) {
        try {
            LearningTask task = learningPathService.getTask(taskId, userId);
            if (task == null || task.getSkillId() == null) return;
            updateOrCreateSkillMastery(userId, task.getSkillId(), "BASIC", "LEARNING");
        } catch (Exception e) {
            log.warn("同步 mastery 失败 userId={}, taskId={}", userId, taskId, e);
        }
    }

    /**
     * 更新或创建技能掌握记录
     */
    private void updateOrCreateSkillMastery(Long userId, Long skillId, String level, String source) {
        UserSkillMastery existing = masteryMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserSkillMastery>()
                        .eq(UserSkillMastery::getUserId, userId)
                        .eq(UserSkillMastery::getSkillId, skillId));

        Skill skill = skillMapper.selectById(skillId);
        String skillName = skill != null ? skill.getName() : "未知技能";

        if (existing != null) {
            // 只有新等级更高才升级
            if (shouldUpgradeLevel(existing.getLevel(), level)) {
                existing.setLevel(level);
            }
            existing.setSource(source);
            existing.setUpdatedAt(LocalDateTime.now());
            masteryMapper.updateById(existing);
        } else {
            UserSkillMastery mastery = new UserSkillMastery();
            mastery.setUserId(userId);
            mastery.setSkillId(skillId);
            mastery.setSkillName(skillName);
            mastery.setLevel(level);
            mastery.setSource(source);
            mastery.setFirstMasteredAt(LocalDateTime.now());
            mastery.setReviewCount(0);
            mastery.setCreatedAt(LocalDateTime.now());
            masteryMapper.insert(mastery);
        }
    }

    /**
     * 判断是否应该升级等级
     */
    private boolean shouldUpgradeLevel(String currentLevel, String newLevel) {
        List<String> order = List.of("BASIC", "INTERMEDIATE", "ADVANCED", "EXPERT");
        int currentIdx = order.indexOf(currentLevel);
        int newIdx = order.indexOf(newLevel);
        return newIdx > currentIdx;
    }

    /**
     * 将数据库题目转换为 DTO 格式
     */
    private List<TestQuestionDTO> convertFromDbQuestions(List<SkillTestQuestion> dbQuestions) {
        List<TestQuestionDTO> result = new ArrayList<>();
        for (SkillTestQuestion q : dbQuestions) {
            try {
                List<String> options = objectMapper.readValue(q.getOptions(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
                result.add(new TestQuestionDTO(q.getQuestion(), options, q.getCorrectAnswer()));
            } catch (JsonProcessingException e) {
                log.warn("解析数据库题目 options JSON 失败: id={}", q.getId(), e);
            }
        }
        return result;
    }
}
