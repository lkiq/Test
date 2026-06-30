package com.xuelian.career.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuelian.career.entity.JobApplication;
import org.apache.ibatis.annotations.Mapper;

/**
 * 岗位投递记录 Mapper
 */
@Mapper
public interface JobApplicationMapper extends BaseMapper<JobApplication> {
}
