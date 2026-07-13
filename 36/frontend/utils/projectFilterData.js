/** 企业项目库筛选项（与产品稿对齐） */

import { CHINA_PROVINCES as REGION_PROVINCES } from './chinaRegions.js'

export const FILTER_BLUE = '#78B9B1'

/** 所属行业 */
export const INDUSTRY_OPTIONS = [
  '文化娱乐',
  '消费电商',
  '汽车出行',
  '教育',
  '金融',
  '企业服务',
  '产业升级',
  '前沿技术',
  '医疗',
  '人工智能',
  '医疗健康',
  '先进制造',
  '新能源',
  '机器人'
]

/** 融资轮次 */
export const ROUND_OPTIONS = [
  '未融资',
  '种子轮',
  '天使轮',
  'Pre-A轮',
  'A轮',
  'A+轮',
  'B轮',
  'B++轮',
  'C轮',
  '战略融资',
  '已上市',
  '未披露'
]

/** 中国省级行政区（来源：utils/chinaRegions.json） */
export const CHINA_PROVINCES = REGION_PROVINCES

export const OVERSEAS_REGIONS = ['美国', '新加坡', '日本', '英国', '德国', '其他海外']

/** 侧边栏 - 项目优势 */
export const ADVANTAGE_OPTIONS = [
  '专精特新小巨人',
  '专精特新',
  '创新型中小企业',
  '高新技术企业',
  '科技型中小企业',
  '独角兽',
  '瞪羚企业',
  '雏鹰企业'
]

/** 侧边栏 - 成立时间 / 融资时间（展开后完整列表） */
export const ESTABLISHMENT_YEAR_OPTIONS = [
  '2026年',
  '2025年',
  '2024年',
  '2023年',
  '2022年',
  '2021年',
  '2020年',
  '2019年',
  '2018年',
  '2017年',
  '2016年',
  '2015年',
  '2014年',
  '2013年',
  '2012年',
  '2011年',
  '2010年及以前'
]

/** @deprecated 使用 ESTABLISHMENT_YEAR_OPTIONS */
export const FOUNDED_YEAR_OPTIONS = ESTABLISHMENT_YEAR_OPTIONS

/** 侧边栏 - 联贝科创报道 */
export const LB_REPORT_OPTIONS = ['是', '否']

/** 侧边栏 - 正在融资 */
export const FINANCING_OPTIONS = ['是', '否']

/** 项目推荐排序 */
export const SORT_OPTIONS = [
  { label: '项目推荐', value: 'recommend' },
  { label: '最近更新', value: 'updated' },
  { label: '最新收录', value: 'newest' }
]
