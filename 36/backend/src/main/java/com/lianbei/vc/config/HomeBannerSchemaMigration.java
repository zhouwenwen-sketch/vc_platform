package com.lianbei.vc.config;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** 开发环境：首页轮播表、初始数据及后台菜单 */
@Component
@Profile("dev")
public class HomeBannerSchemaMigration implements org.springframework.beans.factory.InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(HomeBannerSchemaMigration.class);

  private final JdbcTemplate jdbcTemplate;

  public HomeBannerSchemaMigration(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void afterPropertiesSet() {
    try {
      ensureSchema();
      seedData();
      ensureAdminMenu();
    } catch (Exception ex) {
      log.warn("[schema] home_banner migration skipped: {}", ex.getMessage());
    }
  }

  private void ensureSchema() {
    jdbcTemplate.execute(
        """
        CREATE TABLE IF NOT EXISTS home_banner (
          id          BIGINT PRIMARY KEY AUTO_INCREMENT,
          image_url   VARCHAR(512) NOT NULL COMMENT '轮播图片',
          title       VARCHAR(128) NOT NULL COMMENT '主标题',
          subtitle    VARCHAR(256) DEFAULT NULL COMMENT '副标题',
          link_url    VARCHAR(512) DEFAULT NULL COMMENT '跳转链接',
          sort_order  INT          NOT NULL DEFAULT 0 COMMENT '排序，越小越靠前',
          status      TINYINT      NOT NULL DEFAULT 1 COMMENT '1=启用 0=禁用',
          create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
          update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
          KEY idx_home_banner_status_sort (status, sort_order)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页轮播图'
        """);
    log.info("[schema] home_banner ensured");
  }

  private void seedData() {
    Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM home_banner", Long.class);
    if (count != null && count > 0) {
      return;
    }
    jdbcTemplate.update(
        """
        INSERT INTO home_banner (image_url, title, subtitle, sort_order, status)
        VALUES (?, ?, ?, ?, 1)
        """,
        "https://picsum.photos/750/280?random=1",
        "免费对接优质项目",
        "十年媒体资源积累，助力创业者链接资本",
        1);
    jdbcTemplate.update(
        """
        INSERT INTO home_banner (image_url, title, subtitle, sort_order, status)
        VALUES (?, ?, ?, ?, 1)
        """,
        "https://picsum.photos/750/280?random=2",
        "WISE2025 商业之王",
        "年度创投盛典火热报名中",
        2);
    log.info("[schema] home_banner seed data inserted");
  }

  private void ensureAdminMenu() {
    Long contentGroupId = findPermissionId("menu:content");
    if (contentGroupId == null) {
      return;
    }
    Long homeGroupId = findPermissionId("menu:content:home");
    if (homeGroupId == null) {
      jdbcTemplate.update(
          """
          INSERT INTO admin_permission (code, name, type, parent_id, path, resource, sort_order)
          VALUES (?, ?, 'menu', ?, NULL, NULL, ?)
          """,
          "menu:content:home",
          "首页",
          contentGroupId,
          0);
      homeGroupId = findPermissionId("menu:content:home");
      log.info("[schema] added menu:content:home");
    }
    if (findPermissionId("crud:home_banner") != null) {
      return;
    }
    jdbcTemplate.update(
        """
        INSERT INTO admin_permission (code, name, type, parent_id, path, resource, sort_order)
        VALUES (?, ?, 'menu', ?, ?, ?, ?)
        """,
        "crud:home_banner",
        "轮播图",
        homeGroupId,
        "/crud/home_banner",
        "home_banner",
        1);
    Long permId = findPermissionId("crud:home_banner");
    if (permId == null) {
      return;
    }
    List<Long> roleIds =
        jdbcTemplate.queryForList(
            "SELECT id FROM admin_role WHERE code IN ('super_admin', 'editor')", Long.class);
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
    log.info("[schema] added crud:home_banner admin menu");
  }

  private Long findPermissionId(String code) {
    List<Long> ids =
        jdbcTemplate.queryForList("SELECT id FROM admin_permission WHERE code = ?", Long.class, code);
    return ids.isEmpty() ? null : ids.get(0);
  }
}
