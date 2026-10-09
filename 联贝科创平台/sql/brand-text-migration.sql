-- 将数据库中残留的旧品牌文案替换为大学生创投（在已有 vc_platform 库执行）
USE vc_platform;

-- 复合词先替换，避免「36氪硬氪」变成「大学生创投硬氪」
UPDATE news SET attribution = REPLACE(REPLACE(REPLACE(REPLACE(attribution, '36氪硬氪', '大学生创投'), '36氪创投派', '大学生创投派'), '36氪', '大学生创投'), '硬氪', '大学生创投')
WHERE attribution LIKE '%36%' OR attribution LIKE '%硬氪%';

UPDATE news SET content = REPLACE(REPLACE(REPLACE(REPLACE(content, '36氪硬氪', '大学生创投'), '36氪创投派', '大学生创投派'), '36氪', '大学生创投'), '硬氪', '大学生创投')
WHERE content LIKE '%36%' OR content LIKE '%硬氪%';

UPDATE news SET summary = REPLACE(REPLACE(REPLACE(REPLACE(summary, '36氪硬氪', '大学生创投'), '36氪创投派', '大学生创投派'), '36氪', '大学生创投'), '硬氪', '大学生创投')
WHERE summary LIKE '%36%' OR summary LIKE '%硬氪%';

UPDATE news SET title = REPLACE(REPLACE(REPLACE(REPLACE(title, '36氪硬氪', '大学生创投'), '36氪创投派', '大学生创投派'), '36氪', '大学生创投'), '硬氪', '大学生创投')
WHERE title LIKE '%36%' OR title LIKE '%硬氪%';

UPDATE news SET source = REPLACE(REPLACE(source, '36氪', '大学生创投'), '硬氪', '大学生创投')
WHERE source LIKE '%36%' OR source LIKE '%硬氪%';

UPDATE research_report SET title = REPLACE(REPLACE(REPLACE(title, '36氪硬氪', '大学生创投'), '36氪创投派', '大学生创投派'), '36氪', '大学生创投')
WHERE title LIKE '%36%' OR title LIKE '%硬氪%';

UPDATE research_report SET summary = REPLACE(REPLACE(REPLACE(summary, '36氪硬氪', '大学生创投'), '36氪创投派', '大学生创投派'), '36氪', '大学生创投')
WHERE summary LIKE '%36%' OR summary LIKE '%硬氪%';

UPDATE research_report SET content = REPLACE(REPLACE(REPLACE(content, '36氪硬氪', '大学生创投'), '36氪创投派', '大学生创投派'), '36氪', '大学生创投')
WHERE content LIKE '%36%' OR content LIKE '%硬氪%';

UPDATE research_report SET publish_by = REPLACE(publish_by, '36氪', '大学生创投')
WHERE publish_by LIKE '%36%';

-- 将数据库中残留的「联贝」品牌文案替换为大学生创投（已有库升级时执行）
UPDATE news SET attribution = REPLACE(REPLACE(REPLACE(attribution, '联贝创投平台', '大学生创投平台'), '联贝创投派', '大学生创投派'), '联贝', '大学生创投')
WHERE attribution LIKE '%联贝%';
UPDATE news SET content = REPLACE(REPLACE(REPLACE(content, '联贝创投平台', '大学生创投平台'), '联贝创投派', '大学生创投派'), '联贝', '大学生创投')
WHERE content LIKE '%联贝%';
UPDATE news SET summary = REPLACE(REPLACE(REPLACE(summary, '联贝创投平台', '大学生创投平台'), '联贝创投派', '大学生创投派'), '联贝', '大学生创投')
WHERE summary LIKE '%联贝%';
UPDATE news SET title = REPLACE(REPLACE(REPLACE(title, '联贝创投平台', '大学生创投平台'), '联贝创投派', '大学生创投派'), '联贝', '大学生创投')
WHERE title LIKE '%联贝%';
UPDATE news SET source = REPLACE(REPLACE(source, '联贝创投平台', '大学生创投平台'), '联贝', '大学生创投')
WHERE source LIKE '%联贝%';

UPDATE research_report SET title = REPLACE(REPLACE(title, '联贝创投平台', '大学生创投平台'), '联贝', '大学生创投')
WHERE title LIKE '%联贝%';
UPDATE research_report SET summary = REPLACE(REPLACE(summary, '联贝创投平台', '大学生创投平台'), '联贝', '大学生创投')
WHERE summary LIKE '%联贝%';
UPDATE research_report SET content = REPLACE(REPLACE(content, '联贝创投平台', '大学生创投平台'), '联贝', '大学生创投')
WHERE content LIKE '%联贝%';
UPDATE research_report SET publish_by = REPLACE(REPLACE(publish_by, '联贝研究院', '大学生创投研究院'), '联贝', '大学生创投')
WHERE publish_by LIKE '%联贝%';

UPDATE activity SET title = REPLACE(title, '联贝', '大学生创投'), description = REPLACE(description, '联贝', '大学生创投'), location = REPLACE(location, '联贝', '大学生创投')
WHERE title LIKE '%联贝%' OR description LIKE '%联贝%' OR location LIKE '%联贝%';

UPDATE ma_deal SET brand_name = REPLACE(REPLACE(brand_name, '联贝并购', '大学生创投并购'), '联贝', '大学生创投')
WHERE brand_name LIKE '%联贝%';
