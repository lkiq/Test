package com.xuelian.career.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 技能词典表 - 维护平台技能标签库，供画像、岗位、学习资源使用
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("skill")
public class Skill {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 技能名称（唯一） */
    private String name;

    /** 技能类别：编程语言/框架/数据库/工具/软技能 */
    private String category;

    /** 技能描述 */
    private String description;

    /** 逻辑删除标记 */
    @TableLogic
    private Integer isDeleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
