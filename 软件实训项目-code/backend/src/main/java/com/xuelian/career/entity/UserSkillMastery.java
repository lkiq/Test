package com.xuelian.career.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 用户技能掌握记录表 - 持久化用户对技能的学习进度和掌握度
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_skill_mastery")
public class UserSkillMastery {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 技能ID */
    private Long skillId;

    /** 技能名称（冗余以便查询） */
    private String skillName;

    /** 掌握等级：BASIC(了解)/INTERMEDIATE(掌握)/ADVANCED(熟练)/EXPERT(精通) */
    private String level;

    /** 来源：LEARNING(学完任务)/TEST(通过测试) */
    private String source;

    /** 首次掌握日期 */
    private LocalDateTime firstMasteredAt;

    /** 复习次数 */
    private Integer reviewCount;

    /** 上次复习时间 */
    private LocalDateTime lastReviewedAt;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
