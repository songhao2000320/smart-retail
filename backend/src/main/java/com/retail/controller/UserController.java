package com.retail.controller;

import com.retail.dto.ApiResponse;
import com.retail.interceptor.RequireRole;
import com.retail.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @RequireRole("admin")
    public ApiResponse listUsers(@RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int pageSize,
                                  @RequestParam(required = false) String role) {
        return userService.listUsers(role, page, pageSize);
    }

    @PutMapping("/{userId}/role")
    @RequireRole("admin")
    public ApiResponse updateRole(@PathVariable Long userId, @RequestBody Map<String, String> body) {
        String role = body.get("role");
        return userService.updateUserRole(userId, role);
    }

    @PutMapping("/{userId}/status")
    @RequireRole("admin")
    public ApiResponse updateStatus(@PathVariable Long userId, @RequestBody Map<String, Object> body) {
        Integer status = (Integer) body.get("status");
        return userService.updateUserStatus(userId, status);
    }
}
