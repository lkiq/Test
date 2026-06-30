package com.xuelian.career.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuelian.career.entity.Enterprise;

/**
 * 企业信息管理服务接口 - 管理HR注册时的企业信息
 */
public interface EnterpriseInfoService extends IService<Enterprise> {

    /**
     * 根据HR用户ID查询企业信息
     */
    Enterprise getByHrUserId(Long hrUserId);

    /**
     * 更新认证状态
     */
    void updateVerifyStatus(Long enterpriseId, String status);
}
