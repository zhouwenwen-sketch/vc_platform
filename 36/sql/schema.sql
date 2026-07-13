-- 大学生创投平台数据库 DDL（MySQL 5.7+）
CREATE DATABASE IF NOT EXISTS vc_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE vc_platform;

-- ----------------------------
-- 快讯表
-- ----------------------------
DROP TABLE IF EXISTS connection_record;
DROP TABLE IF EXISTS user_auth_record;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS activity;
DROP TABLE IF EXISTS institution;
DROP TABLE IF EXISTS project;
DROP TABLE IF EXISTS news;

CREATE TABLE news (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  title VARCHAR(512) NOT NULL COMMENT '标题',
  summary VARCHAR(1024) DEFAULT NULL COMMENT '摘要',
  source VARCHAR(128) DEFAULT NULL COMMENT '来源/作者',
  tag VARCHAR(64) DEFAULT NULL COMMENT '轮次标签，如 A轮、B轮',
  region VARCHAR(64) DEFAULT NULL COMMENT '地区',
  news_type VARCHAR(32) DEFAULT '快讯' COMMENT '类型：快讯/文章',
  project_name VARCHAR(128) DEFAULT NULL COMMENT '关联项目名称',
  project_id BIGINT DEFAULT NULL COMMENT '关联 project.id',
  content TEXT COMMENT '正文',
  source_url VARCHAR(512) DEFAULT NULL COMMENT '原文链接',
  cover_url VARCHAR(512) DEFAULT NULL COMMENT '封面/头图',
  source_avatar VARCHAR(512) DEFAULT NULL COMMENT '来源头像',
  content_format VARCHAR(16) DEFAULT 'text' COMMENT 'text | html',
  attribution TEXT DEFAULT NULL COMMENT '版权声明',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_news_create_time (create_time),
  KEY idx_news_region (region),
  KEY idx_news_tag (tag),
  KEY idx_news_project_id (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='融资快讯';

-- ----------------------------
-- 项目表
-- ----------------------------
CREATE TABLE project (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  name VARCHAR(255) NOT NULL COMMENT '项目简称/品牌名',
  slug VARCHAR(128) DEFAULT NULL COMMENT '稳定标识',
  round VARCHAR(64) DEFAULT NULL COMMENT '融资轮次',
  region VARCHAR(64) DEFAULT NULL COMMENT '地区',
  category VARCHAR(64) DEFAULT NULL COMMENT '分类/赛道',
  tags TEXT DEFAULT NULL COMMENT '标签，逗号分隔(过渡字段)',
  company_desc VARCHAR(1024) DEFAULT NULL COMMENT '公司描述/一句话',
  intro TEXT DEFAULT NULL COMMENT '详情介绍',
  investment_amount VARCHAR(64) DEFAULT NULL COMMENT '融资金额展示',
  status_label VARCHAR(32) DEFAULT NULL COMMENT '状态标签，如已上市、A轮',
  location VARCHAR(64) DEFAULT NULL COMMENT '城市',
  founding_year VARCHAR(16) DEFAULT NULL COMMENT '成立年份',
  logo_url VARCHAR(512) DEFAULT NULL COMMENT 'Logo地址',
  website VARCHAR(512) DEFAULT NULL COMMENT '官网',
  is_financing TINYINT NOT NULL DEFAULT 0 COMMENT '是否在融（首页横滑）',
  is_hot TINYINT NOT NULL DEFAULT 1 COMMENT '是否热门列表展示',
  is_certified TINYINT NOT NULL DEFAULT 0 COMMENT '是否认证',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '0草稿 1已发布 2下架',
  latest_round VARCHAR(64) DEFAULT NULL COMMENT '最新轮次(冗余)',
  latest_amount VARCHAR(64) DEFAULT NULL COMMENT '最新融资金额(冗余)',
  latest_financing_date DATE DEFAULT NULL COMMENT '最新融资日期(冗余)',
  national_economy_industry TEXT DEFAULT NULL COMMENT '国民经济产业',
  strategic_emerging_industry TEXT DEFAULT NULL COMMENT '战略新兴产业',
  high_precision_industry TEXT DEFAULT NULL COMMENT '高精尖产业',
  listing_board VARCHAR(64) DEFAULT NULL COMMENT '上市板块',
  listing_date DATE DEFAULT NULL COMMENT '上市日期',
  employee_count VARCHAR(32) DEFAULT NULL COMMENT '雇员人数',
  manager_count VARCHAR(32) DEFAULT NULL COMMENT '管理人员人数',
  import_source VARCHAR(128) DEFAULT NULL COMMENT '数据来源',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_project_slug (slug),
  KEY idx_project_create_time (create_time),
  KEY idx_project_category (category),
  KEY idx_project_round (round),
  KEY idx_project_region (region),
  KEY idx_project_is_financing (is_financing),
  KEY idx_project_is_hot (is_hot)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='创投项目';

CREATE TABLE project_business (
  id BIGINT PRIMARY KEY COMMENT '与 project.id 一致',
  project_id BIGINT NOT NULL COMMENT 'project.id',
  full_name VARCHAR(255) DEFAULT NULL COMMENT '工商全称/企业主体',
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

CREATE TABLE project_financing (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  financing_date DATE DEFAULT NULL,
  round VARCHAR(64) NOT NULL,
  amount VARCHAR(64) DEFAULT NULL,
  amount_currency VARCHAR(16) DEFAULT 'CNY',
  is_latest TINYINT NOT NULL DEFAULT 0,
  sort_order INT NOT NULL DEFAULT 0,
  source VARCHAR(128) DEFAULT NULL COMMENT '融资来源',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_financing_project (project_id, sort_order),
  KEY idx_financing_date (financing_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目融资历史';

CREATE TABLE project_financing_investor (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  financing_id BIGINT NOT NULL,
  institution_id BIGINT DEFAULT NULL,
  investor_name VARCHAR(128) NOT NULL,
  investor_role VARCHAR(16) DEFAULT NULL COMMENT 'lead=领投 follow=跟投',
  KEY idx_pfi_financing (financing_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='融资投资方';

CREATE TABLE project_dynamic (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  event_date DATE DEFAULT NULL,
  content TEXT NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_project_dynamic_project (project_id, sort_order),
  KEY idx_project_dynamic_date (event_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目动态';

CREATE TABLE project_shareholder (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  shareholder_name VARCHAR(128) NOT NULL,
  ratio VARCHAR(32) DEFAULT NULL,
  capital VARCHAR(64) DEFAULT NULL,
  capital_date VARCHAR(32) DEFAULT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  KEY idx_shareholder_project (project_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目股东';

CREATE TABLE project_team_member (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  member_name VARCHAR(64) NOT NULL,
  title VARCHAR(128) DEFAULT NULL,
  avatar_url VARCHAR(512) DEFAULT NULL,
  bio TEXT DEFAULT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  KEY idx_team_project (project_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目团队成员';

CREATE TABLE project_tag (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  UNIQUE KEY uk_tag_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目标签';

CREATE TABLE project_tag_rel (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  tag_id BIGINT NOT NULL,
  UNIQUE KEY uk_project_tag (project_id, tag_id),
  KEY idx_tag_rel_tag (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目-标签关联';

-- ----------------------------
-- 投资机构表
-- ----------------------------
CREATE TABLE institution (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  name VARCHAR(255) NOT NULL COMMENT '机构名称',
  entity_name VARCHAR(255) DEFAULT NULL COMMENT '主体名称',
  inst_type VARCHAR(32) DEFAULT NULL COMMENT '机构类型：VC/PE/CVC等',
  investment_field VARCHAR(64) DEFAULT NULL COMMENT '投资领域',
  recent_investment VARCHAR(255) DEFAULT NULL COMMENT '最近投资项目',
  event_count INT NOT NULL DEFAULT 0 COMMENT '投资事件数',
  founded_year VARCHAR(16) DEFAULT NULL COMMENT '成立时间',
  logo_url VARCHAR(512) DEFAULT NULL COMMENT 'Logo地址',
  intro TEXT DEFAULT NULL COMMENT '机构介绍',
  manage_scale VARCHAR(128) DEFAULT NULL COMMENT '管理规模',
  website VARCHAR(512) DEFAULT NULL COMMENT '官网',
  investment_fields TEXT DEFAULT NULL COMMENT '投资领域完整列表，逗号分隔',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_inst_field (investment_field),
  KEY idx_inst_type (inst_type),
  KEY idx_inst_year (founded_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投资机构';

CREATE TABLE institution_team_member (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='机构团队成员';

CREATE TABLE institution_fund_manager (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  institution_id BIGINT NOT NULL,
  full_name VARCHAR(255) NOT NULL COMMENT '基金管理人全称',
  legal_person VARCHAR(64) DEFAULT NULL COMMENT '法人代表',
  inst_type VARCHAR(64) DEFAULT NULL COMMENT '机构类型',
  office_address VARCHAR(512) DEFAULT NULL COMMENT '办公地址',
  registered_capital VARCHAR(64) DEFAULT NULL COMMENT '注册资本',
  paid_in_capital VARCHAR(64) DEFAULT NULL COMMENT '实缴资本',
  paid_in_ratio VARCHAR(32) DEFAULT NULL COMMENT '实缴比例',
  registration_no VARCHAR(64) DEFAULT NULL COMMENT '登记编号',
  establish_date VARCHAR(32) DEFAULT NULL COMMENT '成立时间',
  register_date VARCHAR(32) DEFAULT NULL COMMENT '登记时间',
  sort_order INT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_inst_fund_manager (institution_id, full_name),
  KEY idx_inst_fund_manager_inst (institution_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='机构基金管理人';

-- ----------------------------
-- 活动表（含普通活动与每日路演）
-- ----------------------------
CREATE TABLE activity (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  title VARCHAR(512) NOT NULL COMMENT '活动标题',
  cover_url VARCHAR(512) DEFAULT NULL COMMENT '封面图',
  location VARCHAR(255) DEFAULT NULL COMMENT '地点',
  start_time DATETIME NOT NULL COMMENT '开始时间',
  end_time DATETIME NOT NULL COMMENT '结束时间',
  status VARCHAR(32) NOT NULL DEFAULT 'ongoing' COMMENT '状态：registering-正在报名 ongoing-进行中 ended-已结束',
  participant_count INT NOT NULL DEFAULT 0 COMMENT '参与人数',
  description TEXT COMMENT '活动详情',
  activity_type VARCHAR(32) NOT NULL DEFAULT 'event' COMMENT '类型：event-活动 roadshow-路演',
  banner_urls TEXT COMMENT '轮播图 JSON 数组',
  detail_images TEXT COMMENT '详情长图 JSON 数组',
  price DECIMAL(10,2) DEFAULT 0 COMMENT '报名价格',
  price_text VARCHAR(64) DEFAULT NULL COMMENT '价格展示文案',
  organizer_id BIGINT DEFAULT NULL COMMENT '主办方 institution.id',
  organizer_name VARCHAR(255) DEFAULT NULL COMMENT '主办方展示名',
  like_count INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_activity_status (status),
  KEY idx_activity_type (activity_type),
  KEY idx_activity_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='创投活动/路演';

CREATE TABLE activity_registration (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  activity_id BIGINT NOT NULL COMMENT '活动ID',
  name VARCHAR(64) NOT NULL COMMENT '姓名',
  phone VARCHAR(20) NOT NULL COMMENT '联系电话',
  org_name VARCHAR(255) NOT NULL COMMENT '单位名称',
  position VARCHAR(128) NOT NULL COMMENT '职位',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_activity_reg (user_id, activity_id),
  KEY idx_activity_reg_activity (activity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动报名记录';

-- ----------------------------
-- 用户表（MySQL 保留字需反引号）
-- ----------------------------
CREATE TABLE `user` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  phone VARCHAR(20) NOT NULL COMMENT '手机号',
  nickname VARCHAR(64) DEFAULT NULL COMMENT '昵称',
  avatar VARCHAR(512) DEFAULT NULL COMMENT '头像',
  role VARCHAR(32) DEFAULT NULL COMMENT '角色：investor/entrepreneur',
  auth_status VARCHAR(32) NOT NULL DEFAULT 'none' COMMENT '认证状态：none/pending/approved/rejected',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_user_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';

-- ----------------------------
-- 认证记录表
-- ----------------------------
CREATE TABLE user_auth_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  auth_type VARCHAR(32) NOT NULL COMMENT '认证类型：investor/entrepreneur',
  status VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT 'pending/approved/rejected',
  apply_data TEXT COMMENT '申请资料JSON',
  audit_remark VARCHAR(512) DEFAULT NULL COMMENT '审核备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_auth_user_id (user_id),
  KEY idx_auth_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户认证记录';

-- ----------------------------
-- 对接记录表
-- ----------------------------
CREATE TABLE connection_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  target_type VARCHAR(32) NOT NULL COMMENT '目标类型：project/activity',
  target_id BIGINT NOT NULL COMMENT '目标ID',
  target_name VARCHAR(255) NOT NULL COMMENT '目标名称',
  connection_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '对接时间',
  status VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT 'pending/success/cancelled',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_conn_user_id (user_id),
  KEY idx_conn_time (connection_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对接记录';

-- ----------------------------
-- 初始化数据（融资快报由 project_dynamic 迁移生成，见 NewsFromProjectDynamicMigration）
-- ----------------------------

INSERT INTO project (name, round, region, category, tags, company_desc, investment_amount, status_label, location, founding_year, is_financing, is_hot) VALUES
('忆生科技', '天使轮', '广东省', '前沿技术', '前沿技术', '专注于具身智能与世界模型的前沿科技公司', NULL, '天使轮', '广东省', '2023年', 0, 1),
('联汇科技', 'B++轮', '浙江省', '企业服务', '企业服务', '数字音视频技术研发及系统集成服务商', NULL, 'B++轮', '浙江省', '2003年', 0, 1),
('黄页88', '未融资', '北京市', '文化娱乐', '文化娱乐', '企业黄页信息平台', NULL, '未融资', '北京市', '2010年', 0, 1),
('常岳新能源', 'A+轮', '江苏省', '新能源', '新能源,能源环保', '新能源电池流通解决方案提供商', '数千万', 'A+轮', '江苏省', '2019年', 1, 1),
('智材科技', '战略融资', '浙江省', '人工智能', '人工智能,新材料', '人工智能+前沿新材料+实验验证', '未透露', '战略融资', '浙江省', '2022年', 1, 1),
('星尘智能', 'B轮', '广东省', '人工智能', '人工智能,机器人', '具身智能与人形机器人研发', '超10亿', 'B轮', '广东省', '2022年', 1, 1),
('昆仑元', '天使轮', '北京市', '人工智能', '人工智能', 'AGI全栈能力前沿科技公司', '数百万', '天使轮', '北京市', '2023年', 1, 1),
('君合盟', 'C轮', '广东省', '医疗健康', '医疗健康,生物制药', '专注广阔市场前沿蛋白药物研发', '未透露', 'C轮', '广东省', '2017年', 1, 1),
('MiniMax', '已上市', '上海市', '人工智能', '物联网/硬件,智能硬件', '人工智能技术研发商', '未透露', '已上市', '上海市', '2021年', 0, 1),
('白兔控股', '战略融资', '四川省', '消费电商', '消费电商,企业服务', '一站式品牌、营销、电商服务提供商', '未透露', '战略融资', '四川省', '2018年', 0, 1),
('宁德时代', '已上市', '福建省', '先进制造', '先进制造,能源环保', '先进电池、能源存储解决方案服务商', '未透露', '已上市', '福建省', '2011年', 0, 1),
('金胜电子', 'A轮', '广东省', '先进制造', '先进制造,半导体', 'SSD产品研发制造商', '未透露', 'A轮', '广东省', '2010年', 0, 1),
('华融科创', 'A轮', '上海市', '医疗健康', '医疗健康', '生物技术推广服务商', '超亿人民币', 'A轮', '上海市', '2020年', 0, 1),
('具身智航', '未融资', '北京市', '前沿技术', '前沿技术,先进制造', '拥有无卫星导航和仿生视觉的核心技术', NULL, '未融资', '北京市', '2019年', 0, 1),
('睿禾健康', '种子轮', '上海市', '医疗健康', '医疗健康,人工智能', 'AI驱动的可结算预防医疗平台', '未透露', '种子轮', '上海市', '2024年', 0, 1);

-- ----------------------------
-- 研究院报告表
-- ----------------------------
CREATE TABLE research_report (
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
  publish_by VARCHAR(64) NOT NULL DEFAULT '大学生创投研究院' COMMENT '发布人',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '0草稿 1已发布',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_report_publish_date (publish_date),
  KEY idx_report_type (report_type),
  KEY idx_report_industry (industry)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='研究院报告';

-- ----------------------------
-- 研究院报告分类表
-- ----------------------------
CREATE TABLE research_category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  category_type VARCHAR(32) NOT NULL COMMENT '类型：report_type/industry/tag',
  name VARCHAR(64) NOT NULL COMMENT '分类名称',
  sort_order INT NOT NULL DEFAULT 0,
  KEY idx_category_type (category_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='研究院报告分类';

-- ----------------------------
-- 融资并购项目表
-- ----------------------------
CREATE TABLE ma_deal (
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
  publish_time DATETIME NOT NULL COMMENT '发布时间',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '0草稿 1已发布',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_ma_deal_project_no (project_no),
  KEY idx_ma_deal_category (category),
  KEY idx_ma_deal_publish_time (publish_time),
  KEY idx_ma_deal_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='融资并购项目';

INSERT INTO institution (name, entity_name, inst_type, investment_field, recent_investment, event_count, founded_year) VALUES
('科德教育', '苏州科德教育科技股份有限公司', 'CVC', '教育', '星尘智能', 1, '1993年'),
('中博聚力', '中博聚力（北京）投资管理有限公司', 'PE', '企业服务', 'zopnote', 4, '2015年'),
('小苗资本', '上海小苗股权投资管理有限公司', 'VC', '前沿技术', '禹鼎科技', 3, '2014年'),
('鼎晖投资', '鼎晖股权投资管理（天津）有限公司', 'VC', '消费电商', '跃然创新', 12, '2002年'),
('愉悦资本', '愉悦资本管理有限公司', 'VC', '汽车出行', '商用清洁机器人华东区', 8, '2014年'),
('首钢基金', '北京首钢基金有限公司', 'VC', '产业升级', '华封科技', 6, '2014年'),
('真格基金', '北京真格天成投资管理有限公司', 'VC', '前沿技术', '忆生科技', 28, '2011年'),
('红杉中国', '红杉资本中国基金', 'VC', '人工智能', '星尘智能', 156, '2005年'),
('高瓴创投', '高瓴创投', 'VC', '医疗健康', '华融科创', 42, '2005年'),
('经纬创投', '经纬中国', 'VC', '人工智能', '智材科技', 88, '2008年'),
('凯辉基金', '凯辉投资咨询（上海）有限公司', 'PE', '医疗健康', 'Ascend', 15, '2006年'),
('比邻星投资', '比邻星投资', 'VC', '医疗健康', '华融科创', 9, '2015年'),
('文化娱乐基金', '文娱产业投资基金', 'VC', '文化娱乐', '黄页88', 5, '2018年'),
('消费电商资本', '新消费产业基金', 'CVC', '消费电商', '白兔控股', 7, '2016年'),
('汽车出行创投', '出行产业投资基金', 'VC', '汽车出行', '常岳新能源', 4, '2019年'),
('金融产业基金', '金融科技投资基金', 'PE', '金融', 'MiniMax', 11, '2012年'),
('企业服务联盟', '企业服务产业基金', 'VC', '企业服务', '联汇科技', 6, '2017年'),
('产业升级基金', '产业升级投资基金', 'CVC', '产业升级', '金胜电子', 10, '2010年'),
('前沿技术创投', '硬科技早期基金', '天使', '前沿技术', '昆仑元', 18, '2020年'),
('医疗创新基金', '医疗健康专项基金', 'VC', '医疗', '君合盟', 14, '2013年'),
('张通社', '上海张通社信息科技有限公司', '战略融资', '企业服务', '具身智能', 5, '2018年');

INSERT INTO activity (title, cover_url, location, start_time, end_time, status, participant_count, description, activity_type, banner_urls, detail_images, price, price_text, organizer_id, organizer_name, like_count) VALUES
('人工智能新材料预测大模型产业化项目路演', 'https://picsum.photos/690/320?random=10', '线上直播', NOW() + INTERVAL 2 DAY, NOW() + INTERVAL 2 DAY + INTERVAL 3 HOUR, 'ongoing', 520, '聚焦新材料与AI融合产业化路径', 'roadshow', NULL, NULL, 0, '免费', NULL, NULL, 0),
('具身智能商业化落地路径专题路演', 'https://picsum.photos/690/320?random=11', '上海 · 浦东', NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 'ongoing', 186, '头部项目与投资人面对面', 'roadshow', NULL, NULL, 0, '免费', NULL, NULL, 0),
('新能源储能产业链创新项目路演', 'https://picsum.photos/690/320?random=12', '深圳 · 前海', NOW() + INTERVAL 7 DAY, NOW() + INTERVAL 7 DAY + INTERVAL 3 HOUR, 'ongoing', 95, '储能赛道优质项目集中路演', 'roadshow', NULL, NULL, 0, '免费', NULL, NULL, 0),
('医疗健康AI项目路演专场', 'https://picsum.photos/690/320?random=13', '杭州 · 未来科技城', NOW() + INTERVAL 10 DAY, NOW() + INTERVAL 10 DAY + INTERVAL 2 HOUR, 'ongoing', 128, '可结算预防医疗创新项目', 'roadshow', NULL, NULL, 0, '免费', NULL, NULL, 0),
('硬科技创业者训练营路演', 'https://picsum.photos/690/320?random=14', '北京 · 大学生创投', NOW() + INTERVAL 12 DAY, NOW() + INTERVAL 12 DAY + INTERVAL 4 HOUR, 'ongoing', 210, '早期硬科技项目展示', 'roadshow', NULL, NULL, 0, '免费', NULL, NULL, 0),
('消费品牌增长力项目路演', 'https://picsum.photos/690/320?random=15', '广州 · 琶洲', NOW() + INTERVAL 15 DAY, NOW() + INTERVAL 15 DAY + INTERVAL 3 HOUR, 'ongoing', 76, '新消费品牌融资路演', 'roadshow', NULL, NULL, 0, '免费', NULL, NULL, 0),
('WISE2025 商业之王 · 创投对接会', 'https://picsum.photos/690/320?random=40', '上海 · 浦东嘉里大酒店', NOW() + INTERVAL 15 DAY, NOW() + INTERVAL 15 DAY + INTERVAL 4 HOUR, 'registering', 328, '年度创投盛典对接', 'event', '["https://picsum.photos/750/420?random=801"]', '["https://picsum.photos/750/1400?random=811","https://picsum.photos/750/900?random=812"]', 0, '免费', 21, '张通社 & 加冕研究院', 0),
('人工智能产业创新峰会', 'https://picsum.photos/690/320?random=41', '北京 · 中关村', NOW() + INTERVAL 20 DAY, NOW() + INTERVAL 20 DAY + INTERVAL 8 HOUR, 'ongoing', 512, 'AI产业趋势与投融资', 'event', '["https://picsum.photos/750/420?random=821"]', '["https://picsum.photos/750/1200?random=822"]', 0, '免费', 21, '张通社', 0),
('新能源储能投融资论坛', 'https://picsum.photos/690/320?random=42', '深圳 · 前海', NOW() + INTERVAL 25 DAY, NOW() + INTERVAL 25 DAY + INTERVAL 3 HOUR, 'ongoing', 186, '储能产业链资本论坛', 'event', NULL, NULL, 0, '免费', NULL, NULL, 0),
('2025年度创投领袖闭门会', 'https://picsum.photos/690/320?random=46', '上海 · 静安区', NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 30 DAY + INTERVAL 4 HOUR, 'ended', 89, '已结束', 'event', NULL, NULL, 0, '免费', NULL, NULL, 0),
('消费品牌增长力大会', 'https://picsum.photos/690/320?random=47', '广州 · 琶洲会展中心', NOW() - INTERVAL 60 DAY, NOW() - INTERVAL 60 DAY + INTERVAL 8 HOUR, 'ended', 856, '已结束', 'event', NULL, NULL, 0, '免费', NULL, NULL, 0),
('FA服务与融资策略公开课', 'https://picsum.photos/690/320?random=50', '线上直播', NOW() - INTERVAL 90 DAY, NOW() - INTERVAL 90 DAY + INTERVAL 2 HOUR, 'ended', 2340, '已结束', 'event', NULL, NULL, 0, '免费', NULL, NULL, 0),
('报告订购 | 《2026年具身智能行业——世界模型驱动下的投资窗口》', 'https://picsum.photos/750/420?random=801', '张通社 & 加冕研究院', NOW() + INTERVAL 60 DAY, NOW() + INTERVAL 60 DAY + INTERVAL 8 HOUR, 'registering', 0, '具身智能行业投资窗口报告订购活动', 'event', '["https://picsum.photos/750/420?random=801","https://picsum.photos/750/420?random=802"]', '["https://picsum.photos/750/1400?random=811","https://picsum.photos/750/900?random=812"]', 200, '200元', 21, '张通社 & 加冕研究院', 0);

INSERT INTO `user` (phone, nickname, avatar, role, auth_status) VALUES
('13800138000', '测试投资人', NULL, 'investor', 'approved');

INSERT INTO connection_record (user_id, target_type, target_id, target_name, connection_time, status) VALUES
(1, 'project', 1, '常岳新能源', NOW() - INTERVAL 2 DAY, 'success'),
(1, 'project', 3, '星尘智能', NOW() - INTERVAL 1 DAY, 'pending'),
(1, 'activity', 7, 'WISE2025 商业之王 · 创投对接会', NOW() - INTERVAL 5 HOUR, 'pending');

-- ----------------------------
-- 首页轮播图
-- ----------------------------
CREATE TABLE IF NOT EXISTS home_banner (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  image_url   VARCHAR(512) NOT NULL COMMENT '轮播图片',
  title       VARCHAR(128) NOT NULL COMMENT '主标题',
  subtitle    VARCHAR(256) DEFAULT NULL COMMENT '副标题',
  link_url    VARCHAR(512) DEFAULT NULL COMMENT '跳转链接',
  sort_order  INT          NOT NULL DEFAULT 0 COMMENT '排序，越小越靠前',
  status      TINYINT      NOT NULL DEFAULT 1 COMMENT '1=启用 0=禁用',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_home_banner_status_sort (status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页轮播图';

INSERT INTO home_banner (image_url, title, subtitle, sort_order, status) VALUES
('https://picsum.photos/750/280?random=1', '免费对接优质项目', '十年媒体资源积累，助力创业者链接资本', 1, 1),
('https://picsum.photos/750/280?random=2', 'WISE2025 商业之王', '年度创投盛典火热报名中', 2, 1);

-- ----------------------------
-- 项目集
-- ----------------------------
CREATE TABLE IF NOT EXISTS project_collection_tab (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  name        VARCHAR(64)  NOT NULL COMMENT 'Tab 名称',
  sort_order  INT          NOT NULL DEFAULT 0 COMMENT '排序',
  UNIQUE KEY uk_project_collection_tab_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目集 Tab';

CREATE TABLE IF NOT EXISTS project_collection (
  id               BIGINT PRIMARY KEY AUTO_INCREMENT,
  category         VARCHAR(64)   NOT NULL COMMENT '所属 Tab',
  badge            VARCHAR(64)   DEFAULT NULL COMMENT '角标',
  title            VARCHAR(256)  NOT NULL COMMENT '标题',
  cover            VARCHAR(512)  NOT NULL COMMENT '封面图',
  cover_title      VARCHAR(512)  DEFAULT NULL COMMENT '封面叠字',
  collection_date  DATE          DEFAULT NULL COMMENT '展示日期',
  summary          VARCHAR(512)  DEFAULT NULL COMMENT '列表摘要',
  description      TEXT          DEFAULT NULL COMMENT '详情描述',
  projects_data    LONGTEXT      DEFAULT NULL COMMENT '项目 JSON 数组',
  sort_order       INT           NOT NULL DEFAULT 0 COMMENT '排序',
  status           TINYINT       NOT NULL DEFAULT 1 COMMENT '1=启用 0=禁用',
  create_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_project_collection_category_status (category, status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目集';
