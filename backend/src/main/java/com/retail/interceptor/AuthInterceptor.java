package com.retail.interceptor;

import com.retail.util.JwtUtil;
import com.retail.util.UserContext;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT 认证拦截器 - 解析 Authorization Header，验证 Token 有效性
 */
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 处理 OPTIONS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或Token缺失\",\"data\":null}");
            return false;
        }

        String token = authHeader.substring(7);
        if (!JwtUtil.validateToken(token)) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"Token已过期或无效\",\"data\":null}");
            return false;
        }

        // 设置用户上下文
        UserContext.setUserId(JwtUtil.getUserId(token));
        UserContext.setUsername(JwtUtil.getUsername(token));
        UserContext.setRole(JwtUtil.getRole(token));
        UserContext.setStoreId(JwtUtil.getStoreId(token));

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清除线程变量，防止内存泄漏
        UserContext.clear();
    }
}
