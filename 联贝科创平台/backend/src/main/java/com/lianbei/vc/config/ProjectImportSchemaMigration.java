package com.lianbei.vc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Locale;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** 项目导入相关表结构（启动时自动迁移） */
@Component
public class ProjectImportSchemaMigration implements org.springframework.beans.factory.InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(ProjectImportSchemaMigration.class);

  private final JdbcTemplate jdbcTemplate;

  public ProjectImportSchemaMigration(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void afterPropertiesSet() {
    try {
      ensureProjectColumns();
      ensureFinancingColumns();
      ensureInvestorColumns();
      ensureDynamicTable();
    } catch (Exception ex) {
      log.warn("[schema] project import migration skipped: {}", ex.getMessage());
    }
  }

  private void ensureProjectColumns() {
    widenProjectTagsColumn();
    addColumnIfMissing("project", "national_economy_industry", "TEXT DEFAULT NULL");
    addColumnIfMissing("project", "strategic_emerging_industry", "TEXT DEFAULT NULL");
    addColumnIfMissing("project", "high_precision_industry", "TEXT DEFAULT NULL");
    addColumnIfMissing("project", "listing_board", "VARCHAR(64) DEFAULT NULL");
    addColumnIfMissing("project", "listing_date", "DATE DEFAULT NULL");
    addColumnIfMissing("project", "employee_count", "VARCHAR(32) DEFAULT NULL");
    addColumnIfMissing("project", "manager_count", "VARCHAR(32) DEFAULT NULL");
    addColumnIfMissing("project", "import_source", "VARCHAR(128) DEFAULT NULL");
    addColumnIfMissing("project", "website", "VARCHAR(512) DEFAULT NULL COMMENT '官网'");
  }

  private void ensureFinancingColumns() {
    addColumnIfMissing("project_financing", "source", "VARCHAR(128) DEFAULT NULL");
  }

  private void ensureInvestorColumns() {
    addColumnIfMissing("project_financing_investor", "investor_role", "VARCHAR(16) DEFAULT NULL");
  }

  private void ensureDynamicTable() {
    jdbcTemplate.execute(
        """
        CREATE TABLE IF NOT EXISTS project_dynamic (
          id BIGINT PRIMARY KEY AUTO_INCREMENT,
          project_id BIGINT NOT NULL,
          event_date DATE DEFAULT NULL,
          content TEXT NOT NULL,
          sort_order INT NOT NULL DEFAULT 0,
          create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
          KEY idx_project_dynamic_project (project_id, sort_order),
          KEY idx_project_dynamic_date (event_date)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目动态'
        """);
    log.info("[schema] project_dynamic ensured");
  }

  private void addColumnIfMissing(String table, String column, String definition) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*) FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?
            """,
            Integer.class,
            table,
            column);
    if (count != null && count > 0) {
      return;
    }
    jdbcTemplate.execute(
        "ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
    log.info("[schema] added column {}.{}", table, column);
  }

  /** Excel 导入标签可能远超 512 字符 */
  private void widenProjectTagsColumn() {
    if (!tableExists("project")) {
      return;
    }
    String dataType =
        jdbcTemplate.queryForObject(
            """
            SELECT DATA_TYPE FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'project' AND COLUMN_NAME = 'tags'
            """,
            String.class);
    if (dataType == null) {
      return;
    }
    String normalized = dataType.toLowerCase(Locale.ROOT);
    if ("text".equals(normalized) || "mediumtext".equals(normalized) || "longtext".equals(normalized)) {
      return;
    }
    jdbcTemplate.execute(
        "ALTER TABLE project MODIFY COLUMN tags TEXT DEFAULT NULL COMMENT '标签，逗号分隔'");
    log.info("[schema] widened project.tags from {} to TEXT", dataType);
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
