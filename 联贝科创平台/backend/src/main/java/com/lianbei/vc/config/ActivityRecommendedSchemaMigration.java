package com.lianbei.vc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** 活动推荐字段：所有环境启动时补齐 is_recommended 列 */
@Component
public class ActivityRecommendedSchemaMigration implements org.springframework.beans.factory.InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(ActivityRecommendedSchemaMigration.class);

  private final JdbcTemplate jdbcTemplate;

  public ActivityRecommendedSchemaMigration(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void afterPropertiesSet() {
    try {
      ensureColumn();
    } catch (Exception ex) {
      log.warn("[schema] activity is_recommended migration skipped: {}", ex.getMessage());
    }
  }

  private void ensureColumn() {
    if (!tableExists("activity") || columnExists("activity", "is_recommended")) {
      return;
    }
    jdbcTemplate.execute(
        """
        ALTER TABLE activity
        ADD COLUMN is_recommended TINYINT NOT NULL DEFAULT 0 COMMENT '首页推荐 1=是' AFTER like_count
        """);
    log.info("[schema] added activity.is_recommended");
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
