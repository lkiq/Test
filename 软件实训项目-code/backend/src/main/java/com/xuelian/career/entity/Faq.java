package com.xuelian.career.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 常见问题表（FAQ） - 智能客服知识库
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("faq")
public class Faq {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分类：PLATFORM/POSITION/LEARNING */
    private String category;

    /** 问题 */
    private String question;

    /** 标准答案 */
    private String answer;

    /** 关键词（用于匹配） */
    private String keywords;

    /** 排序编号 */
    private Integer sortOrder;

    /** 逻辑删除标记 */
    @TableLogic
    private Integer isDeleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
