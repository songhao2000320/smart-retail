# Research: 智能零售用户行为分析系统

**Feature**: 001-retail-behavior-analysis
**Date**: 2026-05-29
**Status**: Complete

## 技术选型决策

### 1. 前端框架

**Decision**: Vue 3 + Element Plus + Pinia + ECharts

**Rationale**: 
- Constitution 明确要求使用 Vue 生态
- Vue 3 Composition API 提供更好的 TypeScript 支持和代码组织
- Element Plus 是 Vue 3 生态最成熟的桌面端 UI 组件库，适合后台管理系统
- Pinia 是 Vue 官方推荐的状态管理方案，比 Vuex 更轻量
- ECharts 对柱状图、折线图、饼图、雷达图、热力图均有原生支持，无需引入额外图表库

**Alternatives considered**:
- React + Ant Design：不符合 Constitution 的技术栈约束
- V-charts/ECharts 封装库：过度封装可能导致热力图等定制需求难以实现
- D3.js：过于底层，开发效率低，不适合快速交付

### 2. 后端框架

**Decision**: Spring 5 + Spring MVC + MyBatis-Plus 3.x

**Rationale**:
- Constitution 明确要求使用 SSM 框架
- MyBatis-Plus 在 MyBatis 基础上提供分页插件、自动填充、逻辑删除等开箱即用功能
- Spring MVC RESTful 支持成熟，配合 Jackson 实现 JSON 序列化
- Maven 作为构建工具，依赖管理清晰

**Alternatives considered**:
- Spring Boot：虽然更现代化，但 Constitution 要求 SSM 传统三层架构，且用户可能对 XML 配置方式更熟悉
- JPA/Hibernate：自动建表能力虽好，但复杂查询灵活性不如 MyBatis
- Gradle：国内 Maven 使用更广泛，中央仓库镜像更完善

### 3. 数据库

**Decision**: MySQL 8.0+

**Rationale**:
- Constitution 明确要求 MySQL
- 8.0 版本支持窗口函数、CTE（用于客流统计的时间聚合计算）
- JSON 字段支持（可用于存储顾客停留区域列表等半结构化数据）

**Alternatives considered**:
- PostgreSQL：功能更强但 Constitution 已指定 MySQL
- MongoDB：不适合需要强关联查询和事务的业务场景

### 4. 认证方案

**Decision**: JWT (JSON Web Token) + 拦截器校验

**Rationale**:
- 前后端分离架构下，JWT 是无状态认证的最佳实践
- Token 中携带用户 ID 和角色，前端存储于 localStorage，后端拦截器解析校验
- 支持 Token 过期自动刷新机制
- 结合 Spring MVC 拦截器实现细粒度的 API 权限控制

**Alternatives considered**:
- Session + Cookie：不适合前后端分离架构，存在跨域问题
- OAuth2：过于重量级，本系统为单租户内部系统，不需要第三方授权

### 5. 文件处理

**Decision**: Apache POI 5.x（Excel）+ iText 7（PDF）

**Rationale**:
- Apache POI 是 Java 生态 Excel 处理的事实标准，支持 .xlsx 读写
- iText 7 支持 HTML→PDF 转换，可将仪表盘页面渲染为 PDF
- 两者 Maven 依赖成熟，社区文档丰富

**Alternatives considered**:
- EasyExcel（阿里）：流式读写性能更好，但 API 定制性不如 POI 直接
- Apache PDFBox：免费但 HTML→PDF 能力弱于 iText

### 6. 项目结构

**Decision**: Maven 多模块项目（父 POM + backend 模块），前端独立目录

**Rationale**:
- 前后端分离，前端 Vue 项目与后端 Java 项目独立构建部署
- Maven 多模块便于未来拆分微服务
- 前端使用 Vite 作为构建工具（Vue 3 官方推荐，替代 webpack）

**Project Layout**:
```
retail-behavior-analysis/
├── backend/                    # Maven 项目根目录
│   ├── pom.xml                 # 父 POM
│   └── src/
│       └── main/
│           ├── java/com/retail/
│           │   ├── controller/  # 控制器层
│           │   ├── service/     # 服务层
│           │   │   └── impl/
│           │   ├── mapper/      # MyBatis Mapper
│           │   ├── entity/      # 实体类
│           │   ├── dto/         # 数据传输对象
│           │   ├── config/      # 配置类
│           │   ├── interceptor/ # 拦截器
│           │   └── util/        # 工具类
│           └── resources/
│               ├── mapper/      # MyBatis XML
│               └── applicationContext.xml
├── frontend/                   # Vue 3 项目
│   ├── src/
│   │   ├── views/              # 页面组件
│   │   ├── components/         # 通用组件
│   │   ├── router/             # 路由配置
│   │   ├── stores/             # Pinia 状态管理
│   │   ├── api/                # API 请求封装
│   │   └── utils/              # 工具函数
│   └── vite.config.js
└── specs/                      # Spec-Kit 文档
```

## 风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|---------|
| 热力图在门店区域布局坐标与前端渲染的对齐偏差 | 热力图位置不准确 | 使用百分比坐标体系（0-100%），前端自适应缩放 |
| Excel 大批量导入性能瓶颈 | 导入超时或内存溢出 | POI 使用 SXSSF 流式写入，批量导入分批处理（每批 500 条） |
| JWT Token 无状态导致无法强制下线 | 安全风险 | Token 有效期设为 2 小时，配合刷新 Token 机制 |
| 统计查询在数据量大时性能下降 | 页面响应慢 | 建立合理的索引，复杂统计使用定时任务预计算汇总表 |
