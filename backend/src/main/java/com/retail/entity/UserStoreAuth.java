package com.retail.entity;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 用户门店授权关系表
 * 数据分析师可以查看多个门店的数据
 */
@TableName("t_user_store_auth")
public class UserStoreAuth extends BaseEntity {

    private Long userId;
    private Long storeId;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }
}
