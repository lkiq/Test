package com.xuelian.career.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 简历文件表 - 存储用户上传的简历文件记录（仅保存，不做分析）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("resume_file")
public class ResumeFile {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 原始文件名 */
    private String fileName;

    /** 文件相对路径 */
    private String fileUrl;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 文件类型（pdf/docx） */
    private String fileType;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
