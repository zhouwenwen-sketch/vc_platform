-- 活动详情扩展字段 + 报名表
ALTER TABLE activity
  ADD COLUMN IF NOT EXISTS banner_urls   TEXT         COMMENT '轮播图 JSON 数组',
  ADD COLUMN IF NOT EXISTS detail_images TEXT         COMMENT '详情长图 JSON 数组',
  ADD COLUMN IF NOT EXISTS price         DECIMAL(10,2) DEFAULT 0 COMMENT '报名价格',
  ADD COLUMN IF NOT EXISTS price_text    VARCHAR(64)  COMMENT '价格展示文案',
  ADD COLUMN IF NOT EXISTS organizer_id  BIGINT       COMMENT '主办方 institution.id',
  ADD COLUMN IF NOT EXISTS organizer_name VARCHAR(255) COMMENT '主办方展示名',
  ADD COLUMN IF NOT EXISTS like_count    INT NOT NULL DEFAULT 0 COMMENT '点赞数';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动报名记录';
