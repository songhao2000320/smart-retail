package com.retail.service.impl;

import com.retail.dto.ApiResponse;
import com.retail.entity.Store;
import com.retail.entity.Zone;
import com.retail.mapper.StoreMapper;
import com.retail.mapper.ZoneMapper;
import com.retail.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StoreServiceImpl implements StoreService {

    @Autowired
    private StoreMapper storeMapper;

    @Autowired
    private ZoneMapper zoneMapper;

    @Override
    public ApiResponse listAll() {
        List<Store> stores = storeMapper.selectAll();
        return ApiResponse.success(stores);
    }

    @Override
    public ApiResponse listByPage(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<Store> stores = storeMapper.selectByPage(offset, pageSize);
        int total = storeMapper.countAll();

        Map<String, Object> result = new HashMap<>();
        result.put("list", stores);
        result.put("total", total);
        return ApiResponse.success(result);
    }

    @Override
    public ApiResponse getById(Long id) {
        Store store = storeMapper.selectById(id);
        if (store == null) {
            return ApiResponse.error("门店不存在");
        }
        return ApiResponse.success(store);
    }

    @Override
    @Transactional
    public ApiResponse create(String name, String address, String phone) {
        Store store = new Store();
        store.setName(name);
        store.setAddress(address);
        store.setPhone(phone);
        store.setStatus(1);
        storeMapper.insert(store);
        return ApiResponse.success("门店创建成功", store);
    }

    @Override
    @Transactional
    public ApiResponse update(Long id, String name, String address, String phone) {
        Store store = storeMapper.selectById(id);
        if (store == null) {
            return ApiResponse.error("门店不存在");
        }
        store.setName(name);
        store.setAddress(address);
        store.setPhone(phone);
        storeMapper.updateById(store);
        return ApiResponse.success("门店更新成功", null);
    }

    @Override
    @Transactional
    public ApiResponse delete(Long id) {
        Store store = storeMapper.selectById(id);
        if (store == null) {
            return ApiResponse.error("门店不存在");
        }
        // 软删除门店
        store.setStatus(0);
        storeMapper.updateById(store);
        // 同时软删除关联区域
        zoneMapper.deleteByStoreId(id);
        return ApiResponse.success("门店已删除", null);
    }

    // ===== 区域管理 =====

    @Override
    public ApiResponse listZones(Long storeId) {
        Store store = storeMapper.selectById(storeId);
        if (store == null) {
            return ApiResponse.error("门店不存在");
        }
        List<Zone> zones = zoneMapper.selectByStoreId(storeId);
        return ApiResponse.success(zones);
    }

    @Override
    @Transactional
    public ApiResponse createZone(Long storeId, String name, Double posX, Double posY, Double width, Double height) {
        Store store = storeMapper.selectById(storeId);
        if (store == null) {
            return ApiResponse.error("门店不存在");
        }
        Zone zone = new Zone();
        zone.setStoreId(storeId);
        zone.setName(name);
        zone.setPosX(posX);
        zone.setPosY(posY);
        zone.setWidth(width);
        zone.setHeight(height);
        zone.setStatus(1);
        zoneMapper.insert(zone);
        return ApiResponse.success("区域创建成功", zone);
    }

    @Override
    @Transactional
    public ApiResponse updateZone(Long id, String name, Double posX, Double posY, Double width, Double height) {
        Zone zone = zoneMapper.selectById(id);
        if (zone == null) {
            return ApiResponse.error("区域不存在");
        }
        zone.setName(name);
        zone.setPosX(posX);
        zone.setPosY(posY);
        zone.setWidth(width);
        zone.setHeight(height);
        zoneMapper.updateById(zone);
        return ApiResponse.success("区域更新成功", null);
    }

    @Override
    @Transactional
    public ApiResponse deleteZone(Long id) {
        Zone zone = zoneMapper.selectById(id);
        if (zone == null) {
            return ApiResponse.error("区域不存在");
        }
        zone.setStatus(0);
        zoneMapper.updateById(zone);
        return ApiResponse.success("区域已删除", null);
    }
}
