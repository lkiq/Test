package com.xuelian.career.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 路径统计信息 DTO - 路径列表页展示
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PathStatsDTO {
    private Long pathId;
    private String jobTitle;
    private String mode;                // SINGLE / MERGED
    private Integer totalTasks;
    private Integer completedTasks;
    private Integer overallProgress;    // 0-100
    private Map<String, Integer> stageProgress;  // BASIC:80, FRAMEWORK:50...
    private LocalDateTime createdAt;
}
