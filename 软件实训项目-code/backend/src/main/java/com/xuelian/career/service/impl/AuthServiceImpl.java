package com.xuelian.career.service.impl;

import com.xuelian.career.common.BusinessException;
import com.xuelian.career.dto.request.LoginRequest;
import com.xuelian.career.dto.request.RegisterRequest;
import com.xuelian.career.dto.response.LoginResponse;
import com.xuelian.career.entity.Enterprise;
import com.xuelian.career.entity.User;
import com.xuelian.career.service.AuthService;
import com.xuelian.career.service.EnterpriseInfoService;
import com.xuelian.career.service.MailService;
import com.xuelian.career.service.UserService;
import com.xuelian.career.util.JwtUtil;
import com.xuelian.career.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现类 - 实现注册、登录、密码重置的完整业务流程
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final EnterpriseInfoService enterpriseInfoService;
    private final MailService mailService;
    private final JwtUtil jwtUtil;
    private final PasswordUtil passwordUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    /** Redis Key 前缀：登录会话 */
    private static final String TOKEN_PREFIX = "auth:token:";
    /** Redis Key 前缀：密码重置验证码 */
    private static final String RESET_CODE_PREFIX = "reset:code:";
    /** Redis Key 前缀：邮箱登录验证码 */
    private static final String LOGIN_CODE_PREFIX = "login:code:";
    /** 验证码有效期（分钟） */
    private static final long CODE_EXPIRE_MINUTES = 5;

    /**
     * 用户注册流程：
     * 1. 校验用户名和邮箱唯一性
     * 2. BCrypt 加密密码
     * 3. 保存用户到数据库
     * 4. 如果是HR角色，创建企业信息记录
     * 5. 生成 JWT Token
     * 6. Redis 存储会话
     * 7. 返回登录信息
     */
    @Override
    public LoginResponse register(RegisterRequest request) {
        // 1. 校验用户名是否已存在
        if (userService.isUsernameExists(request.getUsername())) {
            throw new BusinessException(400, "用户名已存在");
        }

        // 2. 校验邮箱是否已被注册
        if (userService.isEmailExists(request.getEmail())) {
            throw new BusinessException(400, "该邮箱已被注册");
        }

        // 3. HR 角色校验企业信息必填
        if ("HR".equals(request.getRole())) {
            if (request.getCompanyName() == null || request.getCompanyName().isBlank()) {
                throw new BusinessException(400, "企业HR注册时公司名称不能为空");
            }
            if (request.getCompanyIndustry() == null || request.getCompanyIndustry().isBlank()) {
                throw new BusinessException(400, "企业HR注册时所属行业不能为空");
            }
        }

        // 4. 创建用户实体
        User user = new User();
        user.setUsername(request.getUsername());
        user.setRealName(request.getRealName());
        user.setPasswordHash(passwordUtil.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setEducation(request.getEducation());
        user.setSchool(request.getSchool());
        user.setMajor(request.getMajor());
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // 5. 保存用户
        userService.createUser(user);
        log.info("新用户注册成功: username={}, role={}", user.getUsername(), user.getRole());

        // 6. 如果是HR，创建企业信息记录
        if ("HR".equals(request.getRole())) {
            Enterprise enterprise = new Enterprise();
            enterprise.setHrUserId(user.getId());
            enterprise.setCompanyName(request.getCompanyName());
            enterprise.setCompanyIndustry(request.getCompanyIndustry());
            enterprise.setCompanySize(request.getCompanySize());
            enterprise.setCompanyAddress(request.getCompanyAddress());
            enterprise.setContactName(request.getRealName()); // HR的真实姓名作为联系人
            enterprise.setContactPosition(request.getContactPosition());
            enterprise.setVerifyStatus("PENDING");
            enterpriseInfoService.save(enterprise);
            log.info("企业信息已创建: companyName={}, hrUserId={}", request.getCompanyName(), user.getId());
        }

        // 7. 生成 Token 并返回
        return buildLoginResponse(user);
    }

    /**
     * 用户登录流程：
     * 1. 查询用户
     * 2. BCrypt 校验密码
     * 3. 检查账号状态
     * 4. 生成 JWT Token
     * 5. Redis 存储会话
     * 6. 返回登录信息
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        // 1. 查询用户
        User user = userService.getByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 2. 校验密码
        if (!passwordUtil.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 3. 检查账号状态
        if ("DISABLED".equals(user.getStatus())) {
            throw new BusinessException(403, "账号已被禁用，请联系管理员");
        }

        log.info("用户登录成功: userId={}, role={}", user.getId(), user.getRole());

        // 4. 生成 Token 并返回
        return buildLoginResponse(user);
    }

    /**
     * 忘记密码 - 发送验证码到邮箱
     * 1. 校验邮箱是否存在
     * 2. 生成6位验证码
     * 3. 存入 Redis（5分钟有效）
     * 4. 发送邮件
     */
    public void sendResetCode(String email) {
        // 1. 校验邮箱是否已注册
        User user = userService.getByEmail(email);
        if (user == null) {
            throw new BusinessException(404, "该邮箱未注册");
        }

        if ("DISABLED".equals(user.getStatus())) {
            throw new BusinessException(403, "该账号已被禁用");
        }

        // 2. 生成验证码
        String code = mailService.generateCode();

        // 3. 存入 Redis（覆盖旧验证码）
        redisTemplate.opsForValue().set(
                RESET_CODE_PREFIX + email,
                code,
                CODE_EXPIRE_MINUTES,
                TimeUnit.MINUTES
        );

        // 4. 发送邮件
        mailService.sendVerificationCode(email, code);
        log.info("密码重置验证码已发送: email={}", email);
    }

    /**
     * 重置密码
     * 1. 校验邮箱
     * 2. 校验验证码
     * 3. 更新密码
     * 4. 返回成功（不自动登录，用户需手动登录）
     */
    public void resetPassword(String email, String code, String newPassword) {
        // 1. 校验邮箱是否已注册
        User user = userService.getByEmail(email);
        if (user == null) {
            throw new BusinessException(404, "该邮箱未注册");
        }

        // 2. 校验验证码
        String cachedCode = (String) redisTemplate.opsForValue().get(RESET_CODE_PREFIX + email);
        if (cachedCode == null) {
            throw new BusinessException(400, "验证码已过期，请重新获取");
        }
        if (!cachedCode.equals(code)) {
            throw new BusinessException(400, "验证码错误");
        }

        // 3. 检查账号状态
        if ("DISABLED".equals(user.getStatus())) {
            throw new BusinessException(403, "账号已被禁用，请联系管理员");
        }

        // 4. 更新密码
        user.setPasswordHash(passwordUtil.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userService.updateUser(user);

        // 5. 删除验证码（防止重复使用）
        redisTemplate.delete(RESET_CODE_PREFIX + email);

        log.info("密码重置成功: email={}, userId={}", email, user.getId());
    }

    /**
     * 邮箱验证码登录 - 发送验证码
     * 1. 校验邮箱已注册
     * 2. 生成6位验证码
     * 3. 存入 Redis（5分钟有效）
     * 4. 发送邮件
     */
    public void sendLoginCode(String email) {
        // 1. 校验邮箱是否已注册
        User user = userService.getByEmail(email);
        if (user == null) {
            throw new BusinessException(404, "该邮箱未注册");
        }

        if ("DISABLED".equals(user.getStatus())) {
            throw new BusinessException(403, "该账号已被禁用");
        }

        // 2. 生成验证码
        String code = mailService.generateCode();

        // 3. 存入 Redis
        redisTemplate.opsForValue().set(
                LOGIN_CODE_PREFIX + email,
                code,
                CODE_EXPIRE_MINUTES,
                TimeUnit.MINUTES
        );

        // 4. 发送邮件
        mailService.sendLoginCode(email, code);
        log.info("邮箱登录验证码已发送: email={}", email);
    }

    /**
     * 邮箱验证码登录
     * 1. 校验邮箱已注册
     * 2. 校验验证码
     * 3. 返回 Token
     */
    public LoginResponse loginByCode(String email, String code) {
        // 1. 校验邮箱是否已注册
        User user = userService.getByEmail(email);
        if (user == null) {
            throw new BusinessException(404, "该邮箱未注册");
        }

        // 2. 校验验证码
        String cachedCode = (String) redisTemplate.opsForValue().get(LOGIN_CODE_PREFIX + email);
        if (cachedCode == null) {
            throw new BusinessException(400, "验证码已过期，请重新获取");
        }
        if (!cachedCode.equals(code)) {
            throw new BusinessException(400, "验证码错误");
        }

        // 3. 检查账号状态
        if ("DISABLED".equals(user.getStatus())) {
            throw new BusinessException(403, "账号已被禁用，请联系管理员");
        }

        // 4. 删除验证码（一次性使用）
        redisTemplate.delete(LOGIN_CODE_PREFIX + email);

        log.info("邮箱验证码登录成功: email={}, userId={}", email, user.getId());

        // 5. 返回 Token
        return buildLoginResponse(user);
    }

    /**
     * 构建登录响应：生成 JWT Token，存储 Redis 会话
     * @param user 用户实体
     * @return 登录响应
     */
    private LoginResponse buildLoginResponse(User user) {
        // 生成 JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        // Token 过期时间（秒）
        long expireSeconds = jwtUtil.getExpiration();

        // Redis 存储会话：key=auth:token:{token} → value=userId，设置过期时间
        redisTemplate.opsForValue().set(
                TOKEN_PREFIX + token,
                user.getId(),
                expireSeconds,
                TimeUnit.SECONDS
        );

        return LoginResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .token(token)
                .expireTime(System.currentTimeMillis() / 1000 + expireSeconds)
                .build();
    }
}
