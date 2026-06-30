package com.xuelian.career.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuelian.career.entity.InterviewRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 模拟面试记录 Mapper 接口
 */
@Mapper
public interface InterviewRecordMapper extends BaseMapper<InterviewRecord> {
}
