package com.xuelian.career.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuelian.career.entity.AssessmentDimensionConfig;
import com.xuelian.career.entity.AssessmentSummaryConfig;
import com.xuelian.career.mapper.AssessmentDimensionConfigMapper;
import com.xuelian.career.mapper.AssessmentSummaryConfigMapper;
import com.xuelian.career.service.AssessmentConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 测评配置服务实现
 * - 数据库未命中时使用内置默认配置兜底，避免服务不可用
 * - 本地缓存 5 分钟，配置低频变更
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentConfigServiceImpl implements AssessmentConfigService {

    private final AssessmentDimensionConfigMapper dimensionConfigMapper;
    private final AssessmentSummaryConfigMapper summaryConfigMapper;
    private final ObjectMapper objectMapper;

    /** 维度配置缓存：key = dimensionCode，value = 该维度全部分段配置 */
    private volatile Map<String, List<AssessmentDimensionConfig>> dimConfigCache = new ConcurrentHashMap<>();
    /** 综合评语模板缓存 */
    private volatile List<AssessmentSummaryConfig> summaryConfigCache = new ArrayList<>();
    /** 缓存写入时间戳 */
    private volatile long cacheTimestamp = 0L;
    /** 缓存有效期 5 分钟 */
    private static final long CACHE_TTL_MS = 5 * 60 * 1000L;

    @Override
    public AssessmentDimensionConfig getConfig(String dimensionCode, int score) {
        List<AssessmentDimensionConfig> configs = getDimConfigs(dimensionCode);
        if (configs != null) {
            for (AssessmentDimensionConfig c : configs) {
                if (score >= c.getScoreMin() && score <= c.getScoreMax()) {
                    return c;
                }
            }
        }
        // 数据库未命中，使用内置默认配置兜底
        log.warn("维度 [{}] 分数 [{}] 未命中数据库配置，使用内置默认配置兜底", dimensionCode, score);
        return buildDefaultConfig(dimensionCode, score);
    }

    @Override
    public AssessmentSummaryConfig getSummaryConfig(int totalScore, int strengthCount, int weaknessCount) {
        List<AssessmentSummaryConfig> configs = getSummaryConfigs();
        if (configs != null) {
            // 优先匹配：总分范围 + 优势维度数 + 薄弱维度数 三条件全中
            for (AssessmentSummaryConfig c : configs) {
                if (!inRange(totalScore, c.getTotalScoreMin(), c.getTotalScoreMax())) continue;
                if (c.getStrengthCountMin() != null && (strengthCount < c.getStrengthCountMin()
                        || strengthCount > c.getStrengthCountMax())) continue;
                if (c.getWeaknessCountMin() != null && (weaknessCount < c.getWeaknessCountMin()
                        || weaknessCount > c.getWeaknessCountMax())) continue;
                return c;
            }
            // 次级匹配：仅总分范围
            for (AssessmentSummaryConfig c : configs) {
                if (inRange(totalScore, c.getTotalScoreMin(), c.getTotalScoreMax())
                        && c.getStrengthCountMin() == null && c.getWeaknessCountMin() == null) {
                    return c;
                }
            }
        }
        log.warn("总分 [{}] 优势数 [{}] 薄弱数 [{}] 未命中综合评语模板，由调用方默认逻辑兜底",
                totalScore, strengthCount, weaknessCount);
        return null;
    }

    @Override
    public void refreshCache() {
        cacheTimestamp = 0L;
        log.info("测评配置缓存已标记刷新");
    }

    // ==================== 内部方法 ====================

    private List<AssessmentDimensionConfig> getDimConfigs(String dimensionCode) {
        ensureCacheFresh();
        return dimConfigCache.get(dimensionCode);
    }

    private List<AssessmentSummaryConfig> getSummaryConfigs() {
        ensureCacheFresh();
        return summaryConfigCache;
    }

    /**
     * 缓存失效则重新加载
     */
    private synchronized void ensureCacheFresh() {
        if (System.currentTimeMillis() - cacheTimestamp < CACHE_TTL_MS && !dimConfigCache.isEmpty()) {
            return;
        }
        try {
            // 加载维度配置
            List<AssessmentDimensionConfig> dimConfigs = dimensionConfigMapper.selectList(
                    new LambdaQueryWrapper<AssessmentDimensionConfig>()
                            .eq(AssessmentDimensionConfig::getIsDeleted, 0)
                            .orderByAsc(AssessmentDimensionConfig::getDimensionCode)
                            .orderByAsc(AssessmentDimensionConfig::getSortOrder));
            Map<String, List<AssessmentDimensionConfig>> dimMap = new ConcurrentHashMap<>();
            for (AssessmentDimensionConfig c : dimConfigs) {
                dimMap.computeIfAbsent(c.getDimensionCode(), k -> new ArrayList<>()).add(c);
            }
            // 加载综合评语模板
            List<AssessmentSummaryConfig> summaryConfigs = summaryConfigMapper.selectList(
                    new LambdaQueryWrapper<AssessmentSummaryConfig>()
                            .eq(AssessmentSummaryConfig::getIsDeleted, 0)
                            .orderByAsc(AssessmentSummaryConfig::getTotalScoreMin));

            this.dimConfigCache = dimMap;
            this.summaryConfigCache = summaryConfigs != null ? summaryConfigs : new ArrayList<>();
            this.cacheTimestamp = System.currentTimeMillis();
            log.info("测评配置缓存加载完成：维度配置 {} 组，综合评语模板 {} 条",
                    dimMap.size(), this.summaryConfigCache.size());
        } catch (Exception e) {
            log.error("测评配置缓存加载失败，使用空缓存兜底: {}", e.getMessage(), e);
            if (dimConfigCache.isEmpty()) {
                dimConfigCache = new ConcurrentHashMap<>();
            }
            if (summaryConfigCache == null) {
                summaryConfigCache = new ArrayList<>();
            }
            // 失败时也更新时间戳，避免每次查询都打 DB
            cacheTimestamp = System.currentTimeMillis();
        }
    }

    private boolean inRange(int value, Integer min, Integer max) {
        if (min == null || max == null) return true;
        return value >= min && value <= max;
    }

    /**
     * 内置默认配置兜底（数据库未初始化或异常时使用）
     * 提供极简但可用的文案，避免服务不可用
     */
    private AssessmentDimensionConfig buildDefaultConfig(String dimensionCode, int score) {
        AssessmentDimensionConfig config = new AssessmentDimensionConfig();
        config.setDimensionCode(dimensionCode);
        config.setDimensionName(getDefaultDimName(dimensionCode));
        config.setLevelType(getDefaultLevelType(score));
        config.setScoreMin(Math.max(0, score - score % 30));
        config.setScoreMax(config.getScoreMin() + 29);
        config.setLevelLabel(getLevelLabel(score));
        config.setAnalysis(String.format("当前 %s 得分 %d 分，整体尚有成长空间，建议针对性提升。",
                config.getDimensionName(), score));
        config.setCompensateStrategy(null);
        config.setApplicationTemplates(null);
        config.setEntryTasks("[\"针对性补强基础概念\", \"每日练习相关题目 3 道\"]");
        config.setAdvanceTasks("[\"完成一个小型实战项目\", \"阅读相关领域的入门书籍\"]");
        config.setPracticeTasks("[\"参与团队协作项目\", \"输出一份学习总结文档\"]");
        config.setEntryDuration("2-3周");
        config.setAdvanceDuration("3-4周");
        config.setPracticeDuration("持续");
        config.setTone("ENCOURAGING");
        config.setSortOrder(1);
        config.setIsDeleted(0);
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        return config;
    }

    private String getDefaultDimName(String code) {
        return switch (code) {
            case "PROGRAMMING" -> "编程能力";
            case "LOGIC" -> "逻辑推理";
            case "PRODUCT" -> "产品思维";
            case "TECH" -> "技术素养";
            case "COMMUNICATION" -> "沟通表达";
            default -> code;
        };
    }

    private String getDefaultLevelType(int score) {
        if (score >= 85) return "ELITE";
        if (score >= 70) return "HIGH";
        if (score >= 55) return "MID";
        if (score >= 30) return "LOW";
        return "ZERO";
    }

    private String getLevelLabel(int score) {
        if (score >= 85) return "优秀";
        if (score >= 70) return "良好";
        if (score >= 55) return "一般";
        return "待提升";
    }
}
