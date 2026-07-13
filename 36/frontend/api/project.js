import { request, SUCCESS_CODE } from '@/utils/request.js'
import { serializeFilterValues } from '@/utils/filterMulti.js'
import { mapProjectCard, mapLibraryProjectItem, normalizePage } from '@/utils/transform.js'
import { buildProjectCardMetaLine } from '@/utils/projectCardMeta.js'
import { resolveMediaUrl } from '@/utils/mediaUrl.js'

/** 首页在融前 N 条 */
export function fetchFeaturedProjects(limit = 6) {
  return request({
    url: '/api/projects/featured',
    data: { limit },
    showError: false
  }).then((res) => {
    if (res.code !== SUCCESS_CODE) return res
    return { code: SUCCESS_CODE, data: (res.data || []).map(mapFinancingCardFromProject) }
  })
}

function mapFinancingCardFromProject(row) {
  return {
    id: row.id,
    title: row.name || '',
    round: row.round || row.statusLabel || '',
    desc: row.companyDesc || ''
  }
}

/** 分页项目列表（项目 Tab 热门） */
export function fetchProjectPage(pageNum = 1, pageSize = 6, filters = {}) {
  const { category, round, region } = filters
  const params = { pageNum, pageSize }
  if (category) params.category = category
  if (round) params.round = round
  if (region) params.region = region

  return request({
    url: '/api/projects/page',
    data: params
  }).then((res) => {
    if (res.code !== SUCCESS_CODE) return res
    const page = normalizePage(res.data, pageNum, pageSize, mapProjectCard)
    return { code: SUCCESS_CODE, data: page }
  })
}

/** 项目 Tab 页顶部配置 */
export function fetchProjectPageInit() {
  return request({
    url: '/api/projects/init',
    showError: false
  })
}

/** 项目详情（公司详情页） */
export function fetchProjectDetail(id) {
  return request({
    url: `/api/projects/detail/${id}`,
    showError: true
  }).then((res) => {
    if (res.code !== SUCCESS_CODE) return res
    const data = res.data || {}
    const teamMembers = (data.teamMembers || data.team_members || []).map((m) => ({
      ...m,
      avatar: resolveMediaUrl(m.avatar || m.avatarUrl || m.avatar_url || '')
    }))
    return {
      code: SUCCESS_CODE,
      data: {
        ...data,
        logoUrl: resolveMediaUrl(data.logoUrl || data.logo_url || ''),
        website: data.website || data.website_url || '',
        teamMembers
      }
    }
  })
}

/** 入驻申请 - 检查项目名称是否已存在于项目库 */
export function checkProjectNameExists(name) {
  const trimmed = (name || '').trim()
  if (!trimmed) {
    return Promise.resolve({ code: SUCCESS_CODE, data: { exists: false } })
  }
  return request({
    url: '/api/projects/check-name',
    data: { name: trimmed },
    showError: false
  })
}

/** 项目集 Tab */
export function fetchCollectionTabs() {
  return request({ url: '/api/projects/collections/tabs', showError: false })
}

/** 项目集列表 */
export function fetchProjectCollections(pageNum = 1, pageSize = 10, tab = '') {
  const params = { pageNum, pageSize }
  if (tab) params.tab = tab
  return request({
    url: '/api/projects/collections',
    data: params,
    showError: false
  }).then((res) => {
    if (res.code !== SUCCESS_CODE) return res
    const page = normalizePage(res.data, pageNum, pageSize, mapCollectionCard)
    return { code: SUCCESS_CODE, data: page }
  })
}

function mapCollectionCard(row) {
  return {
    ...row,
    cover: resolveMediaUrl(row.cover || ''),
    coverTitle: row.coverTitle || row.cover_title || ''
  }
}

/** 项目集详情 */
export function fetchProjectCollectionById(id, pageNum = 1, pageSize = 20, filters = {}) {
  const { keyword, round, industry } = filters
  const params = { pageNum, pageSize }
  if (keyword) params.keyword = keyword
  const roundValue = serializeFilterValues(round)
  const industryValue = serializeFilterValues(industry)
  if (roundValue) params.round = roundValue
  if (industryValue) params.industry = industryValue
  return request({
    url: `/api/projects/collections/${id}`,
    data: params,
    showError: true
  }).then((res) => {
    if (res.code !== SUCCESS_CODE) return res
    const d = res.data || {}
    const list = (d.list || []).map(mapCollectionProjectItem)
    return {
      code: SUCCESS_CODE,
      data: {
        id: d.id,
        title: d.title,
        cover: resolveMediaUrl(d.cover || ''),
        coverTitle: d.coverTitle || d.cover_title || '',
        badge: d.badge || '',
        projectCount: d.projectCount,
        date: d.date,
        description: d.description || d.summary || '',
        summary: d.summary || '',
        total: d.total ?? list.length,
        list,
        hasMore: d.hasMore ?? false
      }
    }
  })
}

function mapCollectionProjectItem(row) {
  return {
    id: row.id,
    name: row.name,
    round: row.round || '',
    companyDesc: row.companyDesc || row.description || '',
    logoUrl: resolveMediaUrl(row.logoUrl || ''),
    metaLine: buildProjectCardMetaLine(row)
  }
}

/** 企业项目库筛选项（行业/轮次/优势/成立时间） */
export function fetchLibraryFilterOptions() {
  return request({
    url: '/api/projects/library/filters',
    showError: false
  }).then((res) => {
    if (res.code !== SUCCESS_CODE) return res
    return { code: SUCCESS_CODE, data: res.data || {} }
  })
}

/** 企业项目库列表（筛选 + 分页） */
export function fetchProjectCollectionDetail(pageNum = 1, pageSize = 10, filters = {}) {
  return fetchProjectLibraryPage(pageNum, pageSize, filters)
}

function fetchProjectLibraryPage(pageNum = 1, pageSize = 10, filters = {}) {
  const {
    keyword,
    industry,
    region,
    regionScope,
    round,
    advantage,
    foundedYear,
    lbReport,
    financing,
    sortBy
  } = filters
  const params = { pageNum, pageSize }
  if (keyword) params.keyword = keyword
  const industryValue = serializeFilterValues(industry)
  const regionValue = serializeFilterValues(region)
  const roundValue = serializeFilterValues(round)
  const advantageValue = serializeFilterValues(advantage)
  const foundedYearValue = serializeFilterValues(foundedYear)
  const lbReportValue = serializeFilterValues(lbReport)
  if (industryValue) params.industry = industryValue
  if (regionValue) params.region = regionValue
  if (regionScope) params.regionScope = regionScope
  if (roundValue) params.round = roundValue
  if (advantageValue) params.advantage = advantageValue
  if (foundedYearValue) params.foundedYear = foundedYearValue
  if (lbReportValue) params.lbReport = lbReportValue
  if (financing) params.financing = financing
  params.sortBy = sortBy || 'recommend'

  return request({
    url: '/api/projects/collection/detail',
    data: params,
    showError: false
  }).then((res) => {
    if (res.code !== SUCCESS_CODE) return res
    const page = normalizePage(res.data, pageNum, pageSize, mapLibraryProjectItem)
    return { code: SUCCESS_CODE, data: { ...page, total: res.data.total ?? page.total } }
  })
}

/** 在融项目列表（is_financing = 1） */
export function fetchFinancingProjectPage(pageNum = 1, pageSize = 10, filters = {}) {
  return fetchProjectLibraryPage(pageNum, pageSize, { ...filters, financing: 'yes' })
}
