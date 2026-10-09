package com.lianbei.vc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** 开发环境：活动详情扩展字段、报名表及种子数据 */
@Component
@Profile("dev")
public class ActivityDetailSchemaMigration implements org.springframework.beans.factory.InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(ActivityDetailSchemaMigration.class);

  private final JdbcTemplate jdbcTemplate;

  public ActivityDetailSchemaMigration(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void afterPropertiesSet() {
    try {
      ensureSchema();
      seedOrganizerIfNeeded();
      seedActivityDetailIfNeeded();
      seedEndedEventActivitiesIfNeeded();
    } catch (Exception ex) {
      log.warn("[schema] activity detail migration skipped: {}", ex.getMessage());
    }
  }

  private void ensureSchema() {
    if (!tableExists("activity")) {
      return;
    }
    addColumnIfMissing(
        "activity",
        "banner_urls",
        "TEXT DEFAULT NULL COMMENT '轮播图 JSON 数组' AFTER activity_type");
    addColumnIfMissing(
        "activity",
        "detail_images",
        "TEXT DEFAULT NULL COMMENT '详情长图 JSON 数组' AFTER banner_urls");
    addColumnIfMissing(
        "activity",
        "price",
        "DECIMAL(10,2) DEFAULT 0 COMMENT '报名价格' AFTER detail_images");
    addColumnIfMissing(
        "activity",
        "price_text",
        "VARCHAR(64) DEFAULT NULL COMMENT '价格展示文案' AFTER price");
    addColumnIfMissing(
        "activity",
        "organizer_id",
        "BIGINT DEFAULT NULL COMMENT '主办方 institution.id' AFTER price_text");
    addColumnIfMissing(
        "activity",
        "organizer_name",
        "VARCHAR(255) DEFAULT NULL COMMENT '主办方展示名' AFTER organizer_id");
    addColumnIfMissing(
        "activity",
        "like_count",
        "INT NOT NULL DEFAULT 0 COMMENT '点赞数' AFTER organizer_name");
    addColumnIfMissing(
        "activity",
        "is_recommended",
        "TINYINT NOT NULL DEFAULT 0 COMMENT '首页推荐 1=是' AFTER like_count");

    jdbcTemplate.execute(
        """
        CREATE TABLE IF NOT EXISTS activity_registration (
          id            BIGINT PRIMARY KEY AUTO_INCREMENT,
          user_id       BIGINT       NOT NULL,
          activity_id   BIGINT       NOT NULL,
          name          VARCHAR(64)  NOT NULL COMMENT '姓名',
          phone         VARCHAR(20)  NOT NULL COMMENT '联系电话',
          org_name      VARCHAR(255) NOT NULL COMMENT '单位名称',
          position      VARCHAR(128) NOT NULL COMMENT '职位',
          create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
          update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
          UNIQUE KEY uk_activity_reg (user_id, activity_id),
          KEY idx_activity_reg_activity (activity_id)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动报名记录'
        """);
    log.info("[schema] activity detail schema ensured");
  }

  private void seedOrganizerIfNeeded() {
    if (!tableExists("institution")) {
      return;
    }
    Integer count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM institution WHERE entity_name LIKE '%张通社%'",
            Integer.class);
    if (count != null && count > 0) {
      return;
    }
    jdbcTemplate.update(
        """
        INSERT INTO institution (name, entity_name, inst_type, investment_field, event_count, founded_year, logo_url)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """,
        "张通社",
        "上海张通社信息科技有限公司",
        "战略融资",
        "企业服务",
        5,
        "2018年",
        "https://picsum.photos/120/120?random=901");
    log.info("[schema] seeded organizer institution");
  }

  private void seedActivityDetailIfNeeded() {
    if (!tableExists("activity") || !columnExists("activity", "detail_images")) {
      return;
    }
    Long organizerId =
        jdbcTemplate.query(
            "SELECT id FROM institution WHERE entity_name LIKE '%张通社%' LIMIT 1",
            rs -> rs.next() ? rs.getLong("id") : null);

    String title =
        "报告订购 | 《2026年具身智能行业——世界模型驱动下的投资窗口》";
    Integer exists =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM activity WHERE title = ?", Integer.class, title);
    if (exists != null && exists > 0) {
      return;
    }

    String bannerJson =
        "[\"https://picsum.photos/750/420?random=801\",\"https://picsum.photos/750/420?random=802\"]";
    String detailJson =
        "[\"https://picsum.photos/750/1400?random=811\",\"https://picsum.photos/750/900?random=812\"]";

    jdbcTemplate.update(
        """
        INSERT INTO activity
          (title, cover_url, location, start_time, end_time, status, participant_count,
           description, activity_type, banner_urls, detail_images, price, price_text,
           organizer_id, organizer_name, like_count)
        VALUES (?, ?, ?, NOW() + INTERVAL 60 DAY, NOW() + INTERVAL 60 DAY + INTERVAL 8 HOUR,
                'registering', 0, '具身智能行业投资窗口报告订购活动', 'event',
                ?, ?, 200, '200元', ?, '张通社 & 加冕研究院', 0)
        """,
        title,
        "https://picsum.photos/750/420?random=801",
        "张通社 & 加冕研究院",
        bannerJson,
        detailJson,
        organizerId);

    jdbcTemplate.update(
        """
        UPDATE activity SET status = 'registering', price_text = '200元',
          banner_urls = ?, detail_images = ?, organizer_id = ?, organizer_name = '张通社 & 加冕研究院'
        WHERE title LIKE '%WISE2025%' AND activity_type = 'event' LIMIT 1
        """,
        bannerJson,
        detailJson,
        organizerId);

    jdbcTemplate.update(
        """
        UPDATE activity SET status = 'ended'
        WHERE title LIKE '%闭门会%' OR title LIKE '%增长力大会%' OR title LIKE '%公开课%'
        """);

    log.info("[schema] seeded activity detail data");
    markRecommendedActivitiesIfNeeded();
  }

  private void markRecommendedActivitiesIfNeeded() {
    if (!columnExists("activity", "is_recommended")) {
      return;
    }
    jdbcTemplate.update(
        """
        UPDATE activity SET is_recommended = 1
        WHERE activity_type = 'event'
          AND (title LIKE '%报告订购%' OR title LIKE '%新能源储能%' OR title LIKE '%人工智能产业%')
        """);
    log.info("[schema] marked recommended event activities");
  }

  private void seedEndedEventActivitiesIfNeeded() {
    if (!tableExists("activity")) {
      return;
    }
    Integer endedCount =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM activity WHERE activity_type = 'event' AND status = 'ended'",
            Integer.class);
    if (endedCount != null && endedCount > 0) {
      return;
    }

    Long organizerId =
        jdbcTemplate.query(
            "SELECT id FROM institution WHERE entity_name LIKE '%张通社%' LIMIT 1",
            rs -> rs.next() ? rs.getLong("id") : null);

    String[][] endedEvents = {
      {"联贝科创 WISE 闭门会 · 产业投资对接", "https://picsum.photos/750/420?random=803"},
      {"2025 企业增长力大会", "https://picsum.photos/750/420?random=804"}
    };

    for (String[] event : endedEvents) {
      Integer exists =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM activity WHERE title = ?", Integer.class, event[0]);
      if (exists != null && exists > 0) {
        jdbcTemplate.update(
            "UPDATE activity SET status = 'ended', activity_type = 'event' WHERE title = ?",
            event[0]);
        continue;
      }
      jdbcTemplate.update(
          """
          INSERT INTO activity
            (title, cover_url, location, start_time, end_time, status, participant_count,
             description, activity_type, organizer_id, organizer_name)
          VALUES (?, ?, '张通社 & 加冕研究院', NOW() - INTERVAL 90 DAY,
                  NOW() - INTERVAL 89 DAY, 'ended', 186, '已结束活动', 'event', ?, '张通社 & 加冕研究院')
          """,
          event[0],
          event[1],
          organizerId);
    }
    log.info("[schema] seeded ended event activities");
  }

  private void addColumnIfMissing(String table, String column, String ddl) {
    if (!columnExists(table, column)) {
      jdbcTemplate.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + ddl);
      log.info("[schema] added {}.{}", table, column);
    }
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
