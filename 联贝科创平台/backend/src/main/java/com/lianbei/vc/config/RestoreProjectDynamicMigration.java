package com.lianbei.vc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 若融资快报迁移已清空 project_dynamic，则从已关联项目的快讯还原动态数据，供项目详情「行业资讯」展示。
 */
@Component
public class RestoreProjectDynamicMigration
    implements org.springframework.beans.factory.InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(RestoreProjectDynamicMigration.class);

  private static final String MIGRATION_KEY = "restore_project_dynamic_v1";

  private final JdbcTemplate jdbcTemplate;

  public RestoreProjectDynamicMigration(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void afterPropertiesSet() {
    try {
      ensureMigrationLogTable();
      if (isDone()) {
        return;
      }
      if (!tableExists("news") || !tableExists("project_dynamic") || !tableExists("project")) {
        return;
      }
      if (restoreFromNews()) {
        markDone();
      }
    } catch (Exception ex) {
      log.warn("[schema] restore project_dynamic failed: {}", ex.getMessage());
    }
  }

  private boolean restoreFromNews() {
    Long dynamicCount =
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM project_dynamic", Long.class);
    if (dynamicCount != null && dynamicCount > 0) {
      log.info("[schema] restore project_dynamic skipped: table already has data");
      return false;
    }

    Integer newsDone =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM schema_migration_log WHERE migration_key = ?",
            Integer.class,
            "news_from_project_dynamic_v1");
    if (newsDone == null || newsDone == 0) {
      return false;
    }

    int inserted =
        jdbcTemplate.update(
            """
            INSERT INTO project_dynamic (project_id, content, event_date, sort_order, create_time)
            SELECT
              n.project_id,
              TRIM(n.content),
              DATE(n.create_time),
              0,
              n.create_time
            FROM news n
            WHERE n.project_id IS NOT NULL
              AND n.news_type = '快讯'
              AND n.content IS NOT NULL
              AND TRIM(n.content) != ''
            ORDER BY n.create_time ASC, n.id ASC
            """);

    if (inserted > 0) {
      log.info("[schema] restored {} project_dynamic row(s) from linked news flash(es)", inserted);
      return true;
    }
    return false;
  }

  private void ensureMigrationLogTable() {
    jdbcTemplate.execute(
        """
        CREATE TABLE IF NOT EXISTS schema_migration_log (
          migration_key VARCHAR(128) PRIMARY KEY,
          executed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        """);
  }

  private boolean isDone() {
    Integer count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM schema_migration_log WHERE migration_key = ?",
            Integer.class,
            MIGRATION_KEY);
    return count != null && count > 0;
  }

  private void markDone() {
    jdbcTemplate.update(
        "INSERT INTO schema_migration_log (migration_key) VALUES (?) ON DUPLICATE KEY UPDATE executed_at = NOW()",
        MIGRATION_KEY);
    log.info("[schema] restore_project_dynamic migration completed");
  }

  private boolean tableExists(String table) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*) FROM information_schema.TABLES
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?
            """,
            Integer.class,
            table);
    return count != null && count > 0;
  }
}
