# Data Model: 智能零售用户行为分析系统

**Feature**: 001-retail-behavior-analysis
**Date**: 2026-05-29

## Entity Relationship Diagram

```
┌──────────────┐       ┌──────────────┐       ┌──────────────────┐
│     User     │       │    Store     │       │ UserStoreAuth    │
├──────────────┤       ├──────────────┤       ├──────────────────┤
│ id (PK)      │       │ id (PK)      │       │ id (PK)          │
│ username     │       │ name         │       │ user_id (FK)     │
│ password     │  ┌────│ address      │       │ store_id (FK)    │
│ role         │  │    │ phone        │       │ create_time      │
│ store_id (FK)│──┘    │ status       │       └──────────────────┘
│ status       │       │ create_time  │
│ create_time  │       │ update_time  │              ▲
│ update_time  │       └──────────────┘              │
└──────────────┘              │                      │
       ▲                      │ 1:N                  │ N:1
       │                      ▼                      │
       │              ┌──────────────┐               │
       │              │    Zone      │               │
       │              ├──────────────┤               │
       │              │ id (PK)      │               │
       │              │ store_id (FK)│               │
       │              │ name         │               │
       │              │ pos_x        │               │
       │              │ pos_y        │               │
       │              │ width        │               │
       │              │ height       │               │
       │              │ status       │               │
       │              └──────────────┘               │
       │                      │                      │
       │                      │ 1:N                  │
       │                      ▼                      │
       │              ┌──────────────────┐           │
       │              │ CustomerBehavior │           │
       │              ├──────────────────┤           │
       │              │ id (PK)          │           │
       └──────────────│ store_id (FK)    │───────────┘
        (created_by)  │ entry_time       │
                      │ leave_time       │
                      │ is_purchased     │
                      │ created_by (FK)  │
                      │ create_time      │
                      └──────────────────┘
                              │
                              │ 1:N
                              ▼
                      ┌──────────────┐
                      │  ZoneStay    │
                      ├──────────────┤
                      │ id (PK)      │
                      │ behavior_id  │
                      │ zone_id (FK) │
                      │ entry_time   │
                      │ leave_time   │
                      └──────────────┘
```

## Entity Definitions

### 1. User (用户表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| username | VARCHAR(50) | NOT NULL, UNIQUE | 用户名 |
| password | VARCHAR(255) | NOT NULL | BCrypt 加密密码 |
| role | VARCHAR(20) | NOT NULL | 角色：admin / manager / analyst |
| store_id | BIGINT | NULLABLE, FK→store.id | 所属门店（店长必填，管理员/分析师可为空） |
| status | TINYINT | NOT NULL, DEFAULT 1 | 状态：1=正常, 0=锁定 |
| locked_until | DATETIME | NULLABLE | 锁定截止时间 |
| login_fail_count | INT | DEFAULT 0 | 连续登录失败次数 |
| create_time | DATETIME | NOT NULL | 创建时间 |
| update_time | DATETIME | NOT NULL | 更新时间 |

**索引**: `idx_username` (username), `idx_store_id` (store_id)

### 2. Store (门店表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| name | VARCHAR(100) | NOT NULL | 门店名称 |
| address | VARCHAR(255) | NULLABLE | 门店地址 |
| phone | VARCHAR(20) | NULLABLE | 联系电话 |
| status | TINYINT | NOT NULL, DEFAULT 1 | 状态：1=正常, 0=已删除（逻辑删除） |
| create_time | DATETIME | NOT NULL | 创建时间 |
| update_time | DATETIME | NOT NULL | 更新时间 |

**索引**: `idx_status` (status)

### 3. Zone (功能区域表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| store_id | BIGINT | NOT NULL, FK→store.id | 所属门店 |
| name | VARCHAR(50) | NOT NULL | 区域名称（如"生鲜区"） |
| pos_x | DOUBLE | NOT NULL | 平面 X 坐标（百分比，0-100） |
| pos_y | DOUBLE | NOT NULL | 平面 Y 坐标（百分比，0-100） |
| width | DOUBLE | NOT NULL | 区域宽度（百分比） |
| height | DOUBLE | NOT NULL | 区域高度（百分比） |
| status | TINYINT | NOT NULL, DEFAULT 1 | 状态：1=正常, 0=已删除 |
| create_time | DATETIME | NOT NULL | 创建时间 |
| update_time | DATETIME | NOT NULL | 更新时间 |

**索引**: `idx_store_id` (store_id)

### 4. CustomerBehavior (顾客行为记录表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| store_id | BIGINT | NOT NULL, FK→store.id | 所属门店 |
| entry_time | DATETIME | NOT NULL | 进店时间 |
| leave_time | DATETIME | NOT NULL | 离开时间 |
| is_purchased | TINYINT | NOT NULL, DEFAULT 0 | 是否购买：1=是, 0=否 |
| created_by | BIGINT | NOT NULL, FK→user.id | 录入人 ID |
| status | TINYINT | NOT NULL, DEFAULT 1 | 状态：1=有效, 0=无效（门店删除后标记） |
| create_time | DATETIME | NOT NULL | 创建时间 |

**索引**: `idx_store_entry` (store_id, entry_time), `idx_entry_time` (entry_time), `idx_store_status` (store_id, status)

**校验规则**:
- `leave_time` > `entry_time`
- 停留时长 = `leave_time` - `entry_time`

### 5. ZoneStay (区域停留记录表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| behavior_id | BIGINT | NOT NULL, FK→customer_behavior.id | 关联行为记录 |
| zone_id | BIGINT | NOT NULL, FK→zone.id | 停留区域 |
| entry_time | DATETIME | NOT NULL | 进入该区域时间 |
| leave_time | DATETIME | NOT NULL | 离开该区域时间 |

**索引**: `idx_behavior_id` (behavior_id), `idx_zone_id` (zone_id)

**校验规则**:
- `leave_time` > `entry_time`
- `entry_time` >= 关联 behavior 的 `entry_time`
- `leave_time` <= 关联 behavior 的 `leave_time`

### 6. UserStoreAuth (用户门店授权表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| user_id | BIGINT | NOT NULL, FK→user.id | 用户 ID（数据分析师） |
| store_id | BIGINT | NOT NULL, FK→store.id | 授权门店 ID |
| create_time | DATETIME | NOT NULL | 创建时间 |

**索引**: `idx_user_id` (user_id), `idx_store_id` (store_id), `uk_user_store` (user_id, store_id) UNIQUE

## 数据流关系

```
管理员 ──创建──▶ 门店 ──包含──▶ 功能区域
  │
  └──分配──▶ 用户角色（admin/manager/analyst）
                │
                ▼
店长 ──录入──▶ 顾客行为记录 ──包含──▶ 区域停留记录
                │                       │
                ▼                       ▼
          客流统计 ◀────────── 按门店/时间聚合
          热力图   ◀────────── 按区域/停留次数聚合
          停留分析 ◀────────── 按区域/停留时长聚合
          转化率   ◀────────── 按购买标记统计
```

## 状态转换

### 用户状态
```
[正常] ──(连续5次登录失败)──▶ [锁定] ──(15分钟后)──▶ [正常]
```

### 门店状态
```
[正常] ──(管理员删除)──▶ [已删除]
  │                        │
  └── 关联行为数据有效      └── 关联行为数据标记为无效
```

## 预计算汇总表（可选优化）

当数据量增长后，可增加以下预计算表以加速查询：

- `daily_traffic_stats`: 每日客流统计汇总（store_id, stat_date, visitor_count）
- `zone_heatmap_daily`: 每日区域热力汇总（zone_id, stat_date, stay_count, total_duration）

v1 阶段不强制实现预计算表，直接基于原始数据实时聚合。
