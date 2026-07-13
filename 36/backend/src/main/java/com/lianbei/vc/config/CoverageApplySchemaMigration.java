package com.lianbei.vc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** 开发环境：寻求报道申请表 */
@Component
@Profile("dev")
public class CoverageApplySchemaMigration implements org.springframework.beans.factory.InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(CoverageApplySchemaMigration.class);

  private final JdbcTemplate jdbcTemplate;

  public CoverageApplySchemaMigration(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void afterPropertiesSet() {
    try {
      ensureSchema();
    } catch (Exception ex) {
      log.warn("[schema] coverage_apply_record migration skipped: {}", ex.getMessage());
    }
  }

  private void ensureSchema() {
    jdbcTemplate.execute(
        """
        CREATE TABLE IF NOT EXISTS coverage_apply_record (
          id            BIGINT PRIMARY KEY AUTO_INCREMENT,
          user_id       BIGINT       NOT NULL,
          project_id    BIGINT       NOT NULL,
          project_name  VARCHAR(128) NOT NULL,
          report_type   VARCHAR(32)  NOT NULL,
          status        VARCHAR(32)  NOT NULL DEFAULT 'pending',
          apply_data    JSON         NOT NULL,
          news_id       BIGINT       DEFAULT NULL,
          audit_remark  VARCHAR(512) DEFAULT NULL,
          auditor_id    BIGINT       DEFAULT NULL,
          create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
          update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
          UNIQUE KEY uk_coverage_user_project (user_id, project_id),
          KEY idx_coverage_status (status),
          KEY idx_coverage_project (project_id)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='寻求报道申请'
        """);
    log.info("[schema] coverage_apply_record ensured");
  }
}
