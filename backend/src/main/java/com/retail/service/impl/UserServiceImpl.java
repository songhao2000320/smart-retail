package com.retail.service.impl;

import com.retail.dto.ApiResponse;
import com.retail.entity.User;
import com.retail.mapper.UserMapper;
import com.retail.service.UserService;
import com.retail.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public ApiResponse register(String username, String password) {
        // 检查用户名是否已存在
        User existUser = userMapper.selectByUsername(username);
        if (existUser != null) {
            return ApiResponse.error("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        // 使用 MD5 加密（与前端 BCrypt 对应简化，实际生产建议 BCrypt）
        user.setPassword(DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8)));
        user.setRole("analyst");  // 默认角色：数据分析师
        user.setStatus(1);        // 正常状态
        user.setLoginFailCount(0);

        userMapper.insert(user);
        return ApiResponse.success("注册成功", null);
    }

    @Override
    public ApiResponse login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            return ApiResponse.error("用户名或密码错误");
        }

        // 检查是否被锁定
        if (user.getStatus() != null && user.getStatus() == 0) {
            if (user.getLockedUntil() != null) {
                LocalDateTime lockedUntil = LocalDateTime.parse(user.getLockedUntil(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                if (LocalDateTime.now().isBefore(lockedUntil)) {
                    return ApiResponse.error("账号已被锁定，请" + lockedUntil.format(
                            DateTimeFormatter.ofPattern("HH:mm:ss")) + "后再试");
                }
                // 锁定期已过，自动解锁
                userMapper.updateLoginFail(user.getId(), 0, 1, null);
            } else {
                return ApiResponse.error("账号已被锁定，请联系管理员");
            }
        }

        // 验证密码
        String inputPwd = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        if (!inputPwd.equals(user.getPassword())) {
            // 登录失败，增加失败次数
            int failCount = (user.getLoginFailCount() == null ? 0 : user.getLoginFailCount()) + 1;
            if (failCount >= 5) {
                // 5次失败后锁定15分钟
                String lockedUntil = LocalDateTime.now().plusMinutes(15)
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                userMapper.updateLoginFail(user.getId(), failCount, 0, lockedUntil);
                return ApiResponse.error("密码错误次数过多，账号已锁定15分钟");
            }
            userMapper.updateLoginFail(user.getId(), failCount, null, null);
            return ApiResponse.error("用户名或密码错误，剩余尝试次数：" + (5 - failCount));
        }

        // 登录成功，重置失败计数
        userMapper.updateLoginFail(user.getId(), 0, 1, null);

        // 生成 JWT
        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole(), user.getStoreId());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("role", user.getRole());
        userInfo.put("storeId", user.getStoreId());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", userInfo);

        return ApiResponse.success("登录成功", result);
    }

    @Override
    public ApiResponse getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("role", user.getRole());
        userInfo.put("storeId", user.getStoreId());

        return ApiResponse.success(userInfo);
    }

    @Override
    public ApiResponse listUsers(String role, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<User> users = userMapper.selectList(role, offset, pageSize);
        int total = userMapper.countList(role);

        // 脱敏：移除密码字段
        List<Map<String, Object>> list = new ArrayList<>();
        for (User u : users) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", u.getId());
            item.put("username", u.getUsername());
            item.put("role", u.getRole());
            item.put("storeId", u.getStoreId());
            item.put("status", u.getStatus());
            item.put("lockedUntil", u.getLockedUntil());
            item.put("loginFailCount", u.getLoginFailCount());
            item.put("createTime", u.getCreateTime());
            item.put("updateTime", u.getUpdateTime());
            list.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);

        return ApiResponse.success(result);
    }

    @Override
    @Transactional
    public ApiResponse updateUserRole(Long userId, String role) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }
        if (!Arrays.asList("admin", "manager", "analyst").contains(role)) {
            return ApiResponse.error("无效的角色");
        }
        userMapper.updateRole(userId, role);
        return ApiResponse.success("角色修改成功", null);
    }

    @Override
    @Transactional
    public ApiResponse updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }
        userMapper.updateStatus(userId, status);
        // 如果解锁，同时重置失败次数
        if (status == 1) {
            userMapper.updateLoginFail(userId, 0, 1, null);
        }
        return ApiResponse.success("操作成功", null);
    }
}
