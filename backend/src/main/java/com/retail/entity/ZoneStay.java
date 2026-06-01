package com.retail.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 区域停留记录实体
 */
@TableName("t_zone_stay")
public class ZoneStay implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联行为记录ID */
    private Long behaviorId;

    /** 停留区域ID */
    private Long zoneId;

    /** 进入该区域时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime entryTime;

    /** 离开该区域时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime leaveTime;

    // ===== 非数据库字段 =====
    private transient String zoneName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBehaviorId() { return behaviorId; }
    public void setBehaviorId(Long behaviorId) { this.behaviorId = behaviorId; }

    public Long getZoneId() { return zoneId; }
    public void setZoneId(Long zoneId) { this.zoneId = zoneId; }

    public LocalDateTime getEntryTime() { return entryTime; }
    public void setEntryTime(LocalDateTime entryTime) { this.entryTime = entryTime; }

    public LocalDateTime getLeaveTime() { return leaveTime; }
    public void setLeaveTime(LocalDateTime leaveTime) { this.leaveTime = leaveTime; }

    public String getZoneName() { return zoneName; }
    public void setZoneName(String zoneName) { this.zoneName = zoneName; }
}
