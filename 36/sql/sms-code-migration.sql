-- 短信验证码记录表（登录等场景）
CREATE TABLE IF NOT EXISTS sms_code_record (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  phone         VARCHAR(20)  NOT NULL COMMENT '手机号',
  code          VARCHAR(10)  NOT NULL COMMENT '验证码',
  scene         VARCHAR(32)  NOT NULL DEFAULT 'login' COMMENT '场景：login',
  status        VARCHAR(16)  NOT NULL DEFAULT 'unused' COMMENT 'unused/used/expired',
  fail_count    TINYINT      NOT NULL DEFAULT 0 COMMENT '校验失败次数',
  expire_time   DATETIME     NOT NULL COMMENT '过期时间',
  used_time     DATETIME     DEFAULT NULL COMMENT '验证成功时间',
  send_ip       VARCHAR(64)  DEFAULT NULL COMMENT '发送请求IP',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY idx_sms_phone_status (phone, status),
  KEY idx_sms_expire_time (expire_time),
  KEY idx_sms_phone_create (phone, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短信验证码记录';
