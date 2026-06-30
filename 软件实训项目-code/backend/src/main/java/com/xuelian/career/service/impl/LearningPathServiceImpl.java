package com.xuelian.career.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuelian.career.dto.response.GapReportResponse;
import com.xuelian.career.dto.response.PathStatsDTO;
import com.xuelian.career.dto.response.SkillsMatrixDTO;
import com.xuelian.career.entity.*;
import com.xuelian.career.mapper.*;
import com.xuelian.career.service.GapAnalysisService;
import com.xuelian.career.service.LearningPathService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学习路径服务实现 - 四阶段生成（BASIC→FRAMEWORK→PROJECT→INTERVIEW）
 * 支持双模式：单岗位/多岗位合并/各岗位独立
 * V5 修正：generatePath 复用已有路径 + refreshTasks 保留学习进度
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LearningPathServiceImpl implements LearningPathService {

    private final LearningPathMapper pathMapper;
    private final LearningTaskMapper taskMapper;
    private final LearningResourceMapper resourceMapper;
    private final JobSkillRequirementMapper requirementMapper;
    private final SkillMapper skillMapper;
    private final GapAnalysisService gapAnalysisService;
    private final AssessmentResultMapper assessmentResultMapper;
    private final JobPositionMapper jobPositionMapper;

    private static final String[] STAGES = {"BASIC", "FRAMEWORK", "PROJECT", "INTERVIEW"};

    @Value("${learning.search-url-template:https://search.bilibili.com/all?keyword={keyword}}")
    private String searchUrlTemplate;

    // ==================== 单岗位生成（V5 修正：复用已有路径） ====================

    @Override
    public LearningPath generatePath(Long userId) {
        // 优先返回已有活跃路径中的第一条，没有才创建合并路径作为默认
        List<LearningPath> existing = pathMapper.selectList(
                new LambdaQueryWrapper<LearningPath>()
                        .eq(LearningPath::getUserId, userId)
                        .eq(LearningPath::getStatus, "ACTIVE")
                        .orderByDesc(LearningPath::getCreatedAt)
                        .last("LIMIT 1"));
        if (!existing.isEmpty()) {
            return existing.get(0);
        }
        // 没有任何路径时，创建一个合并路径作为默认
        return generatePath(userId, (Long) null);
    }

    @Override
    public LearningPath generatePath(Long userId, Long jobId) {
        // V5 修正：按 (userId, jobId) 查找已有活跃路径，复用而非归档
        LearningPath existingPath = findActivePath(userId, jobId);

        LearningPath path;
        if (existingPath != null) {
            // 已有路径 → 只刷新任务（保留已有进度）
            path = existingPath;
            log.info("复用已有学习路径 pathId={}, jobId={}", path.getId(), jobId);
        } else {
            // 无已有路径 → 创建新路径
            path = createPathRecord(userId, jobId);
            log.info("创建新学习路径 pathId={}, jobId={}", path.getId(), jobId);
        }

        // 获取技能列表
        List<Skill> skills = getSkillsForJob(jobId);

        // 按差距优先级排序
        if (jobId != null && !skills.isEmpty()) {
            skills = prioritizeWeakSkills(userId, jobId, skills);
        }

        // V5 修正：刷新任务（保留旧任务状态，增加新技能任务）
        refreshTasks(path.getId(), userId, skills);

        return path;
    }

    // ==================== 多岗位生成（双模式） ====================

    @Override
    public List<LearningPath> generatePath(Long userId, List<Long> jobIds, String mode) {
        if (jobIds == null || jobIds.isEmpty()) {
            return List.of(generatePath(userId));
        }
        if (jobIds.size() == 1) {
            return List.of(generatePath(userId, jobIds.get(0)));
        }

        if ("MERGED".equalsIgnoreCase(mode)) {
            return List.of(generateOrRefreshMergedPath(userId, jobIds));
        } else {
            return generateOrRefreshSeparatePaths(userId, jobIds);
        }
    }

    /**
     * 合并综合模式：V5 修正 — 查找已有合并路径复用
     */
    private LearningPath generateOrRefreshMergedPath(Long userId, List<Long> jobIds) {
        // V5 修正：查找已有合并路径（targetJobId == null 表示合并模式）
        LearningPath existingPath = findActiveMergedPath(userId);

        LearningPath path;
        if (existingPath != null) {
            path = existingPath;
            log.info("复用已有合并学习路径 pathId={}", path.getId());
        } else {
            path = createPathRecord(userId, null);
        }

        // 收集所有岗位技能
        List<Skill> skills = collectMergedSkills(jobIds);

        // 按多岗位差距排序
        if (!skills.isEmpty()) {
            skills = prioritizeByMultiGap(userId, jobIds, skills);
        }

        // 刷新任务
        refreshTasks(path.getId(), userId, skills);

        return path;
    }

    /**
     * 各岗位独立模式：V5 修正 — 逐个查找已有路径复用
     */
    private List<LearningPath> generateOrRefreshSeparatePaths(Long userId, List<Long> jobIds) {
        List<LearningPath> paths = new ArrayList<>();

        for (Long jobId : jobIds) {
            // V5 修正：每个岗位独立查找已有路径
            LearningPath existingPath = findActivePath(userId, jobId);

            LearningPath path;
            if (existingPath != null) {
                path = existingPath;
            } else {
                path = createPathRecord(userId, jobId);
            }

            List<Skill> skills = getSkillsForJob(jobId);
            if (!skills.isEmpty()) {
                skills = prioritizeWeakSkills(userId, jobId, skills);
            }

            refreshTasks(path.getId(), userId, skills);
            paths.add(path);
        }
        return paths;
    }

    // ==================== V5 核心：刷新任务（保留已有进度） ====================

    /**
     * 以 (skillId, stage) 为分组单位刷新任务
     * 同组内已有全部任务保留不动，只新增新分组的任务，标记废弃分组
     */
    private void refreshTasks(Long pathId, Long userId, List<Skill> newSkills) {
        // 1. 查询已有任务（排除已废弃的）
        List<LearningTask> existingTasks = taskMapper.selectList(
                new LambdaQueryWrapper<LearningTask>()
                        .eq(LearningTask::getPathId, pathId)
                        .ne(LearningTask::getStatus, "OBSOLETE"));

        // 2. 按 (skillId, stage) 分组
        Map<String, List<LearningTask>> existingGroupMap = existingTasks.stream()
                .collect(Collectors.groupingBy(t -> t.getSkillId() + "_" + t.getStage()));

        // 3. 遍历新技能组合
        int maxOrder = existingTasks.stream()
                .mapToInt(t -> t.getSortOrder() != null ? t.getSortOrder() : 0)
                .max().orElse(0) + 1;
        LocalDate today = LocalDate.now();
        Set<String> neededKeys = new HashSet<>();
        int newTaskCount = 0;

        for (String stage : STAGES) {
            for (Skill skill : newSkills) {
                String groupKey = skill.getId() + "_" + stage;
                neededKeys.add(groupKey);

                if (existingGroupMap.containsKey(groupKey)) {
                    // 该组已有任务 → 全部保留，不修改任何字段
                    for (LearningTask t : existingGroupMap.get(groupKey)) {
                        if (t.getSortOrder() != null) {
                            maxOrder = Math.max(maxOrder, t.getSortOrder() + 1);
                        }
                    }
                } else {
                    // 全新的 (skillId, stage) → 生成新任务
                    List<LearningResource> resources = findResourcesForSkill(skill.getId(), stage);
                    if (!resources.isEmpty()) {
                        for (LearningResource res : resources) {
                            insertNewTask(pathId, userId, skill, res, stage, "NOT_STARTED", maxOrder++, today);
                            newTaskCount++;
                        }
                    } else {
                        // 兜底：没有资源时自动生成默认任务
                        insertFallbackTask(pathId, userId, skill, stage, "NOT_STARTED", maxOrder++, today);
                        newTaskCount++;
                    }
                }
            }
        }

        // 4. 标记不需要的分组为 OBSOLETE
        int obsoleteCount = 0;
        for (Map.Entry<String, List<LearningTask>> entry : existingGroupMap.entrySet()) {
            if (!neededKeys.contains(entry.getKey())) {
                for (LearningTask t : entry.getValue()) {
                    t.setStatus("OBSOLETE");
                    taskMapper.updateById(t);
                    obsoleteCount++;
                }
            }
        }

        log.info("refreshTasks pathId={}: 保留 {} 个任务, 新增 {} 个任务, 废弃 {} 个任务",
                pathId,
                existingTasks.size() - obsoleteCount,
                newTaskCount,
                obsoleteCount);
    }

    /**
     * 查找技能在指定阶段的资源（V5 修正：移除 LIMIT 2）
     */
    private List<LearningResource> findResourcesForSkill(Long skillId, String stage) {
        return resourceMapper.selectList(
                new LambdaQueryWrapper<LearningResource>()
                        .eq(LearningResource::getSkillId, skillId)
                        .eq(LearningResource::getStage, stage));
    }

    /**
     * 插入新任务（关联资源）
     */
    private void insertNewTask(Long pathId, Long userId, Skill skill, LearningResource res,
                                String stage, String status, int order, LocalDate today) {
        LearningTask task = new LearningTask();
        task.setPathId(pathId);
        task.setUserId(userId);
        task.setSkillId(skill.getId());
        task.setTitle(res.getTitle());
        task.setDescription(res.getDescription());
        task.setResourceUrl(res.getUrl());
        task.setStage(stage);
        task.setStatus(status);
        task.setSortOrder(order);
        task.setDueDate(today.plusDays(order));
        task.setCreatedAt(LocalDateTime.now());
        taskMapper.insert(task);
    }

    /**
     * 插入兜底任务（无预设资源时的默认任务）
     */
    private void insertFallbackTask(Long pathId, Long userId, Skill skill,
                                     String stage, String status, int order, LocalDate today) {
        LearningTask task = new LearningTask();
        task.setPathId(pathId);
        task.setUserId(userId);
        task.setSkillId(skill.getId());
        task.setStage(stage);
        task.setStatus(status);
        task.setSortOrder(order);
        task.setDueDate(today.plusDays(order));
        task.setCreatedAt(LocalDateTime.now());

        // 生成搜索关键词和链接
        String searchKeyword = buildSearchKeyword(skill.getName(), stage);
        task.setTitle(getDefaultTaskTitle(skill.getName(), stage));
        task.setDescription("暂无预设学习资源，请通过搜索自主学习");
        task.setResourceUrl(buildSearchUrl(searchKeyword));

        taskMapper.insert(task);
    }

    private String buildSearchKeyword(String skillName, String stage) {
        Map<String, String> stageSuffix = Map.of(
                "BASIC", "入门教程",
                "FRAMEWORK", "进阶教程",
                "PROJECT", "实战项目",
                "INTERVIEW", "面试题"
        );
        return skillName + " " + stageSuffix.getOrDefault(stage, "教程");
    }

    private String buildSearchUrl(String keyword) {
        String template = searchUrlTemplate;
        return template.replace("{keyword}", URLEncoder.encode(keyword, StandardCharsets.UTF_8));
    }

    private String getDefaultTaskTitle(String skillName, String stage) {
        Map<String, String> stagePrefix = Map.of(
                "BASIC", "掌握", "FRAMEWORK", "精通",
                "PROJECT", "实战", "INTERVIEW", "面试准备"
        );
        return stagePrefix.getOrDefault(stage, "学习") + " " + skillName;
    }

    // ==================== 重新生成（归档后创建全新路径） ====================

    @Override
    public List<LearningPath> regenerateAll(Long userId, List<Long> jobIds, String mode) {
        // 归档旧路径
        archiveActivePaths(userId);

        // 创建全新路径
        if (jobIds == null || jobIds.isEmpty()) {
            return List.of(createNewPathInternal(userId, null));
        }
        if (jobIds.size() == 1) {
            return List.of(createNewPathInternal(userId, jobIds.get(0)));
        }

        if ("MERGED".equalsIgnoreCase(mode)) {
            return List.of(createNewMergedPathInternal(userId, jobIds));
        } else {
            return createNewSeparatePathsInternal(userId, jobIds);
        }
    }

    private LearningPath createNewPathInternal(Long userId, Long jobId) {
        LearningPath path = createPathRecord(userId, jobId);
        List<Skill> skills = getSkillsForJob(jobId);
        if (jobId != null && !skills.isEmpty()) {
            skills = prioritizeWeakSkills(userId, jobId, skills);
        }
        generateTasks(path, userId, skills);
        return path;
    }

    private LearningPath createNewMergedPathInternal(Long userId, List<Long> jobIds) {
        LearningPath path = createPathRecord(userId, null);
        List<Skill> skills = collectMergedSkills(jobIds);
        if (!skills.isEmpty()) {
            skills = prioritizeByMultiGap(userId, jobIds, skills);
        }
        generateTasks(path, userId, skills);
        return path;
    }

    private List<LearningPath> createNewSeparatePathsInternal(Long userId, List<Long> jobIds) {
        List<LearningPath> paths = new ArrayList<>();
        for (Long jobId : jobIds) {
            paths.add(createNewPathInternal(userId, jobId));
        }
        return paths;
    }

    // ==================== 查询辅助 ====================

    /**
     * 查找用户指定岗位的活跃路径
     */
    private LearningPath findActivePath(Long userId, Long jobId) {
        LambdaQueryWrapper<LearningPath> wrapper = new LambdaQueryWrapper<LearningPath>()
                .eq(LearningPath::getUserId, userId)
                .eq(LearningPath::getStatus, "ACTIVE");
        if (jobId != null) {
            wrapper.eq(LearningPath::getTargetJobId, jobId);
        } else {
            wrapper.isNull(LearningPath::getTargetJobId);
        }
        wrapper.orderByDesc(LearningPath::getCreatedAt).last("LIMIT 1");
        List<LearningPath> paths = pathMapper.selectList(wrapper);
        return paths.isEmpty() ? null : paths.get(0);
    }

    /**
     * 查找用户的合并活跃路径（targetJobId == null）
     */
    private LearningPath findActiveMergedPath(Long userId) {
        List<LearningPath> paths = pathMapper.selectList(new LambdaQueryWrapper<LearningPath>()
                .eq(LearningPath::getUserId, userId)
                .eq(LearningPath::getStatus, "ACTIVE")
                .isNull(LearningPath::getTargetJobId)
                .orderByDesc(LearningPath::getCreatedAt)
                .last("LIMIT 1"));
        return paths.isEmpty() ? null : paths.get(0);
    }

    // ==================== 技能获取与排序 ====================

    private List<Skill> getSkillsForJob(Long jobId) {
        if (jobId == null) {
            List<Long> skillIds = requirementMapper.selectList(null).stream()
                    .map(JobSkillRequirement::getSkillId).distinct().limit(8).collect(Collectors.toList());
            return skillIds.isEmpty() ? new ArrayList<>() : skillMapper.selectBatchIds(skillIds);
        }
        List<JobSkillRequirement> reqs = requirementMapper.selectList(
                new LambdaQueryWrapper<JobSkillRequirement>().eq(JobSkillRequirement::getJobId, jobId));
        List<Long> skillIds = reqs.stream().map(JobSkillRequirement::getSkillId).distinct().collect(Collectors.toList());
        return skillIds.isEmpty() ? new ArrayList<>() : skillMapper.selectBatchIds(skillIds);
    }

    private List<Skill> collectMergedSkills(List<Long> jobIds) {
        Set<Long> seen = new LinkedHashSet<>();
        for (Long jobId : jobIds) {
            List<JobSkillRequirement> reqs = requirementMapper.selectList(
                    new LambdaQueryWrapper<JobSkillRequirement>().eq(JobSkillRequirement::getJobId, jobId));
            reqs.stream().map(JobSkillRequirement::getSkillId).forEach(seen::add);
        }
        if (seen.isEmpty()) return new ArrayList<>();
        return skillMapper.selectBatchIds(new ArrayList<>(seen));
    }

    private List<Skill> prioritizeWeakSkills(Long userId, Long jobId, List<Skill> skills) {
        try {
            GapReportResponse report = gapAnalysisService.analyze(userId, jobId);
            return sortByGap(report, skills);
        } catch (Exception e) {
            log.warn("优先排序短板技能失败，使用默认顺序: {}", e.getMessage());
            return skills;
        }
    }

    private List<Skill> prioritizeByMultiGap(Long userId, List<Long> jobIds, List<Skill> skills) {
        try {
            GapReportResponse report = gapAnalysisService.analyzeMultiple(userId, jobIds);
            return sortByGap(report, skills);
        } catch (Exception e) {
            log.warn("多岗位差距排序失败，使用默认顺序: {}", e.getMessage());
            return skills;
        }
    }

    private List<Skill> sortByGap(GapReportResponse report, List<Skill> skills) {
        if (report == null || report.getGaps() == null) return skills;

        Set<String> criticalSkills = new LinkedHashSet<>();
        Set<String> warningSkills = new LinkedHashSet<>();

        for (var gap : report.getGaps()) {
            if ("严重不足".equals(gap.getGapDegree())) {
                criticalSkills.add(gap.getSkillName());
            } else if ("需要提升".equals(gap.getGapDegree())) {
                warningSkills.add(gap.getSkillName());
            }
        }

        if (criticalSkills.isEmpty() && warningSkills.isEmpty()) return skills;

        List<Skill> sorted = new ArrayList<>(skills);
        sorted.sort((a, b) -> {
            boolean aCritical = criticalSkills.contains(a.getName());
            boolean bCritical = criticalSkills.contains(b.getName());
            if (aCritical != bCritical) return aCritical ? -1 : 1;

            boolean aWarn = warningSkills.contains(a.getName());
            boolean bWarn = warningSkills.contains(b.getName());
            if (aWarn != bWarn) return aWarn ? -1 : 1;

            return 0;
        });
        return sorted;
    }

    // ==================== 任务生成（仅用于"重新生成"场景） ====================

    private void generateTasks(LearningPath path, Long userId, List<Skill> skills) {
        int order = 0;
        LocalDate today = LocalDate.now();
        for (String stage : STAGES) {
            for (Skill skill : skills) {
                List<LearningResource> resources = findResourcesForSkill(skill.getId(), stage);
                if (!resources.isEmpty()) {
                    for (LearningResource res : resources) {
                        insertNewTask(path.getId(), userId, skill, res, stage, "NOT_STARTED", order++, today);
                    }
                } else {
                    insertFallbackTask(path.getId(), userId, skill, stage, "NOT_STARTED", order++, today);
                }
            }
        }
    }

    // ==================== 路径管理 ====================

    private LearningPath createPathRecord(Long userId, Long targetJobId) {
        LearningPath path = new LearningPath();
        path.setUserId(userId);
        path.setTargetJobId(targetJobId);
        path.setDailyHours(2.0);
        path.setTotalDays(30);
        path.setStatus("ACTIVE");
        path.setCreatedAt(LocalDateTime.now());
        pathMapper.insert(path);
        return path;
    }

    private void archiveActivePaths(Long userId) {
        List<LearningPath> activePaths = pathMapper.selectList(
                new LambdaQueryWrapper<LearningPath>()
                        .eq(LearningPath::getUserId, userId)
                        .eq(LearningPath::getStatus, "ACTIVE"));
        for (LearningPath p : activePaths) {
            p.setStatus("ARCHIVED");
            pathMapper.updateById(p);
        }
        log.info("归档了 {} 条活跃路径, userId={}", activePaths.size(), userId);
    }

    // ==================== 路径查询 ====================

    @Override
    public LearningPath getPath(Long userId) {
        List<LearningPath> paths = pathMapper.selectList(new LambdaQueryWrapper<LearningPath>()
                .eq(LearningPath::getUserId, userId)
                .eq(LearningPath::getStatus, "ACTIVE")
                .last("LIMIT 1"));
        return paths.isEmpty() ? null : paths.get(0);
    }

    @Override
    public List<LearningPath> getPaths(Long userId) {
        List<LearningPath> paths = pathMapper.selectList(new LambdaQueryWrapper<LearningPath>()
                .eq(LearningPath::getUserId, userId)
                .eq(LearningPath::getStatus, "ACTIVE")
                .orderByDesc(LearningPath::getCreatedAt));
        // 按 jobId=null 排最后（合并路径放后面）
        paths.sort((a, b) -> {
            boolean aNull = a.getTargetJobId() == null;
            boolean bNull = b.getTargetJobId() == null;
            if (aNull != bNull) return aNull ? 1 : -1;
            return 0;
        });
        return paths;
    }

    // ==================== 路径统计（V5 新增） ====================

    @Override
    public List<PathStatsDTO> getPathListWithStats(Long userId) {
        List<LearningPath> paths = getPaths(userId);

        return paths.parallelStream().map(path -> {
            List<LearningTask> tasks = taskMapper.selectList(
                    new LambdaQueryWrapper<LearningTask>()
                            .eq(LearningTask::getPathId, path.getId())
                            .eq(LearningTask::getUserId, userId)
                            .ne(LearningTask::getStatus, "OBSOLETE"));

            int total = tasks.size();
            long completed = tasks.stream()
                    .filter(t -> "LEARNING_COMPLETED".equals(t.getStatus())
                            || "TEST_PASSED".equals(t.getStatus())).count();
            int progress = total > 0 ? (int) (completed * 100 / total) : 0;

            // 各阶段进度
            Map<String, Integer> stageProgress = new LinkedHashMap<>();
            for (String s : STAGES) {
                long stageTotal = tasks.stream().filter(t -> s.equals(t.getStage())).count();
                long stageDone = tasks.stream()
                        .filter(t -> s.equals(t.getStage())
                                && ("LEARNING_COMPLETED".equals(t.getStatus())
                                || "TEST_PASSED".equals(t.getStatus())))
                        .count();
                stageProgress.put(s, stageTotal > 0 ? (int) (stageDone * 100 / stageTotal) : 0);
            }

            // 获取岗位名称
            String jobTitle = null;
            if (path.getTargetJobId() != null) {
                JobPosition job = jobPositionMapper.selectById(path.getTargetJobId());
                jobTitle = job != null ? job.getTitle() : null;
            } else {
                jobTitle = "综合学习路径";
            }

            PathStatsDTO dto = new PathStatsDTO();
            dto.setPathId(path.getId());
            dto.setJobTitle(jobTitle);
            dto.setMode(path.getTargetJobId() == null ? "MERGED" : "SINGLE");
            dto.setTotalTasks(total);
            dto.setCompletedTasks((int) completed);
            dto.setOverallProgress(progress);
            dto.setStageProgress(stageProgress);
            dto.setCreatedAt(path.getCreatedAt());
            return dto;
        }).toList();
    }

    @Override
    public Map<String, Object> getPathMeta(Long pathId) {
        LearningPath path = pathMapper.selectById(pathId);
        if (path == null) return Map.of();

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("pathId", path.getId());

        Long targetJobId = path.getTargetJobId();
        if (targetJobId != null) {
            JobPosition job = jobPositionMapper.selectById(targetJobId);
            meta.put("jobTitle", job != null ? job.getTitle() : "未知岗位");
            meta.put("mode", "SINGLE");
        } else {
            meta.put("mode", "MERGED");
            meta.put("jobTitle", "综合学习路径");
        }
        return meta;
    }

    // ==================== V5 新增：技能-岗位矩阵 ====================

    @Override
    public List<SkillsMatrixDTO> getPathSkillsMatrix(Long pathId) {
        // 查询路径所有任务的技能ID
        List<LearningTask> tasks = taskMapper.selectList(
                new LambdaQueryWrapper<LearningTask>()
                        .eq(LearningTask::getPathId, pathId)
                        .ne(LearningTask::getStatus, "OBSOLETE"));

        Set<Long> skillIds = tasks.stream()
                .map(LearningTask::getSkillId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (skillIds.isEmpty()) return List.of();

        List<Skill> skills = skillMapper.selectBatchIds(new ArrayList<>(skillIds));
        Map<Long, String> skillNameMap = skills.stream()
                .collect(Collectors.toMap(Skill::getId, Skill::getName, (a, b) -> a));

        // 查询每个技能来自哪些岗位
        List<JobSkillRequirement> allReqs = requirementMapper.selectList(
                new LambdaQueryWrapper<JobSkillRequirement>()
                        .in(JobSkillRequirement::getSkillId, new ArrayList<>(skillIds)));
        Map<Long, Set<Long>> skillToJobs = new HashMap<>();
        for (JobSkillRequirement req : allReqs) {
            skillToJobs.computeIfAbsent(req.getSkillId(), k -> new LinkedHashSet<>()).add(req.getJobId());
        }

        // 获取岗位名称映射
        Set<Long> allJobIds = allReqs.stream().map(JobSkillRequirement::getJobId).collect(Collectors.toSet());
        Map<Long, String> jobNameMap = new HashMap<>();
        if (!allJobIds.isEmpty()) {
            List<JobPosition> jobs = jobPositionMapper.selectBatchIds(new ArrayList<>(allJobIds));
            jobs.forEach(j -> jobNameMap.put(j.getId(), j.getTitle()));
        }

        List<SkillsMatrixDTO> result = new ArrayList<>();
        for (Long skillId : skillIds) {
            SkillsMatrixDTO dto = new SkillsMatrixDTO();
            dto.setSkillId(skillId);
            dto.setSkillName(skillNameMap.getOrDefault(skillId, "未知技能"));
            String category = skills.stream()
                    .filter(s -> s.getId().equals(skillId))
                    .findFirst().map(Skill::getCategory).orElse(null);
            dto.setCategory(category);
            dto.setSourceJobs(
                    skillToJobs.getOrDefault(skillId, Set.of()).stream()
                            .map(jobId -> jobNameMap.getOrDefault(jobId, "岗位#" + jobId))
                            .toList()
            );
            result.add(dto);
        }
        return result;
    }

    // ==================== 任务操作 ====================

    @Override
    public void updateTaskStatus(Long taskId, String status) {
        LearningTask task = taskMapper.selectById(taskId);
        if (task != null) {
            String oldStatus = task.getStatus();
            task.setStatus(status);
            taskMapper.updateById(task);

            // 进度互通：只同步 TEST_PASSED 状态到其他路径的同技能任务
            if ("TEST_PASSED".equals(status) && !"TEST_PASSED".equals(oldStatus)) {
                syncSameSkillTasks(task.getUserId(), task.getSkillId(), task.getStage(), "TEST_PASSED");
            }
        }
    }

    /**
     * 同步所有活跃路径中同(skillId, stage)的任务状态
     * 实现进度互通：一个路径里的技能测试通过了，其他路径里的同技能任务也标记为完成
     */
    private void syncSameSkillTasks(Long userId, Long skillId, String stage, String newStatus) {
        // 1. 找出该用户所有活跃路径的 pathId
        List<Long> activePathIds = pathMapper.selectList(
                new LambdaQueryWrapper<LearningPath>()
                        .eq(LearningPath::getUserId, userId)
                        .eq(LearningPath::getStatus, "ACTIVE"))
                .stream().map(LearningPath::getId).toList();

        if (activePathIds.isEmpty()) return;

        // 2. 找出所有同(skillId, stage)且未废弃的任务
        List<LearningTask> siblingTasks = taskMapper.selectList(
                new LambdaQueryWrapper<LearningTask>()
                        .in(LearningTask::getPathId, activePathIds)
                        .eq(LearningTask::getSkillId, skillId)
                        .eq(LearningTask::getStage, stage)
                        .ne(LearningTask::getStatus, "OBSOLETE"));

        // 3. 批量同步状态
        for (LearningTask t : siblingTasks) {
            if (!"TEST_PASSED".equals(t.getStatus())) {  // 避免重复更新
                t.setStatus(newStatus);
                taskMapper.updateById(t);
            }
        }
        log.info("同步同技能任务: userId={}, skillId={}, stage={}, 同步{}条",
                userId, skillId, stage, siblingTasks.size());
    }

    @Override
    public List<LearningTask> getTasks(Long userId) {
        List<Long> activePathIds = pathMapper.selectList(
                new LambdaQueryWrapper<LearningPath>()
                        .eq(LearningPath::getUserId, userId)
                        .eq(LearningPath::getStatus, "ACTIVE"))
                .stream().map(LearningPath::getId).collect(Collectors.toList());

        if (activePathIds.isEmpty()) return List.of();

        return taskMapper.selectList(new LambdaQueryWrapper<LearningTask>()
                .in(LearningTask::getPathId, activePathIds)
                .eq(LearningTask::getUserId, userId)
                .ne(LearningTask::getStatus, "OBSOLETE")
                .orderByAsc(LearningTask::getSortOrder));
    }

    @Override
    public List<LearningTask> getTasks(Long userId, Long pathId) {
        return taskMapper.selectList(new LambdaQueryWrapper<LearningTask>()
                .eq(LearningTask::getPathId, pathId)
                .eq(LearningTask::getUserId, userId)
                .ne(LearningTask::getStatus, "OBSOLETE")
                .orderByAsc(LearningTask::getSortOrder));
    }

    @Override
    public LearningTask getTask(Long taskId, Long userId) {
        return taskMapper.selectOne(new LambdaQueryWrapper<LearningTask>()
                .eq(LearningTask::getId, taskId)
                .eq(LearningTask::getUserId, userId));
    }

    @Override
    public List<LearningResource> listResources(Long skillId, String stage) {
        LambdaQueryWrapper<LearningResource> wrapper = new LambdaQueryWrapper<>();
        if (skillId != null) wrapper.eq(LearningResource::getSkillId, skillId);
        if (stage != null && !stage.isEmpty()) wrapper.eq(LearningResource::getStage, stage);
        return resourceMapper.selectList(wrapper);
    }
}
