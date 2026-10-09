-- =============================================================================
-- 清空「项目库」全部数据（不可恢复，执行前请备份 vc_platform）
-- 不影响：institution、news 正文、activity、user 等（仅解除与项目的关联）
-- =============================================================================
USE vc_platform;

-- -----------------------------------------------------------------------------
-- 一、预览当前数量
-- -----------------------------------------------------------------------------
SELECT 'project' AS tbl, COUNT(*) AS cnt FROM project
UNION ALL SELECT 'project_business', COUNT(*) FROM project_business
UNION ALL SELECT 'project_financing', COUNT(*) FROM project_financing
UNION ALL SELECT 'project_financing_investor', COUNT(*) FROM project_financing_investor
UNION ALL SELECT 'project_dynamic', COUNT(*) FROM project_dynamic
UNION ALL SELECT 'project_team_member', COUNT(*) FROM project_team_member
UNION ALL SELECT 'project_shareholder', COUNT(*) FROM project_shareholder
UNION ALL SELECT 'project_tag_rel', COUNT(*) FROM project_tag_rel
UNION ALL SELECT 'project_tag', COUNT(*) FROM project_tag
UNION ALL SELECT 'coverage_apply_record', COUNT(*) FROM coverage_apply_record
UNION ALL SELECT 'connection_record(project)', COUNT(*) FROM connection_record WHERE target_type = 'project';

-- -----------------------------------------------------------------------------
-- 二、清空（确认后执行；默认 ROLLBACK，改 COMMIT 才生效）
-- -----------------------------------------------------------------------------
START TRANSACTION;

SET FOREIGN_KEY_CHECKS = 0;

-- 子表 → 主表
TRUNCATE TABLE project_financing_investor;
TRUNCATE TABLE project_financing;
TRUNCATE TABLE project_dynamic;
TRUNCATE TABLE project_team_member;
TRUNCATE TABLE project_shareholder;
TRUNCATE TABLE project_tag_rel;
TRUNCATE TABLE project_business;
TRUNCATE TABLE project;

-- 标签字典（若需保留标签定义，注释掉下一行）
TRUNCATE TABLE project_tag;

SET FOREIGN_KEY_CHECKS = 1;

-- 业务关联表（引用 project_id）
DELETE FROM coverage_apply_record;

DELETE FROM connection_record WHERE target_type = 'project';

UPDATE project_onboard_record SET project_id = NULL WHERE project_id IS NOT NULL;

UPDATE news SET project_id = NULL WHERE project_id IS NOT NULL;

-- 确认无误后：把 ROLLBACK 改成 COMMIT;
ROLLBACK;
-- COMMIT;

-- -----------------------------------------------------------------------------
-- 三、验证（COMMIT 后执行）
-- -----------------------------------------------------------------------------
-- SELECT COUNT(*) FROM project;
-- SELECT COUNT(*) FROM project_financing;
