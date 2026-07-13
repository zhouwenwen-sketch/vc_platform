-- 项目库 V2：主表扩展 + 工商/融资/股东/团队/标签子表
-- 幂等脚本：可重复执行。dev 环境启动后端时 ProjectSchemaMigration 可能已添加部分字段。
USE vc_platform;

DELIMITER $$

DROP PROCEDURE IF EXISTS add_column_if_missing$$
CREATE PROCEDURE add_column_if_missing(
  IN p_table VARCHAR(64),
  IN p_column VARCHAR(64),
  IN p_ddl TEXT
)
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table
      AND COLUMN_NAME = p_column
  ) THEN
    SET @sql = CONCAT('ALTER TABLE ', p_table, ' ADD COLUMN ', p_column, ' ', p_ddl);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END$$

DROP PROCEDURE IF EXISTS add_index_if_missing$$
CREATE PROCEDURE add_index_if_missing(
  IN p_table VARCHAR(64),
  IN p_index VARCHAR(64),
  IN p_ddl TEXT
)
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table
      AND INDEX_NAME = p_index
  ) THEN
    SET @sql = CONCAT('ALTER TABLE ', p_table, ' ADD ', p_ddl);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END$$

DELIMITER ;

-- 1. 扩展 project 主表（逐列检查，避免 Duplicate column）
CALL add_column_if_missing('project', 'slug', 'VARCHAR(128) DEFAULT NULL COMMENT ''稳定标识'' AFTER name');
CALL add_column_if_missing('project', 'intro', 'TEXT DEFAULT NULL COMMENT ''详情介绍'' AFTER company_desc');
CALL add_column_if_missing('project', 'is_certified', 'TINYINT NOT NULL DEFAULT 0 COMMENT ''是否认证'' AFTER is_hot');
CALL add_column_if_missing('project', 'status', 'TINYINT NOT NULL DEFAULT 1 COMMENT ''0草稿 1已发布 2下架'' AFTER is_certified');
CALL add_column_if_missing('project', 'latest_round', 'VARCHAR(64) DEFAULT NULL COMMENT ''最新轮次(冗余)'' AFTER status');
CALL add_column_if_missing('project', 'latest_amount', 'VARCHAR(64) DEFAULT NULL COMMENT ''最新融资金额(冗余)'' AFTER latest_round');
CALL add_column_if_missing('project', 'latest_financing_date', 'DATE DEFAULT NULL COMMENT ''最新融资日期(冗余)'' AFTER latest_amount');

CALL add_index_if_missing('project', 'uk_project_slug', 'UNIQUE KEY uk_project_slug (slug)');

-- 2. 工商信息（企业主体）
CREATE TABLE IF NOT EXISTS project_business (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id BIGINT NOT NULL COMMENT 'project.id',
  full_name VARCHAR(255) NOT NULL COMMENT '工商全称/企业主体',
  english_name VARCHAR(255) DEFAULT NULL,
  legal_person VARCHAR(64) DEFAULT NULL,
  registered_address VARCHAR(512) DEFAULT NULL,
  establish_date DATE DEFAULT NULL,
  unified_social_credit_code VARCHAR(32) DEFAULT NULL,
  data_source VARCHAR(32) DEFAULT 'manual',
  verified_at DATETIME DEFAULT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_business_project (project_id),
  KEY idx_business_full_name (full_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目工商信息';

-- 3. 融资历史
CREATE TABLE IF NOT EXISTS project_financing (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  financing_date DATE DEFAULT NULL,
  round VARCHAR(64) NOT NULL,
  amount VARCHAR(64) DEFAULT NULL,
  amount_currency VARCHAR(16) DEFAULT 'CNY',
  is_latest TINYINT NOT NULL DEFAULT 0,
  sort_order INT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_financing_project (project_id, sort_order),
  KEY idx_financing_date (financing_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目融资历史';

CREATE TABLE IF NOT EXISTS project_financing_investor (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  financing_id BIGINT NOT NULL,
  institution_id BIGINT DEFAULT NULL,
  investor_name VARCHAR(128) NOT NULL,
  KEY idx_pfi_financing (financing_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='融资投资方';

-- 4. 股东
CREATE TABLE IF NOT EXISTS project_shareholder (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  shareholder_name VARCHAR(128) NOT NULL,
  ratio VARCHAR(32) DEFAULT NULL,
  capital VARCHAR(64) DEFAULT NULL,
  capital_date VARCHAR(32) DEFAULT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  KEY idx_shareholder_project (project_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目股东';

-- 5. 团队成员
CREATE TABLE IF NOT EXISTS project_team_member (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  member_name VARCHAR(64) NOT NULL,
  title VARCHAR(128) DEFAULT NULL,
  avatar_url VARCHAR(512) DEFAULT NULL,
  bio TEXT DEFAULT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  KEY idx_team_project (project_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目团队成员';

-- 6. 标签
CREATE TABLE IF NOT EXISTS project_tag (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  UNIQUE KEY uk_tag_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目标签';

CREATE TABLE IF NOT EXISTS project_tag_rel (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  tag_id BIGINT NOT NULL,
  UNIQUE KEY uk_project_tag (project_id, tag_id),
  KEY idx_tag_rel_tag (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目-标签关联';

-- 7. news 关联 project_id
CALL add_column_if_missing('news', 'project_id', 'BIGINT DEFAULT NULL COMMENT ''关联 project.id'' AFTER project_name');
CALL add_index_if_missing('news', 'idx_news_project_id', 'KEY idx_news_project_id (project_id)');

UPDATE news n
INNER JOIN project p ON n.project_name = p.name
SET n.project_id = p.id
WHERE n.project_id IS NULL AND n.project_name IS NOT NULL AND n.project_name != '';

-- 清理临时存储过程
DROP PROCEDURE IF EXISTS add_column_if_missing;
DROP PROCEDURE IF EXISTS add_index_if_missing;
