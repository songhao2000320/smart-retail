package com.retail.controller;

import com.retail.dto.ApiResponse;
import com.retail.interceptor.RequireRole;
import com.retail.service.UserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user-auth")
public class UserAuthController {

    @Autowired
    private UserAuthService userAuthService;

    /**
     * 获取用户授权门店列表
     */
    @GetMapping("/{userId}")
    @RequireRole("admin")
    public ApiResponse getUserAuths(@PathVariable Long userId) {
        return userAuthService.getUserAuths(userId);
    }

    /**
     * 为用户授权门店
     */
    @PostMapping
    @RequireRole("admin")
    public ApiResponse saveUserAuths(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        @SuppressWarnings("unchecked")
        List<Integer> rawIds = (List<Integer>) body.get("storeIds");
        List<Long> storeIds = new java.util.ArrayList<>();
        if (rawIds != null) {
            for (Integer id : rawIds) {
                storeIds.add(id.longValue());
            }
        }
        return userAuthService.saveUserAuths(userId, storeIds);
    }
}
