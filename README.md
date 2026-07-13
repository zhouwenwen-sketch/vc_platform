# 联贝科创平台 (LianBei VC)

> 一个面向科创投资的全栈平台，包含 **C 端小程序/H5**、**Spring Boot 后端 API**、**Vue3 管理后台** 三端。
>
> 微信小程序端展示项目库、投资机构、融资事件、研究报告、活动路演等内容；管理后台提供动态 CRUD、RBAC 权限管理、Excel 批量导入、筛选标签管理等能力。

---

## 目录

- [项目结构](#项目结构)
- [技术栈](#技术栈)
- [快速开始（本地开发）](#快速开始本地开发)
- [本地服务与账号](#本地服务与账号)
- [主要功能模块](#主要功能模块)
- [部署上线](#部署上线)
- [相关文档](#相关文档)

---

## 项目结构

```
创投平台/
├── 36/                            # 项目源码根目录
│   ├── backend/                   # Spring Boot 后端 API（Java 17）
│   │   ├── src/main/java/         # Java 源码（com.lianbei.vc）
│   │   ├── src/main/resources/    # 配置文件（application-*.yml）
│   │   └── pom.xml                # Maven 依赖
│   ├── frontend/                  # uni-app C 端（微信小程序 / H5）
│   │   ├── pages/                 # 页面（首页、项目库、机构库、融资、研究院…）
│   │   ├── components/            # 公共组件
│   │   ├── api/                   # 接口封装
│   │   └── pages.json             # 路由与 TabBar 配置
│   ├── admin/                     # Vue3 管理后台 SPA
│   │   ├── src/views/             # 页面（工作台、CRUD、筛选管理、导入…）
│   │   ├── src/api/               # Axios 请求封装
│   │   └── vite.config.js         # Vite 配置
│   ├── sql/                       # 数据库脚本（DDL + 迁移 + 种子数据）
│   ├── uploads/                   # 本地上传文件目录
│   ├── PROJECT_MEMORY.md          # 接口文档、表结构、开发备忘
│   ├── DEPLOY.md                  # 上线部署清单
│   └── README.md                  # 详细运行说明（内部）
├── api_sample.json                # API 返回示例
├── uploads/                       # 上传文件（项目级）
├── 实习项目总结.md                 # 实习/项目总结文档
└── README.md                      # 本文件（项目总览）
```

> ⚠️ 开发时请注意：**HBuilderX 必须打开 `36/frontend/` 目录**（不是上级目录），因为 `pages.json` 在其中。

---

## 技术栈

### 后端

| 类别 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 17 |
| 框架 | Spring Boot | 3.2.5 |
| ORM | MyBatis-Plus | 3.5.6 |
| 数据库 | MySQL | 5.7+ / 8.x（utf8mb4） |
| 安全 | Spring Security Crypto | BCrypt 密码加密 |
| Excel | Apache POI | 5.2.5（项目/机构批量导入） |
| 构建 | Maven | 3.6+ |

### C 端（小程序 / H5）

| 类别 | 技术 | 说明 |
|------|------|------|
| 框架 | uni-app（Vue 3） | 支持微信小程序 + H5 |
| UI 库 | uView-Plus | ^3.1.30 |
| 状态管理 | Pinia | ^2.1.7 |
| 构建 | Vite + @dcloudio/vite-plugin-uni | HBuilderX 或 CLI 编译 |

### 管理后台

| 类别 | 技术 | 说明 |
|------|------|------|
| 框架 | Vue 3 | ^3.5.12 |
| 构建 | Vite | ^5.4.10 |
| UI 库 | Element Plus | ^2.8.4 |
| 路由 | Vue Router | ^4.4.5 |
| 状态管理 | Pinia | ^2.2.4 |
| HTTP | Axios | ^1.7.7 |

---

## 快速开始（本地开发）

### 环境要求

| 工具 | 版本要求 | 用途 |
|------|----------|------|
| JDK | 17 | 编译运行后端 |
| Maven | 3.6+ | 后端依赖管理 |
| MySQL | 5.7+ / 8.x | 数据存储 |
| Node.js | 18+ | 前端依赖 |
| HBuilderX | 5.x（推荐） | 编译运行 C 端 uni-app |

### 1. 初始化数据库

```powershell
# 在 36/ 目录下执行
mysql -u root -p < sql/schema.sql
```

默认创建 `vc_platform` 库，包含完整表结构和示例数据。

### 2. 配置数据库连接

编辑 `36/backend/src/main/resources/application-dev.yml`，修改数据库密码（默认 `123456`）：

```yaml
spring:
  datasource:
    username: root
    password: "你的MySQL密码"
```

### 3. 启动后端

```powershell
cd 36/backend
mvn spring-boot:run
```

- 端口：**8080**
- 首次启动自动建表、写入管理端菜单和管理员账号
- 验证：浏览器访问 http://localhost:8080/api/home/init

> 国内网络可用项目内的阿里云镜像配置：`mvn -s maven-settings.xml spring-boot:run`

### 4. 启动管理后台

```powershell
cd 36/admin
npm install
npm run dev
```

- 地址：**http://localhost:5174**
- `/api` 和 `/uploads` 已代理到 `http://localhost:8080`

### 5. 运行 C 端（可选）

方式 A（HBuilderX，推荐）：
1. HBuilderX → **文件 → 导入** → 选择 `36/frontend/` 目录
2. **运行 → 运行到浏览器**（H5）
3. 或 **运行 → 运行到小程序模拟器 → 微信开发者工具**

方式 B（仅验证 API）：不跑 C 端也可通过管理后台 http://localhost:5174 管理数据。

### 启动顺序

```
MySQL（服务）
    ↓
后端 :8080
    ↓
管理后台 :5174 ──→ 代理到后端
C 端小程序/H5 ──→ 直连后端 :8080
```

---

## 本地服务与账号

| 项目 | 值 |
|------|-----|
| 后端 API | http://localhost:8080 |
| 管理后台 | http://localhost:5174 |
| MySQL | 127.0.0.1:3306 / 库 `vc_platform` |
| 管理端账号 | `admin` / `admin123` |
| C 端短信验证码 | **123456**（mock，不发真实短信） |
| 微信一键登录 | mock 模式，返回测试号 `13800138000` |
| API 成功码 | `code === 200` |

---

## 主要功能模块

### C 端（微信小程序 / H5）

| 模块 | 说明 |
|------|------|
| 首页 | 轮播图、快捷菜单、推荐项目/机构/融资事件 |
| 项目库 | 多维筛选（行业、轮次、地区）、项目详情（融资/工商/团队） |
| 项目集 | 主题式项目集合（如"新能源"、"AI"），由 JSON 配置驱动 |
| 机构库 | 投资机构列表与详情（含投资事件反查，5 Tab 页面） |
| 融资事件 | 融资动态列表与精选 |
| 融资快报 | 快讯/文章双模板 |
| 研究院 | 研究报告列表与分类筛选 |
| 活动/路演 | 活动列表、详情、报名 |
| 投资人认证 | 在线提交认证申请，后台审核 |
| 寻求报道 | 提交报道申请，后台审核 |
| 项目入驻 | 创业者提交项目信息，**需后台人工审核** |
| 搜索 | 全站项目搜索 |

### 管理后台

| 模块 | 说明 |
|------|------|
| 工作台 | 数据概览仪表盘 |
| 动态 CRUD | 统一的数据管理页面，覆盖所有业务表 |
| 筛选管理 | 配置各列表页的筛选项（行业、轮次、地区等） |
| 审核管理 | 项目入驻审核、寻求报道审核、投资人认证审核 |
| 轮播图管理 | 首页轮播图 CRUD + 图片上传 |
| Excel 导入 | 项目和机构的批量导入（含多 sheet） |
| RBAC 权限 | 管理员账号、角色、权限管理 |

### 后端 API 模块

| 前缀 | 说明 |
|------|------|
| `/api/home` | 首页初始化 |
| `/api/news` | 融资快讯 |
| `/api/projects` | 项目库 / 详情 / 入驻 |
| `/api/institutions` | 机构库 |
| `/api/financing-events` | 融资事件 |
| `/api/activities` | 活动 / 路演 |
| `/api/research` | 研究院报告 |
| `/api/filters` | 筛选标签 bundle |
| `/api/user` | 登录、认证、个人中心 |
| `/api/admin/**` | 管理后台（CRUD、审核、上传） |

---

## 部署上线

> 详细部署流程见 [36/DEPLOY.md](./36/DEPLOY.md)。

### 生产环境

- **生产域名**：`https://lbweixin.lianbei88.com`
- **后端部署**：打包 JAR → 使用 `prod` profile 启动 → 配置 Nginx 反向代理
- **小程序发布**：HBuilderX → **发行 → 小程序-微信**
- **微信登录**：需在服务器配置 `WX_APP_SECRET` 环境变量

### 一键打包后端

```powershell
cd 36/backend
mvn clean package -DskipTests
# 生成 target/lianbei-vc-backend-0.3.0-SNAPSHOT.jar
```

---

## 相关文档

| 文档 | 说明 |
|------|------|
| [36/README.md](./36/README.md) | 详细本地运行说明（含常见问题） |
| [36/PROJECT_MEMORY.md](./36/PROJECT_MEMORY.md) | 完整 API 接口文档、数据表结构、开发规范 |
| [36/DEPLOY.md](./36/DEPLOY.md) | 上线部署清单与配置说明 |

---

## 许可证

本项目为内部项目，代码仅限授权人员使用。
