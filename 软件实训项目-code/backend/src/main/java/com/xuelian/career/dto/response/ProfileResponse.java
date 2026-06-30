package com.xuelian.career.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 求职画像响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private Long id;
    private Long userId;
    private String school;
    private String major;
    private String education;
    private String grade;
    private String skillTags;
    private String targetRoles;
    private String expectedCity;
    private String expectedSalary;
    private String jobStatus;
    private String summary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
