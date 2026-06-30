package com.xuelian.career.util;

import com.xuelian.career.config.JwtConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类 - 负责 Token 生成、解析与校验
 * 使用 JJWT 0.12.x API
 */
@Component
public class JwtUtil {

    private final JwtConfig jwtConfig;

    public JwtUtil(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8);
        // JJWT 要求密钥至少 256 位（32 字节），若密钥不足则补零
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 32));
            keyBytes = padded;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 JWT Token
     * @param userId   用户ID
     * @param username 用户名
     * @param role     角色
     * @param claims   额外声明
     * @return JWT Token 字符串
     */
    public String generateToken(Long userId, String username, String role, Map<String, Object> claims) {
        long now = System.currentTimeMillis();
        long expiration = jwtConfig.getExpiration() * 1000;

        JwtBuilder builder = Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expiration));

        if (claims != null && !claims.isEmpty()) {
            claims.forEach(builder::claim);
        }

        return builder.signWith(getSigningKey()).compact();
    }

    /**
     * 生成 JWT Token（无额外声明）
     */
    public String generateToken(Long userId, String username, String role) {
        return generateToken(userId, username, role, null);
    }

    /**
     * 从 Token 中解析 Claims
     * @param token JWT Token
     * @return 解析后的 Claims 对象
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 Token 中获取用户 ID
     */
    public Long getUserId(String token) {
        return parseToken(token).get("userId", Long.class);
    }

    /**
     * 从 Token 中获取用户名
     */
    public String getUsername(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * 从 Token 中获取角色
     */
    public String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    /**
     * 验证 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 获取 Token 过期时间（秒）
     */
    public long getExpiration() {
        return jwtConfig.getExpiration();
    }
}
