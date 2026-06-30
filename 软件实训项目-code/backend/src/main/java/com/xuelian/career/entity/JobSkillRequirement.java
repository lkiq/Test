package com.xuelian.career.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 岗位技能要求表 - 关联岗位与所需技能及要求等级
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("job_skill_requirement")
public class JobSkillRequirement {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联岗位ID */
    private Long jobId;

    /** 关联技能ID */
    private Long skillId;

    /** 要求等级：了解/掌握/熟练/精通 */
    private String requiredLevel;

    /** 该技能在此岗位中的权重 */
    private Double weight;
}
