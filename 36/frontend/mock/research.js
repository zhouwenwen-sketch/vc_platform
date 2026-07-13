/**
 * 研究院报告分类数据
 */
export const reportCategories = {
  reportTypes: [
    { id: 1, name: '行业研究', categoryType: 'report_type', sortOrder: 1 },
    { id: 2, name: '专题报告', categoryType: 'report_type', sortOrder: 2 },
    { id: 3, name: '数据洞察', categoryType: 'report_type', sortOrder: 3 },
    { id: 4, name: '深度分析', categoryType: 'report_type', sortOrder: 4 }
  ],
  industries: [
    { id: 1, name: '人工智能', categoryType: 'industry', sortOrder: 1 },
    { id: 2, name: '医疗健康', categoryType: 'industry', sortOrder: 2 },
    { id: 3, name: '先进制造', categoryType: 'industry', sortOrder: 3 },
    { id: 4, name: '新能源', categoryType: 'industry', sortOrder: 4 },
    { id: 5, name: '消费电商', categoryType: 'industry', sortOrder: 5 },
    { id: 6, name: '企业服务', categoryType: 'industry', sortOrder: 6 },
    { id: 7, name: '文化娱乐', categoryType: 'industry', sortOrder: 7 }
  ],
  tags: [
    { id: 1, name: '热门赛道', categoryType: 'tag', sortOrder: 1 },
    { id: 2, name: '产业洞察', categoryType: 'tag', sortOrder: 2 },
    { id: 3, name: '前沿技术', categoryType: 'tag', sortOrder: 3 },
    { id: 4, name: '短研洞察', categoryType: 'tag', sortOrder: 4 },
    { id: 5, name: '其他', categoryType: 'tag', sortOrder: 5 }
  ]
}

/**
 * 研究院报告列表数据（模拟276条数据中的部分）
 */
export const reportList = [
  {
    id: 1,
    title: '中国AI应用出海：算力筑基，场景聚力——《2025年中国AI应用出海企业发展需求洞察报告》发布！',
    summary: '随着全球AI市场的爆发式增长，中国AI应用企业凭借技术代际突破、国内场景创新经验及政策支持，加速向海外市场扩张。',
    reportType: '行业研究',
    industry: '人工智能',
    tags: '热门赛道,产业洞察,前沿技术',
    publishDate: '2025-07-31',
    viewCount: 12580,
    publishBy: '大学生创投研究院'
  },
  {
    id: 2,
    title: '2023年中国专精特新系列之优质中小企业进阶路径洞察报告',
    summary: '本报告深入分析专精特新中小企业的发展现状、面临挑战及进阶路径，为企业发展提供参考。',
    reportType: '专题报告',
    industry: '先进制造',
    tags: '短研洞察,产业洞察,其他',
    publishDate: '2023-07-26',
    viewCount: 8920,
    publishBy: '大学生创投研究院'
  },
  {
    id: 3,
    title: '2023年中国各省区市专精特新"小巨人"企业发展洞察报告',
    summary: '分析全国各省市专精特新"小巨人"企业分布情况、发展特点及区域差异。',
    reportType: '数据洞察',
    industry: '先进制造',
    tags: '短研洞察,产业洞察,其他',
    publishDate: '2023-07-07',
    viewCount: 6540,
    publishBy: '大学生创投研究院'
  },
  {
    id: 4,
    title: '2025年中国大模型产业发展白皮书',
    summary: '深度剖析大模型技术发展趋势、产业应用场景及商业化路径。',
    reportType: '深度分析',
    industry: '人工智能',
    tags: '热门赛道,前沿技术',
    publishDate: '2025-06-15',
    viewCount: 18900,
    publishBy: '大学生创投研究院'
  },
  {
    id: 5,
    title: '医疗AI应用场景与商业化路径研究报告',
    summary: '探讨医疗AI在诊断、药物研发、健康管理等场景的应用现状与发展前景。',
    reportType: '行业研究',
    industry: '医疗健康',
    tags: '前沿技术,产业洞察',
    publishDate: '2025-05-28',
    viewCount: 11200,
    publishBy: '大学生创投研究院'
  },
  {
    id: 6,
    title: '新能源汽车产业链投资价值分析报告',
    summary: '分析新能源汽车产业链各环节投资机会、风险及发展趋势。',
    reportType: '专题报告',
    industry: '新能源',
    tags: '热门赛道,产业洞察',
    publishDate: '2025-05-12',
    viewCount: 9850,
    publishBy: '大学生创投研究院'
  },
  {
    id: 7,
    title: '2025年消费电商趋势报告：直播电商与内容电商融合发展',
    summary: '研究直播电商与内容电商的融合趋势、商业模式创新及用户行为变化。',
    reportType: '行业研究',
    industry: '消费电商',
    tags: '热门赛道,产业洞察',
    publishDate: '2025-04-25',
    viewCount: 14300,
    publishBy: '大学生创投研究院'
  },
  {
    id: 8,
    title: '企业数字化转型路径与实践案例报告',
    summary: '总结企业数字化转型的成功经验、关键路径及典型案例。',
    reportType: '深度分析',
    industry: '企业服务',
    tags: '产业洞察,前沿技术',
    publishDate: '2025-04-08',
    viewCount: 7680,
    publishBy: '大学生创投研究院'
  },
  {
    id: 9,
    title: 'AIGC内容创作行业发展报告',
    summary: '分析AIGC技术在内容创作领域的应用现状、商业模式及未来趋势。',
    reportType: '数据洞察',
    industry: '文化娱乐',
    tags: '前沿技术,热门赛道',
    publishDate: '2025-03-20',
    viewCount: 16500,
    publishBy: '大学生创投研究院'
  },
  {
    id: 10,
    title: '2024年中国AI芯片市场研究报告',
    summary: '深入分析AI芯片市场格局、技术发展趋势及主要玩家竞争态势。',
    reportType: '行业研究',
    industry: '人工智能',
    tags: '前沿技术,产业洞察',
    publishDate: '2024-12-15',
    viewCount: 13200,
    publishBy: '大学生创投研究院'
  },
  {
    id: 11,
    title: '生物医药创新药研发趋势报告',
    summary: '探讨生物医药领域创新药研发的最新趋势、投资热点及挑战。',
    reportType: '专题报告',
    industry: '医疗健康',
    tags: '产业洞察,其他',
    publishDate: '2024-11-28',
    viewCount: 8900,
    publishBy: '大学生创投研究院'
  },
  {
    id: 12,
    title: '智能制造装备行业发展白皮书',
    summary: '分析智能制造装备行业的技术发展、市场规模及投资机会。',
    reportType: '深度分析',
    industry: '先进制造',
    tags: '前沿技术,产业洞察',
    publishDate: '2024-10-10',
    viewCount: 10500,
    publishBy: '大学生创投研究院'
  }
]
