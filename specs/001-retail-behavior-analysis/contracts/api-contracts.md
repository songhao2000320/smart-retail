# API Contracts: 智能零售用户行为分析系统

**Feature**: 001-retail-behavior-analysis
**Date**: 2026-05-29
**Base URL**: `/api/v1`

## 通用约定

### 请求头

```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json (普通请求)
Content-Type: multipart/form-data (文件上传)
```

### 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

**状态码**:
- `200`: 成功
- `400`: 请求参数错误
- `401`: 未登录 / Token 过期
- `403`: 无权限
- `404`: 资源不存在
- `500`: 服务器内部错误

### 分页请求参数

```
GET /api/v1/resource?page=1&size=10
```

### 分页响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [...],
    "total": 100,
    "page": 1,
    "size": 10
  }
}
```

---

## 模块一：认证与用户管理

### 1.1 用户注册

```
POST /api/v1/auth/register
```

**Request Body**:
```json
{
  "username": "zhangsan",
  "password": "123456"
}
```

**Response** (200):
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "id": 1,
    "username": "zhangsan",
    "role": "manager"
  }
}
```

**校验规则**:
- username: 必填，3-50 字符，字母数字下划线
- password: 必填，6-20 字符

---

### 1.2 用户登录

```
POST /api/v1/auth/login
```

**Request Body**:
```json
{
  "username": "zhangsan",
  "password": "123456"
}
```

**Response** (200):
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "user": {
      "id": 1,
      "username": "zhangsan",
      "role": "manager",
      "storeId": 1
    }
  }
}
```

**错误响应** (400):
```json
{
  "code": 400,
  "message": "用户名或密码错误",
  "data": null
}
```

---

### 1.3 获取当前用户信息

```
GET /api/v1/auth/me
```

**Response** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "zhangsan",
    "role": "manager",
    "storeId": 1
  }
}
```

---

### 1.4 用户列表（管理员）

```
GET /api/v1/users?page=1&size=10&role=manager
```

**权限**: admin

**Response** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "username": "zhangsan",
        "role": "manager",
        "storeId": 1,
        "storeName": "朝阳门店",
        "status": 1,
        "createTime": "2026-05-29 10:00:00"
      }
    ],
    "total": 25,
    "page": 1,
    "size": 10
  }
}
```

---

### 1.5 修改用户角色（管理员）

```
PUT /api/v1/users/{userId}/role
```

**权限**: admin

**Request Body**:
```json
{
  "role": "analyst"
}
```

**Response** (200):
```json
{
  "code": 200,
  "message": "角色修改成功",
  "data": null
}
```

---

## 模块二：门店管理

### 2.1 门店列表

```
GET /api/v1/stores?page=1&size=10&keyword=朝阳
```

**权限**: admin（全部门店） / manager（仅自己门店）

**Response** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "name": "朝阳门店",
        "address": "北京市朝阳区xxx路100号",
        "phone": "010-12345678",
        "status": 1,
        "createTime": "2026-05-01 10:00:00"
      }
    ],
    "total": 50,
    "page": 1,
    "size": 10
  }
}
```

---

### 2.2 创建门店

```
POST /api/v1/stores
```

**权限**: admin

**Request Body**:
```json
{
  "name": "朝阳门店",
  "address": "北京市朝阳区xxx路100号",
  "phone": "010-12345678"
}
```

**Response** (200):
```json
{
  "code": 200,
  "message": "门店创建成功",
  "data": { "id": 1 }
}
```

---

### 2.3 编辑门店

```
PUT /api/v1/stores/{storeId}
```

**权限**: admin

**Request Body**:
```json
{
  "name": "朝阳门店（旗舰店）",
  "address": "北京市朝阳区xxx路100号",
  "phone": "010-87654321"
}
```

---

### 2.4 删除门店

```
DELETE /api/v1/stores/{storeId}
```

**权限**: admin

**Response** (200):
```json
{
  "code": 200,
  "message": "门店已删除",
  "data": null
}
```

---

### 2.5 门店详情

```
GET /api/v1/stores/{storeId}
```

**Response** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "朝阳门店",
    "address": "北京市朝阳区xxx路100号",
    "phone": "010-12345678",
    "status": 1,
    "zones": [
      {
        "id": 1,
        "name": "生鲜区",
        "posX": 10.5,
        "posY": 20.0,
        "width": 25.0,
        "height": 15.0
      }
    ]
  }
}
```

---

## 模块三：区域管理

### 3.1 门店区域列表

```
GET /api/v1/stores/{storeId}/zones
```

**Response** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "生鲜区",
      "posX": 10.5,
      "posY": 20.0,
      "width": 25.0,
      "height": 15.0,
      "status": 1
    }
  ]
}
```

---

### 3.2 添加区域

```
POST /api/v1/stores/{storeId}/zones
```

**权限**: admin / manager（自己的门店）

**Request Body**:
```json
{
  "name": "生鲜区",
  "posX": 10.5,
  "posY": 20.0,
  "width": 25.0,
  "height": 15.0
}
```

**校验**: posX/posY 范围 0-100，width/height > 0 且 <= 100

---

### 3.3 编辑区域

```
PUT /api/v1/zones/{zoneId}
```

**权限**: admin / manager（自己的门店）

**Request Body**:
```json
{
  "name": "生鲜蔬果区",
  "posX": 10.5,
  "posY": 20.0,
  "width": 30.0,
  "height": 15.0
}
```

---

### 3.4 删除区域

```
DELETE /api/v1/zones/{zoneId}
```

**权限**: admin / manager（自己的门店）

---

## 模块四：顾客行为数据

### 4.1 行为记录列表

```
GET /api/v1/behaviors?storeId=1&startDate=2026-05-01&endDate=2026-05-29&page=1&size=20
```

**权限**: admin / manager（自己的门店）/ analyst（授权门店）

**Response** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "storeId": 1,
        "storeName": "朝阳门店",
        "entryTime": "2026-05-29 10:00:00",
        "leaveTime": "2026-05-29 10:35:00",
        "isPurchased": 1,
        "zoneStays": [
          { "zoneId": 1, "zoneName": "生鲜区", "entryTime": "2026-05-29 10:02:00", "leaveTime": "2026-05-29 10:15:00" },
          { "zoneId": 2, "zoneName": "零食区", "entryTime": "2026-05-29 10:18:00", "leaveTime": "2026-05-29 10:30:00" }
        ],
        "createdBy": "zhangsan",
        "createTime": "2026-05-29 10:40:00"
      }
    ],
    "total": 500,
    "page": 1,
    "size": 20
  }
}
```

---

### 4.2 逐条录入行为记录

```
POST /api/v1/behaviors
```

**权限**: admin / manager（自己的门店）

**Request Body**:
```json
{
  "storeId": 1,
  "entryTime": "2026-05-29 10:00:00",
  "leaveTime": "2026-05-29 10:35:00",
  "isPurchased": 1,
  "zoneStays": [
    { "zoneId": 1, "entryTime": "2026-05-29 10:02:00", "leaveTime": "2026-05-29 10:15:00" },
    { "zoneId": 2, "entryTime": "2026-05-29 10:18:00", "leaveTime": "2026-05-29 10:30:00" }
  ]
}
```

**校验规则**:
- leaveTime > entryTime
- zoneStays 中每个 entryTime >= behavior.entryTime
- zoneStays 中每个 leaveTime <= behavior.leaveTime
- zoneStays 中每个 leaveTime > entryTime
- zoneStays 中 zoneId 必须属于 storeId 门店

---

### 4.3 批量导入 Excel

```
POST /api/v1/behaviors/import
```

**权限**: admin / manager（自己的门店）

**Content-Type**: multipart/form-data

**Request**:
- `file`: Excel 文件 (.xlsx)
- `storeId`: 门店 ID

**Response** (200):
```json
{
  "code": 200,
  "message": "导入完成",
  "data": {
    "total": 1000,
    "success": 950,
    "failed": 50,
    "failures": [
      { "row": 5, "reason": "离开时间早于进店时间" },
      { "row": 12, "reason": "区域ID不存在: 99" }
    ]
  }
}
```

**约束**: 文件大小 ≤ 10MB

---

### 4.4 下载导入模板

```
GET /api/v1/behaviors/template
```

**Response**: Excel 文件流（Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet）

---

## 模块五：分析统计

### 5.1 客流统计

```
GET /api/v1/analytics/traffic?storeId=1&startDate=2026-05-01&endDate=2026-05-29&granularity=day
```

**参数**:
- `granularity`: hour / day / week / month

**权限**: admin / manager（自己的门店）/ analyst（授权门店）

**Response** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": [
    { "period": "2026-05-01", "visitorCount": 150 },
    { "period": "2026-05-02", "visitorCount": 180 }
  ]
}
```

---

### 5.2 热力图数据

```
GET /api/v1/analytics/heatmap?storeId=1&startDate=2026-05-01&endDate=2026-05-29&mode=count
```

**参数**:
- `mode`: count（按停留次数）/ duration（按停留时长，秒）

**Response** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "zoneId": 1,
      "zoneName": "生鲜区",
      "posX": 10.5,
      "posY": 20.0,
      "width": 25.0,
      "height": 15.0,
      "value": 350,
      "maxValue": 500
    }
  ]
}
```

**说明**: `value` 为热力值，`maxValue` 为该门店全部区域中的最大值，前端根据 value/maxValue 比例映射颜色梯度。

---

### 5.3 停留时长统计

```
GET /api/v1/analytics/duration?storeId=1&startDate=2026-05-01&endDate=2026-05-29
```

**Response** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "avgDuration": 2100,
    "zones": [
      { "zoneId": 1, "zoneName": "生鲜区", "totalDuration": 54000, "percentage": 45.0 },
      { "zoneId": 2, "zoneName": "零食区", "totalDuration": 36000, "percentage": 30.0 }
    ]
  }
}
```

**说明**: avgDuration 单位为秒。

---

### 5.4 多门店停留时长对比（雷达图）

```
GET /api/v1/analytics/duration/compare?storeIds=1,2,3&startDate=2026-05-01&endDate=2026-05-29
```

**Response** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "zones": ["生鲜区", "零食区", "收银台", "日用品区"],
    "stores": [
      {
        "storeId": 1,
        "storeName": "朝阳门店",
        "durations": [54000, 36000, 12000, 18000]
      },
      {
        "storeId": 2,
        "storeName": "海淀门店",
        "durations": [62000, 28000, 15000, 20000]
      }
    ]
  }
}
```

---

### 5.5 购买转化率

```
GET /api/v1/analytics/conversion?storeId=1&startDate=2026-05-01&endDate=2026-05-29
```

**Response** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": [
    { "date": "2026-05-01", "visitorCount": 150, "purchaseCount": 45, "rate": 30.0 },
    { "date": "2026-05-02", "visitorCount": 180, "purchaseCount": 60, "rate": 33.3 }
  ]
}
```

**说明**: rate 为百分比（0-100），visitorCount=0 时不返回该日数据。

---

### 5.6 多门店转化率对比

```
GET /api/v1/analytics/conversion/compare?storeIds=1,2,3&startDate=2026-05-01&endDate=2026-05-29
```

**Response** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "dates": ["2026-05-01", "2026-05-02"],
    "stores": [
      { "storeId": 1, "storeName": "朝阳门店", "rates": [30.0, 33.3] },
      { "storeId": 2, "storeName": "海淀门店", "rates": [28.5, 31.2] }
    ]
  }
}
```

---

### 5.7 仪表盘汇总

```
GET /api/v1/analytics/dashboard?storeId=1
```

**Response** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "todayTraffic": 150,
    "todayConversionRate": 30.0,
    "heatmapPreview": [
      { "zoneId": 1, "zoneName": "生鲜区", "posX": 10.5, "posY": 20.0, "width": 25.0, "height": 15.0, "value": 350, "maxValue": 500 }
    ],
    "durationDistribution": [
      { "zoneId": 1, "zoneName": "生鲜区", "percentage": 45.0 }
    ]
  }
}
```

---

## 模块六：数据导出

### 6.1 统计报表 Excel 导出

```
GET /api/v1/export/traffic?storeId=1&startDate=2026-05-01&endDate=2026-05-29&type=traffic
```

**参数**:
- `type`: traffic / conversion / duration

**Response**: Excel 文件流

---

### 6.2 仪表盘 PDF 导出

```
GET /api/v1/export/dashboard?storeId=1
```

**Response**: PDF 文件流

---

## 模块七：用户授权管理

### 7.1 为用户授权门店

```
POST /api/v1/user-auth
```

**权限**: admin

**Request Body**:
```json
{
  "userId": 5,
  "storeIds": [1, 2, 3]
}
```

---

### 7.2 获取用户授权门店列表

```
GET /api/v1/user-auth/{userId}
```

**权限**: admin

**Response** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": [
    { "storeId": 1, "storeName": "朝阳门店" },
    { "storeId": 2, "storeName": "海淀门店" }
  ]
}
```
