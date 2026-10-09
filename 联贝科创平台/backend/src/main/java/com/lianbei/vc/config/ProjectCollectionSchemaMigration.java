package com.lianbei.vc.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** 项目集表、初始数据及后台菜单（所有环境启动时自动补齐） */
@Component
public class ProjectCollectionSchemaMigration
    implements org.springframework.beans.factory.InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(ProjectCollectionSchemaMigration.class);

  private final JdbcTemplate jdbcTemplate;
  private final ObjectMapper objectMapper;

  public ProjectCollectionSchemaMigration(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
    this.jdbcTemplate = jdbcTemplate;
    this.objectMapper = objectMapper;
  }

  @Override
  public void afterPropertiesSet() {
    try {
      ensureSchema();
    } catch (Exception ex) {
      log.error("[schema] project_collection ensureSchema failed", ex);
    }
    try {
      seedData();
    } catch (Exception ex) {
      log.error("[schema] project_collection seedData failed", ex);
    }
    try {
      ensureAdminMenu();
    } catch (Exception ex) {
      log.error("[schema] project_collection ensureAdminMenu failed", ex);
    }
  }

  private void ensureSchema() {
    jdbcTemplate.execute(
        """
        CREATE TABLE IF NOT EXISTS project_collection_tab (
          id          BIGINT PRIMARY KEY AUTO_INCREMENT,
          name        VARCHAR(64)  NOT NULL COMMENT 'Tab 名称',
          sort_order  INT          NOT NULL DEFAULT 0 COMMENT '排序',
          UNIQUE KEY uk_project_collection_tab_name (name)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目集 Tab'
        """);
    jdbcTemplate.execute(
        """
        CREATE TABLE IF NOT EXISTS project_collection (
          id               BIGINT PRIMARY KEY AUTO_INCREMENT,
          category         VARCHAR(64)   NOT NULL COMMENT '所属 Tab',
          badge            VARCHAR(64)   DEFAULT NULL COMMENT '角标',
          title            VARCHAR(256)  NOT NULL COMMENT '标题',
          cover            VARCHAR(512)  NOT NULL COMMENT '封面图',
          cover_title      VARCHAR(512)  DEFAULT NULL COMMENT '封面叠字',
          collection_date  DATE          DEFAULT NULL COMMENT '展示日期',
          summary          VARCHAR(512)  DEFAULT NULL COMMENT '列表摘要',
          description      TEXT          DEFAULT NULL COMMENT '详情描述',
          projects_data    LONGTEXT      DEFAULT NULL COMMENT '项目 JSON 数组',
          sort_order       INT           NOT NULL DEFAULT 0 COMMENT '排序',
          status           TINYINT       NOT NULL DEFAULT 1 COMMENT '1=启用 0=禁用',
          create_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
          update_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
          KEY idx_project_collection_category_status (category, status, sort_order)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目集'
        """);
    log.info("[schema] project_collection ensured");
  }

  private void seedData() throws IOException {
    Long collectionCount =
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM project_collection", Long.class);
    if (collectionCount != null && collectionCount > 0) {
      return;
    }
    ClassPathResource resource = new ClassPathResource("config/project-collections.json");
    Map<String, Object> config =
        objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});

    @SuppressWarnings("unchecked")
    List<String> tabs = (List<String>) config.getOrDefault("tabs", List.of());
    int tabSort = 1;
    for (String tab : tabs) {
      if ("全部".equals(tab)) {
        continue;
      }
      jdbcTemplate.update(
          "INSERT IGNORE INTO project_collection_tab (name, sort_order) VALUES (?, ?)",
          tab,
          tabSort++);
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> collections =
        (List<Map<String, Object>>) config.getOrDefault("collections", List.of());
    int sort = 1;
    for (Map<String, Object> item : collections) {
      String projectsJson = objectMapper.writeValueAsString(item.getOrDefault("projects", List.of()));
      jdbcTemplate.update(
          """
          INSERT INTO project_collection
            (id, category, badge, title, cover, cover_title, collection_date, summary, description, projects_data, sort_order, status)
          VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1)
          """,
          toLong(item.get("id")),
          stringValue(item.get("category")),
          stringValue(item.get("badge")),
          stringValue(item.get("title")),
          stringValue(item.get("cover")),
          stringValue(item.get("coverTitle")),
          stringValue(item.get("date")),
          stringValue(item.get("summary")),
          stringValue(item.get("description")),
          projectsJson,
          sort++);
    }
    log.info("[schema] project_collection seed data inserted");
  }

  private void ensureAdminMenu() {
    Long projectGroupId = findPermissionId("menu:content:project");
    if (projectGroupId == null) {
      log.warn("[schema] project_collection menu skipped: menu:content:project not found");
      return;
    }
    if (findPermissionId("crud:project_collection") != null) {
      return;
    }
    jdbcTemplate.update(
        """
        INSERT INTO admin_permission (code, name, type, parent_id, path, resource, sort_order)
        VALUES (?, ?, 'menu', ?, ?, ?, ?)
        """,
        "crud:project_collection",
        "项目集",
        projectGroupId,
        "/crud/project_collection",
        "project_collection",
        10);
    Long permId = findPermissionId("crud:project_collection");
    if (permId == null) {
      log.warn("[schema] project_collection menu insert failed");
      return;
    }
    grantToRolesWithProjectMenu(permId);
    log.info("[schema] added crud:project_collection admin menu");
  }

  /** 给所有已拥有「项目」子菜单的角色授权，避免自定义角色看不到新菜单 */
  private void grantToRolesWithProjectMenu(Long permId) {
    List<Long> roleIds =
        jdbcTemplate.queryForList(
            """
            SELECT DISTINCT rp.role_id
            FROM admin_role_permission rp
            JOIN admin_permission child ON child.id = rp.permission_id
            JOIN admin_permission parent ON parent.id = child.parent_id
            WHERE parent.code = 'menu:content:project'
            """,
            Long.class);
    for (Long roleId : roleIds) {
      Long exists =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM admin_role_permission WHERE role_id = ? AND permission_id = ?",
              Long.class,
              roleId,
              permId);
      if (exists != null && exists == 0) {
        jdbcTemplate.update(
            "INSERT INTO admin_role_permission (role_id, permission_id) VALUES (?, ?)",
            roleId,
            permId);
      }
    }
  }

  private Long findPermissionId(String code) {
    List<Long> ids =
        jdbcTemplate.queryForList("SELECT id FROM admin_permission WHERE code = ?", Long.class, code);
    return ids.isEmpty() ? null : ids.get(0);
  }

  private Long toLong(Object value) {
    if (value instanceof Number number) {
      return number.longValue();
    }
    return Long.parseLong(String.valueOf(value));
  }

  private String stringValue(Object value) {
    if (value == null) {
      return null;
    }
    String text = String.valueOf(value).trim();
    return text.isEmpty() ? null : text;
  }
}
