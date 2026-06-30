package com.xuelian.career.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 推荐记录表 - 通用记录表，存储职业探索/岗位匹配/差距分析/企业推荐/成长报告等各类推荐结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("recommendation_record")
public class RecommendationRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 推荐类型：CAREER_EXPLORATION/JOB_MATCH/GAP_ANALYSIS/ENTERPRISE_REC/GROWTH_REPORT */
    private String type;

    /** 输入文本（用户的偏好/岗位ID等） */
    private String inputText;

    /** 推荐结果详情（JSON） */
    private String resultJson;

    /** 结果来源：AI/FALLBACK/RULE/CACHE */
    private String source;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
