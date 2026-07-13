package com.lianbei.vc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 开发环境启动时自动补全 news 表字段（等价于 sql/news-article-migration.sql）。
 * 在 Web 容器启动前执行，避免接口先于迁移被访问。
 */
@Component
@Profile("dev")
public class NewsArticleSchemaMigration implements org.springframework.beans.factory.InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(NewsArticleSchemaMigration.class);

  private final JdbcTemplate jdbcTemplate;

  public NewsArticleSchemaMigration(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void afterPropertiesSet() {
    try {
      ensureColumns();
      seedArticleSamples();
    } catch (Exception ex) {
      log.warn("[schema] news article migration skipped: {}", ex.getMessage());
    }
  }

  private void ensureColumns() {
    if (!tableExists("news")) {
      return;
    }
    if (!columnExists("news", "content")) {
      jdbcTemplate.execute(
          "ALTER TABLE news ADD COLUMN content TEXT COMMENT '正文' AFTER project_name");
      log.info("[schema] added news.content");
    }
    if (!columnExists("news", "source_url")) {
      jdbcTemplate.execute(
          "ALTER TABLE news ADD COLUMN source_url VARCHAR(512) DEFAULT NULL COMMENT '原文链接' AFTER content");
      log.info("[schema] added news.source_url");
    }
    if (!columnExists("news", "cover_url")) {
      jdbcTemplate.execute(
          "ALTER TABLE news ADD COLUMN cover_url VARCHAR(512) DEFAULT NULL COMMENT '封面/头图' AFTER source_url");
      log.info("[schema] added news.cover_url");
    }
    if (!columnExists("news", "source_avatar")) {
      jdbcTemplate.execute(
          "ALTER TABLE news ADD COLUMN source_avatar VARCHAR(512) DEFAULT NULL COMMENT '来源头像' AFTER cover_url");
      log.info("[schema] added news.source_avatar");
    }
    if (!columnExists("news", "content_format")) {
      jdbcTemplate.execute(
          "ALTER TABLE news ADD COLUMN content_format VARCHAR(16) DEFAULT 'text' COMMENT 'text | html' AFTER source_avatar");
      log.info("[schema] added news.content_format");
    }
    if (!columnExists("news", "attribution")) {
      jdbcTemplate.execute(
          "ALTER TABLE news ADD COLUMN attribution TEXT DEFAULT NULL COMMENT '版权声明' AFTER content_format");
      log.info("[schema] added news.attribution");
    }
  }

  private void seedArticleSamples() {
    if (!columnExists("news", "cover_url")) {
      return;
    }
    String attr =
        "本文来自大学生创投，大学生创投经授权发布。"
            + "\n该文观点仅代表作者本人，大学生创投平台仅提供信息存储空间服务。";
    jdbcTemplate.update(
        """
        UPDATE news SET
          cover_url = 'https://picsum.photos/750/420?random=article1',
          content_format = 'text',
          attribution = ?
        WHERE news_type = '文章' AND title LIKE '深圳具身公司星尘智能%' AND cover_url IS NULL
        """,
        attr.replace("大学生创投，", "大学生创投，"));
    jdbcTemplate.update(
        """
        UPDATE news SET
          cover_url = 'https://picsum.photos/750/420?random=article2',
          content_format = 'text',
          attribution = ?
        WHERE news_type = '文章' AND title LIKE '人工智能新材料%' AND cover_url IS NULL
        """,
        attr.replace("大学生创投，", "大学生创投，"));
    jdbcTemplate.update(
        """
        UPDATE news SET
          cover_url = 'https://picsum.photos/750/420?random=article3',
          content_format = 'text',
          attribution = ?
        WHERE news_type = '文章' AND title = '君合盟完成C轮融资' AND cover_url IS NULL
        """,
        attr);
    jdbcTemplate.update(
        """
        UPDATE news SET
          cover_url = 'https://picsum.photos/750/420?random=article4',
          content_format = 'text',
          attribution = ?
        WHERE news_type = '文章' AND title LIKE 'MiniMax%' AND cover_url IS NULL
        """,
        attr.replace("大学生创投，", "大学生创投派，"));
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
