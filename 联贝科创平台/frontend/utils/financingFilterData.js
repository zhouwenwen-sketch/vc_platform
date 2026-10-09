/** 融资事件页筛选项 */



import { ESTABLISHMENT_YEAR_OPTIONS } from './projectFilterData.js'



/** 侧边栏 - 融资轮次（完整） */

export const EVENT_ROUND_OPTIONS = [

  '未融资',

  '种子轮',

  '天使轮',

  'Pre-A轮',

  'Pre-A+轮',

  'A轮',

  'A+轮',

  'Pre-B轮',

  'B轮',

  'B+轮',

  'C轮',

  'C+轮',

  'D轮',

  'D+轮',

  'E轮',

  'F轮',

  'G轮',

  'H轮',

  '股权融资',

  '战略融资',

  '定向增发',

  'Pre-IPO',

  '基石轮',

  '已上市',

  'IPO',

  '新三板',

  '已退市/私有化',

  '并购/合并',

  '其他'

]



/** 项目入驻 - 计划融资轮次 */

export const ONBOARD_SEEKING_ROUND_OPTIONS = EVENT_ROUND_OPTIONS.filter(

  (item) => item !== '未融资'

)



/** 项目入驻 - 融资金额币种 */

export const ONBOARD_CURRENCY_OPTIONS = ['人民币', '美元', '港元', '欧元']



/** 侧边栏 - 融资时间（与项目库成立时间一致） */

export const FINANCING_YEAR_OPTIONS = ESTABLISHMENT_YEAR_OPTIONS



/** 侧边栏 - 货币币种 */

export const CURRENCY_OPTIONS = [

  '人民币',

  '美元',

  '港元',

  '欧元',

  '英镑',

  '卢比',

  '日元',

  '其他'

]

