-- 融资快报详情页字段迁移（已有 vc_platform 库执行此脚本）
USE vc_platform;

ALTER TABLE news
  ADD COLUMN content TEXT COMMENT '正文' AFTER project_name,
  ADD COLUMN source_url VARCHAR(512) DEFAULT NULL COMMENT '原文链接' AFTER content;

UPDATE news SET content = '大学生创投获悉，「星源智」近日完成新一轮融资，具体金额尚未披露。本轮融资将主要用于核心技术研发与产品商业化落地。', source_url = 'https://lianbei.com/newsflash/1' WHERE title = '「星源智」完成新一轮融资';
UPDATE news SET content = '大学生创投首发获悉，深圳具身智能公司星尘智能已完成超10亿元B轮融资，投后估值突破百亿元。本轮融资由多家知名机构共同参与，资金将用于人形机器人量产、具身智能大模型研发及海外市场拓展。', source_url = 'https://lianbei.com/p/2' WHERE title LIKE '深圳具身公司星尘智能%';
UPDATE news SET content = '大学生创投获悉，具身智能公司「星尘智能」完成超10亿元B轮融资，投后估值破百亿。', source_url = 'https://lianbei.com/newsflash/3' WHERE title = '「星尘智能」完成超10亿B轮融资';
UPDATE news SET content = '大学生创投获悉，生物技术推广服务商「华融科创」完成A轮融资，由比邻星投资领投，金额超亿元人民币。', source_url = 'https://lianbei.com/newsflash/4' WHERE title = '华融科创完成A轮融资，比邻星投资领投';
UPDATE news SET content = '大学生创投获悉，「智材科技」的人工智能新材料预测大模型产业化项目获得战略投资。公司致力于将AI与前沿新材料研发相结合。', source_url = 'https://lianbei.com/p/5' WHERE title LIKE '人工智能新材料%';
UPDATE news SET content = '大学生创投获悉，新能源电池流通解决方案提供商「常岳新能源」完成A+轮融资，融资金额达数千万元。', source_url = 'https://lianbei.com/newsflash/6' WHERE title = '常岳新能源完成A+轮融资';
UPDATE news SET content = '大学生创投获悉，AGI全栈能力前沿科技公司「昆仑元」获天使轮融资，融资金额数百万。', source_url = 'https://lianbei.com/newsflash/7' WHERE title = '昆仑元获天使轮融资';
UPDATE news SET content = '大学生创投获悉，专注广阔市场前沿蛋白药物研发的「君合盟」完成C轮融资，金额尚未透露。', source_url = 'https://lianbei.com/p/8' WHERE title = '君合盟完成C轮融资';
UPDATE news SET content = '大学生创投获悉，AI驱动的可结算预防医疗平台「睿禾健康」完成种子轮融资。', source_url = 'https://lianbei.com/newsflash/9' WHERE title = '睿禾健康完成种子轮融资';
UPDATE news SET content = '大学生创投获悉，拥有无卫星导航和仿生视觉核心技术的「具身智航」完成Pre-A轮融资。', source_url = 'https://lianbei.com/newsflash/10' WHERE title = '具身智航完成Pre-A轮融资';
UPDATE news SET content = '大学生创投派获悉，人工智能技术研发商 MiniMax 持续领跑大模型赛道，市场估值再创新高。', source_url = 'https://lianbei.com/p/11' WHERE title LIKE 'MiniMax%';
UPDATE news SET content = '大学生创投获悉，「西湖心辰」完成B轮融资，具体金额尚未披露。', source_url = 'https://lianbei.com/newsflash/12' WHERE title = '西湖心辰完成B轮融资';
