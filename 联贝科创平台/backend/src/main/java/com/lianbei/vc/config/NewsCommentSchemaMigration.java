package com.lianbei.vc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** 开发环境：资讯评论表 */
@Component
@Profile("dev")
public class NewsCommentSchemaMigration implements org.springframework.beans.factory.InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(NewsCommentSchemaMigration.class);

  private final JdbcTemplate jdbcTemplate;

  public NewsCommentSchemaMigration(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void afterPropertiesSet() {
    try {
      ensureSchema();
    } catch (Exception ex) {
      log.warn("[schema] news_comment migration skipped: {}", ex.getMessage());
    }
  }

  private void ensureSchema() {
    jdbcTemplate.execute(
        """
        CREATE TABLE IF NOT EXISTS news_comment (
          id          BIGINT PRIMARY KEY AUTO_INCREMENT,
          news_id     BIGINT       NOT NULL,
          user_id     BIGINT       NOT NULL,
          nickname    VARCHAR(64)  NOT NULL,
          avatar      VARCHAR(512) DEFAULT NULL,
          content     VARCHAR(500) NOT NULL,
          create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
          KEY idx_news_comment_news (news_id, create_time),
          KEY idx_news_comment_user (user_id)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资讯评论'
        """);
    log.info("[schema] news_comment ensured");
  }
}
