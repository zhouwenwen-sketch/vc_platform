-- 用户参与活动记录
CREATE TABLE IF NOT EXISTS user_activity_record (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  user_id       BIGINT       NOT NULL COMMENT '用户ID',
  activity_id   BIGINT       NOT NULL COMMENT '活动ID',
  join_status   VARCHAR(32)  NOT NULL COMMENT 'reserved-已预约 signed_up-已报名 attended-已参加',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_user_activity (user_id, activity_id),
  KEY idx_user_activity_user (user_id),
  KEY idx_user_activity_status (user_id, join_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户参与活动记录';

INSERT INTO user_activity_record (user_id, activity_id, join_status)
VALUES (5, 1, 'reserved');  -- reserved / signed_up / attended
