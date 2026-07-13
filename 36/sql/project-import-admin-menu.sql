-- 管理后台「项目导入」菜单（线上库若未自动迁移可执行）
USE vc_platform;

SET @project_group_id = (SELECT id FROM admin_permission WHERE code = 'menu:content:project' LIMIT 1);

INSERT INTO admin_permission (code, name, type, parent_id, path, resource, sort_order)
SELECT 'project:import', '项目导入', 'menu', @project_group_id, '/content/project-import', NULL, 2
FROM DUAL
WHERE @project_group_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM admin_permission WHERE code = 'project:import');

SET @perm_id = (SELECT id FROM admin_permission WHERE code = 'project:import' LIMIT 1);

INSERT INTO admin_role_permission (role_id, permission_id)
SELECT r.id, @perm_id
FROM admin_role r
WHERE r.code IN ('super_admin', 'editor')
  AND @perm_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM admin_role_permission arp
    WHERE arp.role_id = r.id AND arp.permission_id = @perm_id
  );
