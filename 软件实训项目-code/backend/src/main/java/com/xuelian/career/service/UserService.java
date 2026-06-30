package com.xuelian.career.service;

import com.xuelian.career.entity.User;

/**
 * 用户服务接口 - 提供用户查询和基本操作
 */
public interface UserService {

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户实体，不存在返回 null
     */
    User getByUsername(String username);

    /**
     * 根据邮箱查询用户
     * @param email 邮箱
     * @return 用户实体，不存在返回 null
     */
    User getByEmail(String email);

    /**
     * 创建新用户
     * @param user 用户实体
     * @return 创建后的用户实体
     */
    User createUser(User user);

    /**
     * 更新用户
     * @param user 用户实体
     */
    void updateUser(User user);

    /**
     * 根据用户ID查询
     * @param userId 用户ID
     * @return 用户实体
     */
    User getById(Long userId);

    /**
     * 检查用户名是否已存在
     * @param username 用户名
     * @return true 存在 / false 不存在
     */
    boolean isUsernameExists(String username);

    /**
     * 检查邮箱是否已被注册
     * @param email 邮箱
     * @return true 存在 / false 不存在
     */
    boolean isEmailExists(String email);

    /**
     * 修改密码
     * @param userId      用户ID
     * @param oldPassword 旧密码（明文）
     * @param newPassword 新密码（明文）
     * @return true 成功 / false 旧密码不匹配
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);
}
