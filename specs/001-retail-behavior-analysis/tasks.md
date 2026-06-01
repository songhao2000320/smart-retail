# Tasks: 智能零售用户行为分析系统

**Input**: Design documents from `specs/001-retail-behavior-analysis/`

**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/api-contracts.md, quickstart.md

**Tests**: Not requested — test tasks are omitted.

**Organization**: Tasks are grouped by user story to enable independent implementation. Within each user story, frontend is developed first, then backend, per user directive.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Frontend**: `frontend/src/`
- **Backend**: `backend/src/main/java/com/retail/` + `backend/src/main/resources/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization, basic structure, and development environment

### 1.1 Backend Maven Project Init

- [ ] T001 Create Maven project structure: `backend/pom.xml` with Spring 5 + Spring MVC + MyBatis-Plus 3.x + JWT (jjwt) + Apache POI + iText dependencies
- [ ] T002 [P] Create Spring configuration files: `backend/src/main/resources/applicationContext.xml` (Spring context + MyBatis-Plus config)
- [ ] T003 [P] Create Spring MVC configuration: `backend/src/main/resources/spring-mvc.xml` (CORS, Jackson, file upload, interceptor registration)
- [ ] T004 [P] Create database connection properties: `backend/src/main/resources/jdbc.properties`
- [ ] T005 [P] Create web.xml: `backend/src/main/webapp/WEB-INF/web.xml` (Spring ContextLoaderListener + DispatcherServlet + encoding filter)

### 1.2 Frontend Vite + Vue 3 Project Init

- [ ] T006 Initialize Vue 3 + Vite project in `frontend/`: package.json with Vue 3, Element Plus 2.x, Pinia 2.x, ECharts 5.x, Axios, Vue Router 4.x
- [ ] T007 [P] Configure Vite: `frontend/vite.config.js` (proxy to backend localhost:8080, alias `@` → `src`)
- [ ] T008 [P] Create global styles: `frontend/src/styles/global.css` (Element Plus theme override, layout utilities)
- [ ] T009 [P] Create Axios instance: `frontend/src/api/request.js` (baseURL, request/response interceptors, JWT token injection, 401 redirect)

### 1.3 Database Schema

- [ ] T010 Create SQL init script: `backend/src/main/resources/sql/schema.sql` — all 6 tables (user, store, zone, customer_behavior, zone_stay, user_store_auth) with indexes per data-model.md
- [ ] T011 [P] Create SQL seed data: `backend/src/main/resources/sql/seed.sql` — default admin account (admin/admin123, BCrypt-encoded), sample stores

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

### 2.1 Backend Foundation

- [ ] T012 Create unified response DTO: `backend/src/main/java/com/retail/dto/ApiResponse.java` — `{code, message, data}` with static success/error factory methods
- [ ] T013 [P] Create JWT utility: `backend/src/main/java/com/retail/util/JwtUtil.java` — generate token, parse token, validate expiry, extract userId+role+storeId from claims
- [ ] T014 [P] Create JWT authentication interceptor: `backend/src/main/java/com/retail/interceptor/AuthInterceptor.java` — parse Authorization header, validate JWT, set ThreadLocal user context
- [ ] T015 [P] Create role authorization interceptor: `backend/src/main/java/com/retail/interceptor/RoleInterceptor.java` — check `@RequireRole` annotation on controller methods, compare with JWT role
- [ ] T016 [P] Create `@RequireRole` annotation: `backend/src/main/java/com/retail/interceptor/RequireRole.java` — value field for role names (admin/manager/analyst)
- [ ] T017 Create CORS configuration: `backend/src/main/java/com/retail/config/CorsConfig.java` — allow frontend origin, all headers, credentials
- [ ] T018 [P] Create global exception handler: `backend/src/main/java/com/retail/config/GlobalExceptionHandler.java` — `@ControllerAdvice` handling validation errors, auth errors, business exceptions → unified ApiResponse
- [ ] T019 [P] Create BaseEntity with common fields: `backend/src/main/java/com/retail/entity/BaseEntity.java` — id, createTime, updateTime (extended by all entities)

### 2.2 Frontend Foundation

- [ ] T020 Create main App entry: `frontend/src/App.vue` — `<router-view />` with Element Plus `<el-config-provider>` locale="zh-cn"
- [ ] T021 [P] Create main layout component: `frontend/src/components/AppLayout.vue` — sidebar (role-based menu) + topbar (user info, logout) + `<router-view>` slot
- [ ] T022 [P] Create router with guards: `frontend/src/router/index.js` — all route definitions with meta.roles, beforeEach guard (check auth, check role)
- [ ] T023 [P] Create auth Pinia store: `frontend/src/stores/auth.js` — state: token/user/role, actions: login/logout/register/fetchUser, persist token to localStorage
- [ ] T024 [P] Create app Pinia store: `frontend/src/stores/app.js` — state: currentStoreId/currentStoreName, sidebar collapse
- [ ] T025 [P] Create main.js entry: `frontend/src/main.js` — createApp, use Router, use Pinia, use Element Plus (Chinese locale), mount

**Checkpoint**: Foundation ready — user story implementation can now begin in parallel

---

## Phase 3: User Story 1 — 用户登录与角色权限管理 (Priority: P1) 🎯 MVP

**Goal**: Users can register, login, and see role-specific menus/data. Admin can manage user roles.

**Independent Test**: Register → Login → Verify role-based menu visibility → Admin changes a user's role → Verify changed access.

### Frontend Implementation for US1

- [ ] T026 [P] [US1] Create login page: `frontend/src/views/login/Login.vue` — username + password form, login button, link to register, error display, redirect after login
- [ ] T027 [P] [US1] Create register page: `frontend/src/views/login/Register.vue` — username + password + confirm password form, validation rules, success → redirect to login
- [ ] T028 [US1] Create auth API module: `frontend/src/api/auth.js` — `login(username, password)`, `register(username, password)`, `getCurrentUser()`
- [ ] T029 [P] [US1] Create user management page: `frontend/src/views/user/UserList.vue` — user table (pagination, role filter), role change dropdown per row (admin only)
- [ ] T030 [US1] Create user API module: `frontend/src/api/user.js` — `getUserList(params)`, `updateUserRole(userId, role)`
- [ ] T031 [US1] Add login/register/user routes to router: `frontend/src/router/index.js` — `/login` (public), `/register` (public), `/users` (admin only)

### Backend Implementation for US1

- [ ] T032 [P] [US1] Create User entity: `backend/src/main/java/com/retail/entity/User.java` — fields per data-model.md (id, username, password, role, storeId, status, lockedUntil, loginFailCount, createTime, updateTime)
- [ ] T033 [P] [US1] Create UserMapper interface: `backend/src/main/java/com/retail/mapper/UserMapper.java` — selectByUsername, selectList (with role filter), updateRole, updateLoginFail, updateStatus
- [ ] T034 [P] [US1] Create UserMapper XML: `backend/src/main/resources/mapper/UserMapper.xml` — SQL for all mapper methods
- [ ] T035 [US1] Create UserService interface + impl: `backend/src/main/java/com/retail/service/UserService.java` + `backend/src/main/java/com/retail/service/impl/UserServiceImpl.java` — register (BCrypt encode, duplicate check), login (verify password, check lock, update fail count, generate JWT), getCurrentUser, listUsers (admin), updateRole (admin)
- [ ] T036 [US1] Create AuthController: `backend/src/main/java/com/retail/controller/AuthController.java` — POST `/api/v1/auth/register`, POST `/api/v1/auth/login`, GET `/api/v1/auth/me`
- [ ] T037 [US1] Create UserController: `backend/src/main/java/com/retail/controller/UserController.java` — GET `/api/v1/users` (admin), PUT `/api/v1/users/{userId}/role` (admin)
- [ ] T038 [US1] Add login failure lock logic: after 5 consecutive failures, set status=0, lockedUntil=now+15min; on successful login reset counter

**Checkpoint**: US1 complete — users can register, login, and admin can manage roles. Menu visibility driven by role.

---

## Phase 4: User Story 2 — 门店与区域管理 (Priority: P1) 🎯 MVP

**Goal**: Admin creates/manages stores and their zones (functional areas). Zones serve as spatial anchors for analytics.

**Independent Test**: Admin creates store → Adds zones → Edits zone info → Deletes store (soft delete) → Verify zone list persists but store marked deleted.

### Frontend Implementation for US2

- [ ] T039 [P] [US2] Create store list page: `frontend/src/views/store/StoreList.vue` — store table (search by keyword, pagination), create/edit/delete buttons (admin only)
- [ ] T040 [P] [US2] Create store form dialog component: `frontend/src/views/store/StoreFormDialog.vue` — name/address/phone form fields, validation, create vs edit mode
- [ ] T041 [P] [US2] Create zone management page: `frontend/src/views/store/ZoneManage.vue` — zone list under selected store, add/edit/delete zone operations
- [ ] T042 [P] [US2] Create zone form dialog component: `frontend/src/views/store/ZoneFormDialog.vue` — name, posX, posY, width, height form fields with validation (0-100 range)
- [ ] T043 [P] [US2] Create store floor plan canvas component: `frontend/src/components/ZoneCanvas.vue` — renders rectangular zones on a 2D canvas based on posX/posY/width/height, supports click-to-select zone
- [ ] T044 [US2] Create store API module: `frontend/src/api/store.js` — `getStoreList(params)`, `getStoreDetail(id)`, `createStore(data)`, `updateStore(id, data)`, `deleteStore(id)`
- [ ] T045 [US2] Create zone API module: `frontend/src/api/zone.js` — `getZones(storeId)`, `createZone(storeId, data)`, `updateZone(zoneId, data)`, `deleteZone(zoneId)`
- [ ] T046 [US2] Add store/zone routes to router: `frontend/src/router/index.js` — `/stores` (admin/manager), `/stores/:id/zones` (admin/manager)

### Backend Implementation for US2

- [ ] T047 [P] [US2] Create Store entity: `backend/src/main/java/com/retail/entity/Store.java` — id, name, address, phone, status, createTime, updateTime
- [ ] T048 [P] [US2] Create Zone entity: `backend/src/main/java/com/retail/entity/Zone.java` — id, storeId, name, posX, posY, width, height, status, createTime, updateTime
- [ ] T049 [P] [US2] Create StoreMapper interface: `backend/src/main/java/com/retail/mapper/StoreMapper.java` — selectList (keyword search, status filter), selectById, insert, update, updateStatus (soft delete)
- [ ] T050 [P] [US2] Create StoreMapper XML: `backend/src/main/resources/mapper/StoreMapper.xml`
- [ ] T051 [P] [US2] Create ZoneMapper interface: `backend/src/main/java/com/retail/mapper/ZoneMapper.java` — selectByStoreId, selectById, insert, update, updateStatus (soft delete)
- [ ] T052 [P] [US2] Create ZoneMapper XML: `backend/src/main/resources/mapper/ZoneMapper.xml`
- [ ] T053 [US2] Create StoreService interface + impl: `backend/src/main/java/com/retail/service/StoreService.java` + `backend/src/main/java/com/retail/service/impl/StoreServiceImpl.java` — listStores (admin=all, manager=own), getStoreDetail (with zones), create/update/delete (admin only, delete=soft delete + mark behaviors invalid)
- [ ] T054 [US2] Create ZoneService interface + impl: `backend/src/main/java/com/retail/service/ZoneService.java` + `backend/src/main/java/com/retail/service/impl/ZoneServiceImpl.java` — getZones, createZone (validate pos/width/height range), updateZone, deleteZone
- [ ] T055 [US2] Create StoreController: `backend/src/main/java/com/retail/controller/StoreController.java` — GET `/api/v1/stores`, POST `/api/v1/stores`, GET `/api/v1/stores/{id}`, PUT `/api/v1/stores/{id}`, DELETE `/api/v1/stores/{id}`
- [ ] T056 [US2] Create ZoneController: `backend/src/main/java/com/retail/controller/ZoneController.java` — GET `/api/v1/stores/{storeId}/zones`, POST `/api/v1/stores/{storeId}/zones`, PUT `/api/v1/zones/{zoneId}`, DELETE `/api/v1/zones/{zoneId}`

**Checkpoint**: US2 complete — admin can manage stores and zones, floor plan renders zone layout.

---

## Phase 5: User Story 3 — 顾客行为数据录入 (Priority: P1) 🎯 MVP

**Goal**: Store managers can enter customer behavior records (single entry or Excel batch import). This is the data source for all analytics.

**Independent Test**: Enter a single behavior record → Verify it appears in list → Import an Excel file → Verify success/failure counts → Filter list by date/store.

### Frontend Implementation for US3

- [ ] T057 [P] [US3] Create behavior record list page: `frontend/src/views/behavior/BehaviorList.vue` — data table (store name, entry/leave time, purchased badge, zone stays), date range filter, store selector, pagination
- [ ] T058 [P] [US3] Create behavior entry form component: `frontend/src/views/behavior/BehaviorEntryForm.vue` — store selector, entry time picker, leave time picker, purchased switch, zone stay sub-form (zone selector + entry/leave time, add/remove rows), validation (leave > entry)
- [ ] T059 [P] [US3] Create Excel import dialog component: `frontend/src/views/behavior/ImportDialog.vue` — file upload (drag-drop + click), store selector, template download link, import result summary (success/fail counts + failure details table)
- [ ] T060 [US3] Create behavior API module: `frontend/src/api/behavior.js` — `getBehaviorList(params)`, `createBehavior(data)`, `importExcel(file, storeId)`, `downloadTemplate()`
- [ ] T061 [US3] Add behavior routes to router: `frontend/src/router/index.js` — `/behaviors` (admin/manager), `/behaviors/entry` (admin/manager)

### Backend Implementation for US3

- [ ] T062 [P] [US3] Create CustomerBehavior entity: `backend/src/main/java/com/retail/entity/CustomerBehavior.java` — id, storeId, entryTime, leaveTime, isPurchased, createdBy, status, createTime
- [ ] T063 [P] [US3] Create ZoneStay entity: `backend/src/main/java/com/retail/entity/ZoneStay.java` — id, behaviorId, zoneId, entryTime, leaveTime
- [ ] T064 [P] [US3] Create BehaviorMapper interface: `backend/src/main/java/com/retail/mapper/BehaviorMapper.java` — selectList (storeId, date range, pagination), insert, selectById
- [ ] T065 [P] [US3] Create BehaviorMapper XML: `backend/src/main/resources/mapper/BehaviorMapper.xml` — list query with store name join, date range filter
- [ ] T066 [P] [US3] Create ZoneStayMapper interface: `backend/src/main/java/com/retail/mapper/ZoneStayMapper.java` — batchInsert, selectByBehaviorId, selectByBehaviorIds
- [ ] T067 [P] [US3] Create ZoneStayMapper XML: `backend/src/main/resources/mapper/ZoneStayMapper.xml`
- [ ] T068 [US3] Create BehaviorService interface + impl: `backend/src/main/java/com/retail/service/BehaviorService.java` + `backend/src/main/java/com/retail/service/impl/BehaviorServiceImpl.java` — createBehavior (validate time, validate zones belong to store, transactional insert behavior + zoneStays), listBehaviors (role-based data scope), importExcel (parse, validate row-by-row, collect failures, batch insert valid rows)
- [ ] T069 [US3] Create Excel import utility: `backend/src/main/java/com/retail/util/ExcelUtil.java` — parse XLSX rows (Apache POI), validate required fields/time formats/zone validity, return success rows + failure list
- [ ] T070 [US3] Create BehaviorController: `backend/src/main/java/com/retail/controller/BehaviorController.java` — GET `/api/v1/behaviors`, POST `/api/v1/behaviors`, POST `/api/v1/behaviors/import` (multipart, 10MB limit), GET `/api/v1/behaviors/template` (stream Excel template)

**Checkpoint**: US3 complete — MVP (US1+US2+US3) is now functional! Data can be entered and listed.

---

## Phase 6: User Story 4 — 客流统计 (Priority: P2)

**Goal**: View visitor traffic trends by hour/day/week/month with bar+line charts, filterable by store and date range.

**Independent Test**: Enter test data → Select store/date range/granularity → Verify chart shows correct visitor counts per period.

### Frontend Implementation for US4

- [ ] T071 [US4] Create traffic analysis page: `frontend/src/views/analytics/Traffic.vue` — store selector, date range picker, granularity tabs (hour/day/week/month), bar chart (visitor count) + line chart (trend) using ECharts, empty state for no data
- [ ] T072 [US4] Create date range picker component: `frontend/src/components/DateRangePicker.vue` — two date pickers with quick presets (today/this week/this month/last 30 days)
- [ ] T073 [US4] Create store selector component: `frontend/src/components/StoreSelector.vue` — dropdown populated from store API, emits storeId changes
- [ ] T074 [US4] Create analytics API module: `frontend/src/api/analytics.js` — `getTraffic(params)` (storeId, startDate, endDate, granularity)
- [ ] T075 [US4] Add analytics route: `frontend/src/router/index.js` — `/analytics/traffic` (all roles)

### Backend Implementation for US4

- [ ] T076 [US4] Create AnalyticsService interface + impl: `backend/src/main/java/com/retail/service/AnalyticsService.java` + `backend/src/main/java/com/retail/service/impl/AnalyticsServiceImpl.java` — getTrafficStats: GROUP BY period (DATE_FORMAT for hour/day/week/month), COUNT behaviors, role-based store filter (analyst=authorized stores only)
- [ ] T077 [US4] Add traffic query to BehaviorMapper: `backend/src/main/java/com/retail/mapper/BehaviorMapper.java` — countByPeriod(storeId, startDate, endDate, granularity) returning list of {period, visitorCount}
- [ ] T078 [US4] Create AnalyticsController (traffic endpoint): `backend/src/main/java/com/retail/controller/AnalyticsController.java` — GET `/api/v1/analytics/traffic?storeId=&startDate=&endDate=&granularity=`

**Checkpoint**: US4 complete — traffic statistics with multi-granularity charts.

---

## Phase 7: User Story 5 — 热力图分析 (Priority: P2)

**Goal**: Heatmap rendered on store floor plan based on zone visit count/duration, with toggle between two modes.

**Independent Test**: Enter data with known zone preferences → View heatmap → Verify hot zones are red, cold zones blue → Toggle count/duration mode → Verify color distribution changes.

### Frontend Implementation for US5

- [ ] T079 [US5] Create heatmap analysis page: `frontend/src/views/analytics/Heatmap.vue` — store selector, date range picker, mode toggle (count/duration), floor plan with color overlay, color legend (blue→red gradient)
- [ ] T080 [US5] Enhance ZoneCanvas component: `frontend/src/components/ZoneCanvas.vue` — add heatmap overlay mode (fill each zone rect with color from value/maxValue ratio using HSL color scale), zone tooltip (name + value), click to highlight
- [ ] T081 [US5] Add heatmap API to analytics module: `frontend/src/api/analytics.js` — `getHeatmap(params)` (storeId, startDate, endDate, mode)

### Backend Implementation for US5

- [ ] T082 [US5] Add heatmap query to ZoneStayMapper: `backend/src/main/java/com/retail/mapper/ZoneStayMapper.java` — aggregateByZone(storeId, startDate, endDate, mode) — if mode=count: COUNT GROUP BY zoneId; if mode=duration: SUM(TIMESTAMPDIFF) GROUP BY zoneId; include zone name/coordinates via join
- [ ] T083 [US5] Add heatmap method to AnalyticsService: getHeatmapData — query zone stats, calculate maxValue, return list of {zoneId, zoneName, posX, posY, width, height, value, maxValue}
- [ ] T084 [US5] Add heatmap endpoint to AnalyticsController: GET `/api/v1/analytics/heatmap?storeId=&startDate=&endDate=&mode=`

**Checkpoint**: US5 complete — interactive heatmap on store floor plan.

---

## Phase 8: User Story 6 — 停留时长分析 (Priority: P2)

**Goal**: Average stay duration + per-zone duration breakdown via pie chart and radar chart for multi-store comparison.

**Independent Test**: Enter data with varying zone durations → Verify pie chart percentages match → Select multiple stores → Verify radar chart shows correct per-zone values.

### Frontend Implementation for US6

- [ ] T085 [US6] Create duration analysis page: `frontend/src/views/analytics/Duration.vue` — store selector, date range picker, avg duration display card, pie chart (per-zone duration %), multi-store radar chart (select 2+ stores), empty state
- [ ] T086 [US6] Add duration API to analytics module: `frontend/src/api/analytics.js` — `getDuration(params)` (single store), `getDurationCompare(params)` (multiple storeIds)

### Backend Implementation for US6

- [ ] T087 [US6] Add duration queries to ZoneStayMapper: `backend/src/main/java/com/retail/mapper/ZoneStayMapper.java` — sumDurationByZone(storeId, startDate, endDate) for single store; sumDurationByZoneForStores(storeIds, startDate, endDate) for multi-store comparison
- [ ] T088 [US6] Add duration methods to AnalyticsService: getDurationStats — avg duration (from CustomerBehavior leave_time - entry_time), per-zone total + percentage; getDurationCompare — per-store per-zone totals for radar chart
- [ ] T089 [US6] Add duration endpoints to AnalyticsController: GET `/api/v1/analytics/duration`, GET `/api/v1/analytics/duration/compare`

**Checkpoint**: US6 complete — duration analysis with pie chart and radar chart.

---

## Phase 9: User Story 7 — 购买转化分析 (Priority: P2)

**Goal**: Daily conversion rate (purchased/visitors) trend line chart, with multi-store comparison support.

**Independent Test**: Enter data with mixed purchase status → Verify conversion rate = purchased/visitors correctly → Verify 0-visitor days excluded → Compare multiple stores.

### Frontend Implementation for US7

- [ ] T090 [US7] Create conversion analysis page: `frontend/src/views/analytics/Conversion.vue` — store selector (single + multi), date range picker, line chart (rate % per day), multi-store overlay mode (different colored lines), empty state
- [ ] T091 [US7] Add conversion API to analytics module: `frontend/src/api/analytics.js` — `getConversion(params)` (single store), `getConversionCompare(params)` (multiple storeIds)

### Backend Implementation for US7

- [ ] T092 [US7] Add conversion queries to BehaviorMapper: `backend/src/main/java/com/retail/mapper/BehaviorMapper.java` — dailyStats(storeId/ids, startDate, endDate) returning {date, visitorCount, purchaseCount}
- [ ] T093 [US7] Add conversion methods to AnalyticsService: getConversion — calculate rate per day, exclude days with visitorCount=0; getConversionCompare — per-store daily rates
- [ ] T094 [US7] Add conversion endpoints to AnalyticsController: GET `/api/v1/analytics/conversion`, GET `/api/v1/analytics/conversion/compare`

**Checkpoint**: US7 complete — conversion rate analysis. All P2 analysis modules done!

---

## Phase 10: User Story 8 — 可视化仪表盘 (Priority: P3)

**Goal**: Single-page dashboard integrating today's traffic, conversion rate, heatmap thumbnail, and duration distribution. Big-screen optimized (1920x1080+).

**Independent Test**: After all analysis modules have data → Open dashboard → Verify all 4 components display data → Switch store → All components update → Click heatmap thumbnail → Navigate to heatmap page.

### Frontend Implementation for US8

- [ ] T095 [US8] Create dashboard page: `frontend/src/views/dashboard/Dashboard.vue` — 2x2 grid layout, store selector header, 4 cards: (1) today traffic with trend sparkline, (2) today conversion rate with gauge/indicator, (3) heatmap thumbnail (mini ZoneCanvas), (4) duration pie chart mini; all clickable to navigate to full pages; responsive for 1920x1080+
- [ ] T096 [US8] Create stat card component: `frontend/src/components/StatCard.vue` — title, value, optional subtitle/icon, loading skeleton, click handler
- [ ] T097 [US8] Add dashboard API to analytics module: `frontend/src/api/analytics.js` — `getDashboard(storeId)` returning todayTraffic, todayConversionRate, heatmapPreview, durationDistribution
- [ ] T098 [US8] Add dashboard route: `frontend/src/router/index.js` — `/dashboard` (default route after login, all roles)

### Backend Implementation for US8

- [ ] T099 [US8] Add dashboard aggregation to AnalyticsService: getDashboard — aggregate today's traffic (COUNT behaviors where entryTime today), today's conversion rate, heatmap preview (top 10 zones by count), duration distribution (per-zone %)
- [ ] T100 [US8] Add dashboard endpoint to AnalyticsController: GET `/api/v1/analytics/dashboard?storeId=`

**Checkpoint**: US8 complete — unified dashboard for big-screen display.

---

## Phase 11: User Story 9 — 数据导入导出 (Priority: P3)

**Goal**: Excel template download, Excel export for reports, PDF export for dashboard. File size limit enforcement.

**Independent Test**: Download template → Fill and re-upload → Verify import result → Export traffic report as Excel → Export dashboard as PDF → Verify file contents.

### Frontend Implementation for US9

- [ ] T101 [P] [US9] Add export buttons to Traffic page: "导出 Excel" button calling export API, download file blob
- [ ] T102 [P] [US9] Add export button to Conversion page: "导出 Excel" button
- [ ] T103 [P] [US9] Add export button to Dashboard page: "导出 PDF" button calling export API, download file blob
- [ ] T104 [US9] Create export API module: `frontend/src/api/export.js` — `exportTraffic(params)` (responseType blob), `exportConversion(params)`, `exportDashboard(storeId)` (PDF)

### Backend Implementation for US9

- [ ] T105 [P] [US9] Create Excel export utility: `backend/src/main/java/com/retail/util/ExcelExportUtil.java` — generate XLSX workbook with Apache POI for traffic/conversion/duration data
- [ ] T106 [P] [US9] Create PDF export utility: `backend/src/main/java/com/retail/util/PdfExportUtil.java` — generate PDF with iText, render dashboard snapshot as simple PDF report
- [ ] T107 [US9] Create ExportService interface + impl: `backend/src/main/java/com/retail/service/ExportService.java` + `backend/src/main/java/com/retail/service/impl/ExportServiceImpl.java` — exportTraffic, exportConversion, exportDashboard (all return file stream)
- [ ] T108 [US9] Create ExportController: `backend/src/main/java/com/retail/controller/ExportController.java` — GET `/api/v1/export/traffic?storeId=&startDate=&endDate=&type=`, GET `/api/v1/export/dashboard?storeId=` (both return file stream with appropriate Content-Type)

**Checkpoint**: US9 complete — full data import/export capability.

---

## Phase 12: User Authorization (Cross-cutting for US1)

**Purpose**: Admin assigns stores to analyst users. This was specified in spec.md FR-005/FR-006 and the UserStoreAuth entity.

### Frontend Implementation

- [ ] T109 [P] [US1] Add authorization UI to user management: `frontend/src/views/user/UserList.vue` — "授权门店" button per analyst row, multi-select store dialog, save authorization
- [ ] T110 [US1] Create user auth API module: `frontend/src/api/userauth.js` — `saveAuth(userId, storeIds)`, `getUserAuth(userId)`

### Backend Implementation

- [ ] T111 [P] [US1] Create UserStoreAuth entity: `backend/src/main/java/com/retail/entity/UserStoreAuth.java` — id, userId, storeId, createTime
- [ ] T112 [P] [US1] Create UserStoreAuthMapper interface + XML: `backend/src/main/java/com/retail/mapper/UserStoreAuthMapper.java` + `backend/src/main/resources/mapper/UserStoreAuthMapper.xml` — batchInsert, deleteByUserId, selectByUserId
- [ ] T113 [US1] Create UserAuthController: `backend/src/main/java/com/retail/controller/UserAuthController.java` — POST `/api/v1/user-auth` (admin, batch replace storeIds for user), GET `/api/v1/user-auth/{userId}` (admin)
- [ ] T114 [US1] Update AnalyticsService: add role-based store filtering — if user is analyst, filter analytics queries to authorized stores via UserStoreAuthMapper

**Checkpoint**: Cross-cutting authorization complete — analysts only see authorized store data.

---

## Phase 13: Polish & Cross-Cutting Concerns

**Purpose**: Final touches across all user stories.

- [ ] T115 [P] Add loading states (v-loading / skeleton) to all pages: BehaviorList, Traffic, Heatmap, Duration, Conversion, Dashboard
- [ ] T116 [P] Add error handling and empty states to all pages: meaningful messages for no data, network errors, permission denied
- [ ] T117 [P] Add login failure lock UI feedback: show "账号已锁定，请15分钟后重试" message on login page when account locked
- [ ] T118 [P] Code cleanup: remove console.log, unused imports, consistent formatting across frontend/ and backend/
- [ ] T119 Run quickstart.md validation: verify all setup steps work end-to-end (DB init → backend start → frontend start → login → full flow)
- [ ] T120 [P] Final security review: verify all SQL uses parameterized queries (MyBatis #{} syntax), all passwords BCrypt-encoded, JWT expiry set, CORS restricted

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion — **BLOCKS all user stories**
- **User Stories (Phase 3-12)**: All depend on Foundational phase completion
  - US1 (Phase 3) → US2 (Phase 4) → US3 (Phase 5) → [US4, US5, US6, US7 can go in parallel] → US8 (Phase 10) → US9 (Phase 11)
  - US8 depends on US4-US7 being complete (needs all analysis data)
  - US12 (Authorization) can be done alongside US1-US3
- **Polish (Phase 13)**: Depends on all desired user stories being complete

### User Story Dependencies

```
Phase 1: Setup
    ↓
Phase 2: Foundational (BLOCKS everything below)
    ↓
Phase 3: US1 登录与角色权限 ← Phase 12: 用户授权 (parallel with US1)
    ↓
Phase 4: US2 门店与区域管理 (needs US1 for auth context)
    ↓
Phase 5: US3 顾客行为数据录入 (needs US2 for store/zone references)
    ↓
    ├── Phase 6: US4 客流统计
    ├── Phase 7: US5 热力图分析    ← All three can run in parallel
    ├── Phase 8: US6 停留时长分析
    └── Phase 9: US7 购买转化分析
    ↓
Phase 10: US8 可视化仪表盘 (needs US4+US5+US6+US7)
    ↓
Phase 11: US9 数据导入导出
    ↓
Phase 13: Polish
```

### Within Each User Story (Frontend First)

1. Frontend pages/components (parallel where marked [P])
2. Frontend API modules
3. Backend Entity classes (parallel where marked [P])
4. Backend Mapper interfaces + XML (parallel where marked [P])
5. Backend Service interface + impl
6. Backend Controller
7. Integration & validation

### Parallel Opportunities

- **Phase 1**: T002-T005 (backend configs) can all run in parallel
- **Phase 1**: T007-T009 (frontend configs) can all run in parallel
- **Phase 2**: T013-T016 (interceptors), T018-T019 (config/entity) can run in parallel
- **Phase 2**: T021-T025 (frontend foundation) can run in parallel
- **Phase 3**: T026-T027 (login/register pages) parallel; T032-T034 (entity + mapper) parallel
- **Phase 4**: T039-T043 (all frontend pages/components) parallel; T047-T052 (all entities + mappers) parallel
- **Phase 5**: T057-T059 (all frontend components) parallel; T062-T067 (all entities + mappers) parallel
- **Phase 6-9**: Can all run in parallel after Phase 5
- **Phase 11**: T101-T103 (export buttons) parallel; T105-T106 (export utils) parallel
- **Phase 13**: All [P] tasks can run in parallel

---

## Implementation Strategy

### MVP First (US1 + US2 + US3)

1. Complete Phase 1: Setup — project scaffolding
2. Complete Phase 2: Foundational — JWT, interceptors, layout, router
3. Complete Phase 3: US1 — Login, register, role management
4. Complete Phase 4: US2 — Store & zone management
5. Complete Phase 5: US3 — Behavior data entry & import
6. **STOP and VALIDATE**: Full MVP is functional — users can login, manage stores/zones, enter behavior data
7. Deploy/demo if ready

### Incremental Delivery

1. Setup + Foundational → Foundation ready
2. Add US1 → Auth system → Demo login/roles
3. Add US2 → Store management → Demo store CRUD + floor plan
4. Add US3 → Data entry → **MVP Complete!** Demo full data entry flow
5. Add US4 → Traffic charts → Demo visitor trends
6. Add US5 → Heatmap → Demo floor plan heatmap
7. Add US6 → Duration analysis → Demo pie + radar charts
8. Add US7 → Conversion analysis → Demo conversion trends
9. Add US8 → Dashboard → **Full Platform!** Demo big screen
10. Add US9 → Import/Export → Demo Excel & PDF export

### Parallel Team Strategy

With multiple developers after Foundational phase:

- **Developer A**: US1 (Auth) → US2 (Stores) → US4 (Traffic) → US8 (Dashboard)
- **Developer B**: US3 (Behaviors) → US5 (Heatmap) → US6 (Duration)
- **Developer C**: US7 (Conversion) → US9 (Export) → Phase 13 (Polish)

---

## Notes

- [P] tasks = different files, no dependencies — can execute in parallel
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- **Frontend-first within each story** per user directive: pages → API layer → backend entities → mapper → service → controller
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence
- Total task count: 120 tasks across 13 phases
