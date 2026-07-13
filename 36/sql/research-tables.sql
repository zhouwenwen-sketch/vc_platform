-- 研究院表结构（幂等）
USE vc_platform;

CREATE TABLE IF NOT EXISTS research_report (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  title VARCHAR(512) NOT NULL COMMENT '报告标题',
  summary VARCHAR(2048) DEFAULT NULL COMMENT '简介/摘要',
  report_type VARCHAR(64) DEFAULT NULL COMMENT '报告类型',
  industry VARCHAR(64) DEFAULT NULL COMMENT '所属行业',
  tags VARCHAR(512) DEFAULT NULL COMMENT '特色分类标签，逗号分隔',
  publish_date DATE NOT NULL COMMENT '发布日期',
  content TEXT COMMENT '报告正文',
  cover_url VARCHAR(512) DEFAULT NULL COMMENT '封面图',
  view_count INT NOT NULL DEFAULT 0 COMMENT '阅读量',
  publish_by VARCHAR(64) NOT NULL DEFAULT '联贝科创研究院' COMMENT '发布人',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '0草稿 1已发布',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_report_publish_date (publish_date),
  KEY idx_report_type (report_type),
  KEY idx_report_industry (industry)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='研究院报告';

CREATE TABLE IF NOT EXISTS research_category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  category_type VARCHAR(32) NOT NULL COMMENT '类型：report_type/industry/tag',
  name VARCHAR(64) NOT NULL COMMENT '分类名称',
  sort_order INT NOT NULL DEFAULT 0,
  KEY idx_category_type (category_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='研究院报告分类';

INSERT INTO research_category (category_type, name, sort_order)
SELECT 'report_type', '行业研究', 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_category WHERE category_type = 'report_type' AND name = '行业研究');
INSERT INTO research_category (category_type, name, sort_order)
SELECT 'report_type', '专题报告', 2 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_category WHERE category_type = 'report_type' AND name = '专题报告');
INSERT INTO research_category (category_type, name, sort_order)
SELECT 'report_type', '数据洞察', 3 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_category WHERE category_type = 'report_type' AND name = '数据洞察');
INSERT INTO research_category (category_type, name, sort_order)
SELECT 'report_type', '深度分析', 4 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_category WHERE category_type = 'report_type' AND name = '深度分析');
INSERT INTO research_category (category_type, name, sort_order)
SELECT 'industry', '人工智能', 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_category WHERE category_type = 'industry' AND name = '人工智能');
INSERT INTO research_category (category_type, name, sort_order)
SELECT 'industry', '医疗健康', 2 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_category WHERE category_type = 'industry' AND name = '医疗健康');
INSERT INTO research_category (category_type, name, sort_order)
SELECT 'industry', '先进制造', 3 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_category WHERE category_type = 'industry' AND name = '先进制造');
INSERT INTO research_category (category_type, name, sort_order)
SELECT 'industry', '新能源', 4 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_category WHERE category_type = 'industry' AND name = '新能源');
INSERT INTO research_category (category_type, name, sort_order)
SELECT 'industry', '消费电商', 5 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_category WHERE category_type = 'industry' AND name = '消费电商');
INSERT INTO research_category (category_type, name, sort_order)
SELECT 'industry', '企业服务', 6 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_category WHERE category_type = 'industry' AND name = '企业服务');
INSERT INTO research_category (category_type, name, sort_order)
SELECT 'industry', '文化娱乐', 7 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_category WHERE category_type = 'industry' AND name = '文化娱乐');
INSERT INTO research_category (category_type, name, sort_order)
SELECT 'tag', '热门赛道', 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_category WHERE category_type = 'tag' AND name = '热门赛道');
INSERT INTO research_category (category_type, name, sort_order)
SELECT 'tag', '产业洞察', 2 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_category WHERE category_type = 'tag' AND name = '产业洞察');
INSERT INTO research_category (category_type, name, sort_order)
SELECT 'tag', '前沿技术', 3 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_category WHERE category_type = 'tag' AND name = '前沿技术');
INSERT INTO research_category (category_type, name, sort_order)
SELECT 'tag', '短篇洞察', 4 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_category WHERE category_type = 'tag' AND name = '短篇洞察');
INSERT INTO research_category (category_type, name, sort_order)
SELECT 'tag', '其他', 5 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_category WHERE category_type = 'tag' AND name = '其他');
