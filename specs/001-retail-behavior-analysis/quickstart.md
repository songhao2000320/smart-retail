# Quickstart: 智能零售用户行为分析系统

**Feature**: 001-retail-behavior-analysis
**Date**: 2026-05-29

## 环境要求

| 工具 | 版本要求 | 说明 |
|------|---------|------|
| JDK | 8+ / 11+ | 后端运行环境 |
| Maven | 3.6+ | 后端构建工具 |
| MySQL | 8.0+ | 数据库 |
| Node.js | 16+ | 前端构建环境 |
| npm | 8+ | 前端包管理 |

## 快速启动步骤

### 1. 数据库初始化

```sql
-- 创建数据库
CREATE DATABASE retail_behavior DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建初始管理员账号（密码为 BCrypt 加密的 "admin123"）
-- 实际实现中通过初始化脚本或 Seed Data 完成
```

### 2. 后端启动

```bash
# 进入后端目录
cd backend

# 修改数据库连接配置
# 编辑 src/main/resources/jdbc.properties
# jdbc.url=jdbc:mysql://localhost:3306/retail_behavior?useSSL=false&serverTimezone=Asia/Shanghai
# jdbc.username=root
# jdbc.password=your_password

# Maven 构建
mvn clean package -DskipTests

# 部署到 Tomcat 或直接运行
# 方式一：Tomcat 部署
# 将 target/retail-behavior.war 放入 Tomcat webapps 目录

# 方式二：Maven Tomcat 插件
mvn tomcat7:run
```

### 3. 前端启动

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 配置 API 地址
# 编辑 .env.development
# VITE_API_BASE_URL=http://localhost:8080/api/v1

# 启动开发服务器
npm run dev

# 浏览器访问 http://localhost:5173
```

### 4. 验证

1. 打开浏览器访问 `http://localhost:5173`
2. 使用初始管理员账号登录
3. 创建第一个门店和区域
4. 录入一条顾客行为数据
5. 查看客流统计和热力图

## 项目结构

```
retail-behavior-analysis/
├── backend/                          # Java Maven 项目
│   ├── pom.xml                       # 父 POM（依赖管理）
│   └── src/main/
│       ├── java/com/retail/
│       │   ├── controller/           # REST 控制器
│       │   │   ├── AuthController.java
│       │   │   ├── UserController.java
│       │   │   ├── StoreController.java
│       │   │   ├── ZoneController.java
│       │   │   ├── BehaviorController.java
│       │   │   ├── AnalyticsController.java
│       │   │   └── ExportController.java
│       │   ├── service/              # 业务逻辑层
│       │   │   ├── UserService.java
│       │   │   ├── StoreService.java
│       │   │   ├── BehaviorService.java
│       │   │   └── AnalyticsService.java
│       │   ├── service/impl/         # 服务实现
│       │   ├── mapper/               # MyBatis Mapper 接口
│       │   ├── entity/               # 实体类
│       │   ├── dto/                  # 数据传输对象
│       │   ├── config/               # Spring 配置
│       │   ├── interceptor/          # JWT 拦截器
│       │   └── util/                 # 工具类
│       └── resources/
│           ├── mapper/               # MyBatis XML 映射
│           ├── applicationContext.xml
│           ├── spring-mvc.xml
│           └── jdbc.properties
├── frontend/                         # Vue 3 项目
│   ├── src/
│   │   ├── views/                    # 页面组件
│   │   │   ├── login/               # 登录页
│   │   │   ├── dashboard/           # 仪表盘
│   │   │   ├── store/               # 门店管理
│   │   │   ├── behavior/            # 数据录入
│   │   │   ├── analytics/           # 分析页面
│   │   │   │   ├── Traffic.vue
│   │   │   │   ├── Heatmap.vue
│   │   │   │   ├── Duration.vue
│   │   │   │   └── Conversion.vue
│   │   │   └── user/                # 用户管理
│   │   ├── components/              # 通用组件
│   │   ├── router/                  # 路由
│   │   ├── stores/                  # Pinia store
│   │   ├── api/                     # API 封装
│   │   └── utils/                   # 工具函数
│   ├── vite.config.js
│   └── package.json
└── specs/                           # Spec-Kit 文档
    └── 001-retail-behavior-analysis/
```

## 关键依赖

### 后端 Maven 依赖

```xml
<!-- Spring 核心 -->
<dependency>spring-context</dependency>
<dependency>spring-webmvc</dependency>

<!-- MyBatis-Plus -->
<dependency>mybatis-plus</dependency>

<!-- MySQL 驱动 -->
<dependency>mysql-connector-java</dependency>

<!-- JWT -->
<dependency>jjwt</dependency>

<!-- 密码加密 -->
<dependency>spring-security-crypto</dependency>  <!-- BCrypt -->

<!-- Excel -->
<dependency>poi-ooxml</dependency>

<!-- JSON -->
<dependency>jackson-databind</dependency>
```

### 前端 npm 依赖

```json
{
  "dependencies": {
    "vue": "^3.4",
    "vue-router": "^4.3",
    "pinia": "^2.1",
    "element-plus": "^2.7",
    "echarts": "^5.5",
    "axios": "^1.7"
  }
}
```

## 常见问题

**Q: 前端请求后端 API 跨域怎么办？**
A: 后端配置 CORS 过滤器，允许前端开发服务器域名（`http://localhost:5173`）。

**Q: 如何生成 JWT 密钥？**
A: 使用 HS256 算法，密钥配置在 `applicationContext.xml` 或 properties 文件中，生产环境建议使用环境变量。

**Q: 热力图坐标如何校准？**
A: 坐标使用百分比体系（0-100），前端根据门店平面图容器尺寸自适应缩放，无需像素级校准。
