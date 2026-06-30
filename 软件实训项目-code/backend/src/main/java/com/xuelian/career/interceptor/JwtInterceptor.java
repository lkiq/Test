package com.xuelian.career.interceptor;

import com.xuelian.career.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 认证拦截器 - 校验请求头中的 Bearer Token
 * 从 Authorization 头中提取 Token → 解析用户ID/用户名/角色 → 存入 request attribute 供后续使用
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public JwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS 预检请求放行（CORS）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 从请求头获取 Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("请求缺少 Token: {} {}", request.getMethod(), request.getRequestURI());
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"未登录，请先登录\"}");
            return false;
        }

        String token = authHeader.substring(7);

        // 校验 Token 有效性
        if (!jwtUtil.validateToken(token)) {
            log.warn("Token 无效或已过期: {} {}", request.getMethod(), request.getRequestURI());
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"登录已过期，请重新登录\"}");
            return false;
        }

        // 将用户信息存入 request attribute，供 Controller 和后续拦截器使用
        request.setAttribute("userId", jwtUtil.getUserId(token));
        request.setAttribute("username", jwtUtil.getUsername(token));
        request.setAttribute("role", jwtUtil.getRole(token));

        return true;
    }
}
