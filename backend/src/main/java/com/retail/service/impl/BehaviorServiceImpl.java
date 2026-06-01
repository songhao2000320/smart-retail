package com.retail.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.retail.dto.ApiResponse;
import com.retail.entity.CustomerBehavior;
import com.retail.entity.ZoneStay;
import com.retail.mapper.CustomerBehaviorMapper;
import com.retail.mapper.UserStoreAuthMapper;
import com.retail.mapper.ZoneStayMapper;
import com.retail.service.BehaviorService;
import com.retail.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BehaviorServiceImpl extends ServiceImpl<CustomerBehaviorMapper, CustomerBehavior> implements BehaviorService {

    @Autowired
    private ZoneStayMapper zoneStayMapper;

    @Autowired(required = false)
    private UserStoreAuthMapper userStoreAuthMapper;

    @Override
    public IPage<CustomerBehavior> page(int page, int size, Map<String, Object> params) {
        // analyst 角色数据权限：自动限定为授权门店
        if ("analyst".equals(UserContext.getRole()) && userStoreAuthMapper != null) {
            List<Map<String, Object>> authList = userStoreAuthMapper.selectByUserId(UserContext.getUserId());
            if (authList != null && !authList.isEmpty()) {
                List<Long> storeIds = authList.stream()
                        .map(m -> ((Number) m.get("store_id")).longValue())
                        .distinct()
                        .collect(Collectors.toList());
                params.put("storeIds", storeIds);
            } else {
                // analyst 无任何授权 → 返回空结果
                return new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
            }
        }
        Page<CustomerBehavior> p = new Page<>(page, size);
        return baseMapper.selectPageWithDetails(p, params);
    }

    @Override
    @Transactional
    public ApiResponse create(CustomerBehavior behavior, List<Map<String, Object>> zoneStays) {
        if (behavior.getStoreId() == null) {
            return ApiResponse.error("请选择门店");
        }
        if (behavior.getEntryTime() == null) {
            return ApiResponse.error("请填写进店时间");
        }
        if (behavior.getLeaveTime() == null) {
            return ApiResponse.error("请填写离开时间");
        }
        if (behavior.getLeaveTime().isBefore(behavior.getEntryTime())) {
            return ApiResponse.error("离开时间不能早于进店时间");
        }

        behavior.setCreatedBy(UserContext.getUserId());
        behavior.setStatus(1);
        behavior.setCreateTime(new Date());

        if (behavior.getIsPurchased() == null) {
            behavior.setIsPurchased(0);
        }

        baseMapper.insert(behavior);

        // 保存区域停留记录
        if (zoneStays != null && !zoneStays.isEmpty()) {
            for (Map<String, Object> zs : zoneStays) {
                ZoneStay stay = new ZoneStay();
                stay.setBehaviorId(behavior.getId());
                stay.setZoneId(Long.valueOf(zs.get("zoneId").toString()));
                stay.setEntryTime(LocalDateTime.parse(zs.get("entryTime").toString().replace(" ", "T")));
                stay.setLeaveTime(LocalDateTime.parse(zs.get("leaveTime").toString().replace(" ", "T")));
                zoneStayMapper.insert(stay);
            }
        }

        return ApiResponse.success("添加成功");
    }

    @Override
    @Transactional
    public ApiResponse update(CustomerBehavior behavior, List<Map<String, Object>> zoneStays) {
        CustomerBehavior existing = baseMapper.selectById(behavior.getId());
        if (existing == null) {
            return ApiResponse.error("记录不存在");
        }

        existing.setStoreId(behavior.getStoreId());
        existing.setEntryTime(behavior.getEntryTime());
        existing.setLeaveTime(behavior.getLeaveTime());
        existing.setIsPurchased(behavior.getIsPurchased() != null ? behavior.getIsPurchased() : 0);
        baseMapper.updateById(existing);

        // 先删后插区域停留
        LambdaQueryWrapper<ZoneStay> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ZoneStay::getBehaviorId, behavior.getId());
        zoneStayMapper.delete(wrapper);

        if (zoneStays != null && !zoneStays.isEmpty()) {
            for (Map<String, Object> zs : zoneStays) {
                ZoneStay stay = new ZoneStay();
                stay.setBehaviorId(behavior.getId());
                stay.setZoneId(Long.valueOf(zs.get("zoneId").toString()));
                stay.setEntryTime(LocalDateTime.parse(zs.get("entryTime").toString().replace(" ", "T")));
                stay.setLeaveTime(LocalDateTime.parse(zs.get("leaveTime").toString().replace(" ", "T")));
                zoneStayMapper.insert(stay);
            }
        }

        return ApiResponse.success("更新成功");
    }

    @Override
    public CustomerBehavior getById(Long id) {
        CustomerBehavior behavior = baseMapper.selectById(id);
        if (behavior != null && behavior.getEntryTime() != null && behavior.getLeaveTime() != null) {
            long minutes = java.time.Duration.between(behavior.getEntryTime(), behavior.getLeaveTime()).toMinutes();
            behavior.setStayDurationMinutes((int) minutes);
        }
        return behavior;
    }

    @Override
    public List<ZoneStay> getZoneStays(Long behaviorId) {
        return zoneStayMapper.selectByBehaviorId(behaviorId);
    }

    @Override
    @Transactional
    public ApiResponse delete(Long id) {
        CustomerBehavior behavior = baseMapper.selectById(id);
        if (behavior == null) {
            return ApiResponse.error("记录不存在");
        }
        // 逻辑删除：MyBatis-Plus 自动将 status 设为 0
        baseMapper.deleteById(id);

        // 删除关联区域停留
        LambdaQueryWrapper<ZoneStay> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ZoneStay::getBehaviorId, id);
        zoneStayMapper.delete(wrapper);

        return ApiResponse.success("删除成功");
    }

    @Override
    @Transactional
    public ApiResponse importExcel(List<Map<String, Object>> records) {
        if (records == null || records.isEmpty()) {
            return ApiResponse.error("没有可导入的数据");
        }

        Long userId = UserContext.getUserId();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        int successCount = 0;
        int skipCount = 0;
        StringBuilder errors = new StringBuilder();

        for (int i = 0; i < records.size(); i++) {
            Map<String, Object> record = records.get(i);
            try {
                CustomerBehavior behavior = new CustomerBehavior();

                Object storeIdObj = record.get("storeId");
                if (storeIdObj == null) {
                    skipCount++;
                    errors.append("第").append(i + 1).append("条: 门店ID为空; ");
                    continue;
                }
                behavior.setStoreId(Long.valueOf(storeIdObj.toString()));

                String entryTimeStr = String.valueOf(record.get("entryTime"));
                String leaveTimeStr = String.valueOf(record.get("leaveTime"));
                behavior.setEntryTime(LocalDateTime.parse(entryTimeStr, fmt));
                behavior.setLeaveTime(LocalDateTime.parse(leaveTimeStr, fmt));

                if (behavior.getLeaveTime().isBefore(behavior.getEntryTime())) {
                    skipCount++;
                    errors.append("第").append(i + 1).append("条: 离开时间早于进店时间; ");
                    continue;
                }

                Object isPurchasedObj = record.get("isPurchased");
                behavior.setIsPurchased(isPurchasedObj != null ? Integer.valueOf(isPurchasedObj.toString()) : 0);
                behavior.setCreatedBy(userId);
                behavior.setStatus(1);
                behavior.setCreateTime(new Date());

                baseMapper.insert(behavior);
                successCount++;

                // 保存区域停留记录
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> zoneStays = (List<Map<String, Object>>) record.get("zoneStays");
                if (zoneStays != null && !zoneStays.isEmpty()) {
                    for (Map<String, Object> zs : zoneStays) {
                        ZoneStay stay = new ZoneStay();
                        stay.setBehaviorId(behavior.getId());
                        stay.setZoneId(Long.valueOf(zs.get("zoneId").toString()));
                        stay.setEntryTime(LocalDateTime.parse(String.valueOf(zs.get("entryTime")).replace(" ", "T")));
                        stay.setLeaveTime(LocalDateTime.parse(String.valueOf(zs.get("leaveTime")).replace(" ", "T")));
                        zoneStayMapper.insert(stay);
                    }
                }
            } catch (Exception e) {
                skipCount++;
                errors.append("第").append(i + 1).append("条: ").append(e.getMessage()).append("; ");
            }
        }

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("successCount", successCount);
        result.put("skipCount", skipCount);
        if (errors.length() > 0) {
            result.put("errors", errors.toString());
        }

        String msg = "导入完成：成功 " + successCount + " 条" + (skipCount > 0 ? "，跳过 " + skipCount + " 条" : "");
        return ApiResponse.success(msg, result);
    }
}
