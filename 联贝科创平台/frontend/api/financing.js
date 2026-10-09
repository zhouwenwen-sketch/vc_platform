import { request, SUCCESS_CODE } from '@/utils/request.js'
import { resolveMediaUrl } from '@/utils/mediaUrl.js'
import { serializeFilterValues } from '@/utils/filterMulti.js'
import { normalizePage } from '@/utils/transform.js'

function mapFinancingEvent(row) {
  const projectId = row.projectId ?? row.project_id ?? row.id
  return {
    id: row.id,
    projectId,
    companyName: row.companyName || row.company_name || '',
    logoUrl: resolveMediaUrl(row.logoUrl || row.logo_url || ''),
    description: row.description || '',
    amount: row.amount || '未透露',
    round: row.round || '',
    investors: row.investors || '-',
    date: row.date || ''
  }
}

export { mapFinancingEvent }

/** 首页融资事件预览 */
export function fetchFeaturedFinancingEvents(limit = 6) {
  return request({
    url: '/api/financing-events/featured',
    data: { limit },
    showError: false
  }).then((res) => {
    if (res.code !== SUCCESS_CODE) return res
    const list = (res.data || []).map(mapFinancingEvent)
    return { code: SUCCESS_CODE, data: list }
  })
}

/** 融资事件列表（筛选 + 分页） */
export function fetchFinancingEvents(pageNum = 1, pageSize = 10, filters = {}) {
  const {
    industry,
    region,
    regionScope,
    round,
    financingYear,
    currency
  } = filters
  const params = { pageNum, pageSize }
  const industryValue = serializeFilterValues(industry)
  const regionValue = serializeFilterValues(region)
  const roundValue = serializeFilterValues(round)
  const financingYearValue = serializeFilterValues(financingYear)
  const currencyValue = serializeFilterValues(currency)
  if (industryValue) params.industry = industryValue
  if (regionValue) params.region = regionValue
  if (regionScope) params.regionScope = regionScope
  if (roundValue) params.round = roundValue
  if (financingYearValue) params.financingYear = financingYearValue
  if (currencyValue) params.currency = currencyValue

  return request({
    url: '/api/financing-events/page',
    data: params,
    showError: false
  }).then((res) => {
    if (res.code !== SUCCESS_CODE) return res
    const page = normalizePage(res.data, pageNum, pageSize, mapFinancingEvent)
    return { code: SUCCESS_CODE, data: { ...page, total: res.data.total ?? page.total } }
  })
}
