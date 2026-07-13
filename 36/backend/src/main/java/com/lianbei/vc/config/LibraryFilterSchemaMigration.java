package com.lianbei.vc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** 开发环境：筛选标签表及初始数据 */
@Component
@Profile("dev")
public class LibraryFilterSchemaMigration implements org.springframework.beans.factory.InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(LibraryFilterSchemaMigration.class);

  private final JdbcTemplate jdbcTemplate;

  public LibraryFilterSchemaMigration(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void afterPropertiesSet() {
    try {
      ensureSchema();
      migrateSharedIndustry();
      seedAllGroups();
    } catch (Exception ex) {
      log.warn("[schema] library_filter_option migration skipped: {}", ex.getMessage());
    }
  }

  private void ensureSchema() {
    jdbcTemplate.execute(
        """
        CREATE TABLE IF NOT EXISTS library_filter_option (
          id          BIGINT PRIMARY KEY AUTO_INCREMENT,
          scene       VARCHAR(32)  NOT NULL,
          filter_key  VARCHAR(32)  NOT NULL,
          label       VARCHAR(128) NOT NULL,
          value       VARCHAR(128) NOT NULL,
          sort_order  INT          NOT NULL DEFAULT 0,
          enabled     TINYINT      NOT NULL DEFAULT 1,
          create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
          update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
          KEY idx_library_filter (scene, filter_key, enabled, sort_order)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='列表页筛选标签'
        """);
    log.info("[schema] library_filter_option ensured");
  }

  /** 将旧版 project_library.industry 迁移到 shared.industry */
  private void migrateSharedIndustry() {
    Long sharedCount =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM library_filter_option WHERE scene = ? AND filter_key = ?",
            Long.class,
            "shared",
            "industry");
    if (sharedCount != null && sharedCount > 0) {
      return;
    }
    Long legacyCount =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM library_filter_option WHERE scene = ? AND filter_key = ?",
            Long.class,
            "project_library",
            "industry");
    if (legacyCount != null && legacyCount > 0) {
      jdbcTemplate.update(
          """
          INSERT INTO library_filter_option (scene, filter_key, label, value, sort_order, enabled, create_time, update_time)
          SELECT 'shared', filter_key, label, value, sort_order, enabled, create_time, update_time
          FROM library_filter_option
          WHERE scene = 'project_library' AND filter_key = 'industry'
          """);
      log.info("[schema] migrated project_library.industry to shared.industry");
    }
  }

  private void seedAllGroups() {
    seedIfEmpty(
        "shared",
        "industry",
        new String[] {
          "文化娱乐", "消费电商", "汽车出行", "教育", "金融", "企业服务", "产业升级", "前沿技术",
          "医疗", "人工智能", "医疗健康", "先进制造", "新能源", "机器人"
        });
    seedIfEmpty(
        "project_library",
        "round",
        new String[] {
          "未融资", "种子轮", "天使轮", "Pre-A轮", "A轮", "A+轮", "B轮", "B++轮", "C轮", "战略融资",
          "已上市", "未披露"
        });
    seedIfEmpty(
        "project_library",
        "advantage",
        new String[] {
          "专精特新小巨人", "专精特新", "创新型中小企业", "高新技术企业", "科技型中小企业", "独角兽",
          "瞪羚企业", "雏鹰企业"
        });
    seedIfEmpty(
        "project_library",
        "foundedYear",
        new String[] {
          "2026年", "2025年", "2024年", "2023年", "2022年", "2021年", "2020年", "2019年", "2018年",
          "2017年", "2016年", "2015年", "2014年", "2013年", "2012年", "2011年", "2010年及以前"
        });
    seedIfEmpty(
        "financing_events",
        "round",
        new String[] {
          "未融资", "种子轮", "天使轮", "Pre-A轮", "Pre-A+轮", "A轮", "A+轮", "Pre-B轮", "B轮", "B+轮",
          "C轮", "C+轮", "D轮", "D+轮", "E轮", "F轮", "G轮", "H轮", "股权融资", "战略融资", "定向增发",
          "Pre-IPO", "基石轮", "已上市", "IPO", "新三板", "已退市/私有化", "并购/合并", "其他"
        });
    seedIfEmpty(
        "financing_events",
        "financingYear",
        new String[] {
          "2026年", "2025年", "2024年", "2023年", "2022年", "2021年", "2020年", "2019年", "2018年",
          "2017年", "2016年", "2015年", "2014年", "2013年", "2012年", "2011年", "2010年及以前"
        });
    seedIfEmpty(
        "financing_events",
        "currency",
        new String[] {"人民币", "美元", "港元", "欧元", "英镑", "卢比", "日元", "其他"});
    seedIfEmpty(
        "institution_library",
        "institutionType",
        new String[] {"天使", "VC", "PE", "CVC", "FA", "LP", "孵化器"});
    seedIfEmpty(
        "research_list",
        "reportType",
        new String[] {"行业研究", "专题报告", "数据洞察", "深度分析"});
    seedIfEmpty(
        "research_list",
        "featureTag",
        new String[] {"热门赛道", "产业洞察", "前沿技术", "短篇洞察", "其他"});
    log.info("[schema] library filter options seeded");
  }

  private void seedIfEmpty(String scene, String filterKey, String[] labels) {
    Long count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM library_filter_option WHERE scene = ? AND filter_key = ?",
            Long.class,
            scene,
            filterKey);
    if (count != null && count > 0) {
      return;
    }
    int sort = 1;
    for (String label : labels) {
      jdbcTemplate.update(
          """
          INSERT INTO library_filter_option (scene, filter_key, label, value, sort_order, enabled)
          VALUES (?, ?, ?, ?, ?, 1)
          """,
          scene,
          filterKey,
          label,
          label,
          sort++);
    }
  }
}
