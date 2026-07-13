CREATE TABLE IF NOT EXISTS coverage_apply_record (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id       BIGINT       NOT NULL,
  project_id    BIGINT       NOT NULL,
  project_name  VARCHAR(128) NOT NULL,
  report_type   VARCHAR(32)  NOT NULL,
  status        VARCHAR(32)  NOT NULL DEFAULT 'pending',
  apply_data    JSON         NOT NULL,
  news_id       BIGINT       DEFAULT NULL,
  audit_remark  VARCHAR(512) DEFAULT NULL,
  auditor_id    BIGINT       DEFAULT NULL,
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_coverage_user_project (user_id, project_id),
  KEY idx_coverage_status (status),
  KEY idx_coverage_project (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='寻求报道申请';
