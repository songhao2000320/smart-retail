package com.retail.controller;

import com.retail.dto.ApiResponse;
import com.retail.interceptor.RequireRole;
import com.retail.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/stores")
public class StoreController {

    @Autowired
    private StoreService storeService;

    /** 分页获取门店列表 */
    @GetMapping
    @RequireRole({"admin", "manager", "analyst"})
    public ApiResponse list(@RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "10") int pageSize) {
        return storeService.listByPage(page, pageSize);
    }

    /** 获取所有门店（下拉框用） */
    @GetMapping("/all")
    @RequireRole({"admin", "manager", "analyst"})
    public ApiResponse listAll() {
        return storeService.listAll();
    }

    /** 获取门店详情 */
    @GetMapping("/{id}")
    @RequireRole({"admin", "manager"})
    public ApiResponse getById(@PathVariable Long id) {
        return storeService.getById(id);
    }

    /** 创建门店 */
    @PostMapping
    @RequireRole("admin")
    public ApiResponse create(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.trim().isEmpty()) {
            return ApiResponse.error("门店名称不能为空");
        }
        return storeService.create(name.trim(), body.get("address"), body.get("phone"));
    }

    /** 更新门店 */
    @PutMapping("/{id}")
    @RequireRole("admin")
    public ApiResponse update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.trim().isEmpty()) {
            return ApiResponse.error("门店名称不能为空");
        }
        return storeService.update(id, name.trim(), body.get("address"), body.get("phone"));
    }

    /** 删除门店 */
    @DeleteMapping("/{id}")
    @RequireRole("admin")
    public ApiResponse delete(@PathVariable Long id) {
        return storeService.delete(id);
    }

    // ===== 区域管理 =====

    /** 获取门店下的区域列表 */
    @GetMapping("/{storeId}/zones")
    @RequireRole({"admin", "manager"})
    public ApiResponse listZones(@PathVariable Long storeId) {
        return storeService.listZones(storeId);
    }

    /** 创建区域 */
    @PostMapping("/{storeId}/zones")
    @RequireRole({"admin", "manager"})
    public ApiResponse createZone(@PathVariable Long storeId, @RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        if (name == null || name.trim().isEmpty()) {
            return ApiResponse.error("区域名称不能为空");
        }
        return storeService.createZone(storeId, name.trim(),
                toDouble(body.get("posX")), toDouble(body.get("posY")),
                toDouble(body.get("width")), toDouble(body.get("height")));
    }

    /** 更新区域 */
    @PutMapping("/{storeId}/zones/{id}")
    @RequireRole({"admin", "manager"})
    public ApiResponse updateZone(@PathVariable Long storeId, @PathVariable Long id,
                                   @RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        if (name == null || name.trim().isEmpty()) {
            return ApiResponse.error("区域名称不能为空");
        }
        return storeService.updateZone(id, name.trim(),
                toDouble(body.get("posX")), toDouble(body.get("posY")),
                toDouble(body.get("width")), toDouble(body.get("height")));
    }

    /** 删除区域 */
    @DeleteMapping("/{storeId}/zones/{id}")
    @RequireRole({"admin", "manager"})
    public ApiResponse deleteZone(@PathVariable Long storeId, @PathVariable Long id) {
        return storeService.deleteZone(id);
    }

    private Double toDouble(Object val) {
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        return 0.0;
    }
}
