package com.retail.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 顾客行为记录实体
 */
@TableName("t_customer_behavior")
public class CustomerBehavior implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属门店ID */
    private Long storeId;

    /** 进店时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime entryTime;

    /** 离开时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime leaveTime;

    /** 是否购买：1=是, 0=否 */
    private Integer isPurchased;

    /** 录入人ID */
    private Long createdBy;

    /** 状态：1=有效, 0=无效 */
    @TableLogic
    private Integer status;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    // ===== 非数据库字段：关联查询 =====
    private transient String storeName;
    private transient String creatorName;
    private transient Integer stayDurationMinutes;
    private transient String zoneNames;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }

    public LocalDateTime getEntryTime() { return entryTime; }
    public void setEntryTime(LocalDateTime entryTime) { this.entryTime = entryTime; }

    public LocalDateTime getLeaveTime() { return leaveTime; }
    public void setLeaveTime(LocalDateTime leaveTime) { this.leaveTime = leaveTime; }

    public Integer getIsPurchased() { return isPurchased; }
    public void setIsPurchased(Integer isPurchased) { this.isPurchased = isPurchased; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    public Integer getStayDurationMinutes() { return stayDurationMinutes; }
    public void setStayDurationMinutes(Integer stayDurationMinutes) { this.stayDurationMinutes = stayDurationMinutes; }

    public String getZoneNames() { return zoneNames; }
    public void setZoneNames(String zoneNames) { this.zoneNames = zoneNames; }
}
