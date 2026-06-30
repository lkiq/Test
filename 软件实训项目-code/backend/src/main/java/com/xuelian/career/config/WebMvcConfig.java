package com.xuelian.career.config;

import com.xuelian.career.interceptor.JwtInterceptor;
import com.xuelian.career.interceptor.RoleInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置 - 注册 CORS 跨域与 JWT/角色拦截器
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;
    private final RoleInterceptor roleInterceptor;

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    public WebMvcConfig(JwtInterceptor jwtInterceptor, RoleInterceptor roleInterceptor) {
        this.jwtInterceptor = jwtInterceptor;
        this.roleInterceptor = roleInterceptor;
    }

    /**
     * 配置静态资源映射 - 使上传文件可通过 /uploads/** 访问
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }

    /**
     * 配置 CORS 跨域，允许前端开发服务器（localhost:5173）访问
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 注册拦截器：
     * - JwtInterceptor：对所有 /api/** 校验 Token（排除认证接口）
     * - RoleInterceptor：对需要角色的接口校验权限
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/forgot-password",
                        "/api/auth/reset-password",
                        "/api/auth/send-login-code",
                        "/api/auth/login-by-code",
                        "/api/customer-service/faqs"
                );

        registry.addInterceptor(roleInterceptor)
                .addPathPatterns("/api/admin/**", "/api/enterprise/**");
    }
}
