-- 项目集：建表 + 后台菜单 + 角色授权（生产环境一次性执行）
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

INSERT INTO admin_permission (code, name, type, parent_id, path, resource, sort_order)
SELECT 'crud:project_collection', '项目集', 'menu', p.id, '/crud/project_collection', 'project_collection', 10
FROM admin_permission p
WHERE p.code = 'menu:content:project'
  AND NOT EXISTS (SELECT 1 FROM admin_permission WHERE code = 'crud:project_collection');

INSERT INTO admin_role_permission (role_id, permission_id)
SELECT DISTINCT rp.role_id, perm.id
FROM admin_role_permission rp
JOIN admin_permission child ON child.id = rp.permission_id
JOIN admin_permission parent ON parent.id = child.parent_id AND parent.code = 'menu:content:project'
CROSS JOIN admin_permission perm
WHERE perm.code = 'crud:project_collection'
  AND NOT EXISTS (
    SELECT 1 FROM admin_role_permission x
    WHERE x.role_id = rp.role_id AND x.permission_id = perm.id
  );
