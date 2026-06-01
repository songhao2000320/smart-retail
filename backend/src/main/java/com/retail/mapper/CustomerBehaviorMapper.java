package com.retail.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.retail.entity.CustomerBehavior;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.Map;

@Mapper
public interface CustomerBehaviorMapper extends BaseMapper<CustomerBehavior> {

    IPage<CustomerBehavior> selectPageWithDetails(Page<CustomerBehavior> page, @Param("params") Map<String, Object> params);

    /** 分页查询 - count */

    /** 客流统计：按小时聚合进店人数 */
    java.util.List<Map<String, Object>> trafficByHour(@Param("storeId") Long storeId,
                                                       @Param("storeIds") java.util.List<Long> storeIds,
                                                       @Param("startDate") String startDate,
                                                       @Param("endDate") String endDate);

    /** 客流统计：按天聚合 */
    java.util.List<Map<String, Object>> trafficByDay(@Param("storeId") Long storeId,
                                                      @Param("storeIds") java.util.List<Long> storeIds,
                                                      @Param("startDate") String startDate,
                                                      @Param("endDate") String endDate);

    /** 区域停留统计 */
    java.util.List<Map<String, Object>> zoneStayStats(@Param("storeId") Long storeId,
                                                       @Param("storeIds") java.util.List<Long> storeIds,
                                                       @Param("startDate") String startDate,
                                                       @Param("endDate") String endDate);

    /** 购买转化率 */
    Map<String, Object> conversionRate(@Param("storeId") Long storeId,
                                       @Param("storeIds") java.util.List<Long> storeIds,
                                       @Param("startDate") String startDate,
                                       @Param("endDate") String endDate);

    /** 仪表盘汇总 */
    Map<String, Object> dashboardSummary(@Param("storeId") Long storeId,
                                         @Param("storeIds") java.util.List<Long> storeIds);

    /** 多门店对比统计 */
    java.util.List<Map<String, Object>> multiStoreStats(@Param("storeIds") java.util.List<Long> storeIds,
                                                          @Param("startDate") String startDate,
                                                          @Param("endDate") String endDate);
}
