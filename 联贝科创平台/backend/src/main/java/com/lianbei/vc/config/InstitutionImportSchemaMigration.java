package com.lianbei.vc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** 开发环境：机构导入相关表结构 */
@Component
@Profile("dev")
public class InstitutionImportSchemaMigration implements org.springframework.beans.factory.InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(InstitutionImportSchemaMigration.class);

  private final JdbcTemplate jdbcTemplate;

  public InstitutionImportSchemaMigration(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void afterPropertiesSet() {
    try {
      ensureInstitutionColumns();
      ensureTeamMemberTable();
      ensureFundManagerTable();
    } catch (Exception ex) {
      log.warn("[schema] institution import migration skipped: {}", ex.getMessage());
    }
  }

  private void ensureInstitutionColumns() {
    addColumnIfMissing("institution", "intro", "TEXT DEFAULT NULL COMMENT '机构介绍'");
    addColumnIfMissing("institution", "manage_scale", "VARCHAR(128) DEFAULT NULL COMMENT '管理规模'");
    addColumnIfMissing("institution", "website", "VARCHAR(512) DEFAULT NULL COMMENT '官网'");
    addColumnIfMissing(
        "institution", "investment_fields", "TEXT DEFAULT NULL COMMENT '投资领域完整列表'");
  }

  private void ensureTeamMemberTable() {
    jdbcTemplate.execute(
        """
        CREATE TABLE IF NOT EXISTS institution_team_member (
          id BIGINT PRIMARY KEY AUTO_INCREMENT,
          institution_id BIGINT NOT NULL,
          member_name VARCHAR(64) NOT NULL,
          title VARCHAR(64) DEFAULT NULL,
          avatar VARCHAR(512) DEFAULT NULL,
          bio TEXT DEFAULT NULL,
          sort_order INT NOT NULL DEFAULT 0,
          create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
          update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
          UNIQUE KEY uk_inst_member (institution_id, member_name),
          KEY idx_inst_member_inst (institution_id, sort_order)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='机构团队成员'
        """);
    log.info("[schema] institution_team_member ensured");
  }

  private void ensureFundManagerTable() {
    jdbcTemplate.execute(
        """
        CREATE TABLE IF NOT EXISTS institution_fund_manager (
          id BIGINT PRIMARY KEY AUTO_INCREMENT,
          institution_id BIGINT NOT NULL,
          full_name VARCHAR(255) NOT NULL,
          legal_person VARCHAR(64) DEFAULT NULL,
          inst_type VARCHAR(64) DEFAULT NULL,
          office_address VARCHAR(512) DEFAULT NULL,
          registered_capital VARCHAR(64) DEFAULT NULL,
          paid_in_capital VARCHAR(64) DEFAULT NULL,
          paid_in_ratio VARCHAR(32) DEFAULT NULL,
          registration_no VARCHAR(64) DEFAULT NULL,
          establish_date VARCHAR(32) DEFAULT NULL,
          register_date VARCHAR(32) DEFAULT NULL,
          sort_order INT NOT NULL DEFAULT 0,
          create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
          update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
          UNIQUE KEY uk_inst_fund_manager (institution_id, full_name),
          KEY idx_inst_fund_manager_inst (institution_id, sort_order)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='机构基金管理人'
        """);
    log.info("[schema] institution_fund_manager ensured");
  }

  private void addColumnIfMissing(String table, String column, String definition) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*) FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?
            """,
            Integer.class,
            table,
            column);
    if (count != null && count > 0) {
      return;
    }
    jdbcTemplate.execute(
        "ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
    log.info("[schema] added column {}.{}", table, column);
  }
}
