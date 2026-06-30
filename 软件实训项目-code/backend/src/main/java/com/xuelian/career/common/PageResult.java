package com.xuelian.career.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 分页结果封装 - 用于列表类 API 的分页返回
 * @param <T> 记录数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /** 总记录数 */
    private long total;

    /** 当前页码 */
    private long page;

    /** 每页大小 */
    private long size;

    /** 当前页记录列表 */
    private List<T> records;

    /**
     * 构建分页结果
     */
    public static <T> PageResult<T> of(long total, long page, long size, List<T> records) {
        return new PageResult<>(total, page, size, records);
    }
}
