package com.retail.controller;

import com.retail.dto.ApiResponse;
import com.retail.interceptor.RequireRole;
import com.retail.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    /** 客流统计 */
    @GetMapping("/traffic")
    @RequireRole({"admin", "manager", "analyst"})
    public ApiResponse traffic(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "day") String granularity) {
        Map<String, Object> data = analyticsService.traffic(storeId, startDate, endDate, granularity);
        return ApiResponse.success(data);
    }

    /** 热力图数据 */
    @GetMapping("/heatmap")
    @RequireRole({"admin", "manager", "analyst"})
    public ApiResponse heatmap(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<Map<String, Object>> data = analyticsService.heatmap(storeId, startDate, endDate);
        return ApiResponse.success(data);
    }

    /** 停留时长分析 */
    @GetMapping("/duration")
    @RequireRole({"admin", "manager", "analyst"})
    public ApiResponse duration(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> data = analyticsService.duration(storeId, startDate, endDate);
        return ApiResponse.success(data);
    }

    /** 购买转化分析 */
    @GetMapping("/conversion")
    @RequireRole({"admin", "manager", "analyst"})
    public ApiResponse conversion(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> data = analyticsService.conversion(storeId, startDate, endDate);
        return ApiResponse.success(data);
    }

    /** 仪表盘 */
    @GetMapping("/dashboard")
    @RequireRole({"admin", "manager", "analyst"})
    public ApiResponse dashboard(@RequestParam(required = false) Long storeId) {
        Map<String, Object> data = analyticsService.dashboard(storeId);
        return ApiResponse.success(data);
    }

    /** 多门店对比 */
    @GetMapping("/compare")
    @RequireRole({"admin", "manager"})
    public ApiResponse compare(
            @RequestParam(required = false) String storeIds,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<Long> ids = null;
        if (storeIds != null && !storeIds.isEmpty()) {
            ids = new java.util.ArrayList<>();
            for (String s : storeIds.split(",")) {
                ids.add(Long.valueOf(s.trim()));
            }
        }
        List<Map<String, Object>> data = analyticsService.multiStoreCompare(ids, startDate, endDate);
        return ApiResponse.success(data);
    }
}
