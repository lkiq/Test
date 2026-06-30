package com.xuelian.career.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuelian.career.entity.Enterprise;
import com.xuelian.career.mapper.EnterpriseMapper;
import com.xuelian.career.service.EnterpriseInfoService;
import org.springframework.stereotype.Service;

/**
 * 企业信息管理服务实现类
 */
@Service
public class EnterpriseInfoServiceImpl extends ServiceImpl<EnterpriseMapper, Enterprise> implements EnterpriseInfoService {

    @Override
    public Enterprise getByHrUserId(Long hrUserId) {
        LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Enterprise::getHrUserId, hrUserId);
        return baseMapper.selectOne(wrapper);
    }

    @Override
    public void updateVerifyStatus(Long enterpriseId, String status) {
        Enterprise enterprise = new Enterprise();
        enterprise.setId(enterpriseId);
        enterprise.setVerifyStatus(status);
        baseMapper.updateById(enterprise);
    }
}
