package com.lianbei.vc.admin.config;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/** 开发环境：后台管理表及初始数据 */
@Component
@Profile("dev")
public class AdminSchemaMigration implements org.springframework.beans.factory.InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(AdminSchemaMigration.class);

  private final JdbcTemplate jdbcTemplate;

  public AdminSchemaMigration(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void afterPropertiesSet() {
    try {
      ensureSchema();
      seedData();
      ensureContentSubModules();
      ensureCoverageMenu();
      ensureProjectCrudLabel();
      ensureFilterManageMenu();
      ensureRemoveProjectTagMenu();
      ensureMaDealMenu();
      ensureInstitutionImportMenu();
      ensureInstitutionFundManagerMenu();
      ensureProjectImportMenu();
      ensureProjectCollectionMenu();
    } catch (Exception ex) {
      log.warn("[schema] admin migration skipped: {}", ex.getMessage());
    }
  }

  private void ensureSchema() {
    jdbcTemplate.execute(
        """
        CREATE TABLE IF NOT EXISTS admin_role (
          id          BIGINT PRIMARY KEY AUTO_INCREMENT,
          code        VARCHAR(64)  NOT NULL UNIQUE,
          name        VARCHAR(64)  NOT NULL,
          description VARCHAR(255) DEFAULT NULL,
          status      TINYINT      NOT NULL DEFAULT 1,
          create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
          update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台角色'
        """);
    jdbcTemplate.execute(
        """
        CREATE TABLE IF NOT EXISTS admin_permission (
          id          BIGINT PRIMARY KEY AUTO_INCREMENT,
          code        VARCHAR(128) NOT NULL UNIQUE,
          name        VARCHAR(64)  NOT NULL,
          type        VARCHAR(16)  NOT NULL DEFAULT 'menu',
          parent_id   BIGINT       NOT NULL DEFAULT 0,
          path        VARCHAR(128) DEFAULT NULL,
          resource    VARCHAR(64)  DEFAULT NULL,
          sort_order  INT          NOT NULL DEFAULT 0,
          create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台权限'
        """);
    jdbcTemplate.execute(
        """
        CREATE TABLE IF NOT EXISTS admin_role_permission (
          id            BIGINT PRIMARY KEY AUTO_INCREMENT,
          role_id       BIGINT NOT NULL,
          permission_id BIGINT NOT NULL,
          UNIQUE KEY uk_role_perm (role_id, permission_id)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联'
        """);
    jdbcTemplate.execute(
        """
        CREATE TABLE IF NOT EXISTS admin_user (
          id          BIGINT PRIMARY KEY AUTO_INCREMENT,
          username    VARCHAR(64)  NOT NULL UNIQUE,
          password    VARCHAR(128) NOT NULL,
          nickname    VARCHAR(64)  DEFAULT NULL,
          role_id     BIGINT       NOT NULL,
          status      TINYINT      NOT NULL DEFAULT 1,
          create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
          update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台管理员'
        """);
    log.info("[schema] admin tables ensured");
  }

  private void seedData() {
    Long roleCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM admin_role", Long.class);
    if (roleCount != null && roleCount > 0) {
      return;
    }
    jdbcTemplate.update(
        "INSERT INTO admin_role (code, name, description, status) VALUES (?, ?, ?, 1)",
        "super_admin",
        "超级管理员",
        "拥有全部权限");
    jdbcTemplate.update(
        "INSERT INTO admin_role (code, name, description, status) VALUES (?, ?, ?, 1)",
        "editor",
        "内容编辑",
        "可管理内容数据");

    Long superRoleId =
        jdbcTemplate.queryForObject(
            "SELECT id FROM admin_role WHERE code = 'super_admin'", Long.class);
    Long editorRoleId =
        jdbcTemplate.queryForObject("SELECT id FROM admin_role WHERE code = 'editor'", Long.class);

    seedPermissions();

    List<Long> allPermissionIds =
        jdbcTemplate.queryForList("SELECT id FROM admin_permission", Long.class);
    for (Long permissionId : allPermissionIds) {
      jdbcTemplate.update(
          "INSERT INTO admin_role_permission (role_id, permission_id) VALUES (?, ?)",
          superRoleId,
          permissionId);
    }

    List<Long> editorPermissionIds =
        jdbcTemplate.queryForList(
            "SELECT id FROM admin_permission WHERE code LIKE 'crud:%' OR code = 'menu:dashboard'",
            Long.class);
    for (Long permissionId : editorPermissionIds) {
      jdbcTemplate.update(
          "INSERT INTO admin_role_permission (role_id, permission_id) VALUES (?, ?)",
          editorRoleId,
          permissionId);
    }

    String encoded = new BCryptPasswordEncoder().encode("admin123");
    jdbcTemplate.update(
        "INSERT INTO admin_user (username, password, nickname, role_id, status) VALUES (?, ?, ?, ?, 1)",
        "admin",
        encoded,
        "超级管理员",
        superRoleId);
    log.info("[schema] admin seed data inserted, default account admin/admin123");
  }

  private void seedPermissions() {
    insertPermission("menu:dashboard", "工作台", 0L, "/dashboard", null, 1);

    Long contentGroupId = insertPermission("menu:content", "内容管理", 0L, null, null, 10);
    seedContentModules(contentGroupId);

    Long userGroupId = insertPermission("menu:user", "用户与审核", 0L, null, null, 20);
    insertCrudGroup(
        userGroupId,
        new String[][] {
          {"user", "C端用户"},
          {"user_auth_record", "用户认证"},
          {"user_activity_record", "用户活动"},
          {"user_project", "用户项目"},
          {"connection_record", "对接记录"},
          {"project_onboard_record", "项目入驻"},
          {"activity_publish_request", "活动发布申请"},
          {"sms_code_record", "短信记录"}
        });

    Long systemGroupId = insertPermission("menu:system", "系统管理", 0L, null, null, 100);
    insertPermission("system:admin_user", "管理员账号", systemGroupId, "/system/admin-user", null, 1);
    insertPermission("system:admin_role", "角色权限", systemGroupId, "/system/admin-role", null, 2);
  }

  private void seedContentModules(Long contentGroupId) {
    Long homeId = insertPermission("menu:content:home", "首页", contentGroupId, null, null, 0);
    insertCrudGroup(homeId, new String[][] {{"home_banner", "轮播图"}});

    Long projectId = insertPermission("menu:content:project", "项目", contentGroupId, null, null, 1);
    insertCrudGroup(
        projectId,
        new String[][] {
          {"project", "卡片信息"},
          {"project_business", "项目工商"},
          {"project_financing", "项目融资"},
          {"project_shareholder", "股东信息"},
          {"project_team_member", "团队成员"},
          {"project_tag_rel", "标签关联"},
          {"project_collection", "项目集"}
        });
    Long institutionId =
        insertPermission("menu:content:institution", "机构", contentGroupId, null, null, 2);
    insertCrudGroup(
        institutionId,
        new String[][] {
          {"institution", "投资机构"},
          {"institution_fund_manager", "基金管理人"},
          {"institution_team_member", "机构团队"},
          {"project_financing_investor", "融资投资方"}
        });

    Long newsId = insertPermission("menu:content:news", "资讯", contentGroupId, null, null, 3);
    insertCrudGroup(
        newsId,
        new String[][] {
          {"news", "快讯资讯"},
          {"news_comment", "资讯评论"},
          {"coverage_apply_record", "寻求报道"}
        });

    Long activityId = insertPermission("menu:content:activity", "活动", contentGroupId, null, null, 4);
    insertCrudGroup(
        activityId,
        new String[][] {
          {"activity", "活动"},
          {"activity_registration", "活动报名"}
        });

    Long researchId =
        insertPermission("menu:content:research", "研究院", contentGroupId, null, null, 5);
    insertCrudGroup(
        researchId,
        new String[][] {
          {"research_report", "研究报告"},
          {"research_category", "研究分类"}
        });
    Long maDealId =
        insertPermission("menu:content:ma_deal", "融资并购", contentGroupId, null, null, 6);
    insertCrudGroup(maDealId, new String[][] {{"ma_deal", "融资并购"}});
    insertPermission("library_filter:manage", "筛选管理", contentGroupId, "/filters/manage", null, 7);
  }

  /** 已有库：将内容管理下的平铺菜单重组为可折叠子模块 */
  private void ensureContentSubModules() {
    Long contentGroupId = findPermissionId("menu:content");
    if (contentGroupId == null || findPermissionId("menu:content:project") != null) {
      return;
    }
    log.info("[schema] migrating content menu into collapsible modules");
    seedContentModules(contentGroupId);
    Long projectId = findPermissionId("menu:content:project");
    Long institutionId = findPermissionId("menu:content:institution");
    Long newsId = findPermissionId("menu:content:news");
    Long activityId = findPermissionId("menu:content:activity");
    Long researchId = findPermissionId("menu:content:research");
    reparentCrud(contentGroupId, projectId, "project", "project_business", "project_financing",
        "project_shareholder", "project_team_member", "project_tag_rel");
    reparentCrud(
        contentGroupId,
        institutionId,
        "institution",
        "institution_fund_manager",
        "institution_team_member",
        "project_financing_investor");
    reparentCrud(contentGroupId, newsId, "news", "news_comment", "coverage_apply_record");
    reparentCrud(contentGroupId, activityId, "activity", "activity_registration");
    reparentCrud(contentGroupId, researchId, "research_report", "research_category");
  }

  private void reparentCrud(Long oldParentId, Long newParentId, String... resources) {
    for (int i = 0; i < resources.length; i++) {
      jdbcTemplate.update(
          "UPDATE admin_permission SET parent_id = ?, sort_order = ? WHERE code = ? AND parent_id = ?",
          newParentId,
          i + 1,
          "crud:" + resources[i],
          oldParentId);
    }
  }

  private Long findPermissionId(String code) {
    List<Long> ids =
        jdbcTemplate.queryForList("SELECT id FROM admin_permission WHERE code = ?", Long.class, code);
    return ids.isEmpty() ? null : ids.get(0);
  }

  private void insertCrudGroup(Long parentId, String[][] resources) {
    int sort = 1;
    for (String[] item : resources) {
      Long existingId = findPermissionId("crud:" + item[0]);
      if (existingId != null) {
        jdbcTemplate.update(
            "UPDATE admin_permission SET parent_id = ?, name = ?, path = ?, resource = ?, sort_order = ? WHERE id = ?",
            parentId,
            item[1],
            "/crud/" + item[0],
            item[0],
            sort++,
            existingId);
        continue;
      }
      insertPermission(
          "crud:" + item[0], item[1], parentId, "/crud/" + item[0], item[0], sort++);
    }
  }

  /** 已有库：项目 CRUD 菜单改名为卡片信息 */
  private void ensureProjectCrudLabel() {
    int updated =
        jdbcTemplate.update(
            "UPDATE admin_permission SET name = ? WHERE code = ?",
            "卡片信息",
            "crud:project");
    if (updated > 0) {
      log.info("[schema] renamed crud:project menu to 卡片信息");
    }
  }

  /** 已有库：统一筛选管理菜单（替换旧版项目库筛选） */
  private void ensureFilterManageMenu() {
    Long contentGroupId = findPermissionId("menu:content");
    if (contentGroupId == null) {
      return;
    }
    Long legacyId = findPermissionId("library_filter:project");
    if (legacyId != null) {
      jdbcTemplate.update("DELETE FROM admin_role_permission WHERE permission_id = ?", legacyId);
      jdbcTemplate.update("DELETE FROM admin_permission WHERE id = ?", legacyId);
      log.info("[schema] removed legacy library_filter:project menu");
    }
    if (findPermissionId("library_filter:manage") != null) {
      return;
    }
    log.info("[schema] adding library_filter:manage admin menu");
    insertPermission(
        "library_filter:manage", "筛选管理", contentGroupId, "/filters/manage", null, 6);
    Long permId = findPermissionId("library_filter:manage");
    if (permId == null) {
      return;
    }
    List<Long> roleIds =
        jdbcTemplate.queryForList(
            "SELECT id FROM admin_role WHERE code IN ('super_admin', 'editor')", Long.class);
    for (Long roleId : roleIds) {
      Long count =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM admin_role_permission WHERE role_id = ? AND permission_id = ?",
              Long.class,
              roleId,
              permId);
      if (count != null && count == 0) {
        jdbcTemplate.update(
            "INSERT INTO admin_role_permission (role_id, permission_id) VALUES (?, ?)",
            roleId,
            permId);
      }
    }
  }

  /** 已有库：移除项目标签 CRUD 菜单（标签改在卡片信息中维护） */
  private void ensureRemoveProjectTagMenu() {
    Long permId = findPermissionId("crud:project_tag");
    if (permId == null) {
      return;
    }
    jdbcTemplate.update("DELETE FROM admin_role_permission WHERE permission_id = ?", permId);
    jdbcTemplate.update("DELETE FROM admin_permission WHERE id = ?", permId);
    log.info("[schema] removed crud:project_tag admin menu");
  }

  /** 已有库：增加融资并购管理菜单 */
  private void ensureMaDealMenu() {
    if (findPermissionId("crud:ma_deal") != null) {
      return;
    }
    Long contentGroupId = findPermissionId("menu:content");
    if (contentGroupId == null) {
      return;
    }
    Long groupId = findPermissionId("menu:content:ma_deal");
    if (groupId == null) {
      log.info("[schema] adding menu:content:ma_deal");
      groupId = insertPermission("menu:content:ma_deal", "融资并购", contentGroupId, null, null, 6);
    }
    log.info("[schema] adding crud:ma_deal admin menu");
    insertPermission("crud:ma_deal", "融资并购", groupId, "/crud/ma_deal", "ma_deal", 1);
    grantMenuToEditorRoles("crud:ma_deal");
  }

  private void grantMenuToEditorRoles(String permissionCode) {
    Long permId = findPermissionId(permissionCode);
    if (permId == null) {
      return;
    }
    List<Long> roleIds =
        jdbcTemplate.queryForList("SELECT id FROM admin_role WHERE code IN ('super_admin', 'editor')", Long.class);
    for (Long roleId : roleIds) {
      Long count =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM admin_role_permission WHERE role_id = ? AND permission_id = ?",
              Long.class,
              roleId,
              permId);
      if (count != null && count == 0) {
        jdbcTemplate.update(
            "INSERT INTO admin_role_permission (role_id, permission_id) VALUES (?, ?)", roleId, permId);
      }
    }
  }

  /** 已有库：在资讯模块下增加寻求报道菜单 */
  private void ensureCoverageMenu() {
    if (findPermissionId("crud:coverage_apply_record") != null) {
      return;
    }
    Long newsGroupId = findPermissionId("menu:content:news");
    if (newsGroupId == null) {
      return;
    }
    log.info("[schema] adding coverage_apply_record admin menu");
    insertPermission(
        "crud:coverage_apply_record",
        "寻求报道",
        newsGroupId,
        "/crud/coverage_apply_record",
        "coverage_apply_record",
        3);
    Long permId = findPermissionId("crud:coverage_apply_record");
    if (permId == null) {
      return;
    }
    List<Long> roleIds =
        jdbcTemplate.queryForList("SELECT id FROM admin_role WHERE code IN ('super_admin', 'editor')", Long.class);
    for (Long roleId : roleIds) {
      Long count =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM admin_role_permission WHERE role_id = ? AND permission_id = ?",
              Long.class,
              roleId,
              permId);
      if (count != null && count == 0) {
        jdbcTemplate.update(
            "INSERT INTO admin_role_permission (role_id, permission_id) VALUES (?, ?)", roleId, permId);
      }
    }
  }

  private Long insertPermission(
      String code, String name, Long parentId, String path, String resource, int sortOrder) {
    jdbcTemplate.update(
        """
        INSERT INTO admin_permission (code, name, type, parent_id, path, resource, sort_order)
        VALUES (?, ?, 'menu', ?, ?, ?, ?)
        """,
        code,
        name,
        parentId,
        path,
        resource,
        sortOrder);
    return jdbcTemplate.queryForObject(
        "SELECT id FROM admin_permission WHERE code = ?", Long.class, code);
  }

  /** 已有库：基金管理人 CRUD 菜单 */
  private void ensureInstitutionFundManagerMenu() {
    Long institutionGroupId = findPermissionId("menu:content:institution");
    if (institutionGroupId == null) {
      return;
    }
    ensureCrudMenu(institutionGroupId, "institution_fund_manager", "基金管理人", 2);
    ensureCrudMenu(institutionGroupId, "institution_team_member", "机构团队", 3);
  }

  private void ensureCrudMenu(Long parentId, String resource, String label, int sortOrder) {
    String code = "crud:" + resource;
    if (findPermissionId(code) != null) {
      return;
    }
    log.info("[schema] adding {} admin menu", code);
    insertPermission(code, label, parentId, "/crud/" + resource, resource, sortOrder);
    Long permId = findPermissionId(code);
    if (permId == null) {
      return;
    }
    List<Long> roleIds =
        jdbcTemplate.queryForList(
            "SELECT id FROM admin_role WHERE code IN ('super_admin', 'editor')", Long.class);
    for (Long roleId : roleIds) {
      Long count =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM admin_role_permission WHERE role_id = ? AND permission_id = ?",
              Long.class,
              roleId,
              permId);
      if (count != null && count == 0) {
        jdbcTemplate.update(
            "INSERT INTO admin_role_permission (role_id, permission_id) VALUES (?, ?)",
            roleId,
            permId);
      }
    }
  }

  /** 已有库：机构 Excel 导入菜单 */
  private void ensureInstitutionImportMenu() {
    Long institutionGroupId = findPermissionId("menu:content:institution");
    if (institutionGroupId == null) {
      return;
    }
    if (findPermissionId("institution:import") != null) {
      return;
    }
    log.info("[schema] adding institution:import admin menu");
    insertPermission(
        "institution:import",
        "机构导入",
        institutionGroupId,
        "/content/institution-import",
        null,
        3);
    Long permId = findPermissionId("institution:import");
    if (permId == null) {
      return;
    }
    List<Long> roleIds =
        jdbcTemplate.queryForList(
            "SELECT id FROM admin_role WHERE code IN ('super_admin', 'editor')", Long.class);
    for (Long roleId : roleIds) {
      Long count =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM admin_role_permission WHERE role_id = ? AND permission_id = ?",
              Long.class,
              roleId,
              permId);
      if (count != null && count == 0) {
        jdbcTemplate.update(
            "INSERT INTO admin_role_permission (role_id, permission_id) VALUES (?, ?)",
            roleId,
            permId);
      }
    }
  }

  /** 已有库：项目集 CRUD 菜单 */
  private void ensureProjectCollectionMenu() {
    Long projectGroupId = findPermissionId("menu:content:project");
    if (projectGroupId == null) {
      return;
    }
    ensureCrudMenu(projectGroupId, "project_collection", "项目集", 10);
  }

  /** 已有库：项目 Excel 导入菜单 */
  private void ensureProjectImportMenu() {
    Long projectGroupId = findPermissionId("menu:content:project");
    if (projectGroupId == null) {
      return;
    }
    if (findPermissionId("project:import") != null) {
      return;
    }
    log.info("[schema] adding project:import admin menu");
    insertPermission(
        "project:import",
        "项目导入",
        projectGroupId,
        "/content/project-import",
        null,
        2);
    Long permId = findPermissionId("project:import");
    if (permId == null) {
      return;
    }
    List<Long> roleIds =
        jdbcTemplate.queryForList(
            "SELECT id FROM admin_role WHERE code IN ('super_admin', 'editor')", Long.class);
    for (Long roleId : roleIds) {
      Long count =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM admin_role_permission WHERE role_id = ? AND permission_id = ?",
              Long.class,
              roleId,
              permId);
      if (count != null && count == 0) {
        jdbcTemplate.update(
            "INSERT INTO admin_role_permission (role_id, permission_id) VALUES (?, ?)",
            roleId,
            permId);
      }
    }
  }
}
