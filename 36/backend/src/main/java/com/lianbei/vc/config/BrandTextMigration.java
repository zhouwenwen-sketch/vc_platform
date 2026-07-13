package com.lianbei.vc.config;

import com.lianbei.vc.common.BrandTextSanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 启动时将数据库中残留的旧品牌文案替换为联贝科创。
 * 仅更新仍包含 36氪 / 36kr / 联贝 / 硬氪 等关键词的行。
 */
@Component
public class BrandTextMigration implements org.springframework.beans.factory.InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(BrandTextMigration.class);

  private static final String[][] TABLE_COLUMNS = {
    {"news", "attribution"},
    {"news", "content"},
    {"news", "summary"},
    {"news", "title"},
    {"news", "source"},
    {"research_report", "title"},
    {"research_report", "summary"},
    {"research_report", "content"},
    {"research_report", "publish_by"},
    {"project", "company_desc"},
    {"project", "name"},
    {"activity", "title"},
    {"activity", "description"},
    {"activity", "location"},
    {"institution", "name"},
    {"institution", "entity_name"},
    {"ma_deal", "brand_name"},
  };

  private static final String[] LIKE_PATTERNS = {"%36%", "%联贝%", "%硬氪%"};

  private final JdbcTemplate jdbcTemplate;

  public BrandTextMigration(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void afterPropertiesSet() {
    try {
      migrate();
    } catch (Exception ex) {
      log.warn("[schema] brand text migration skipped: {}", ex.getMessage());
    }
  }

  private void migrate() {
    int total = 0;
    for (String[] tableColumn : TABLE_COLUMNS) {
      total += migrateColumn(tableColumn[0], tableColumn[1]);
    }
    if (total > 0) {
      log.info("[schema] brand text migration updated {} row(s)", total);
    }
  }

  private int migrateColumn(String table, String column) {
    if (!tableExists(table) || !columnExists(table, column)) {
      return 0;
    }
    StringBuilder where = new StringBuilder();
    for (int i = 0; i < LIKE_PATTERNS.length; i++) {
      if (i > 0) {
        where.append(" OR ");
      }
      where.append(column).append(" LIKE ?");
    }
    String sql =
        "SELECT id, "
            + column
            + " FROM "
            + table
            + " WHERE "
            + where;
    var rows = jdbcTemplate.queryForList(sql, (Object[]) LIKE_PATTERNS);
    int updated = 0;
    for (var row : rows) {
      Object id = row.get("id");
      String original = (String) row.get(column);
      String sanitized = BrandTextSanitizer.sanitize(original);
      if (sanitized.equals(original)) {
        continue;
      }
      jdbcTemplate.update(
          "UPDATE " + table + " SET " + column + " = ? WHERE id = ?", sanitized, id);
      updated++;
    }
    return updated;
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
