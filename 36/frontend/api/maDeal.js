import { request, SUCCESS_CODE } from '@/utils/request.js'
import { resolveMediaUrl } from '@/utils/mediaUrl.js'
import { formatRelativeTime, normalizePage } from '@/utils/transform.js'

export const MA_DEAL_TABS = [
  { value: 'all', label: '全部' },
  { value: 'listed_company', label: '上市公司' },
  { value: 'project_asset', label: '项目资产' },
  { value: 'fund_invest', label: '基金产投' },
  { value: 'enterprise_service', label: '企服' }
]

function mapMaDealItem(row) {
  const tagList = row.tagList || (row.tags ? String(row.tags).split(/[,，]/).map((t) => t.trim()).filter(Boolean) : [])
  return {
    id: row.id,
    projectNo: row.projectNo || row.project_no || '',
    brandName: row.brandName || row.brand_name || '大学生创投并购',
    title: row.title || '',
    summary: row.summary || '',
    category: row.category || '',
    categoryLabel: row.categoryLabel || row.category_label || '',
    tagList,
    dealAmountText: row.dealAmountText || row.deal_amount_text || '',
    logoUrl: resolveMediaUrl(row.logoUrl || row.logo_url || ''),
    coverUrl: resolveMediaUrl(row.coverUrl || row.cover_url || ''),
    industry: row.industry || '',
    viewCount: row.viewCount ?? row.view_count ?? 0,
    appointmentCount: row.appointmentCount ?? row.appointment_count ?? 0,
    favoriteCount: row.favoriteCount ?? row.favorite_count ?? 0,
    shareCount: row.shareCount ?? row.share_count ?? 0,
    publishTime: row.publishTime || row.publish_time || '',
    relativeTime: row.relativeTime || row.relative_time || formatRelativeTime(row.publishTime || row.publish_time)
  }
}

export function fetchMaDealPage(pageNum = 1, pageSize = 10, params = {}) {
  const { keyword, category } = params
  const data = { pageNum, pageSize }
  if (keyword) data.keyword = keyword
  if (category && category !== 'all') data.category = category

  return request({
    url: '/api/ma-deals/page',
    data,
    showError: false
  }).then((res) => {
    if (res.code !== SUCCESS_CODE) return res
    const page = normalizePage(res.data, pageNum, pageSize, mapMaDealItem)
    return { code: SUCCESS_CODE, data: page }
  })
}

export function fetchMaDealDetail(id) {
  return request({
    url: `/api/ma-deals/${id}`,
    showError: true
  }).then((res) => {
    if (res.code !== SUCCESS_CODE) return res
    const row = res.data || {}
    const mapped = mapMaDealItem(row)
    return {
      code: SUCCESS_CODE,
      data: {
        ...mapped,
        projectName: row.projectName || row.project_name || '',
        mainBusiness: row.mainBusiness || row.main_business || '',
        controllingStake: row.controllingStake || row.controlling_stake || '',
        marketValue: row.marketValue || row.market_value || '',
        revenueData: row.revenueData || row.revenue_data || '',
        netProfitData: row.netProfitData || row.net_profit_data || '',
        debtRatio: row.debtRatio || row.debt_ratio || '',
        totalAssets: row.totalAssets || row.total_assets || '',
        netAssets: row.netAssets || row.net_assets || '',
        bookFunds: row.bookFunds || row.book_funds || '',
        cooperationIntent: row.cooperationIntent || row.cooperation_intent || '',
        contactPhone: row.contactPhone || row.contact_phone || '',
        appointed: !!row.appointed
      }
    }
  })
}

export function appointMaDeal(id) {
  return request({
    url: `/api/ma-deals/${id}/appoint`,
    method: 'POST',
    auth: true,
    showError: true
  })
}

export function shareMaDeal(id) {
  return request({
    url: `/api/ma-deals/${id}/share`,
    method: 'POST',
    showError: false
  })
}
