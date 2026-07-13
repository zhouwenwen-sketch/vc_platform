-- 融资快报文章详情字段迁移（已有 vc_platform 库执行此脚本）
USE vc_platform;

ALTER TABLE news
  ADD COLUMN cover_url VARCHAR(512) DEFAULT NULL COMMENT '封面/头图' AFTER source_url,
  ADD COLUMN source_avatar VARCHAR(512) DEFAULT NULL COMMENT '来源头像' AFTER cover_url,
  ADD COLUMN content_format VARCHAR(16) DEFAULT 'text' COMMENT 'text | html' AFTER source_avatar,
  ADD COLUMN attribution TEXT DEFAULT NULL COMMENT '版权声明' AFTER content_format;

-- 文章类样例数据补全
UPDATE news SET
  cover_url = 'https://picsum.photos/750/420?random=article1',
  source_avatar = NULL,
  content_format = 'text',
  attribution = '本文来自大学生创投，大学生创投经授权发布。\n该文观点仅代表作者本人，大学生创投平台仅提供信息存储空间服务。'
WHERE news_type = '文章' AND title LIKE '深圳具身公司星尘智能%';

UPDATE news SET
  cover_url = 'https://picsum.photos/750/420?random=article2',
  content_format = 'text',
  attribution = '本文来自大学生创投，大学生创投经授权发布。\n该文观点仅代表作者本人，大学生创投平台仅提供信息存储空间服务。'
WHERE news_type = '文章' AND title LIKE '人工智能新材料%';

UPDATE news SET
  cover_url = 'https://picsum.photos/750/420?random=article3',
  content_format = 'text',
  attribution = '本文来自大学生创投，大学生创投经授权发布。\n该文观点仅代表作者本人，大学生创投平台仅提供信息存储空间服务。'
WHERE news_type = '文章' AND title = '君合盟完成C轮融资';

UPDATE news SET
  cover_url = 'https://picsum.photos/750/420?random=article4',
  content_format = 'text',
  attribution = '本文来自大学生创投派，大学生创投经授权发布。\n该文观点仅代表作者本人，大学生创投平台仅提供信息存储空间服务。'
WHERE news_type = '文章' AND title LIKE 'MiniMax%';
