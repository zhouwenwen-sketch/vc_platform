package com.lianbei.vc.config;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** 开发环境：融资并购表结构补全及种子数据 */
@Component
@Profile("dev")
public class MaDealSeedMigration implements org.springframework.beans.factory.InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(MaDealSeedMigration.class);

  private final JdbcTemplate jdbcTemplate;

  public MaDealSeedMigration(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void afterPropertiesSet() {
    try {
      ensureTable();
      ensureColumns();
      if (needsSeed()) {
        seedDeals();
        log.info("[schema] ma_deal seed imported");
      } else {
        log.info("[schema] ma_deal ready");
      }
    } catch (Exception ex) {
      log.warn("[schema] ma_deal migration skipped: {}", ex.getMessage());
    }
  }

  private void ensureTable() {
    jdbcTemplate.execute(
        """
        CREATE TABLE IF NOT EXISTS ma_deal (
          id BIGINT PRIMARY KEY AUTO_INCREMENT,
          project_no VARCHAR(32) NOT NULL,
          brand_name VARCHAR(64) DEFAULT '大学生创投并购',
          title VARCHAR(256) NOT NULL,
          summary VARCHAR(1024) DEFAULT NULL,
          category VARCHAR(32) NOT NULL DEFAULT 'listed_company',
          tags VARCHAR(256) DEFAULT NULL,
          deal_amount_text VARCHAR(64) DEFAULT NULL,
          logo_url VARCHAR(512) DEFAULT NULL,
          cover_url VARCHAR(512) DEFAULT NULL,
          industry VARCHAR(64) DEFAULT NULL,
          project_name VARCHAR(128) DEFAULT NULL,
          main_business VARCHAR(128) DEFAULT NULL,
          controlling_stake VARCHAR(32) DEFAULT NULL,
          market_value VARCHAR(64) DEFAULT NULL,
          revenue_data VARCHAR(256) DEFAULT NULL,
          net_profit_data VARCHAR(256) DEFAULT NULL,
          debt_ratio VARCHAR(32) DEFAULT NULL,
          total_assets VARCHAR(64) DEFAULT NULL,
          net_assets VARCHAR(64) DEFAULT NULL,
          book_funds VARCHAR(64) DEFAULT NULL,
          cooperation_intent TEXT,
          contact_phone VARCHAR(32) DEFAULT NULL,
          view_count INT NOT NULL DEFAULT 0,
          appointment_count INT NOT NULL DEFAULT 0,
          favorite_count INT NOT NULL DEFAULT 0,
          share_count INT NOT NULL DEFAULT 0,
          publish_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
          sort_order INT NOT NULL DEFAULT 0,
          status TINYINT NOT NULL DEFAULT 1,
          create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
          update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
          UNIQUE KEY uk_ma_deal_project_no (project_no),
          KEY idx_ma_deal_category (category),
          KEY idx_ma_deal_publish_time (publish_time)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
        """);
  }

  /** 线上已有旧表时逐列补全，避免 Unknown column 报错 */
  private void ensureColumns() {
    if (!tableExists("ma_deal")) {
      return;
    }
    addColumnIfMissing("brand_name", "VARCHAR(64) DEFAULT '大学生创投并购'");
    addColumnIfMissing("title", "VARCHAR(256) NOT NULL DEFAULT ''");
    addColumnIfMissing("summary", "VARCHAR(1024) DEFAULT NULL");
    addColumnIfMissing("category", "VARCHAR(32) NOT NULL DEFAULT 'listed_company'");
    addColumnIfMissing("tags", "VARCHAR(256) DEFAULT NULL");
    addColumnIfMissing("deal_amount_text", "VARCHAR(64) DEFAULT NULL");
    addColumnIfMissing("logo_url", "VARCHAR(512) DEFAULT NULL");
    addColumnIfMissing("cover_url", "VARCHAR(512) DEFAULT NULL");
    addColumnIfMissing("industry", "VARCHAR(64) DEFAULT NULL");
    addColumnIfMissing("project_name", "VARCHAR(128) DEFAULT NULL");
    addColumnIfMissing("main_business", "VARCHAR(128) DEFAULT NULL");
    addColumnIfMissing("controlling_stake", "VARCHAR(32) DEFAULT NULL");
    addColumnIfMissing("market_value", "VARCHAR(64) DEFAULT NULL");
    addColumnIfMissing("revenue_data", "VARCHAR(256) DEFAULT NULL");
    addColumnIfMissing("net_profit_data", "VARCHAR(256) DEFAULT NULL");
    addColumnIfMissing("debt_ratio", "VARCHAR(32) DEFAULT NULL");
    addColumnIfMissing("total_assets", "VARCHAR(64) DEFAULT NULL");
    addColumnIfMissing("net_assets", "VARCHAR(64) DEFAULT NULL");
    addColumnIfMissing("book_funds", "VARCHAR(64) DEFAULT NULL");
    addColumnIfMissing("cooperation_intent", "TEXT");
    addColumnIfMissing("contact_phone", "VARCHAR(32) DEFAULT NULL");
    addColumnIfMissing("view_count", "INT NOT NULL DEFAULT 0");
    addColumnIfMissing("appointment_count", "INT NOT NULL DEFAULT 0");
    addColumnIfMissing("favorite_count", "INT NOT NULL DEFAULT 0");
    addColumnIfMissing("share_count", "INT NOT NULL DEFAULT 0");
    addColumnIfMissing("publish_time", "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
    addColumnIfMissing("sort_order", "INT NOT NULL DEFAULT 0");
    addColumnIfMissing("status", "TINYINT NOT NULL DEFAULT 1");
    addColumnIfMissing("create_time", "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
    addColumnIfMissing(
        "update_time",
        "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
  }

  private void addColumnIfMissing(String column, String ddl) {
    if (!columnExists("ma_deal", column)) {
      jdbcTemplate.execute("ALTER TABLE ma_deal ADD COLUMN " + column + " " + ddl);
      log.info("[schema] added ma_deal.{}", column);
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

  private boolean needsSeed() {
    Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ma_deal", Long.class);
    return count == null || count == 0;
  }

  private void seedDeals() {
    LocalDateTime now = LocalDateTime.now();
    insertDeal(
        "MC82606063",
        "蓝创并购",
        "上市公司项目介绍",
        "【并购资产|沪主板|医药生物】600亿上市公司并购资产化学药项目，要求营收1亿元以上，不能亏损，要能出让控股权。",
        "listed_company",
        "并购资产,沪主板,生物医药",
        "600亿",
        "医药生物",
        "S**Y",
        "消费品制造",
        "24%",
        "600亿元",
        "2024: 2753亿元; 2025: 2836亿元",
        "2024: 45亿元; 2025: 57亿元",
        "67%",
        "2331亿元",
        "758亿元",
        "430亿元",
        "并购化学药项目。要求营收1亿元以上，不能亏损。在研产品后劲足，企业有研发能力，在研管线有比较清晰的上市时间表。要能出让控股权，并且接受3年业绩对赌。",
        "400-000-0000",
        115,
        1,
        0,
        4,
        now.minusDays(10),
        100);
    insertDeal(
        "MC82606064",
        "蓝创并购",
        "上市公司项目介绍",
        "【并购资产|沪主板|医药生物】61亿上市公司并购资产化学药项目，要求营收1亿元以上，不能亏损，要能出让控股权。",
        "listed_company",
        "并购资产,沪主板,生物医药",
        "61亿",
        "医药生物",
        "H**K",
        "化学制药",
        "31%",
        "61亿元",
        "2024: 18亿元; 2025: 21亿元",
        "2024: 2.1亿元; 2025: 2.8亿元",
        "42%",
        "88亿元",
        "51亿元",
        "12亿元",
        "寻求化学药优质标的，要求产品管线成熟、具备持续盈利能力，可接受业绩对赌安排。",
        "400-000-0001",
        86,
        0,
        0,
        2,
        now.minusDays(14),
        90);
    insertDeal(
        "MC82606065",
        "大学生创投并购",
        "基金产投项目推介",
        "【基金产投|先进制造】产业基金寻求高端装备领域控股权投资机会。",
        "fund_invest",
        "基金产投,先进制造",
        "20亿",
        "先进制造",
        "某装备集团",
        "高端装备制造",
        "51%",
        "—",
        "2024: 12亿元",
        "2024: 1.2亿元",
        "35%",
        "—",
        "—",
        "—",
        "产业基金拟收购高端装备领域优质企业控股权，要求具备稳定订单与核心技术壁垒。",
        null,
        42,
        0,
        0,
        1,
        now.minusDays(5),
        80);
    insertDeal(
        "MC82606066",
        "大学生创投并购",
        "项目资产转让",
        "【项目资产|企业服务】SaaS 企业控股权转让，年营收超5000万。",
        "project_asset",
        "项目资产,企业服务",
        "3亿",
        "企业服务",
        "某SaaS公司",
        "企业级SaaS",
        "60%",
        "—",
        "2024: 5200万元",
        "2024: 680万元",
        "28%",
        "—",
        "—",
        "—",
        "寻求战略投资方受让控股权，公司客户结构优质，现金流稳定。",
        null,
        33,
        0,
        0,
        0,
        now.minusDays(3),
        70);
  }

  private void insertDeal(
      String projectNo,
      String brandName,
      String title,
      String summary,
      String category,
      String tags,
      String dealAmountText,
      String industry,
      String projectName,
      String mainBusiness,
      String controllingStake,
      String marketValue,
      String revenueData,
      String netProfitData,
      String debtRatio,
      String totalAssets,
      String netAssets,
      String bookFunds,
      String cooperationIntent,
      String contactPhone,
      int viewCount,
      int appointmentCount,
      int favoriteCount,
      int shareCount,
      LocalDateTime publishTime,
      int sortOrder) {
    jdbcTemplate.update(
        """
        INSERT INTO ma_deal (
          project_no, brand_name, title, summary, category, tags, deal_amount_text,
          industry, project_name, main_business, controlling_stake, market_value,
          revenue_data, net_profit_data, debt_ratio, total_assets, net_assets, book_funds,
          cooperation_intent, contact_phone, view_count, appointment_count, favorite_count,
          share_count, publish_time, sort_order, status
        ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,1)
        """,
        projectNo,
        brandName,
        title,
        summary,
        category,
        tags,
        dealAmountText,
        industry,
        projectName,
        mainBusiness,
        controllingStake,
        marketValue,
        revenueData,
        netProfitData,
        debtRatio,
        totalAssets,
        netAssets,
        bookFunds,
        cooperationIntent,
        contactPhone,
        viewCount,
        appointmentCount,
        favoriteCount,
        shareCount,
        publishTime,
        sortOrder);
  }
}
