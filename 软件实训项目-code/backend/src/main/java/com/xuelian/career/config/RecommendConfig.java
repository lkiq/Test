package com.xuelian.career.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 人才推荐评分权重配置 - 从 application.yml 读取 recommend.* 评分权重；
 * 测评维度映射在 @PostConstruct 中硬编码（避免 Spring Boot 对中文 YAML key
 * 的 Map<String, Map<...>> 绑定兼容性问题）
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "recommend")
public class RecommendConfig {

    /** 评分权重配置 */
    private Scoring scoring = new Scoring();

    /** 岗位类型 → 测评维度权重映射 */
    private final Map<String, Map<String, Double>> dimensionMapping = new LinkedHashMap<>();

    @Data
    public static class Scoring {
        /** 技能匹配权重（降低自报偏差影响） */
        private double weightSkill = 0.30;
        /** 测评适配权重 */
        private double weightAssessment = 0.25;
        /** 学习完成权重 */
        private double weightLearning = 0.15;
        /** 学习成果测评权重 */
        private double weightLearningResult = 0.20;
        /** 基础匹配权重 */
        private double weightBasic = 0.10;
    }

    /** 初始化测评维度映射（硬编码，后续可迁移到数据库配置） */
    public RecommendConfig() {
        // 后端开发工程师
        initDim("后端开发工程师", 0.35, 0.25, 0.10, 0.20, 0.10);
        // 前端开发工程师
        initDim("前端开发工程师", 0.25, 0.20, 0.20, 0.20, 0.15);
        // 产品经理
        initDim("产品经理",       0.10, 0.20, 0.40, 0.15, 0.15);
        // 数据分析师
        initDim("数据分析师",     0.20, 0.30, 0.15, 0.25, 0.10);
        // 测试工程师
        initDim("测试工程师",     0.20, 0.25, 0.10, 0.25, 0.20);
        // 运营专员
        initDim("运营专员",       0.10, 0.15, 0.30, 0.10, 0.35);
        // 算法工程师
        initDim("算法工程师",     0.30, 0.30, 0.10, 0.20, 0.10);
        // 移动端开发工程师
        initDim("移动端开发工程师", 0.30, 0.20, 0.15, 0.25, 0.10);
        // 默认
        initDim("DEFAULT",        0.20, 0.20, 0.20, 0.20, 0.20);
    }

    private void initDim(String role, double prog, double logic, double prod, double tech, double comm) {
        Map<String, Double> weights = new LinkedHashMap<>();
        weights.put("programming", prog);
        weights.put("logic", logic);
        weights.put("product", prod);
        weights.put("tech", tech);
        weights.put("communication", comm);
        dimensionMapping.put(role, weights);
    }

    /**
     * 获取指定岗位类型的测评维度权重映射，不存在则返回默认
     */
    public Map<String, Double> getDimensionWeights(String positionType) {
        Map<String, Double> weights = dimensionMapping.get(positionType);
        if (weights != null) return weights;
        weights = dimensionMapping.get("DEFAULT");
        if (weights != null) return weights;
        // 硬编码兜底
        Map<String, Double> defaultWeights = new LinkedHashMap<>();
        defaultWeights.put("programming", 0.20);
        defaultWeights.put("logic", 0.20);
        defaultWeights.put("product", 0.20);
        defaultWeights.put("tech", 0.20);
        defaultWeights.put("communication", 0.20);
        return defaultWeights;
    }
}
