package com.lianbei.vc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 开发环境：项目入驻相关表结构（申请记录、用户-项目关系，供后期审核后台使用）。
 */
@Component
@Profile("dev")
public class OnboardSchemaMigration implements org.springframework.beans.factory.InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(OnboardSchemaMigration.class);

  private final JdbcTemplate jdbcTemplate;

  public OnboardSchemaMigration(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void afterPropertiesSet() {
    try {
      ensureSchema();
    } catch (Exception ex) {
      log.warn("[schema] onboard migration skipped: {}", ex.getMessage());
    }
  }

  private void ensureSchema() {
    createTableIfMissing(
        """
        CREATE TABLE project_onboard_record (
          id BIGINT PRIMARY KEY AUTO_INCREMENT,
          user_id BIGINT NOT NULL,
          project_name VARCHAR(128) NOT NULL,
          status VARCHAR(32) NOT NULL DEFAULT 'pending',
          apply_data JSON NOT NULL,
          project_id BIGINT DEFAULT NULL,
          audit_remark VARCHAR(512) DEFAULT NULL,
          auditor_id BIGINT DEFAULT NULL,
          create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
          update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
          KEY idx_onboard_user (user_id),
          KEY idx_onboard_status (status),
          KEY idx_onboard_project_name (project_name)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        """);

    createTableIfMissing(
        """
        CREATE TABLE user_project (
          id BIGINT PRIMARY KEY AUTO_INCREMENT,
          user_id BIGINT NOT NULL,
          project_id BIGINT NOT NULL,
          role VARCHAR(32) NOT NULL DEFAULT 'owner',
          job_type VARCHAR(64) DEFAULT NULL,
          is_certifier TINYINT NOT NULL DEFAULT 0,
          create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
          UNIQUE KEY uk_user_project (user_id, project_id),
          KEY idx_project_user (project_id)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        """);

    log.info("[schema] onboard tables ensured");
  }

  private void createTableIfMissing(String ddl) {
    String sql = ddl.trim();
    if (!sql.toUpperCase().contains("IF NOT EXISTS")) {
      sql = sql.replaceFirst("CREATE TABLE ", "CREATE TABLE IF NOT EXISTS ");
    }
    jdbcTemplate.execute(sql);
  }
}
