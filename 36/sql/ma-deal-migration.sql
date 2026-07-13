-- 融资并购表结构补全（线上已有旧表时执行）
-- 用法：mysql -h HOST -u USER -p vc_platform < sql/ma-deal-migration.sql

CREATE TABLE IF NOT EXISTS ma_deal (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  project_no VARCHAR(32) NOT NULL COMMENT '项目编号',
  brand_name VARCHAR(64) DEFAULT '大学生创投并购' COMMENT '品牌/发布方名称',
  title VARCHAR(256) NOT NULL COMMENT '主标题',
  summary VARCHAR(1024) DEFAULT NULL COMMENT '列表摘要',
  category VARCHAR(32) NOT NULL DEFAULT 'listed_company' COMMENT '分类',
  tags VARCHAR(256) DEFAULT NULL COMMENT '标签，逗号分隔',
  deal_amount_text VARCHAR(64) DEFAULT NULL COMMENT '交易规模展示文案',
  logo_url VARCHAR(512) DEFAULT NULL COMMENT 'Logo',
  cover_url VARCHAR(512) DEFAULT NULL COMMENT '封面图',
  industry VARCHAR(64) DEFAULT NULL COMMENT '所属行业',
  project_name VARCHAR(128) DEFAULT NULL COMMENT '项目名称',
  main_business VARCHAR(128) DEFAULT NULL COMMENT '主营业务',
  controlling_stake VARCHAR(32) DEFAULT NULL COMMENT '实控股权',
  market_value VARCHAR(64) DEFAULT NULL COMMENT '目前市值',
  revenue_data VARCHAR(256) DEFAULT NULL COMMENT '营业收入',
  net_profit_data VARCHAR(256) DEFAULT NULL COMMENT '年净利润',
  debt_ratio VARCHAR(32) DEFAULT NULL COMMENT '负债率',
  total_assets VARCHAR(64) DEFAULT NULL COMMENT '总资产',
  net_assets VARCHAR(64) DEFAULT NULL COMMENT '净资产',
  book_funds VARCHAR(64) DEFAULT NULL COMMENT '账面资金',
  cooperation_intent TEXT COMMENT '合作意向',
  contact_phone VARCHAR(32) DEFAULT NULL COMMENT '联系电话',
  view_count INT NOT NULL DEFAULT 0 COMMENT '预览量',
  appointment_count INT NOT NULL DEFAULT 0 COMMENT '预约量',
  favorite_count INT NOT NULL DEFAULT 0 COMMENT '收藏量',
  share_count INT NOT NULL DEFAULT 0 COMMENT '转发量',
  publish_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '0草稿 1已发布',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_ma_deal_project_no (project_no),
  KEY idx_ma_deal_category (category),
  KEY idx_ma_deal_publish_time (publish_time),
  KEY idx_ma_deal_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='融资并购项目';

-- 以下存储过程逐列补全（MySQL 5.7 兼容）
DELIMITER //
DROP PROCEDURE IF EXISTS add_ma_deal_column//
CREATE PROCEDURE add_ma_deal_column(IN col_name VARCHAR(64), IN col_ddl TEXT)
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ma_deal' AND COLUMN_NAME = col_name
  ) THEN
    SET @sql = CONCAT('ALTER TABLE ma_deal ADD COLUMN ', col_name, ' ', col_ddl);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//
DELIMITER ;

CALL add_ma_deal_column('brand_name', "VARCHAR(64) DEFAULT '大学生创投并购' COMMENT '品牌/发布方名称'");
CALL add_ma_deal_column('title', "VARCHAR(256) NOT NULL DEFAULT '' COMMENT '主标题'");
CALL add_ma_deal_column('summary', "VARCHAR(1024) DEFAULT NULL COMMENT '列表摘要'");
CALL add_ma_deal_column('category', "VARCHAR(32) NOT NULL DEFAULT 'listed_company' COMMENT '分类'");
CALL add_ma_deal_column('tags', "VARCHAR(256) DEFAULT NULL COMMENT '标签'");
CALL add_ma_deal_column('deal_amount_text', "VARCHAR(64) DEFAULT NULL COMMENT '交易规模'");
CALL add_ma_deal_column('logo_url', "VARCHAR(512) DEFAULT NULL COMMENT 'Logo'");
CALL add_ma_deal_column('cover_url', "VARCHAR(512) DEFAULT NULL COMMENT '封面图'");
CALL add_ma_deal_column('industry', "VARCHAR(64) DEFAULT NULL COMMENT '所属行业'");
CALL add_ma_deal_column('project_name', "VARCHAR(128) DEFAULT NULL COMMENT '项目名称'");
CALL add_ma_deal_column('main_business', "VARCHAR(128) DEFAULT NULL COMMENT '主营业务'");
CALL add_ma_deal_column('controlling_stake', "VARCHAR(32) DEFAULT NULL COMMENT '实控股权'");
CALL add_ma_deal_column('market_value', "VARCHAR(64) DEFAULT NULL COMMENT '目前市值'");
CALL add_ma_deal_column('revenue_data', "VARCHAR(256) DEFAULT NULL COMMENT '营业收入'");
CALL add_ma_deal_column('net_profit_data', "VARCHAR(256) DEFAULT NULL COMMENT '年净利润'");
CALL add_ma_deal_column('debt_ratio', "VARCHAR(32) DEFAULT NULL COMMENT '负债率'");
CALL add_ma_deal_column('total_assets', "VARCHAR(64) DEFAULT NULL COMMENT '总资产'");
CALL add_ma_deal_column('net_assets', "VARCHAR(64) DEFAULT NULL COMMENT '净资产'");
CALL add_ma_deal_column('book_funds', "VARCHAR(64) DEFAULT NULL COMMENT '账面资金'");
CALL add_ma_deal_column('cooperation_intent', "TEXT COMMENT '合作意向'");
CALL add_ma_deal_column('contact_phone', "VARCHAR(32) DEFAULT NULL COMMENT '联系电话'");
CALL add_ma_deal_column('view_count', "INT NOT NULL DEFAULT 0 COMMENT '预览量'");
CALL add_ma_deal_column('appointment_count', "INT NOT NULL DEFAULT 0 COMMENT '预约量'");
CALL add_ma_deal_column('favorite_count', "INT NOT NULL DEFAULT 0 COMMENT '收藏量'");
CALL add_ma_deal_column('share_count', "INT NOT NULL DEFAULT 0 COMMENT '转发量'");
CALL add_ma_deal_column('publish_time', "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间'");
CALL add_ma_deal_column('sort_order', "INT NOT NULL DEFAULT 0 COMMENT '排序'");
CALL add_ma_deal_column('status', "TINYINT NOT NULL DEFAULT 1 COMMENT '0草稿 1已发布'");
CALL add_ma_deal_column('create_time', "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
CALL add_ma_deal_column('update_time', "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");

DROP PROCEDURE IF EXISTS add_ma_deal_column;
