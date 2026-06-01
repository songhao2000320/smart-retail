package com.retail.interceptor;

import com.retail.util.UserContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * 角色权限拦截器 - 检查 @RequireRole 注解，校验用户角色
 */
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 处理 OPTIONS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);

        // 无注解 = 所有已认证用户可访问
        if (requireRole == null) {
            return true;
        }

        String[] allowedRoles = requireRole.value();
        if (allowedRoles.length == 0) {
            return true;
        }

        String currentRole = UserContext.getRole();
        if (currentRole == null) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(403);
            response.getWriter().write("{\"code\":403,\"message\":\"未登录\",\"data\":null}");
            return false;
        }

        // admin 拥有所有权限
        if ("admin".equals(currentRole)) {
            return true;
        }

        // 检查当前角色是否在允许列表中
        boolean hasPermission = Arrays.asList(allowedRoles).contains(currentRole);
        if (!hasPermission) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(403);
            response.getWriter().write("{\"code\":403,\"message\":\"权限不足，当前角色无此操作权限\",\"data\":null}");
            return false;
        }

        return true;
    }
}
