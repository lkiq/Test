package com.xuelian.career.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuelian.career.entity.User;
import com.xuelian.career.mapper.UserMapper;
import com.xuelian.career.service.UserService;
import com.xuelian.career.util.PasswordUtil;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

/**
 * 用户服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordUtil passwordUtil;

    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return baseMapper.selectOne(wrapper);
    }

    @Override
    public User getByEmail(String email) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        return baseMapper.selectOne(wrapper);
    }

    @Override
    public User createUser(User user) {
        baseMapper.insert(user);
        return user;
    }

    @Override
    public void updateUser(User user) {
        baseMapper.updateById(user);
    }

    @Override
    public User getById(Long userId) {
        return baseMapper.selectById(userId);
    }

    @Override
    public boolean isUsernameExists(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return baseMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean isEmailExists(String email) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        return baseMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = baseMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        // 验证旧密码
        if (!passwordUtil.matches(oldPassword, user.getPasswordHash())) {
            return false;
        }
        // 更新为新密码
        user.setPasswordHash(passwordUtil.encode(newPassword));
        baseMapper.updateById(user);
        return true;
    }
}
