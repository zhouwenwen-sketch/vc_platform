package com.lianbei.vc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** 开发环境：活动发布申请表 */
@Component
@Profile("dev")
public class ActivityPublishSchemaMigration implements org.springframework.beans.factory.InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(ActivityPublishSchemaMigration.class);

  private final JdbcTemplate jdbcTemplate;

  public ActivityPublishSchemaMigration(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void afterPropertiesSet() {
    try {
      ensureSchema();
    } catch (Exception ex) {
      log.warn("[schema] activity publish migration skipped: {}", ex.getMessage());
    }
  }

  private void ensureSchema() {
    jdbcTemplate.execute(
        """
        CREATE TABLE IF NOT EXISTS activity_publish_request (
          id              BIGINT PRIMARY KEY AUTO_INCREMENT,
          user_id         BIGINT       NOT NULL,
          title           VARCHAR(512) NOT NULL,
          location        VARCHAR(255) NOT NULL,
          start_time      DATETIME     NOT NULL,
          end_time        DATETIME     NOT NULL,
          organizer_name  VARCHAR(255) NOT NULL,
          price_text      VARCHAR(64)  DEFAULT '免费',
          description     TEXT         NOT NULL,
          contact_name    VARCHAR(64)  NOT NULL,
          contact_phone   VARCHAR(20)  NOT NULL,
          status          VARCHAR(32)  NOT NULL DEFAULT 'pending',
          create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
          update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
          KEY idx_activity_publish_user (user_id),
          KEY idx_activity_publish_status (status)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动发布申请'
        """);
    log.info("[schema] activity_publish_request ensured");
  }
}
