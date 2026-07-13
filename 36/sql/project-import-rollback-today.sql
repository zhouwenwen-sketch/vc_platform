-- =============================================================================
-- 回滚「项目 Excel 导入」数据（按日期）
-- 使用前：先执行「一、预览」，确认无误后再执行「二、删除」
-- 数据库：vc_platform
-- =============================================================================
USE vc_platform;

-- 改成你导入当天的日期（服务器时区一般为 Asia/Shanghai）
SET @import_date = '2026-06-26';

-- -----------------------------------------------------------------------------
-- 一、预览（只读，建议先跑一遍）
-- -----------------------------------------------------------------------------

-- 将受影响的项目（今天新建，或今天被导入更新且带有 import_source）
SELECT id, name, slug, create_time, update_time, import_source
FROM project
WHERE DATE(create_time) = @import_date
   OR (DATE(update_time) = @import_date AND import_source IS NOT NULL AND import_source != '')
ORDER BY id;

-- 本次深创投导入常见项目名（若上面结果为空，可用下面按名称预览）
/*
SELECT id, name, create_time, update_time
FROM project
WHERE name IN (
  '苏州佰睿壹', '大晓机器人', '诺亦腾机器人', '微纳核芯', '清辉联诺',
  '北京视延科技', '纳氟科技', '指尖智擎', '朗捷睿', '上海立芯',
  'PEELSPHERE', '密尔医疗', '血霁生物', '瀚高股份', 'VAST',
  '赣州立探', '墨芯', '理工华汇', '晶溪牧科技'
);
*/

SELECT COUNT(*) AS financing_rows_today
FROM project_financing
WHERE DATE(create_time) = @import_date;

SELECT COUNT(*) AS dynamic_rows_today
FROM project_dynamic
WHERE DATE(create_time) = @import_date;

-- -----------------------------------------------------------------------------
-- 二、删除（确认预览后执行；默认最后 ROLLBACK，改 COMMIT 才真删）
-- -----------------------------------------------------------------------------

START TRANSACTION;

DROP TEMPORARY TABLE IF EXISTS tmp_rollback_project;
CREATE TEMPORARY TABLE tmp_rollback_project (
  id BIGINT PRIMARY KEY
);

-- 命中规则：今天创建的项目 + 今天导入写过的项目（有 import_source 且今天 update）
INSERT INTO tmp_rollback_project (id)
SELECT id
FROM project
WHERE DATE(create_time) = @import_date
   OR (DATE(update_time) = @import_date AND import_source IS NOT NULL AND import_source != '');

-- 若按日期命不中，可改用「按项目名」回滚（取消下面注释并注释掉上面 INSERT）：
/*
INSERT INTO tmp_rollback_project (id)
SELECT id FROM project
WHERE name IN (
  '苏州佰睿壹', '大晓机器人', '诺亦腾机器人', '微纳核芯', '清辉联诺',
  '北京视延科技', '纳氟科技', '指尖智擎', '朗捷睿', '上海立芯',
  'PEELSPHERE', '密尔医疗', '血霁生物', '瀚高股份', 'VAST',
  '赣州立探', '墨芯', '理工华汇', '晶溪牧科技'
);
*/

SELECT COUNT(*) AS projects_to_rollback FROM tmp_rollback_project;

-- 融资投资方
DELETE pfi
FROM project_financing_investor pfi
INNER JOIN project_financing pf ON pf.id = pfi.financing_id
WHERE pf.project_id IN (SELECT id FROM tmp_rollback_project)
   OR DATE(pf.create_time) = @import_date;

-- 融资记录
DELETE FROM project_financing
WHERE project_id IN (SELECT id FROM tmp_rollback_project)
   OR DATE(create_time) = @import_date;

-- 项目动态（含挂到旧项目、但今天写入的动态）
DELETE FROM project_dynamic
WHERE project_id IN (SELECT id FROM tmp_rollback_project)
   OR DATE(create_time) = @import_date;

-- 团队成员
DELETE FROM project_team_member
WHERE project_id IN (SELECT id FROM tmp_rollback_project);

-- 标签关联
DELETE FROM project_tag_rel
WHERE project_id IN (SELECT id FROM tmp_rollback_project);

-- 工商 / 股东（若存在）
DELETE FROM project_business
WHERE project_id IN (SELECT id FROM tmp_rollback_project);

DELETE FROM project_shareholder
WHERE project_id IN (SELECT id FROM tmp_rollback_project);

-- 快讯关联（若有）置空，不删快讯本身
UPDATE news
SET project_id = NULL, project_name = NULL
WHERE project_id IN (SELECT id FROM tmp_rollback_project);

-- 仅删除「今天新建」的项目行；若项目是旧的只做了更新，保留 project 主表
DELETE FROM project
WHERE id IN (SELECT id FROM tmp_rollback_project)
  AND DATE(create_time) = @import_date;

-- 可选：清理已无项目引用的孤立标签
/*
DELETE t FROM project_tag t
LEFT JOIN project_tag_rel r ON r.tag_id = t.id
WHERE r.id IS NULL;
*/

-- 确认无误后：把 ROLLBACK 改成 COMMIT;
ROLLBACK;
-- COMMIT;

DROP TEMPORARY TABLE IF EXISTS tmp_rollback_project;
