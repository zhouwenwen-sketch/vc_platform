# 上线发布清单

> 每次发版前对照本清单。代码侧「上线默认配置」已改回生产模式（2026-06-22）。

---

## 一、代码里已改回上线的项

| 文件 | 状态 |
|------|------|
| `frontend/utils/devConfig.js` | `USE_LOCAL_BACKEND = false` → 小程序连 `https://lbweixin.lianbei88.com` |
| `frontend/manifest.json` | `urlCheck: true` → 真机/体验版校验合法域名 |
| `backend/application.yml` | 微信登录 `mock-enabled: false`（默认不走 mock） |
| `backend/application-prod.yml` | 线上库 + 线上域名 + 关闭 mock |
| `backend/application-dev.yml` | `public-base-url` 改回线上（本地 profile 专用） |
| `admin` 生产构建 | `npm run build` 自动指向 `lbweixin.lianbei88.com`（无需改） |

本地联调时再改：`devConfig.js` → `USE_LOCAL_BACKEND = true` + 局域网 IP。

---

## 二、你需要做的事

### A. 部署后端（必须，否则轮播图等新功能线上不可用）

1. 打包：
   ```powershell
   cd backend
   mvn clean package -DskipTests
   ```
2. 将 `target/*.jar` 上传到服务器 `lbweixin.lianbei88.com`
3. **用 prod 配置启动**（不要用 dev）：
   ```bash
   java -jar lianbei-vc-*.jar --spring.profiles.active=prod
   ```
4. 在服务器设置环境变量（若尚未配置）：
   ```bash
   export WX_APP_SECRET=你的小程序AppSecret
   ```
5. 确认线上库已有 `home_banner` 表（首次部署执行 `sql/schema.sql` 中对应 DDL，或本地跑一次 dev 后端触发迁移）
6. **项目导入**：执行 `sql/project-import-migration.sql`（或重启后端由 `ProjectImportSchemaMigration` 自动加列）；菜单执行 `sql/project-import-admin-menu.sql`
7. **Nginx 导入超时**：Excel 批量导入需加大代理超时（见下文「五、Excel 导入与 Nginx 超时」）

### B. 上传微信小程序

1. HBuilderX 打开 **`frontend/`**
2. 确认 `utils/devConfig.js` 中 **`USE_LOCAL_BACKEND = false`**
3. **发行 → 小程序-微信**（不要用「运行」的开发包去提交审核）
4. 微信公众平台 → **开发管理 → 开发设置 → 服务器域名**，确认已配置：
   - request 合法域名：`https://lbweixin.lianbei88.com`
5. 在微信开发者工具或公众平台提交 **体验版 / 审核版**

### C. 部署管理后台（可选，若要在 `lbweixin.lianbei88.com` 管理轮播）

```powershell
cd admin
npm run build
```

将 `admin/dist/` 静态文件部署到服务器（与现有后台站点相同方式）。

### D. 登录相关（上线后行为）

| 功能 | 生产行为 |
|------|----------|
| 短信验证码 | `vc.sms.enabled: true` → 走 CRM 真实短信，**不是**固定 123456 |
| 微信一键登录 | 需服务器配置 `WX_APP_SECRET`，且 `mock-enabled: false` |
| 管理后台 | `https://lbweixin.lianbei88.com`，账号 `admin` / `admin123`（建议上线后改密） |

---

## 三、发版前自测（建议顺序）

1. 本地 `mvn spring-boot:run` + `--spring.profiles.active=prod` 试启动（或直接在测试机部署）
2. 浏览器访问：`https://lbweixin.lianbei88.com/api/home/init` → 应有 `bannerList`
3. HBuilderX **发行** 后，手机扫 **体验版** 二维码 → 首页轮播、融资快报、登录短信
4. 线上管理后台 → **内容管理 → 首页 → 轮播图** 能增删改

---

## 四、宝塔部署 + 配置 WX_APP_SECRET

### 4.1 先说明：不配 Secret 也能用吗？

| 登录方式 | 是否需要 WX_APP_SECRET |
|----------|------------------------|
| **短信验证码登录** | ❌ 不需要（走 CRM 短信） |
| **微信一键授权手机号登录** | ✅ **必须配置** |

建议上线前仍配置 Secret，否则用户点「微信登录」会失败。

---

### 4.2 在微信公众平台获取 AppSecret

1. 打开 [微信公众平台](https://mp.weixin.qq.com/) → 登录你的小程序
2. **开发 → 开发管理 → 开发设置**
3. 找到 **AppID(小程序ID)**：`wx92254578d6648906`（与项目 `manifest.json` 一致）
4. 点击 **AppSecret** 右侧「生成」或「重置」→ 管理员扫码 → **复制 Secret**（只显示一次，务必保存）

> ⚠️ Secret 不要写进 Git、不要发给无关人员。重置后旧 Secret 立即失效。

---

### 4.3 宝塔里配置环境变量（推荐）

#### 方式 A：Java 项目管理器（宝塔常见）

1. 宝塔面板 → **软件商店** → 安装 **Java 项目管理器**（若已装跳过）
2. **Java 项目** → 找到 `lianbei-vc` 项目 → **设置**
3. 打开 **环境变量** / **项目环境**（不同版本名称略有差异）
4. 新增一条：

   | 变量名 | 变量值 |
   |--------|--------|
   | `WX_APP_SECRET` | 粘贴刚才复制的 AppSecret |
   | `SPRING_PROFILES_ACTIVE` | `prod` |

5. **启动参数** 确认包含（若没有则加上）：

   ```
   --spring.profiles.active=prod
   ```

6. 保存 → **重启项目**

#### 方式 B：启动脚本里 export（无 Java 项目管理器时）

编辑宝塔里该项目的启动 Shell，在 `java -jar` 前面加：

```bash
export WX_APP_SECRET='这里填AppSecret'
export SPRING_PROFILES_ACTIVE=prod

java -jar -Xms512m -Xmx1024m /www/wwwroot/你的路径/lianbei-vc.jar --spring.profiles.active=prod
```

保存后重启。

#### 方式 C：Supervisor / systemd（高级）

在 `[Service]` 或 supervisor `environment` 里加：

```ini
Environment="WX_APP_SECRET=你的AppSecret"
Environment="SPRING_PROFILES_ACTIVE=prod"
```

---

### 4.4 宝塔部署后端 JAR 简要流程

1. 本地打包：`mvn clean package -DskipTests`
2. 宝塔 **文件** → 上传到例如 `/www/wwwroot/lianbei-vc/`
3. **Java 项目** → 添加项目：
   - 项目路径：JAR 所在目录
   - 启动命令：`java -jar lianbei-vc-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod`
   - 端口：`8080`（与 Nginx 反代一致）
4. 按 **4.3** 配置 `WX_APP_SECRET` 和 `prod` profile
5. **网站** → `lbweixin.lianbei88.com` → **反向代理** → 指向 `127.0.0.1:8080`
6. **uploads 目录**：JAR 同级的 `uploads/` 需有写权限（轮播图上传用）

首次部署若缺 `home_banner` 表，在宝塔 **数据库** → phpMyAdmin 执行 `sql/schema.sql` 里 `home_banner` 相关 DDL。

---

### 4.5 配置是否生效 — 自测

1. **短信登录**：小程序输入手机号 → 收真实验证码 → 能登录 ✅
2. **微信登录**：点击授权手机号 → 能登录 ✅（Secret 配错会提示「微信登录服务暂不可用，请使用验证码登录」）
3. **接口**：浏览器打开 `https://lbweixin.lianbei88.com/api/home/init` 有 `bannerList`

Secret 未配置时，后端日志可能出现：`微信小程序未配置，请使用验证码登录`。

---

## 五、发版前还需确认

1. **短信 CRM** — `crm.lianbei88.com` 生产环境能否正常发短信？
2. **管理后台** — `admin/dist` 是否部署在同一域名？
3. **小程序域名** — 公众平台 request 合法域名是否含 `https://lbweixin.lianbei88.com`

---

## 五、Excel 导入与 Nginx 超时

机构/项目 Excel 导入会在一次请求内解析多表并写库，耗时可能超过 Nginx 默认 **60 秒**，浏览器表现为 **504 Gateway Timeout**（后端其实可能仍在跑或已跑完）。

在宝塔 / Nginx 站点配置中，对 `lbweixin.lianbei88.com` 的 `location`（转发到 8080 的那段）增加：

```nginx
client_max_body_size 100m;
proxy_connect_timeout 300s;
proxy_send_timeout 300s;
proxy_read_timeout 300s;
```

保存后 **重载 Nginx**，重新部署含项目导入优化的后端 JAR，再在管理后台重试导入。

日志中若出现 `[project-import] start` 与 `done in xxx ms`，说明后端已处理完成；仅有 504 无 done 日志时，多为代理超时，按上面调大 `proxy_read_timeout`。

建议首次导入：**先只传「项目信息」**，成功后再逐个追加画像、融资、成员、动态。

---

## 六、发版后恢复本地开发

```js
// frontend/utils/devConfig.js
export const USE_LOCAL_BACKEND = true
export const DEV_HOST = 'http://<你的局域网IP>:8080'
```

```yaml
# backend/application-dev.yml
vc:
  public-base-url: http://<你的局域网IP>:8080
```

本地后端 + 本地管理后台（5174）继续联调即可。
