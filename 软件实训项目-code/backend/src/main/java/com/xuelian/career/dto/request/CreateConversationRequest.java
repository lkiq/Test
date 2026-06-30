package com.xuelian.career.dto.request;

import lombok.Data;

/**
 * 创建会话请求
 */
@Data
public class CreateConversationRequest {
    /** 岗位ID */
    private Long jobId;
    /** HR用户ID */
    private Long hrId;
    /** 岗位名称 */
    private String jobTitle;
    /** 企业名称 */
    private String enterpriseName;
}
