package com.xuelian.career.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 测评结果表 - 存储用户的能力测评报告
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("assessment_result")
public class AssessmentResult {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 测评类型 */
    private String type;

    /** 编程能力得分 */
    private Double programmingScore;

    /** 逻辑推理得分 */
    private Double logicScore;

    /** 产品思维得分 */
    private Double productScore;

    /** 技术素养得分 */
    private Double techScore;

    /** 沟通表达得分 */
    private Double communicationScore;

    /** 综合总分 */
    private Double totalScore;

    /** 详细测评结果（JSON） */
    private String resultJson;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
