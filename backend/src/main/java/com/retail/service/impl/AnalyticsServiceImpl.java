package com.retail.service.impl;

import com.retail.mapper.CustomerBehaviorMapper;
import com.retail.mapper.UserStoreAuthMapper;
import com.retail.service.AnalyticsService;
import com.retail.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    @Autowired
    private CustomerBehaviorMapper behaviorMapper;

    @Autowired(required = false)
    private UserStoreAuthMapper userStoreAuthMapper;

    /**
     * 获取当前用户有权访问的门店 ID 列表（analyst 角色数据权限控制）
     * admin/manager 返回 null 表示无限制
     */
    private List<Long> getAuthorizedStoreIds() {
        if ("analyst".equals(UserContext.getRole()) && userStoreAuthMapper != null) {
            List<Map<String, Object>> authList = userStoreAuthMapper.selectByUserId(UserContext.getUserId());
            if (authList != null && !authList.isEmpty()) {
                return authList.stream()
                        .map(m -> ((Number) m.get("store_id")).longValue())
                        .distinct()
                        .collect(Collectors.toList());
            }
            // analyst 没有任何授权 → 返回空列表
            return Collections.emptyList();
        }
        // admin/manager 无限制
        return null;
    }

    /**
     * 从 Map 中安全取值，兼容下划线(today_visitors)和驼峰(todayVisitors)两种 key 格式
     * MyBatis 的 map-underscore-to-camel-case 对 HashMap 的行为因版本而异
     */
    private Object getVal(Map<String, Object> map, String camelKey, Object defaultValue) {
        Object v = map.get(camelKey);
        if (v != null) return v;
        // 兜底：尝试下划线格式
        String underscore = camelKey.replaceAll("(?<=[a-z])([A-Z])", "_$1").toLowerCase();
        v = map.get(underscore);
        return v != null ? v : defaultValue;
    }

    @Override
    public Map<String, Object> traffic(Long storeId, String startDate, String endDate, String granularity) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> data;
        List<Long> authStoreIds = getAuthorizedStoreIds();

        if ("hour".equals(granularity)) {
            data = behaviorMapper.trafficByHour(storeId, authStoreIds, startDate, endDate);
        } else {
            data = behaviorMapper.trafficByDay(storeId, authStoreIds, startDate, endDate);
        }

        // 汇总
        long totalVisitors = 0;
        long totalPurchases = 0;
        for (Map<String, Object> row : data) {
            totalVisitors += ((Number) row.get("total_count")).longValue();
            totalPurchases += ((Number) row.get("purchase_count")).longValue();
        }

        result.put("data", data);
        result.put("totalVisitors", totalVisitors);
        result.put("totalPurchases", totalPurchases);
        result.put("granularity", granularity);
        return result;
    }

    @Override
    public List<Map<String, Object>> heatmap(Long storeId, String startDate, String endDate) {
        return behaviorMapper.zoneStayStats(storeId, getAuthorizedStoreIds(), startDate, endDate);
    }

    @Override
    public Map<String, Object> duration(Long storeId, String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();
        List<Long> authStoreIds = getAuthorizedStoreIds();

        // 按天统计
        List<Map<String, Object>> dailyData = behaviorMapper.trafficByDay(storeId, authStoreIds, startDate, endDate);

        // 整体统计
        double totalAvg = 0;
        int count = 0;
        for (Map<String, Object> row : dailyData) {
            if (row.get("avg_duration_min") != null) {
                totalAvg += ((Number) row.get("avg_duration_min")).doubleValue();
                count++;
            }
        }

        result.put("dailyData", dailyData);
        result.put("overallAvgDuration", count > 0 ? Math.round(totalAvg / count * 10.0) / 10.0 : 0);
        return result;
    }

    @Override
    public Map<String, Object> conversion(Long storeId, String startDate, String endDate) {
        Map<String, Object> data = behaviorMapper.conversionRate(storeId, getAuthorizedStoreIds(), startDate, endDate);
        if (data == null) {
            data = new HashMap<>();
            data.put("totalVisitors", 0);
            data.put("purchaseCount", 0);
            data.put("conversionRate", 0);
            data.put("avgDurationMin", 0);
            data.put("avgPurchaseDurationMin", 0);
            data.put("avgNoPurchaseDurationMin", 0);
        }
        return data;
    }

    @Override
    public Map<String, Object> dashboard(Long storeId) {
        List<Long> authStoreIds = getAuthorizedStoreIds();
        Map<String, Object> raw = behaviorMapper.dashboardSummary(storeId, authStoreIds);
        // 统一转为驼峰格式，避免前端和导出代码需要猜测 key 格式
        Map<String, Object> summary = new HashMap<>();
        if (raw != null) {
            summary.put("todayVisitors", getVal(raw, "todayVisitors", 0));
            summary.put("todayPurchases", getVal(raw, "todayPurchases", 0));
            summary.put("totalVisitors", getVal(raw, "totalVisitors", 0));
            Object rate = getVal(raw, "totalConversionRate", "0");
            summary.put("totalConversionRate", rate instanceof Number ? ((Number) rate).doubleValue() : Double.parseDouble(rate.toString()));
            summary.put("avgStayDuration", getVal(raw, "avgStayDuration", 0));
        } else {
            summary.put("todayVisitors", 0);
            summary.put("todayPurchases", 0);
            summary.put("totalVisitors", 0);
            summary.put("totalConversionRate", 0.0);
            summary.put("avgStayDuration", 0);
        }

        // 最近7天趋势
        String endDate = java.time.LocalDate.now().toString();
        String startDate = java.time.LocalDate.now().minusDays(6).toString();
        List<Map<String, Object>> recentTrend = behaviorMapper.trafficByDay(storeId, authStoreIds, startDate, endDate);

        Map<String, Object> result = new HashMap<>();
        result.put("summary", summary);
        result.put("recentTrend", recentTrend);
        return result;
    }

    @Override
    public List<Map<String, Object>> multiStoreCompare(List<Long> storeIds, String startDate, String endDate) {
        // 对于 analyst 角色，限制只能对比授权的门店
        List<Long> authStoreIds = getAuthorizedStoreIds();
        if (authStoreIds != null) {
            // admin/manager 返回 null，无限制
            if (storeIds != null && !storeIds.isEmpty()) {
                // 取交集：只保留用户指定 + 授权的门店
                storeIds = storeIds.stream().filter(authStoreIds::contains).collect(Collectors.toList());
            } else {
                storeIds = authStoreIds;
            }
        }
        return behaviorMapper.multiStoreStats(storeIds, startDate, endDate);
    }
}
