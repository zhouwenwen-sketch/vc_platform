-- 已有 vc_platform 库增量：投资机构表 + 种子数据
USE vc_platform;

CREATE TABLE IF NOT EXISTS institution (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  name VARCHAR(255) NOT NULL COMMENT '机构名称',
  entity_name VARCHAR(255) DEFAULT NULL COMMENT '主体名称',
  inst_type VARCHAR(32) DEFAULT NULL COMMENT '机构类型：VC/PE/CVC等',
  investment_field VARCHAR(64) DEFAULT NULL COMMENT '投资领域',
  recent_investment VARCHAR(255) DEFAULT NULL COMMENT '最近投资项目',
  event_count INT NOT NULL DEFAULT 0 COMMENT '投资事件数',
  founded_year VARCHAR(16) DEFAULT NULL COMMENT '成立时间',
  logo_url VARCHAR(512) DEFAULT NULL COMMENT 'Logo地址',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_inst_field (investment_field),
  KEY idx_inst_type (inst_type),
  KEY idx_inst_year (founded_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投资机构';

INSERT IGNORE INTO institution (id, name, entity_name, inst_type, investment_field, recent_investment, event_count, founded_year) VALUES
(1, '科德教育', '苏州科德教育科技股份有限公司', 'CVC', '教育', '星尘智能', 1, '1993年'),
(2, '中博聚力', '中博聚力（北京）投资管理有限公司', 'PE', '企业服务', 'zopnote', 4, '2015年'),
(3, '小苗资本', '上海小苗股权投资管理有限公司', 'VC', '前沿技术', '禹鼎科技', 3, '2014年'),
(4, '鼎晖投资', '鼎晖股权投资管理（天津）有限公司', 'VC', '消费电商', '跃然创新', 12, '2002年'),
(5, '愉悦资本', '愉悦资本管理有限公司', 'VC', '汽车出行', '商用清洁机器人华东区', 8, '2014年'),
(6, '首钢基金', '北京首钢基金有限公司', 'VC', '产业升级', '华封科技', 6, '2014年'),
(7, '真格基金', '北京真格天成投资管理有限公司', 'VC', '前沿技术', '忆生科技', 28, '2011年'),
(8, '红杉中国', '红杉资本中国基金', 'VC', '人工智能', '星尘智能', 156, '2005年'),
(9, '高瓴创投', '高瓴创投', 'VC', '医疗健康', '华融科创', 42, '2005年'),
(10, '经纬创投', '经纬中国', 'VC', '人工智能', '智材科技', 88, '2008年');
