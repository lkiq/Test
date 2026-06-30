package com.xuelian.career.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 企业信息表 - 存储HR用户关联的企业信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("enterprise")
public class Enterprise {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的HR用户ID */
    private Long hrUserId;

    /** 公司名称 */
    private String companyName;

    /** 所属行业 */
    private String companyIndustry;

    /** 公司规模 */
    private String companySize;

    /** 公司地址 */
    private String companyAddress;

    /** 公司简介 */
    private String companyDescription;

    /** 联系人姓名 */
    private String contactName;

    /** 联系人职位 */
    private String contactPosition;

    /** 认证状态：PENDING/APPROVED/REJECTED */
    private String verifyStatus;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
