/** 后台 CRUD 字段值中文展示（列表展示 + 表单下拉选项） */

const FIELD_VALUE_MAP = {
  status: {
    submitted: '已提交',
    pending: '待审核',
    approved: '已通过',
    rejected: '已驳回',
    registering: '报名中',
    ongoing: '进行中',
    ended: '已结束',
    success: '已成功',
    cancelled: '已取消',
    unused: '未使用',
    used: '已使用',
    expired: '已过期'
  },
  authStatus: {
    none: '未认证',
    pending: '认证审核中',
    approved: '已认证',
    rejected: '认证未通过'
  },
  authType: {
    investor: '投资人',
    entrepreneur: '创业者'
  },
  role: {
    investor: '投资人',
    entrepreneur: '创业者'
  },
  reportType: {
    latest_financing: '最新融资消息',
    no_financing: '没有新融资，但希望我们报道您的项目'
  },
  debutMedia: {
    yes: '是',
    no: '否'
  },
  needFinancing: {
    yes: '是',
    no: '否'
  },
  activityType: {
    event: '活动',
    roadshow: '路演'
  },
  joinStatus: {
    reserved: '已预约',
    signed_up: '已报名',
    attended: '已参加'
  },
  categoryType: {
    report_type: '报告类型',
    industry: '行业',
    tag: '标签'
  },
  scene: {
    mobilelogin: '登录验证码',
    login: '登录验证码'
  },
  targetType: {
    project: '项目',
    institution: '机构',
    activity: '活动',
    ma_deal: '融资并购'
  },
  contentFormat: {
    text: '纯文本',
    html: 'HTML'
  },
  amountCurrency: {
    CNY: '人民币',
    USD: '美元',
    HKD: '港元',
    EUR: '欧元',
    GBP: '英镑',
    JPY: '日元',
    INR: '卢比'
  },
  dataSource: {
    import: '导入',
    auto: '自动同步',
    manual: '手动录入'
  },
  newsType: {
    快讯: '快讯',
    文章: '文章'
  }
}

/** 同名字段在不同资源下含义不同（如 status 数值枚举） */
const RESOURCE_FIELD_VALUE_MAP = {
  project: {
    status: {
      0: '草稿',
      1: '已发布',
      2: '下架'
    }
  },
  research_report: {
    status: {
      0: '草稿',
      1: '已发布'
    }
  },
  home_banner: {
    status: {
      0: '禁用',
      1: '启用'
    }
  },
  project_collection: {
    status: {
      0: '禁用',
      1: '启用'
    }
  },
  ma_deal: {
    status: {
      0: '草稿',
      1: '已发布'
    },
    category: {
      listed_company: '上市公司',
      project_asset: '项目资产',
      fund_invest: '基金产投',
      enterprise_service: '企服'
    }
  }
}

const FORM_SELECT_OPTIONS = {
  authType: [
    { value: 'investor', label: '投资人' },
    { value: 'entrepreneur', label: '创业者' }
  ],
  role: [
    { value: 'investor', label: '投资人' },
    { value: 'entrepreneur', label: '创业者' }
  ],
  authStatus: [
    { value: 'none', label: '未认证' },
    { value: 'pending', label: '认证审核中' },
    { value: 'approved', label: '已认证' },
    { value: 'rejected', label: '认证未通过' }
  ],
  joinStatus: [
    { value: 'reserved', label: '已预约' },
    { value: 'signed_up', label: '已报名' },
    { value: 'attended', label: '已参加' }
  ],
  activityType: [
    { value: 'event', label: '活动' },
    { value: 'roadshow', label: '路演' }
  ],
  targetType: [
    { value: 'project', label: '项目' },
    { value: 'institution', label: '机构' },
    { value: 'activity', label: '活动' }
  ],
  contentFormat: [
    { value: 'text', label: '纯文本' },
    { value: 'html', label: 'HTML' }
  ],
  amountCurrency: [
    { value: 'CNY', label: '人民币' },
    { value: 'USD', label: '美元' },
    { value: 'HKD', label: '港元' },
    { value: 'EUR', label: '欧元' },
    { value: 'GBP', label: '英镑' },
    { value: 'JPY', label: '日元' },
    { value: 'INR', label: '卢比' }
  ],
  dataSource: [
    { value: 'import', label: '导入' },
    { value: 'auto', label: '自动同步' },
    { value: 'manual', label: '手动录入' }
  ],
  scene: [
    { value: 'mobilelogin', label: '登录验证码' },
    { value: 'login', label: '登录验证码' }
  ],
  categoryType: [
    { value: 'report_type', label: '报告类型' },
    { value: 'industry', label: '行业' },
    { value: 'tag', label: '标签' }
  ],
  reportType: [
    { value: 'latest_financing', label: '最新融资消息' },
    { value: 'no_financing', label: '没有新融资，但希望我们报道您的项目' }
  ],
  newsType: [
    { value: '快讯', label: '快讯' },
    { value: '文章', label: '文章' }
  ]
}

const RESOURCE_STATUS_OPTIONS = {
  project_onboard_record: [
    { value: 'pending', label: '待审核' },
    { value: 'approved', label: '已通过' },
    { value: 'rejected', label: '已驳回' }
  ],
  activity_publish_request: [
    { value: 'pending', label: '待审核' },
    { value: 'approved', label: '已通过' },
    { value: 'rejected', label: '已驳回' }
  ],
  activity: [
    { value: 'registering', label: '报名中' },
    { value: 'ongoing', label: '进行中' },
    { value: 'ended', label: '已结束' }
  ],
  project: [
    { value: 0, label: '草稿' },
    { value: 1, label: '已发布' },
    { value: 2, label: '下架' }
  ],
  research_report: [
    { value: 0, label: '草稿' },
    { value: 1, label: '已发布' }
  ],
  home_banner: [
    { value: 0, label: '禁用' },
    { value: 1, label: '启用' }
  ],
  project_collection: [
    { value: 0, label: '禁用' },
    { value: 1, label: '启用' }
  ],
  connection_record: [
    { value: 'pending', label: '待处理' },
    { value: 'success', label: '已成功' },
    { value: 'cancelled', label: '已取消' }
  ],
  sms_code_record: [
    { value: 'unused', label: '未使用' },
    { value: 'used', label: '已使用' },
    { value: 'expired', label: '已过期' }
  ],
  coverage_apply_record: [
    { value: 'submitted', label: '已提交' },
    { value: 'pending', label: '待审核' },
    { value: 'approved', label: '已通过' },
    { value: 'rejected', label: '已驳回' }
  ],
  user_auth_record: [
    { value: 'pending', label: '待审核' },
    { value: 'approved', label: '已通过' },
    { value: 'rejected', label: '已驳回' }
  ],
  ma_deal: [
    { value: 0, label: '草稿' },
    { value: 1, label: '已发布' }
  ]
}

const BOOLEAN_FIELDS = new Set([
  'isFinancing',
  'isHot',
  'isCertified',
  'isLatest',
  'isCertifier',
  'isRecommended',
  'enabled'
])

const BOOLEAN_VALUE_MAP = {
  true: '是',
  false: '否',
  1: '是',
  0: '否'
}

function normalizeKey(value) {
  if (value === true || value === 'true') return 'true'
  if (value === false || value === 'false') return 'false'
  return String(value)
}

export function formatFieldValue(fieldName, value, resource) {
  if (value == null || value === '') return value

  if (BOOLEAN_FIELDS.has(fieldName) || typeof value === 'boolean') {
    const boolKey = normalizeKey(value)
    if (Object.prototype.hasOwnProperty.call(BOOLEAN_VALUE_MAP, boolKey)) {
      return BOOLEAN_VALUE_MAP[boolKey]
    }
  }

  const text = String(value)

  const resourceMap = resource && RESOURCE_FIELD_VALUE_MAP[resource]?.[fieldName]
  if (resourceMap?.[text] != null) {
    return resourceMap[text]
  }

  const map = FIELD_VALUE_MAP[fieldName]
  return map?.[text] ?? text
}

/** 编辑表单：固定枚举字段的下拉选项（value 仍为原始值） */
export function getFormSelectOptions(fieldName, resource) {
  if (fieldName === 'status') {
    return RESOURCE_STATUS_OPTIONS[resource] || null
  }
  return FORM_SELECT_OPTIONS[fieldName] || null
}

export function hasFormSelectOptions(fieldName, resource) {
  return getFormSelectOptions(fieldName, resource) != null
}
