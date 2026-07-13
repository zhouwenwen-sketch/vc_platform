package com.lianbei.vc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** 开发环境：自动创建短信验证码表 */
@Component
@Profile("dev")
public class SmsSchemaMigration implements org.springframework.beans.factory.InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(SmsSchemaMigration.class);

  private final JdbcTemplate jdbcTemplate;

  public SmsSchemaMigration(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void afterPropertiesSet() {
    try {
      ensureSchema();
    } catch (Exception ex) {
      log.warn("[schema] sms migration skipped: {}", ex.getMessage());
    }
  }

  private void ensureSchema() {
    jdbcTemplate.execute(
        """
        CREATE TABLE IF NOT EXISTS sms_code_record (
          id            BIGINT PRIMARY KEY AUTO_INCREMENT,
          phone         VARCHAR(20)  NOT NULL,
          code          VARCHAR(10)  NOT NULL,
          scene         VARCHAR(32)  NOT NULL DEFAULT 'login',
          status        VARCHAR(16)  NOT NULL DEFAULT 'unused',
          fail_count    TINYINT      NOT NULL DEFAULT 0,
          expire_time   DATETIME     NOT NULL,
          used_time     DATETIME     DEFAULT NULL,
          send_ip       VARCHAR(64)  DEFAULT NULL,
          create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
          KEY idx_sms_phone_status (phone, status),
          KEY idx_sms_expire_time (expire_time),
          KEY idx_sms_phone_create (phone, create_time)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        """);
    log.info("[schema] sms_code_record ensured");
  }
}
