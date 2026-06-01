package com.retail.service;

import com.retail.dto.ApiResponse;
import java.util.List;

public interface UserAuthService {

    /**
     * 获取用户授权门店列表
     */
    ApiResponse getUserAuths(Long userId);

    /**
     * 为用户授权门店（覆盖式更新）
     */
    ApiResponse saveUserAuths(Long userId, List<Long> storeIds);
}
