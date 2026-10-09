-- 项目 Excel 导入相关表结构（MySQL 5.7+，在 vc_platform 库执行）
-- 列已存在时会报错，可忽略该条继续执行下一条
USE vc_platform;

-- 标签字段扩为 TEXT（导入 Excel 常超过 512 字符）
ALTER TABLE project MODIFY COLUMN tags TEXT DEFAULT NULL COMMENT '标签，逗号分隔';

ALTER TABLE project ADD COLUMN national_economy_industry TEXT DEFAULT NULL COMMENT '国民经济产业';
ALTER TABLE project ADD COLUMN strategic_emerging_industry TEXT DEFAULT NULL COMMENT '战略新兴产业';
ALTER TABLE project ADD COLUMN high_precision_industry TEXT DEFAULT NULL COMMENT '高精尖产业';
ALTER TABLE project ADD COLUMN listing_board VARCHAR(64) DEFAULT NULL COMMENT '上市板块';
ALTER TABLE project ADD COLUMN listing_date DATE DEFAULT NULL COMMENT '上市日期';
ALTER TABLE project ADD COLUMN employee_count VARCHAR(32) DEFAULT NULL COMMENT '雇员人数';
ALTER TABLE project ADD COLUMN manager_count VARCHAR(32) DEFAULT NULL COMMENT '管理人员人数';
ALTER TABLE project ADD COLUMN import_source VARCHAR(128) DEFAULT NULL COMMENT '数据来源';

ALTER TABLE project_financing ADD COLUMN source VARCHAR(128) DEFAULT NULL COMMENT '融资来源';

ALTER TABLE project_financing_investor ADD COLUMN investor_role VARCHAR(16) DEFAULT NULL COMMENT 'lead=领投 follow=跟投';

CREATE TABLE IF NOT EXISTS project_dynamic (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  event_date DATE DEFAULT NULL,
  content TEXT NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_project_dynamic_project (project_id, sort_order),
  KEY idx_project_dynamic_date (event_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目动态';
