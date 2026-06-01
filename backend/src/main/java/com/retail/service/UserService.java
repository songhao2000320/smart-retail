package com.retail.service;

import com.retail.dto.ApiResponse;
import com.retail.entity.User;

import java.util.Map;

public interface UserService {

    ApiResponse register(String username, String password);

    ApiResponse login(String username, String password);

    ApiResponse getCurrentUser(Long userId);

    ApiResponse listUsers(String role, int page, int pageSize);

    ApiResponse updateUserRole(Long userId, String role);

    ApiResponse updateUserStatus(Long userId, Integer status);
}
