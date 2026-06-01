<!--
Sync Impact Report
==================
Version change: 0.0.0 → 1.0.0 (initial constitution)
Modified principles: N/A (initial creation)
Added sections:
  - Core Principles (5 principles)
  - Technical Standards
  - Development Workflow
  - Governance
Removed sections: N/A
Templates requiring updates:
  - .specify/templates/plan-template.md ✅ aligned (no changes needed)
  - .specify/templates/spec-template.md ✅ aligned (no changes needed)
  - .specify/templates/tasks-template.md ✅ aligned (no changes needed)
  - .specify/templates/checklist-template.md ✅ aligned (no changes needed)
Follow-up TODOs: None
-->

# 智能零售用户行为分析系统 Constitution

## Core Principles

### I. 前后端分离架构 (Front-Backend Separation)

所有功能 MUST 遵循前后端分离原则：
- 前端使用 Vue 生态（Vue 3 + Vue Router + Vuex/Pinia + Element Plus），仅负责 UI 展示与用户交互
- 后端使用 SSM 框架（Spring + Spring MVC + MyBatis），仅负责业务逻辑与数据持久化
- 前后端通过 RESTful API 通信，统一使用 JSON 格式
- 前端 MUST NOT 直接访问数据库；后端 MUST NOT 包含 UI 渲染逻辑
- 所有 API 接口 MUST 在开发前完成契约定义（URL、Method、Request/Response Schema）

**理由**：确保职责清晰、可独立开发测试、便于后续维护与部署

### II. 角色权限驱动 (Role-Based Access Control)

系统 MUST 以角色权限模型驱动功能设计：
- 三种角色：管理员（admin）、店长（manager）、数据分析师（analyst）
- 每个 API 端点 MUST 声明所需角色权限，后端中间件 MUST 校验
- 前端路由 MUST 按角色过滤，不同角色看到不同菜单与页面
- 数据范围 MUST 按角色隔离：管理员可见全部门店数据，店长仅可见所属门店，分析师可见被授权门店

**理由**：确保数据安全与功能边界清晰，是多租户场景的核心约束

### III. 数据驱动分析 (Data-Driven Analytics)

分析功能 MUST 以结构化数据为基础：
- 所有分析（客流、热力图、停留时长、购买转化）MUST 基于数据库中的原始行为记录计算，禁止硬编码或静态模拟数据
- 统计结果 MUST 可复现：同一查询条件、同一时间范围产生相同结果
- 数据导入 MUST 支持校验：格式校验、字段完整性校验、业务逻辑校验（如离开时间不得早于进店时间）
- 可视化组件 MUST 支持交互式筛选（时间范围、门店、区域）

**理由**：保证分析结果的准确性与可信度，避免"演示系统"与实际脱节

### IV. 渐进式交付 (Incremental Delivery)

功能开发 MUST 按优先级渐进交付：
- P1（MVP 核心）：登录注册与角色权限 → 门店与区域管理 → 顾客行为数据录入
- P2（分析核心）：客流统计 → 热力图分析 → 停留时长分析 → 购买转化分析
- P3（体验增强）：可视化仪表盘 → 数据导入导出
- 每个优先级阶段完成后 MUST 可独立演示和验证
- 后续阶段 MUST NOT 破坏已完成的阶段功能

**理由**：确保每个阶段都有可交付价值，降低开发风险

### V. 代码规范与质量 (Code Quality Standards)

所有代码 MUST 遵循统一规范：
- 后端 Java 代码 MUST 遵循阿里巴巴 Java 开发规范
- 前端 Vue 代码 MUST 使用 ESLint + Prettier 统一格式化
- 数据库表名与字段名 MUST 使用下划线命名（snake_case），Java 实体类使用驼峰命名（camelCase）
- 每个 Service 层方法 MUST 有清晰的职责边界，单一职责原则
- API 响应 MUST 统一封装为 `{ code, message, data }` 格式
- 所有数据库操作 MUST 使用 MyBatis 参数化查询，禁止拼接 SQL

**理由**：保证代码可读性、可维护性和安全性

## Technical Standards

### 技术栈

| 层级 | 技术 | 版本要求 |
|------|------|---------|
| 前端框架 | Vue 3 | ^3.x |
| UI 组件库 | Element Plus | ^2.x |
| 状态管理 | Pinia | ^2.x |
| 图表可视化 | ECharts | ^5.x |
| 后端框架 | Spring + Spring MVC | 5.x |
| ORM | MyBatis / MyBatis-Plus | 3.x |
| 数据库 | MySQL | 8.0+ |
| 构建工具 | Maven | 3.6+ |
| JDK | Java | 8+ / 11+ |
| 文件处理 | Apache POI | 5.x |
| PDF 生成 | iText / Apache PDFBox | 最新稳定版 |

### 数据库设计约束

- 每张表 MUST 包含 `id`（主键自增）、`create_time`、`update_time` 字段
- 关联关系 MUST 使用逻辑外键（业务层维护），不使用数据库物理外键
- 敏感字段（如密码）MUST 加密存储（BCrypt）
- 索引 MUST 建在查询频繁的字段上（门店 ID、时间范围字段、用户 ID）

### API 设计约束

- URL 前缀：`/api/v1/`
- RESTful 风格：资源名用复数名词
- 分页请求统一参数：`page`（页码）、`size`（每页条数）
- 分页响应统一格式：`{ code: 200, message: "success", data: { records: [], total: 0, page: 1, size: 10 } }`

## Development Workflow

### 开发流程

1. **Constitution 确立** → 本文件，定义项目原则与约束
2. **Spec 规格编写** → 将需求分解为功能规格，包含用户故事和验收条件
3. **Plan 技术方案** → 确定技术实现方案、数据模型、API 契约
4. **Tasks 任务拆分** → 按优先级拆分为可独立完成的任务
5. **Implement 编码实现** → 按任务列表逐项实现
6. **Verify 验证** → 每完成一个用户故事，独立测试验证

### 分支策略

- 主分支：`main`（稳定版本）
- 功能分支：`feature/###-功能名称`（如 `feature/001-user-auth`）
- 每个功能分支对应一个独立的用户故事

### 提交规范

- 提交信息格式：`<type>(<scope>): <description>`
- Type：`feat`（功能）、`fix`（修复）、`docs`（文档）、`refactor`（重构）、`style`（格式）、`test`（测试）、`chore`（构建）
- 示例：`feat(auth): 实现用户登录注册接口`

## Governance

### 宪法权威

本 Constitution 是项目的最高指导文件，所有开发决策 MUST 以本文档为基准。当其他文档与本文档冲突时，以本文档为准。

### 修订流程

1. 提出修订提案，说明修改原因与影响范围
2. 在团队内讨论并达成共识
3. 更新 Constitution 文件，递增版本号
4. 检查并同步更新所有相关模板文件
5. 记录修订历史

### 合规检查

- 每个 Plan 阶段的 "Constitution Check" MUST 逐项验证是否符合本文件原则
- Code Review MUST 检查代码是否符合 Technical Standards 中的规范
- 任何违反 MUST 级约束的代码 MUST NOT 合并到主分支

### 版本策略

- MAJOR：原则删除或重大重新定义
- MINOR：新增原则或章节
- PATCH：措辞修正、澄清、格式调整

**Version**: 1.0.0 | **Ratified**: 2026-05-29 | **Last Amended**: 2026-05-29
