package com.retail.service.impl;

import com.retail.dto.ApiResponse;
import com.retail.entity.UserStoreAuth;
import com.retail.mapper.UserStoreAuthMapper;
import com.retail.service.UserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class UserAuthServiceImpl implements UserAuthService {

    @Autowired
    private UserStoreAuthMapper userStoreAuthMapper;

    @Override
    public ApiResponse getUserAuths(Long userId) {
        List<Map<String, Object>> list = userStoreAuthMapper.selectByUserId(userId);
        return ApiResponse.success(list);
    }

    @Override
    @Transactional
    public ApiResponse saveUserAuths(Long userId, List<Long> storeIds) {
        // 删除旧授权
        userStoreAuthMapper.deleteByUserId(userId);

        // 添加新授权
        if (storeIds != null && !storeIds.isEmpty()) {
            List<UserStoreAuth> auths = new ArrayList<>();
            for (Long storeId : storeIds) {
                UserStoreAuth auth = new UserStoreAuth();
                auth.setUserId(userId);
                auth.setStoreId(storeId);
                auths.add(auth);
            }
            for (UserStoreAuth auth : auths) {
                userStoreAuthMapper.insert(auth);
            }
        }

        return ApiResponse.success("授权保存成功");
    }
}
