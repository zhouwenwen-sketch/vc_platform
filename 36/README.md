# 大学生创投平台（本地离线版）

创投类小程序全栈项目，包含 **C 端小程序/H5**、**Spring Boot 后端 API**、**Vue3 管理后台** 三端。

> **当前为本地离线配置**：解压后在自己电脑即可运行，不依赖线上服务器、不发送真实短信、不需要迁移线上数据库数据。

---

## 目录

- [技术栈](#技术栈)
- [需要安装的软件](#需要安装的软件)
- [环境配置](#环境配置)
- [启动项目](#启动项目)
- [本地服务与账号](#本地服务与账号)
- [项目结构](#项目结构)
- [配置文件说明](#配置文件说明)
- [打包发给别人](#打包发给别人)
- [常见问题](#常见问题)
- [相关文档](#相关文档)

---

## 技术栈

### 总览

| 层级 | 技术 |
|------|------|
| C 端 | uni-app + Vue 3 + uView-Plus + Pinia |
| 管理后台 | Vue 3 + Vite + Element Plus + Pinia + Vue Router |
| 后端 API | Spring Boot 3 + MyBatis-Plus + MySQL |
| 数据库 | MySQL 5.7+ / 8.x |
| 构建工具 | Maven（后端）、Vite（前端）、HBuilderX（C 端编译） |

### C 端（`frontend/`）

| 类别 | 技术 | 版本 / 说明 |
|------|------|-------------|
| 框架 | uni-app | Vue 3 模式，支持微信小程序 / H5 |
| UI 库 | uView-Plus | ^3.1.30 |
| 状态管理 | Pinia | ^2.1.7 |
| 样式 | SCSS | sass 1.63 |
| 构建 | Vite + @dcloudio/vite-plugin-uni | 通过 HBuilderX 或 CLI 编译 |
| 图标 | sharp 生成 PNG | `npm run icons:generate` |

### 管理后台（`admin/`）

| 类别 | 技术 | 版本 / 说明 |
|------|------|-------------|
| 框架 | Vue 3 | ^3.5.12 |
| 构建 | Vite | ^5.4.10 |
| UI 库 | Element Plus | ^2.8.4 |
| 图标 | @element-plus/icons-vue | ^2.3.1 |
| 路由 | Vue Router | ^4.4.5 |
| 状态管理 | Pinia | ^2.2.4 |
| HTTP | Axios | ^1.7.7 |
| 样式 | Sass | ^1.80.3 |

### 后端（`backend/`）

| 类别 | 技术 | 版本 / 说明 |
|------|------|-------------|
| 语言 | Java | 17 |
| 框架 | Spring Boot | 3.2.5 |
| Web | Spring Web MVC | REST API |
| ORM | MyBatis-Plus | 3.5.6 |
| 数据库驱动 | MySQL Connector/J | 随 Spring Boot 管理 |
| 安全 | Spring Security Crypto | BCrypt 密码加密（管理端） |
| Excel | Apache POI | 5.2.5（项目/机构批量导入） |
| 工具 | Lombok | 简化实体类 |
| 构建 | Maven | 3.6+ |

### 数据库

| 类别 | 说明 |
|------|------|
| 引擎 | MySQL 5.7+ 或 8.x |
| 字符集 | utf8mb4 |
| 默认库名 | `vc_platform` |
| 初始化脚本 | `sql/schema.sql`（DDL + 示例数据） |
| 迁移 | 后端 dev 环境启动时自动执行 Schema 迁移（建表、菜单、种子数据） |

### 开发与运行工具

| 工具 | 用途 | 是否必须 |
|------|------|----------|
| JDK 17 | 编译运行后端 | **必须** |
| Maven | 后端依赖与启动 | **必须** |
| MySQL | 数据存储 | **必须** |
| Node.js 18+ | 管理后台 / C 端 npm 依赖 | **必须** |
| HBuilderX | 编译运行 C 端 uni-app | 跑小程序/H5 时需要 |
| 微信开发者工具 | 调试微信小程序 | 跑小程序时需要 |
| IntelliJ IDEA / VS Code | 代码编辑 | 可选 |

---

## 需要安装的软件

按顺序安装以下软件（Windows 示例）：

### 1. JDK 17

- 下载：[Eclipse Temurin JDK 17](https://adoptium.net/) 或 Oracle JDK 17
- 安装后验证：

```powershell
java -version
# 应显示 17.x
```

配置环境变量 `JAVA_HOME` 指向 JDK 安装目录。

### 2. Maven

- 下载：[Apache Maven](https://maven.apache.org/download.cgi)
- 解压后将 `bin` 目录加入 `PATH`
- 验证：

```powershell
mvn -version
```

> 国内网络可复用项目内 `backend/maven-settings.xml`（阿里云镜像）：
>
> ```powershell
> mvn -s maven-settings.xml spring-boot:run
> ```

### 3. MySQL

- 下载：[MySQL Community Server](https://dev.mysql.com/downloads/mysql/) 5.7+ 或 8.x
- 安装时设置 root 密码（默认配置为 `123456`，可自行修改）
- 安装后确认 MySQL 服务已启动
- 验证：

```powershell
mysql -u root -p -e "SELECT VERSION();"
```

### 4. Node.js

- 下载：[Node.js LTS](https://nodejs.org/)（建议 18 或 20）
- 验证：

```powershell
node -v
npm -v
```

### 5. HBuilderX（运行 C 端时需要）

- 下载：[HBuilderX](https://www.dcloud.io/hbuilderx.html)
- 用途：编译 uni-app，运行到浏览器或微信开发者工具

### 6. 微信开发者工具（运行小程序时需要）

- 下载：[微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)
- 首次使用需在 HBuilderX 中配置其安装路径

---

## 环境配置

### 1. 解压项目

将压缩包解压到任意目录，确保项目结构如下：

```
36/
├── backend/
├── frontend/
├── admin/
├── sql/
├── uploads/
└── README.md
```

> **注意**：HBuilderX 必须打开 **`frontend/`** 目录，不要打开上级 `36/`。

### 2. 初始化数据库

在 MySQL 中执行初始化脚本（会创建 `vc_platform` 库、表结构和示例数据）：

```powershell
# 在项目根目录 36/ 下执行
mysql -u root -p < sql/schema.sql
```

若 MySQL 不在 PATH 中，可用 Navicat / MySQL Workbench 打开 `sql/schema.sql` 执行。

### 3. 修改数据库密码（如需要）

编辑 `backend/src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    username: root
    password: "你的MySQL密码"   # 默认 123456
```

### 4. 安装前端依赖

```powershell
# 管理后台
cd admin
npm install

# C 端（HBuilderX 运行前建议先装）
cd ../frontend
npm install
```

### 5. C 端 API 地址（按需）

默认配置在 `frontend/utils/devConfig.js`，已指向本机后端：

| 运行方式 | 配置 |
|----------|------|
| H5 / 浏览器 | `http://127.0.0.1:8080`（默认，无需改） |
| 微信小程序 | 改为本机局域网 IP，如 `http://192.168.1.100:8080` |

查看本机 IP：

```powershell
ipconfig
# 找到 IPv4 地址，例如 192.168.1.100
```

小程序还需在微信开发者工具 → **详情 → 本地设置** → 勾选 **「不校验合法域名、web-view…」**。

---

## 启动项目

按以下顺序启动（建议开 3 个终端窗口）：

### 终端 1：启动后端

```powershell
cd backend
mvn spring-boot:run
```

或使用 IDEA 打开 `backend/`，运行主类 `LianbeiVcApplication`，Profile 选 `dev`。

- 端口：**8080**
- 首次启动会自动建表、写入管理端菜单和管理员账号
- 上传文件保存在项目 `uploads/` 目录
- 看到 `Started LianbeiVcApplication` 表示启动成功

验证：浏览器访问 http://localhost:8080/api/home/init ，应返回 JSON。

### 终端 2：启动管理后台

```powershell
cd admin
npm run dev
```

- 地址：**http://localhost:5174**
- `/api` 和 `/uploads` 已代理到 `http://localhost:8080`

### 终端 3：运行 C 端（可选）

**方式 A：HBuilderX（推荐）**

1. HBuilderX → **文件 → 导入** → 选择 `frontend/` 目录
2. **运行 → 运行到浏览器**（H5）
3. 或 **运行 → 运行到小程序模拟器 → 微信开发者工具**

**方式 B：仅验证 API**

不跑 C 端也可通过管理后台 http://localhost:5174 管理数据。

### 启动顺序示意

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

## 项目结构

```
36/
├── backend/                      # Spring Boot 后端
│   ├── src/main/java/            # Java 源码
│   ├── src/main/resources/       # 配置文件（application-*.yml）
│   ├── pom.xml                   # Maven 依赖
│   └── maven-settings.xml        # 国内 Maven 镜像（可选）
├── frontend/                     # uni-app C 端（HBuilderX 打开此目录）
│   ├── pages/                    # 页面
│   ├── components/               # 组件
│   ├── api/                      # 接口封装
│   ├── utils/                    # 工具（含 devConfig.js）
│   ├── static/                   # 静态资源（Tab 图标、首页图标）
│   ├── scripts/                  # 图标生成等脚本
│   └── pages.json                # 页面路由与 TabBar
├── admin/                        # Vue3 管理后台
│   ├── src/views/                # 页面
│   ├── src/api/                  # API 请求
│   └── vite.config.js            # Vite 配置（含 API 代理）
├── sql/
│   └── schema.sql                # 数据库全量 DDL + 示例数据
├── uploads/                      # 本地上传文件目录
├── PROJECT_MEMORY.md             # 接口 / 表结构 / 开发备忘
├── DEPLOY.md                     # 上线部署说明（本地使用可忽略）
└── README.md                     # 本文件
```

---

## 配置文件说明

| 文件 | 作用 |
|------|------|
| `backend/src/main/resources/application.yml` | 后端主配置（端口 8080、profile=dev、短信/微信 mock） |
| `backend/src/main/resources/application-dev.yml` | 本地数据库连接 |
| `frontend/utils/devConfig.js` | C 端 API 地址（`BASE_URL`） |
| `admin/vite.config.js` | 管理后台端口 5174、API 代理 |
| `admin/src/api/request.js` | 管理端 axios 封装 |
| `frontend/styles/theme.scss` | C 端主题色（当前为青绿色 `#78B9B1`） |

---

## 打包发给别人

### 压缩前建议排除（减小体积）

- `backend/target/`
- `frontend/node_modules/`
- `admin/node_modules/`
- `frontend/unpackage/`
- `uploads/` 中的实际上传文件（可保留空目录）

### 对方收到后的步骤

1. 安装 JDK 17、Maven、MySQL、Node.js
2. 执行 `mysql -u root -p < sql/schema.sql`
3. 修改 `application-dev.yml` 中的数据库密码
4. `cd backend && mvn spring-boot:run`
5. `cd admin && npm install && npm run dev`
6. （可选）安装 HBuilderX，导入 `frontend/` 运行 C 端

---

## 常见问题

| 问题 | 解决方法 |
|------|----------|
| 后端启动报数据库连接失败 | 确认 MySQL 已启动；密码与 `application-dev.yml` 一致；已执行 `sql/schema.sql` |
| 管理后台「网络错误」 | 确认后端已在 8080 运行 |
| HBuilderX 报缺少 pages.json | 必须打开 **`frontend/`**，不是上级 `36/` |
| 小程序请求失败 | `devConfig.js` 中改为局域网 IP；勾选「不校验合法域名」；重新从 HBuilderX 运行 |
| 验证码登录失败 | 使用固定码 **123456** |
| Maven 下载依赖慢或失败 | 使用 `mvn -s maven-settings.xml spring-boot:run` |
| 首页 / Tab 图标颜色不对 | 执行 `cd frontend && npm run icons:generate`，再重新编译 C 端 |
| 改了 C 端配置不生效 | 在 HBuilderX「运行 → 运行到微信开发者工具」重新编译，并清除开发者工具缓存 |

---

## 相关文档

| 文档 | 说明 |
|------|------|
| [PROJECT_MEMORY.md](./PROJECT_MEMORY.md) | 接口列表、数据表、业务逻辑备忘 |
| [DEPLOY.md](./DEPLOY.md) | 上线部署流程（本地使用可忽略） |

---

## 主要 API 模块

| 前缀 | 说明 |
|------|------|
| `/api/home` | 首页初始化（轮播、菜单、推荐） |
| `/api/news` | 融资快讯 |
| `/api/projects` | 项目库 / 详情 / 入驻 |
| `/api/institutions` | 机构库 |
| `/api/financing-events` | 融资事件 |
| `/api/activities` | 活动 / 路演 |
| `/api/user` | 登录、认证、个人中心 |
| `/api/admin/**` | 管理后台（CRUD、审核、上传） |
