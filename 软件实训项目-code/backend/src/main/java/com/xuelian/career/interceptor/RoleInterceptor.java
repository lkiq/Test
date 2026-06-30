package com.xuelian.career.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 角色权限拦截器 - 校验当前用户是否具有访问指定接口的角色权限
 * 管理员接口 (/api/admin/**) 需要 ADMIN 角色
 * 企业端接口 (/api/enterprise/**) 需要 HR 角色
 */
@Slf4j
@Component
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS 预检请求放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String requestUri = request.getRequestURI();
        String role = (String) request.getAttribute("role");

        // role 为 null 说明 JwtInterceptor 已经拦截，此处兜底
        if (role == null) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"未登录，请先登录\"}");
            return false;
        }

        // 管理员接口权限校验
        if (requestUri.startsWith("/api/admin/") && !"ADMIN".equals(role)) {
            log.warn("权限不足: userId={}, role={}, uri={}",
                    request.getAttribute("userId"), role, requestUri);
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(403);
            response.getWriter().write("{\"code\":403,\"message\":\"权限不足，仅管理员可访问\"}");
            return false;
        }

        // 企业端接口权限校验
        if (requestUri.startsWith("/api/enterprise/") && !"HR".equals(role) && !"ADMIN".equals(role)) {
            log.warn("权限不足: userId={}, role={}, uri={}",
                    request.getAttribute("userId"), role, requestUri);
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(403);
            response.getWriter().write("{\"code\":403,\"message\":\"权限不足，仅企业HR可访问\"}");
            return false;
        }

        return true;
    }
}
