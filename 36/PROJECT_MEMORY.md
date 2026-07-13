# 大学生创投平台 — 项目记忆

> 供新 Cursor 窗口快速恢复上下文。**以代码为准**，最后更新：2026-07-07（**当前为本地离线配置**）。

---

## 0. 当前开发环境与连接（必读）

### 当前模式：本地离线（2026-07-07）

| 组件 | 配置 |
|------|------|
| 后端 profile | `dev`（`application.yml` → `spring.profiles.active: dev`） |
| 数据库 | 本地 `127.0.0.1:3306/vc_platform`，用户 `root` |
| C 端 API | `frontend/utils/devConfig.js` → `USE_LOCAL_BACKEND = true`，`http://127.0.0.1:8080` |
| 短信 | `vc.sms.enabled: false`，固定验证码 **123456** |
| 微信小程序 | `wx-mini-program.mock-enabled: true`，未配置 app-secret 时 mock 登录 |
| 管理后台 | `http://localhost:5174` → 代理 `/api`、`/uploads` → `localhost:8080` |

**环境安装与启动步骤见 [README.md](./README.md)**

### 连接拓扑

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────────┐
│ 管理后台         │────▶│ 本地后端 :8080    │────▶│ 本地 MySQL           │
│ localhost:5174  │     │ profile=dev      │     │ 127.0.0.1/vc_platform│
└─────────────────┘     └──────────────────┘     └─────────────────────┘

┌─────────────────┐     ┌──────────────────┐
│ C 端小程序/H5    │────▶│ 本地后端 :8080    │
│ devConfig.js    │     │                  │
└─────────────────┘     └──────────────────┘
```

### 推荐工作流

1. 执行 `sql/schema.sql` 初始化本地库（首次）
2. `cd backend && mvn spring-boot:run` — 本地后端 + 本地库
3. `cd admin && npm run dev` — 访问 **localhost:5174** 做后台 CRUD
4. HBuilderX 打开 **`frontend/`** → 运行到浏览器 / 微信开发者工具

### C 端测试（HBuilderX + 微信开发者工具）

- HBuilderX 版本：5.x，**必须打开 `frontend/` 目录**
- 运行：菜单 **运行 → 运行到小程序模拟器 → 微信开发者工具**
- 微信开发者工具：**详情 → 本地设置 → 不校验合法域名**
- 小程序联调本地后端：`devConfig.js` 中 `LOCAL_BACKEND = http://<局域网IP>:8080`（不能用 127.0.0.1）
- 改配置后须从 HBuilderX 重新「运行到微信开发者工具」，并清除开发者工具缓存

### 上线部署

本地离线使用可忽略。**发版步骤见 [DEPLOY.md](./DEPLOY.md)**

---

## 1. 项目是什么

大学生创投类 **uni-app 小程序 / H5** + **Spring Boot API** + **Vue3 管理后台**。

| 端 | 打开目录 | 说明 |
|----|----------|------|
| 工作区根 | `36/` | 含 backend / frontend / admin / sql |
| C 端 | **`frontend/`** | HBuilderX 必须打开此目录（含 `pages.json`） |
| 后端 | **`backend/`** | IDEA / Maven，端口 8080 |
| 管理后台 | **`admin/`** | Vite，端口 5174，开发时代理 `/api` → **localhost:8080** |

**数据库**：`vc_platform`（MySQL 5.7+）

- **当前 dev 配置**：本地 `127.0.0.1:3306`，用户 `root`，密码见 `application-dev.yml`（默认 `123456`）
- **首次初始化**：执行 `sql/schema.sql`；后端 dev 启动时还会自动迁移表/菜单

---

## 2. 技术栈

| 层 | 技术 | 版本/备注 |
|----|------|-----------|
| C 端 | uni-app、Vue 3 `<script setup>`、uView-Plus、Pinia、Sass | uView-Plus ^3.1.30 |
| 管理后台 | Vue 3、Vite 5、Element Plus 2、Pinia、Vue Router 4 | 端口 5174 |
| 后端 | Spring Boot、MyBatis-Plus、Lombok | Boot **3.2.5**、MP **3.5.6**、**JDK 17** |
| 数据库 | MySQL | 5.7+，utf8mb4 |
| 认证 | 模拟 Token（C 端 `AuthInterceptor`）、JWT（管理端 `AdminAuthInterceptor`） | 验证码 mock：**123456** |

**统一响应**：`{ code: 200, message, data }`，成功码 **200**。

---

## 3. 目录结构

```
36/
├── backend/                          # Spring Boot API
│   └── src/main/java/com/lianbei/vc/
│       ├── controller/               # C 端 REST（/api/*）
│       ├── admin/                    # 管理端（/api/admin/*）
│       │   ├── controller/           # 认证、CRUD、审核、筛选管理
│       │   ├── crud/                 # AdminResourceRegistry 动态 CRUD
│       │   └── config/               # AdminSchemaMigration、AdminAuthInterceptor
│       ├── service/impl/             # 业务逻辑 + Assembler（详情 VO 组装）
│       ├── entity/                   # MyBatis-Plus 实体
│       ├── mapper/                   # Mapper 接口 + XML
│       ├── dto/request|response/     # 入参/出参 VO
│       ├── common/                   # Result、PageResult、Constants
│       ├── config/                   # WebMvc、AuthInterceptor、Schema 迁移
│       └── resources/
│           ├── application.yml       # 主配置（profile: dev）
│           ├── application-dev.yml   # 本地 DB
│           └── config/               # JSON 扩展（项目集、详情 ext）
├── frontend/                         # uni-app C 端
│   ├── pages/                        # 页面（home/project/institution/financing/…）
│   ├── components/                   # 公共组件（LibraryFilterDropdown、NewsCard…）
│   ├── api/                          # 按模块封装 request
│   ├── utils/                        # request、filter 常量、authGuard、transform
│   ├── store/                        # Pinia（user）
│   └── pages.json                    # 路由、TabBar（第一项必须是首页）
├── admin/                            # 管理后台 SPA
│   └── src/
│       ├── views/crud/CrudPage.vue   # 通用 CRUD + 审核弹窗
│       ├── views/filters/            # 筛选管理
│       ├── api/                      # axios 封装
│       └── router/index.js
├── sql/                              # schema.sql + 增量 migration
├── uploads/                          # 本地上传目录（vc.upload-dir）
└── PROJECT_MEMORY.md                 # 本文件
```

---

## 4. 核心业务模块

| 模块 | C 端页面 | 数据主表 | 说明 |
|------|----------|----------|------|
| 首页 | `pages/home/index` | `home_banner` + mock JSON | Tab 启动页；轮播图来自 DB |
| 项目库 | `pages/project/library/index` | `project` | 多维筛选列表 |
| 项目详情 | `pages/project/company/index` | `project` + ext JSON | 融资/工商/团队 |
| 项目集 | `pages/project/collection/*` | `project-collections.json` | 配置驱动，非 DB |
| 机构库 | `pages/institution/library/index` | `institution` | 主题色 `#c49a6c` |
| 机构详情 | `pages/institution/detail/index` | `institution` + ext + 反查投资事件 | 5 Tab |
| 融资事件 | `pages/financing/events/index` | `project`（FinancingEventVO） | 与项目库同表不同 VO |
| 融资快报 | `pages/news/*` | `news` | 快讯/文章双模板 |
| 研究院 | `pages/research/list/index` | `research_report` | 报告列表筛选 |
| 寻求报道 | `pages/coverage/apply/index` | `coverage_apply_record` | 需登录，后台审核 |
| 项目入驻 | `pages/project/onboard/index` | `project_onboard_record` | **需人工审核**（auto-approve: false） |
| 投资人认证 | `pages/auth/investor/index` | `user_auth_record` | authGuard 守卫 |
| 活动/路演 | `pages/activity/*` | `activity` | 含报名 |
| 搜索 | `pages/search/index` | `project` 关键字 | |
| **管理后台** | `admin/` | 全表 CRUD + 审核 | 默认 admin/admin123 |

### 项目库 vs 融资事件 vs 详情

```
project 表（实体）
  ├─ 项目库 API → mapLibraryProjectItem（公司视角）
  ├─ 融资事件 API → FinancingEventVO（金额/投资方/日期）
  └─ 详情 API → ProjectDetailVO（共用，ext JSON 按 name 匹配）
```

### 筛选标签（后台可配）

表 `library_filter_option`，管理入口：**内容管理 → 筛选管理**（`/filters/manage`）。

- **所属行业**（`shared.industry`）：项目库、机构库投资领域、研究院、项目集、融资事件顶栏 **共用**
- C 端拉取：`GET /api/filters/bundle/{bundleName}`
- bundle：`project-library` | `collection-detail` | `institution-library` | `financing-events` | `research-list`

**展示名称 vs 筛选值**：C 端提交的是 **筛选值（value）**；须与 DB 字段实际存储一致（如 `category`、`round`、`inst_type`）。匹配方式因字段而异（精确 / LIKE），不是全表模糊搜索。

---

## 5. 后端接口列表

> 分页通用参数：`pageNum`（默认 1）、`pageSize`（默认 10/20）。

### 5.1 C 端 — `/api/home`

| 方法 | 路径 | 参数 | 说明 |
|------|------|------|------|
| GET | `/api/home/init` | — | 首页初始化 |

### 5.2 C 端 — `/api/projects`

| 方法 | 路径 | 主要参数 | 说明 |
|------|------|----------|------|
| GET | `/init` | — | 项目 Tab 初始化 |
| GET | `/page` | category, round, region | 分页列表 |
| GET | `/featured` | limit | 在融精选 |
| GET | `/collections/tabs` | — | 项目集 Tab |
| GET | `/collections` | tabIndex | 项目集列表 |
| GET | `/collections/{id}` | keyword, round, industry | 项目集详情+筛选 |
| GET | `/library/filters` | — | 项目库筛选项（兼容，等同 bundle） |
| GET | `/collection/detail` | keyword, industry, region, regionScope, round, advantage, foundedYear, lbReport, financing, sortBy | **企业项目库** |
| GET | `/search` | keyword | 首页搜索 |
| GET | `/check-name` | name | 入驻重名检查 |
| GET | `/detail/{id}` | — | 完整项目详情 VO |
| GET | `/{id}` | — | 项目实体 |
| POST | `/onboard` | body | 项目入驻申请 |
| GET | `/onboard/status` | — | 入驻状态 |

### 5.3 C 端 — `/api/filters`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/bundle/{bundleName}` | 页面筛选项 bundle |

### 5.4 C 端 — `/api/institutions`

| 方法 | 路径 | 主要参数 | 说明 |
|------|------|----------|------|
| GET | `/library` | keyword, investmentField, instType, foundedYear | 机构库 |
| GET | `/detail/{id}` | — | 机构详情 VO |

### 5.5 C 端 — `/api/financing-events`

| 方法 | 路径 | 主要参数 | 说明 |
|------|------|----------|------|
| GET | `/featured` | limit | 精选 |
| GET | `/page` | industry, region, regionScope, round, financingYear, currency | 融资事件列表 |

### 5.6 C 端 — `/api/research`

| 方法 | 路径 | 主要参数 | 说明 |
|------|------|----------|------|
| GET | `/reports` | reportType, industry, year, tag | 报告列表 |
| GET | `/reports/{id}` | — | 报告详情 |
| GET | `/categories` | — | 分类（部分已被 filter bundle 替代） |
| GET | `/reports/count` | — | 数量 |

### 5.7 C 端 — `/api/news`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/page` | 融资快报列表 |
| GET | `/{id}` | 详情（含 nextNews） |
| GET | `/{id}/comments` | 评论列表 |
| POST | `/{id}/comments` | 发表评论 |

### 5.8 C 端 — `/api/activities`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/page` | 活动列表 |
| GET | `/featured` | 精选路演 |
| GET | `/my/page` | 我的活动 |
| GET | `/{id}` | 详情 |
| POST | `/{id}/register` | 报名 |
| POST | `/publish` | 活动发布申请 |

### 5.9 C 端 — `/api/user`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/mine/init` | 我的页配置 |
| POST | `/sendCode` | 发送验证码 |
| GET | `/sms/config` | 短信配置 |
| POST | `/login` | 登录 |
| GET | `/info` | 用户信息 |
| POST | `/logout` | 登出 |
| POST | `/auth` | 投资人/创业者认证申请 |
| GET | `/connections` | 对接记录 |

### 5.10 C 端 — 其他

| 前缀 | 说明 |
|------|------|
| `/api/coverage/apply` POST | 寻求报道申请 |
| `/api/coverage/check` GET | 是否已申请 |
| `/api/companies/search` GET | 工商主体搜索 |
| `/api/files/upload` POST | 文件上传 |

### 5.11 管理端 — `/api/admin`

| 前缀 | 说明 |
|------|------|
| `/auth/login` POST、`/me` GET、`/logout` POST | 管理员登录 |
| `/users` CRUD | 管理员账号 |
| `/roles` CRUD + `/permissions/all` | 角色权限 |
| `/crud/{resource}` | 动态 CRUD（见 AdminResourceRegistry） |
| `/coverage/{id}/approve\|reject` | 寻求报道审核 |
| `/onboard/{id}/approve\|reject` | 项目入驻审核 |
| `/library-filters` | 筛选标签 meta/list/CRUD（权限 `library_filter:manage`） |

**CRUD 资源**（`crud:{resource}`）：**home_banner**（轮播图）、project（卡片信息）、project_business、project_financing、institution、news、coverage_apply_record、project_onboard_record、research_report 等，完整列表见 `AdminResourceRegistry.java`。

---

## 6. 数据库表结构

> 全量 DDL：`sql/schema.sql`。dev 环境另有 Java 迁移（`@Profile("dev")`）自动补表/菜单/筛选 seed。

### 6.1 核心业务表

| 表名 | 用途 | 关键字段 |
|------|------|----------|
| `project` | 创投项目 | name, category, round, tags, location, founding_year, is_financing |
| `project_business` | 工商信息 | project_id, full_name, legal_person |
| `project_financing` | 融资历史 | project_id, round, amount, financing_date |
| `project_financing_investor` | 投资方 | financing_id, investor_name, institution_id |
| `project_shareholder` | 股东 | project_id, shareholder_name, ratio |
| `project_team_member` | 团队 | project_id, member_name, title |
| `project_tag` / `project_tag_rel` | 标签 | name, project_id+tag_id |
| `institution` | 投资机构 | name, inst_type, investment_field, event_count |
| `news` | 融资快报 | title, news_type, content, source_url, project_id |
| `news_comment` | 评论 | news_id, user_id, content |
| `activity` | 活动/路演 | title, activity_type, status, start_time |
| `activity_registration` | 报名 | user_id, activity_id |
| `research_report` | 研究院报告 | title, report_type, industry, tags, publish_date |
| `research_category` | 报告分类 | category_type, name |

### 6.2 用户与审核表

| 表名 | 用途 |
|------|------|
| `user` | C 端用户（MySQL 保留字，反引号） |
| `user_auth_record` | 认证申请（apply_data JSON） |
| `user_project` | 用户关联项目 |
| `user_activity_record` | 用户活动记录 |
| `connection_record` | 对接记录 |
| `coverage_apply_record` | 寻求报道（apply_data JSON，可审核） |
| `project_onboard_record` | 项目入驻（apply_data JSON，**需审核**） |
| `activity_publish_request` | 活动发布申请 |
| `sms_code_record` | 短信验证码记录 |

### 6.3 管理与配置表

| 表名 | 用途 |
|------|------|
| `admin_user` / `admin_role` / `admin_permission` / `admin_role_permission` | 后台 RBAC |
| `library_filter_option` | 列表页筛选标签（scene + filter_key + label + value） |
| `home_banner` | 首页轮播图（image_url, title, subtitle, link_url, sort_order, status） |

### 6.4 JSON 扩展（按 **name** 匹配，非 id）

| 文件 | 用途 |
|------|------|
| `backend/.../config/project-detail-ext.json` | 项目详情扩展、融资历史 investors |
| `backend/.../config/institution-detail-ext.json` | 机构详情、aliases、联系方式 |
| `backend/.../config/project-collections.json` | 项目集及子项目 |
| `backend/.../mock/home-init.json` | 首页菜单配置 |

---

## 7. 关键配置文件

| 文件 | 作用 |
|------|------|
| `backend/src/main/resources/application.yml` | 端口 8080、profile、upload-dir、onboard.auto-approve、sms |
| `backend/src/main/resources/application-dev.yml` | **本地 MySQL** 连接（127.0.0.1:3306） |
| `backend/.../HomeBannerSchemaMigration.java` | dev：`home_banner` 表 + 菜单 seed |
| `backend/.../WebMvcConfig.java` | CORS、AuthInterceptor、AdminAuthInterceptor、静态 uploads |
| `backend/.../AdminSchemaMigration.java` | dev：后台表/菜单/权限 seed |
| `backend/.../LibraryFilterSchemaMigration.java` | dev：筛选表 + 初始标签 |
| `frontend/utils/devConfig.js` | C 端 `BASE_URL`，**当前默认本地** `http://127.0.0.1:8080` |
| `frontend/utils/request.js` | 统一请求封装，从 devConfig 读取 BASE_URL |
| `frontend/pages.json` | 路由、TabBar、disableScroll |
| `admin/vite.config.js` | 5174，开发时代理 `/api`、`/uploads` → **localhost:8080** |
| `admin/src/api/request.js` | dev 用 `/api` 代理；prod 构建直连 localhost:8080 |
| `admin/src/api/file.js` | 管理端图片上传 `POST /admin/files/upload` |
| `admin/src/utils/fieldValueLabel.js` | 后台枚举字段中文展示 |

---

## 8. 开发规范

### 后端

- 统一 `Result<T>`、`PageResult<T>`；业务异常 `BusinessException`
- Controller 薄、Service 厚；详情用 Assembler（`ProjectDetailAssembler`、`InstitutionDetailAssembler`）
- 魔法值放 `common.constants`
- dev 环境 Schema 用 `InitializingBean` + JdbcTemplate 迁移，**生产需手工 SQL**
- Windows PowerShell 链式命令用 `;`，不用 `&&`

### C 端前端

- Vue 3 组合式 API + `<script setup>`
- 接口只走 `api/*.js`，页面不写死业务数据（筛选项可从 API 拉，常量作 fallback）
- 库类页面：`library-page.scss` + `LibraryFilterDropdown`
- **`u-popup` 筛选抽屉必须在 `.library-page` 外**
- 跳转项目详情：`utils/projectNavigate.js`（融资事件带 `?anchor=financing`）

### 管理后台

- Element Plus + 通用 `CrudPage.vue`
- 审核类：寻求报道、项目入驻有结构化表单视图 + approve/reject
- 筛选管理：`FilterManageView.vue`，权限 `library_filter:manage`

---

## 9. 本地启动

完整步骤见 **[README.md](./README.md)**。简要：

```powershell
# 0. 首次：初始化数据库
mysql -u root -p < sql/schema.sql

# 1. 后端（dev 连本地库，自动迁移表/菜单）
cd backend
mvn spring-boot:run

# 2. 管理后台
cd admin
npm install
npm run dev
# http://localhost:5174  账号 admin / admin123

# 3. C 端 — HBuilderX
#    文件 → 导入 frontend/ 目录
#    运行 → 运行到小程序模拟器 → 微信开发者工具
#    微信开发者工具：详情 → 本地设置 → 不校验合法域名
cd frontend
npm install
```

---

## 10. 常见坑点

1. **HBuilderX 必须打开 `frontend/`**，不是仓库根目录  
2. **`/api/projects/collection/detail`** = 企业**项目库**；**`/api/projects/collections/{id}`** = **项目集**  
3. **`pages.json` 第一项必须是首页**，否则启动进错页  
4. 登录验证码固定 **123456**；dev 环境 `vc.sms.enabled: false`  
5. 项目/机构 ext JSON 的 key 必须与 DB **`name` 字段**一致  
6. 删项目库数据要在后台 **内容管理 → 项目 → 卡片信息**（`project` 表），不是「项目入驻」或「用户项目」  
7. 项目入驻默认 **不自动通过**（`vc.onboard.auto-approve: false`）  
8. 库类页面改布局：检查 `list-area`、`u-popup` 位置、`scrolltolower`（融资事件用 `listReady` 防首屏误加载）  
9. 筛选标签 **value** 必须与数据库字段值一致，否则筛不出数据  
10. 管理端改菜单/权限后需重启后端（dev migration 仅首次或缺失时写入）  
11. 小程序不能访问 `127.0.0.1`，须改 `devConfig.js` 为局域网 IP  
12. 本地上传的图片在 `uploads/`，C 端须连本地后端才能访问

---

## 11. 关键文件索引

| 文件 | 作用 |
|------|------|
| `frontend/pages.json` | 路由、TabBar |
| `frontend/utils/projectNavigate.js` | 项目/融资事件跳转 |
| `frontend/utils/authGuard.js` | 投资人认证守卫 |
| `frontend/utils/coverageGuard.js` | 寻求报道守卫 |
| `frontend/utils/projectFilterData.js` | 筛选项 fallback 常量 |
| `frontend/api/filter.js` | `fetchFilterBundle()` |
| `frontend/components/LibraryFilterDropdown/` | 库类筛选下拉 |
| `frontend/components/ProjectLibraryCard/` | 项目库卡片 |
| `admin/src/views/crud/CrudPage.vue` | 通用 CRUD + 审核 |
| `admin/src/views/filters/FilterManageView.vue` | 筛选管理 |
| `admin/src/components/CoverageApplyDataView.vue` | 寻求报道表单视图 |
| `admin/src/components/ProjectOnboardDataView.vue` | 项目入驻表单视图 |
| `backend/.../LibraryFilterKeys.java` | 筛选项定义与 bundle 映射 |
| `backend/.../AdminResourceRegistry.java` | 后台 CRUD 资源注册（含 home_banner） |
| `backend/.../HomeBannerService.java` | 首页轮播 C 端数据 |
| `sql/schema.sql` | 全量建库 |

---

## 12. 当前进度备忘

### 已完成

- [x] 首页搜索、融资快报列表/详情（快讯+文章）
- [x] 项目库/机构库/融资事件/研究院列表与筛选
- [x] 机构详情（5 Tab + 投资事件反查）
- [x] 投资人认证、寻求报道、项目入驻（后台审核 + 结构化表单）
- [x] 管理后台 CRUD + RBAC + 筛选管理（统一菜单）
- [x] 后台枚举中文化（fieldValueLabel）
- [x] **首页轮播图**（`home_banner` 表 + 后台 CRUD + 图片上传 + C 端读取）
- [x] 代码默认切回**上线配置**（见 DEPLOY.md）

### 待补充 / 占位

- [ ] **线上后端部署**（新 JAR + `--spring.profiles.active=prod` + `WX_APP_SECRET`）

- [ ] 融资快报 HTML 富文本、评论/点赞 API
- [ ] 阅读原文外链、小程序 web-view
- [ ] 投资人认证审核结果页
- [ ] 创业者认证完整流程
- [ ] `institution_investment_event` 正式关联表（二期）
- [ ] 机构库成立时间、研究院发布时间纳入筛选管理
- [ ] 生产环境配置与自动化测试
