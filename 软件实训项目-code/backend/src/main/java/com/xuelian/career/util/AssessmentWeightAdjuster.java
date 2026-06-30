package com.xuelian.career.util;

import com.xuelian.career.entity.AssessmentResult;
import com.xuelian.career.entity.JobPosition;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 岗位-测评维度权重调节器
 * 根据岗位方向计算该岗位与用户五维得分的匹配比例，避免低总分用户在所有技术岗上匹配度崩塌。
 *
 * 设计思路：
 * - 不同岗位方向对五维度的权重不同：后端/算法类岗位更看重编程+逻辑；
 *   产品/技术支持类岗位更看重产品+沟通。
 * - 计算 weightedDimScore ∈ [0, 100]，最终返回 max(0.3, weightedDimScore/100.0)
 *   保留最低 0.3 兜底，与原 assessRatio 区间保持一致。
 *
 * 维度权重映射表（硬编码，先以规则验证业务效果，后续若岗位类型增多再考虑配置化）：
 * | 维度          | 高权重岗位关键词                          | 中权重岗位关键词          |
 * |--------------|------------------------------------------|--------------------------|
 * | PROGRAMMING  | 后端、算法、数据、全栈、开发工程师          | 前端、测试、运维          |
 * | LOGIC        | 算法、数据、后端                          | 测试、产品                |
 * | PRODUCT      | 产品、运营、用户、需求                     | 前端、测试                |
 * | TECH         | 所有含技术/工程/开发/运维岗位              | 产品、运营                |
 * | COMMUNICATION| 产品、前端、技术支持、项目经理             | 测试、运维                |
 */
@Slf4j
public final class AssessmentWeightAdjuster {

    private AssessmentWeightAdjuster() {}

    /** 维度权重：高 0.4 / 中 0.2 / 低 0.1，五维度合计不超过 1.0 */
    private static final double WEIGHT_HIGH = 0.4;
    private static final double WEIGHT_MID = 0.2;
    private static final double WEIGHT_LOW = 0.1;

    /**
     * 根据岗位方向与测评结果计算该岗位的 assessRatio
     * @param job 岗位（依据 title + direction 判断方向）
     * @param result 测评结果（含五维分数），为 null 时返回默认 0.5
     * @return assessRatio ∈ [0.3, 1.0]
     */
    public static double assessRatioForJob(JobPosition job, AssessmentResult result) {
        if (result == null) return 0.5;
        if (job == null) {
            // 无岗位信息时退化为原逻辑：按总分
            return Math.max(0.3, safeScore(result.getTotalScore()) / 100.0);
        }

        String title = job.getTitle() != null ? job.getTitle() : "";
        String direction = job.getDirection() != null ? job.getDirection() : "";
        String hay = (title + " " + direction).toLowerCase();

        // 各维度权重
        double wProg = weightForDim(hay, "PROGRAMMING");
        double wLogic = weightForDim(hay, "LOGIC");
        double wProduct = weightForDim(hay, "PRODUCT");
        double wTech = weightForDim(hay, "TECH");
        double wComm = weightForDim(hay, "COMMUNICATION");

        double sumW = wProg + wLogic + wProduct + wTech + wComm;
        if (sumW <= 0) {
            // 兜底：按总分
            return Math.max(0.3, safeScore(result.getTotalScore()) / 100.0);
        }

        // 加权维度得分
        double weighted = (wProg * safeScore(result.getProgrammingScore())
                + wLogic * safeScore(result.getLogicScore())
                + wProduct * safeScore(result.getProductScore())
                + wTech * safeScore(result.getTechScore())
                + wComm * safeScore(result.getCommunicationScore())) / sumW;

        double ratio = Math.max(0.3, weighted / 100.0);
        log.debug("岗位 [{}] 维度加权: weights=(P={},L={},Pr={},T={},C={}), weightedScore={}, assessRatio={}",
                title, wProg, wLogic, wProduct, wTech, wComm, weighted, ratio);
        return ratio;
    }

    /**
     * 根据岗位方向关键词判断指定维度的权重
     */
    private static double weightForDim(String hay, String dimension) {
        switch (dimension) {
            case "PROGRAMMING":
                // 高权重：后端、算法、数据、全栈、开发工程师
                if (containsAny(hay, "后端", "算法", "数据", "全栈", "开发工程师", "backend", "developer")) {
                    return WEIGHT_HIGH;
                }
                // 中权重：前端、测试、运维
                if (containsAny(hay, "前端", "测试", "运维", "frontend", "test", "devops")) {
                    return WEIGHT_MID;
                }
                return WEIGHT_LOW;

            case "LOGIC":
                // 高权重：算法、数据、后端
                if (containsAny(hay, "算法", "数据", "后端", "algorithm")) {
                    return WEIGHT_HIGH;
                }
                // 中权重：测试、产品
                if (containsAny(hay, "测试", "产品", "test", "product")) {
                    return WEIGHT_MID;
                }
                return WEIGHT_LOW;

            case "PRODUCT":
                // 高权重：产品、运营、用户、需求
                if (containsAny(hay, "产品", "运营", "用户", "需求", "product", "operation")) {
                    return WEIGHT_HIGH;
                }
                // 中权重：前端、测试
                if (containsAny(hay, "前端", "测试", "frontend", "test")) {
                    return WEIGHT_MID;
                }
                return WEIGHT_LOW;

            case "TECH":
                // 高权重：所有含技术/工程/开发/运维岗位
                if (containsAny(hay, "技术", "工程", "开发", "运维", "engineer", "developer", "devops")) {
                    return WEIGHT_HIGH;
                }
                // 中权重：产品、运营
                if (containsAny(hay, "产品", "运营", "product", "operation")) {
                    return WEIGHT_MID;
                }
                return WEIGHT_LOW;

            case "COMMUNICATION":
                // 高权重：产品、前端、技术支持、项目经理
                if (containsAny(hay, "产品", "前端", "技术支持", "项目经理", "product", "frontend", "support", "manager")) {
                    return WEIGHT_HIGH;
                }
                // 中权重：测试、运维
                if (containsAny(hay, "测试", "运维", "test", "devops")) {
                    return WEIGHT_MID;
                }
                return WEIGHT_LOW;

            default:
                return WEIGHT_LOW;
        }
    }

    private static boolean containsAny(String hay, String... keywords) {
        if (hay == null || hay.isEmpty()) return false;
        for (String kw : keywords) {
            if (kw != null && hay.contains(kw.toLowerCase())) return true;
        }
        return false;
    }

    private static double safeScore(Double score) {
        return score == null ? 0.0 : score;
    }
}
