package com.lianbei.vc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 融资快报：清除初始 seed，将各项目「行业资讯」(project_dynamic) 逐条写入 news，类型为快讯并关联项目。
 */
@Component
public class NewsFromProjectDynamicMigration
    implements org.springframework.beans.factory.InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(NewsFromProjectDynamicMigration.class);

  private static final String MIGRATION_KEY = "news_from_project_dynamic_v1";

  private final JdbcTemplate jdbcTemplate;

  public NewsFromProjectDynamicMigration(JdbcTemplate jdbcTemplate) {
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
        log.warn("[schema] news from project_dynamic skipped: required tables missing");
        return;
      }
      if (syncNewsFromDynamics()) {
        markDone();
      }
    } catch (Exception ex) {
      log.warn("[schema] news from project_dynamic migration failed: {}", ex.getMessage());
    }
  }

  private boolean syncNewsFromDynamics() {
    Long dynamicCount =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM project_dynamic WHERE content IS NOT NULL AND TRIM(content) != ''",
            Long.class);
    if (dynamicCount == null || dynamicCount == 0) {
      log.info("[schema] news from project_dynamic skipped: no dynamic rows yet");
      return false;
    }

    clearNewsDependencies();
    int deleted = jdbcTemplate.update("DELETE FROM news");
    log.info("[schema] cleared {} news row(s)", deleted);

    int inserted =
        jdbcTemplate.update(
            """
            INSERT INTO news (
              title, summary, source, tag, region, news_type,
              project_name, project_id, content, content_format, create_time
            )
            SELECT
              LEFT(TRIM(pd.content), 512),
              CASE
                WHEN CHAR_LENGTH(TRIM(pd.content)) > 512 THEN LEFT(TRIM(pd.content), 1024)
                ELSE NULL
              END,
              '联贝科创',
              NULLIF(TRIM(COALESCE(NULLIF(p.latest_round, ''), p.round)), ''),
              NULLIF(TRIM(COALESCE(NULLIF(p.location, ''), p.region)), ''),
              '快讯',
              p.name,
              p.id,
              TRIM(pd.content),
              'text',
              COALESCE(
                TIMESTAMP(pd.event_date, '12:00:00'),
                pd.create_time,
                NOW()
              )
            FROM project_dynamic pd
            INNER JOIN project p ON p.id = pd.project_id
            WHERE pd.content IS NOT NULL AND TRIM(pd.content) != ''
            ORDER BY COALESCE(pd.event_date, DATE(pd.create_time)) DESC, pd.sort_order ASC, pd.id ASC
            """);

    log.info(
        "[schema] imported {} news flash(es) from {} project_dynamic row(s); project_dynamic kept for project detail",
        inserted,
        dynamicCount);
    return true;
  }

  private void clearNewsDependencies() {
    if (tableExists("news_comment")) {
      jdbcTemplate.update("DELETE FROM news_comment");
    }
    if (tableExists("coverage_apply_record") && columnExists("coverage_apply_record", "news_id")) {
      jdbcTemplate.update("UPDATE coverage_apply_record SET news_id = NULL WHERE news_id IS NOT NULL");
    }
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
    log.info("[schema] news_from_project_dynamic migration completed");
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

  private boolean columnExists(String table, String column) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*) FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?
            """,
            Integer.class,
            table,
            column);
    return count != null && count > 0;
  }
}
