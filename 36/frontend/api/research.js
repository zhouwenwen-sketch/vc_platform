import { request, SUCCESS_CODE } from '@/utils/request.js'
import { resolveMediaUrl } from '@/utils/mediaUrl.js'
import { serializeFilterValues } from '@/utils/filterMulti.js'
import { normalizePage } from '@/utils/transform.js'

function splitTags(tags) {
  if (!tags) return []
  if (Array.isArray(tags)) return tags.filter(Boolean)
  return String(tags)
    .split(/[,，]/)
    .map((t) => t.trim())
    .filter(Boolean)
}

function mapResearchReport(row) {
  const tags = row.tags || ''
  const tagList = row.tagList || splitTags(tags)
  const publisherRaw = row.publisher || {
    name: row.publishBy || row.publish_by || '联贝科创研究院',
    avatarUrl: row.publisherAvatarUrl || row.publisher_avatar_url || ''
  }
  const publisher = {
    name: publisherRaw.name || '联贝科创研究院',
    avatarUrl: resolveMediaUrl(publisherRaw.avatarUrl || publisherRaw.avatar_url || '')
  }
  return {
    id: row.id,
    title: row.title || '',
    summary: row.summary || '',
    content: row.content || '',
    contentFormat: row.contentFormat || row.content_format || 'text',
    reportType: row.reportType || row.report_type || '',
    industry: row.industry || '',
    tags,
    tagList,
    publishDate: row.publishDate || row.publish_date || '',
    publishTime: row.publishTime || row.publish_time || row.publishDate || row.publish_date || '',
    viewCount: row.viewCount ?? row.view_count ?? 0,
    likeCount: row.likeCount ?? row.like_count ?? 0,
    commentCount: row.commentCount ?? row.comment_count ?? 0,
    favoriteCount: row.favoriteCount ?? row.favorite_count ?? 0,
    publishBy: publisher.name,
    publisher
  }
}

/**
 * 获取报告列表（分页+筛选）
 */
export function fetchReportList(pageNum = 1, pageSize = 10, filters = {}) {
  const { reportType, industry, year, tag } = filters
  const params = { pageNum, pageSize }
  const reportTypeValue = serializeFilterValues(reportType)
  const industryValue = serializeFilterValues(industry)
  const yearValue = serializeFilterValues(year)
  const tagValue = serializeFilterValues(tag)
  if (reportTypeValue) params.reportType = reportTypeValue
  if (industryValue) params.industry = industryValue
  if (yearValue) params.year = yearValue
  if (tagValue) params.tag = tagValue

  return request({
    url: '/api/research/reports',
    data: params,
    showError: false
  }).then((res) => {
    if (res.code !== SUCCESS_CODE) return res
    const page = normalizePage(res.data, pageNum, pageSize, mapResearchReport)
    return { code: SUCCESS_CODE, data: page }
  })
}

/**
 * 获取报告分类选项
 */
export function fetchReportCategories() {
  return request({
    url: '/api/research/categories',
    showError: false
  })
}

/**
 * 获取报告详情（阅读量 +1）
 */
export function fetchReportDetail(id) {
  return request({
    url: `/api/research/reports/${id}`,
    showError: true
  }).then((res) => {
    if (res.code !== SUCCESS_CODE) return res
    return { code: SUCCESS_CODE, data: mapResearchReport(res.data) }
  })
}

/**
 * 获取报告总数
 */
export function fetchReportCount() {
  return request({
    url: '/api/research/reports/count',
    showError: false
  })
}
