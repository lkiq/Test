package com.xuelian.career.service;

import com.xuelian.career.entity.AssessmentDimensionConfig;
import com.xuelian.career.entity.AssessmentSummaryConfig;

import java.util.List;

/**
 * 能力测评配置服务接口
 * 提供按维度+分数查询分段配置、按总分+优势/薄弱数查询综合评语模板的能力
 */
public interface AssessmentConfigService {

    /**
     * 根据维度代码和分数返回最匹配的分数段配置
     * @param dimensionCode 维度代码：PROGRAMMING/LOGIC/PRODUCT/TECH/COMMUNICATION
     * @param score 分数（0-100）
     * @return 匹配的配置；若数据库未命中则返回内置默认配置（避免服务不可用）
     */
    AssessmentDimensionConfig getConfig(String dimensionCode, int score);

    /**
     * 根据总分、优势维度数、薄弱维度数匹配综合评语模板
     * @param totalScore 总分（0-100）
     * @param strengthCount 优势维度数
     * @param weaknessCount 薄弱维度数
     * @return 匹配的模板；若未命中则返回 null（由调用方走原有默认逻辑兜底）
     */
    AssessmentSummaryConfig getSummaryConfig(int totalScore, int strengthCount, int weaknessCount);

    /**
     * 刷新本地缓存（管理端调用）
     */
    void refreshCache();
}
