# Implementation Plan: 智能零售用户行为分析系统

**Branch**: `001-retail-behavior-analysis` | **Date**: 2026-05-29 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/001-retail-behavior-analysis/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

构建一个智能零售用户行为分析系统，采用前后端分离架构（Vue 3 + SSM + MySQL），实现门店客流统计、热力图分析、停留时长分析、购买转化分析和可视化仪表盘。系统按 P1→P2→P3 优先级渐进交付，先完成用户权限与数据基础，再实现分析功能，最后整合仪表盘与导入导出。

## Technical Context

**Language/Version**: Java 8+ (Backend), JavaScript ES2020+ (Frontend)

**Primary Dependencies**: Spring 5 + Spring MVC + MyBatis-Plus 3.x (Backend), Vue 3 + Element Plus 2.x + Pinia 2.x + ECharts 5.x + Axios (Frontend)

**Storage**: MySQL 8.0+

**Testing**: JUnit 4/5 (Backend), Vitest (Frontend)

**Target Platform**: Web Browser (Chrome/Firefox/Edge latest 2 versions), Java Servlet Container (Tomcat 9+)

**Project Type**: Web application (frontend + backend)

**Performance Goals**: 
- API 响应 < 500ms (P95)
- 图表渲染 < 3s（1000 条数据以内）
- Excel 导入 1000 条 < 10s
- 仪表盘首屏加载 < 5s

**Constraints**:
- 前后端完全分离，通过 RESTful API 通信
- 支持 50 个门店、每门店 20 个区域的数据规模
- 单租户部署，无分布式需求
- 密码 BCrypt 加密存储，JWT 无状态认证

**Scale/Scope**: 9 个用户故事，41 条功能需求，6 张核心数据表，~25 个 API 端点，~15 个前端页面

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| # | Principle | Status | Evidence |
|---|-----------|--------|----------|
| I | 前后端分离架构 | ✅ PASS | Vue 3 前端 + SSM 后端，RESTful API + JSON，契约已在 contracts/ 定义 |
| II | 角色权限驱动 | ✅ PASS | admin/manager/analyst 三角色，JWT 拦截器校验，API 契约标注权限要求 |
| III | 数据驱动分析 | ✅ PASS | 所有分析基于 CustomerBehavior + ZoneStay 原始数据聚合，导入含完整校验规则 |
| IV | 渐进式交付 | ✅ PASS | P1(US1-3) → P2(US4-7) → P3(US8-9)，每阶段独立可交付 |
| V | 代码规范与质量 | ✅ PASS | 统一 API 响应格式 `{code, message, data}`，MyBatis 参数化查询，BCrypt 加密 |

**Post-Design Re-check**: ✅ All principles remain compliant after data model and API contract design.

## Project Structure

### Documentation (this feature)

```text
specs/001-retail-behavior-analysis/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output - 技术选型决策
├── data-model.md        # Phase 1 output - 6 张核心数据表 + ER 图
├── quickstart.md        # Phase 1 output - 环境搭建与启动指南
├── contracts/           # Phase 1 output - 25+ API 端点定义
│   └── api-contracts.md
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
backend/                          # Java Maven 项目
├── pom.xml                       # 父 POM（Spring + MyBatis-Plus + JWT + POI 等）
└── src/main/
    ├── java/com/retail/
    │   ├── controller/           # REST 控制器层
    │   │   ├── AuthController.java         # 登录/注册/获取当前用户
    │   │   ├── UserController.java         # 用户管理（管理员）
    │   │   ├── StoreController.java        # 门店 CRUD
    │   │   ├── ZoneController.java         # 区域管理
    │   │   ├── BehaviorController.java     # 行为数据录入/导入
    │   │   ├── AnalyticsController.java    # 客流/热力图/时长/转化率
    │   │   ├── ExportController.java       # Excel/PDF 导出
    │   │   └── UserAuthController.java     # 用户授权管理
    │   ├── service/              # 业务逻辑接口
    │   │   ├── UserService.java
    │   │   ├── StoreService.java
    │   │   ├── BehaviorService.java
    │   │   ├── AnalyticsService.java
    │   │   └── ExportService.java
    │   ├── service/impl/         # 业务逻辑实现
    │   ├── mapper/               # MyBatis Mapper 接口
    │   ├── entity/               # 实体类（User, Store, Zone, CustomerBehavior, ZoneStay, UserStoreAuth）
    │   ├── dto/                  # 数据传输对象（请求/响应）
    │   ├── config/               # Spring 配置（CORS, 文件上传, Jackson 等）
    │   ├── interceptor/          # JWT 认证拦截器 + 角色权限拦截器
    │   └── util/                 # 工具类（JWT 工具, Excel 工具, PDF 工具）
    └── resources/
        ├── mapper/               # MyBatis XML 映射文件
        ├── applicationContext.xml
        ├── spring-mvc.xml
        └── jdbc.properties

frontend/                         # Vue 3 + Vite 项目
├── src/
│   ├── views/                    # 页面组件
│   │   ├── login/                # 登录/注册页
│   │   ├── dashboard/            # 可视化仪表盘（P3）
│   │   ├── store/                # 门店管理 + 区域配置
│   │   ├── behavior/             # 行为数据录入 + 批量导入
│   │   ├── analytics/            # 分析页面
│   │   │   ├── Traffic.vue       # 客流统计
│   │   │   ├── Heatmap.vue       # 热力图分析
│   │   │   ├── Duration.vue      # 停留时长分析
│   │   │   └── Conversion.vue    # 购买转化分析
│   │   └── user/                 # 用户管理 + 角色分配（管理员）
│   ├── components/               # 可复用组件
│   │   ├── AppLayout.vue         # 主布局（侧边栏+顶栏）
│   │   ├── StoreSelector.vue     # 门店筛选器
│   │   ├── DateRangePicker.vue   # 日期范围选择器
│   │   ├── ZoneCanvas.vue        # 门店平面图渲染组件
│   │   └── StatCard.vue          # 统计卡片组件
│   ├── router/                   # Vue Router 路由配置（含角色守卫）
│   ├── stores/                   # Pinia 状态管理
│   │   ├── auth.js               # 认证状态（token, user info）
│   │   └── app.js                # 全局状态（当前门店等）
│   ├── api/                      # Axios 请求封装
│   │   ├── request.js            # Axios 实例（拦截器配置）
│   │   ├── auth.js               # 认证 API
│   │   ├── store.js              # 门店 API
│   │   ├── behavior.js           # 行为数据 API
│   │   └── analytics.js          # 分析统计 API
│   └── utils/                    # 工具函数
├── vite.config.js
└── package.json

specs/                            # Spec-Kit 文档
└── 001-retail-behavior-analysis/
    ├── spec.md
    ├── plan.md
    ├── research.md
    ├── data-model.md
    ├── quickstart.md
    ├── contracts/
    └── checklists/
```

**Structure Decision**: 采用 Web Application (Option 2) 结构。`backend/` 和 `frontend/` 完全独立，可并行开发。后端使用标准 Maven 三层架构（Controller → Service → Mapper），前端按功能模块组织 pages 和 api。

## Complexity Tracking

> 无 Constitution 违规项，此表为空。
