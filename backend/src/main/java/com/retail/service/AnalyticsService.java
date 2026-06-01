package com.retail.service;

import java.util.List;
import java.util.Map;

public interface AnalyticsService {

    /** 客流统计 */
    Map<String, Object> traffic(Long storeId, String startDate, String endDate, String granularity);

    /** 热力图数据（区域停留） */
    List<Map<String, Object>> heatmap(Long storeId, String startDate, String endDate);

    /** 停留时长分析 */
    Map<String, Object> duration(Long storeId, String startDate, String endDate);

    /** 购买转化分析 */
    Map<String, Object> conversion(Long storeId, String startDate, String endDate);

    /** 仪表盘汇总 */
    Map<String, Object> dashboard(Long storeId);

    /** 多门店对比 */
    List<Map<String, Object>> multiStoreCompare(List<Long> storeIds, String startDate, String endDate);
}
