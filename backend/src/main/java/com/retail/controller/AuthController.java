package com.retail.controller;

import com.retail.dto.ApiResponse;
import com.retail.interceptor.RequireRole;
import com.retail.service.UserService;
import com.retail.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ApiResponse register(@RequestBody RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return ApiResponse.error("用户名不能为空");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            return ApiResponse.error("密码至少6位");
        }
        return userService.register(request.getUsername().trim(), request.getPassword());
    }

    @PostMapping("/login")
    public ApiResponse login(@RequestBody LoginRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return ApiResponse.error("用户名不能为空");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return ApiResponse.error("密码不能为空");
        }
        return userService.login(request.getUsername().trim(), request.getPassword());
    }

    @GetMapping("/me")
    public ApiResponse me() {
        Long userId = UserContext.getUserId();
        return userService.getCurrentUser(userId);
    }

    // 内部请求类
    public static class LoginRequest {
        private String username;
        private String password;
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RegisterRequest {
        private String username;
        private String password;
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
