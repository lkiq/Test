package com.xuelian.career.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 未读消息数响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnreadCountResponse {
    private Integer total;
}
