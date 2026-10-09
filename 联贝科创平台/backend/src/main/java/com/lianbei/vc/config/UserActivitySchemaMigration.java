package com.lianbei.vc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** 开发环境：用户参与活动记录表 */
@Component
@Profile("dev")
public class UserActivitySchemaMigration implements org.springframework.beans.factory.InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(UserActivitySchemaMigration.class);

  private final JdbcTemplate jdbcTemplate;

  public UserActivitySchemaMigration(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void afterPropertiesSet() {
    try {
      ensureSchema();
    } catch (Exception ex) {
      log.warn("[schema] user_activity migration skipped: {}", ex.getMessage());
    }
  }

  private void ensureSchema() {
    jdbcTemplate.execute(
        """
        CREATE TABLE IF NOT EXISTS user_activity_record (
          id            BIGINT PRIMARY KEY AUTO_INCREMENT,
          user_id       BIGINT       NOT NULL,
          activity_id   BIGINT       NOT NULL,
          join_status   VARCHAR(32)  NOT NULL,
          create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
          update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
          UNIQUE KEY uk_user_activity (user_id, activity_id),
          KEY idx_user_activity_user (user_id),
          KEY idx_user_activity_status (user_id, join_status)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        """);
    log.info("[schema] user_activity_record ensured");
  }
}
