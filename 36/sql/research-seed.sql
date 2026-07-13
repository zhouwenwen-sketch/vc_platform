-- 研究院模块种子数据（幂等：可重复执行）
USE vc_platform;

INSERT INTO research_category (category_type, name, sort_order)
SELECT 'report_type', '行业研究', 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_category WHERE category_type = 'report_type' AND name = '行业研究');
INSERT INTO research_category (category_type, name, sort_order)
SELECT 'report_type', '专题报告', 2 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_category WHERE category_type = 'report_type' AND name = '专题报告');
INSERT INTO research_category (category_type, name, sort_order)
SELECT 'report_type', '数据洞察', 3 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_category WHERE category_type = 'report_type' AND name = '数据洞察');
INSERT INTO research_category (category_type, name, sort_order)
SELECT 'report_type', '深度分析', 4 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_category WHERE category_type = 'report_type' AND name = '深度分析');

INSERT INTO research_category (category_type, name, sort_order)
SELECT 'industry', '人工智能', 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_category WHERE category_type = 'industry' AND name = '人工智能');
INSERT INTO research_category (category_type, name, sort_order)
SELECT 'industry', '医疗健康', 2 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_category WHERE category_type = 'industry' AND name = '医疗健康');
INSERT INTO research_category (category_type, name, sort_order)
SELECT 'industry', '先进制造', 3 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_category WHERE category_type = 'industry' AND name = '先进制造');
INSERT INTO research_category (category_type, name, sort_order)
SELECT 'industry', '新能源', 4 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_category WHERE category_type = 'industry' AND name = '新能源');
INSERT INTO research_category (category_type, name, sort_order)
SELECT 'industry', '消费电商', 5 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_category WHERE category_type = 'industry' AND name = '消费电商');
INSERT INTO research_category (category_type, name, sort_order)
SELECT 'industry', '企业服务', 6 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_category WHERE category_type = 'industry' AND name = '企业服务');
INSERT INTO research_category (category_type, name, sort_order)
SELECT 'industry', '文化娱乐', 7 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_category WHERE category_type = 'industry' AND name = '文化娱乐');

INSERT INTO research_category (category_type, name, sort_order)
SELECT 'tag', '热门赛道', 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_category WHERE category_type = 'tag' AND name = '热门赛道');
INSERT INTO research_category (category_type, name, sort_order)
SELECT 'tag', '产业洞察', 2 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_category WHERE category_type = 'tag' AND name = '产业洞察');
INSERT INTO research_category (category_type, name, sort_order)
SELECT 'tag', '前沿技术', 3 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_category WHERE category_type = 'tag' AND name = '前沿技术');
INSERT INTO research_category (category_type, name, sort_order)
SELECT 'tag', '短研洞察', 4 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_category WHERE category_type = 'tag' AND name = '短研洞察');
INSERT INTO research_category (category_type, name, sort_order)
SELECT 'tag', '其他', 5 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_category WHERE category_type = 'tag' AND name = '其他');

INSERT INTO research_report (title, summary, content, report_type, industry, tags, publish_date, view_count, publish_by, status)
SELECT '中国AI应用出海：算力筑基，场景聚力——《2025年中国AI应用出海企业发展需求洞察报告》发布！',
  'GPU云夯实算力底座，为AI应用出海注入核心动能',
  '随着全球AI市场的爆发式增长，2023年全球AI市场规模已达1850亿美元，预计2027年将突破4500亿美元。中国AI应用企业凭借技术代际突破、国内场景创新经验及政策支持，加速向海外市场扩张。',
  '行业研究', '人工智能', '热门赛道,产业洞察,前沿技术', '2025-07-31', 12580, '大学生创投研究院', 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_report WHERE title LIKE '中国AI应用出海%');

INSERT INTO research_report (title, summary, content, report_type, industry, tags, publish_date, view_count, publish_by, status)
SELECT '2023年中国专精特新系列之优质中小企业进阶路径洞察报告',
  '深入分析专精特新中小企业的发展现状、面临挑战及进阶路径。',
  '专精特新中小企业是产业链供应链的关键节点。本报告从政策环境、融资能力、数字化水平与全球化布局四个维度，提出优质中小企业进阶的可行路径。',
  '专题报告', '先进制造', '短研洞察,产业洞察,其他', '2023-07-26', 8920, '大学生创投研究院', 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_report WHERE title LIKE '2023年中国专精特新系列之优质%');

INSERT INTO research_report (title, summary, content, report_type, industry, tags, publish_date, view_count, publish_by, status)
SELECT '2023年中国各省区市专精特新"小巨人"企业发展洞察报告',
  '分析全国各省市专精特新小巨人企业分布情况、发展特点及区域差异。',
  '报告对全国31个省级行政区的专精特新小巨人企业进行统计分析，呈现区域产业集群特征与差异化发展路径。',
  '数据洞察', '先进制造', '短研洞察,产业洞察,其他', '2023-07-07', 6540, '大学生创投研究院', 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_report WHERE title LIKE '2023年中国各省区市专精特新%');

INSERT INTO research_report (title, summary, content, report_type, industry, tags, publish_date, view_count, publish_by, status)
SELECT '2025年中国大模型产业发展白皮书',
  '深度剖析大模型技术发展趋势、产业应用场景及商业化路径。',
  '大模型产业正从通用能力竞赛转向行业深度落地。白皮书覆盖技术栈演进、算力成本、应用商业模式与监管趋势。',
  '深度分析', '人工智能', '热门赛道,前沿技术', '2025-06-15', 18900, '大学生创投研究院', 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_report WHERE title = '2025年中国大模型产业发展白皮书');

INSERT INTO research_report (title, summary, content, report_type, industry, tags, publish_date, view_count, publish_by, status)
SELECT '医疗AI应用场景与商业化路径研究报告',
  '探讨医疗AI在诊断、药物研发、健康管理等场景的应用现状与发展前景。',
  '医疗AI在影像辅助诊断、病理分析与临床决策支持等场景加速落地，本报告梳理头部厂商商业化进展与支付模式创新。',
  '行业研究', '医疗健康', '前沿技术,产业洞察', '2025-05-28', 11200, '大学生创投研究院', 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_report WHERE title = '医疗AI应用场景与商业化路径研究报告');

INSERT INTO research_report (title, summary, content, report_type, industry, tags, publish_date, view_count, publish_by, status)
SELECT '新能源汽车产业链投资价值分析报告',
  '分析新能源汽车产业链各环节投资机会、风险及发展趋势。',
  '从电池、电机、电控到智能驾驶与充换电网络，报告拆解新能源汽车产业链投资地图与关键风险因素。',
  '专题报告', '新能源', '热门赛道,产业洞察', '2025-05-12', 9850, '大学生创投研究院', 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM research_report WHERE title = '新能源汽车产业链投资价值分析报告');

-- 已有旧数据但缺正文时补全
UPDATE research_report
SET content = summary
WHERE (content IS NULL OR content = '')
  AND summary IS NOT NULL
  AND summary <> '';
