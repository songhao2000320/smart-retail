package com.retail.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.retail.dto.ApiResponse;
import com.retail.entity.CustomerBehavior;
import com.retail.entity.ZoneStay;
import java.util.List;
import java.util.Map;

public interface BehaviorService {

    /** 分页查询行为记录 */
    IPage<CustomerBehavior> page(int page, int size, Map<String, Object> params);

    /** 创建行为记录 */
    ApiResponse create(CustomerBehavior behavior, List<Map<String, Object>> zoneStays);

    /** 更新行为记录 */
    ApiResponse update(CustomerBehavior behavior, List<Map<String, Object>> zoneStays);

    /** 获取行为详情（含区域停留） */
    CustomerBehavior getById(Long id);

    /** 获取区域停留记录 */
    List<ZoneStay> getZoneStays(Long behaviorId);

    /** 删除行为记录 */
    ApiResponse delete(Long id);

    /** 批量导入 Excel */
    ApiResponse importExcel(List<Map<String, Object>> records);
}
