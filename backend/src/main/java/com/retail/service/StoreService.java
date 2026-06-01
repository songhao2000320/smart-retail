package com.retail.service;

import com.retail.dto.ApiResponse;

public interface StoreService {

    /** 获取所有门店 */
    ApiResponse listAll();

    /** 分页查询门店 */
    ApiResponse listByPage(int page, int pageSize);

    /** 获取门店详情 */
    ApiResponse getById(Long id);

    /** 创建门店 */
    ApiResponse create(String name, String address, String phone);

    /** 更新门店 */
    ApiResponse update(Long id, String name, String address, String phone);

    /** 软删除门店（同时删除关联区域） */
    ApiResponse delete(Long id);

    // ===== 区域管理 =====

    /** 获取门店下的区域列表 */
    ApiResponse listZones(Long storeId);

    /** 创建区域 */
    ApiResponse createZone(Long storeId, String name, Double posX, Double posY, Double width, Double height);

    /** 更新区域 */
    ApiResponse updateZone(Long id, String name, Double posX, Double posY, Double width, Double height);

    /** 删除区域 */
    ApiResponse deleteZone(Long id);
}
