import { request, SUCCESS_CODE } from '@/utils/request.js'
import { resolveMediaUrl } from '@/utils/mediaUrl.js'
import {
  formatDateTime,
  formatRelativeTime,
  mapNewsItem,
  normalizePage
} from '@/utils/transform.js'

function mapNewsDetail(row) {
  const next = row.nextNews || row.next_news
  const newsType = row.newsType || row.news_type || '快讯'
  return {
    id: row.id,
    title: row.title || '',
    type: newsType,
    author: row.source || '',
    summary: row.summary || '',
    content: row.content || row.summary || row.title || '',
    contentFormat: row.contentFormat || row.content_format || 'text',
    sourceUrl: row.sourceUrl || row.source_url || '',
    coverUrl: resolveMediaUrl(row.coverUrl || row.cover_url || ''),
    sourceAvatar: resolveMediaUrl(row.sourceAvatar || row.source_avatar || ''),
    attribution: row.attribution || '',
    likeCount: row.likeCount ?? row.like_count ?? 0,
    publishTime: row.publishTime || formatDateTime(row.createTime || row.create_time),
    projectId: row.projectId ?? row.project_id ?? null,
    projectName: row.projectName || row.project_name || '',
    logoUrl: resolveMediaUrl(row.logoUrl || row.logo_url || ''),
    round: row.round || row.tag || '',
    tag: row.tag || '',
    region: row.region || '',
    nextNews: next
      ? {
          id: next.id,
          title: next.title || '',
          summary: next.summary || '',
          publishTime: formatRelativeTime(next.createTime || next.create_time)
        }
      : null
  }
}

/** 分页快讯 */
export function fetchNewsPage(pageNum = 1, pageSize = 5) {
  return request({
    url: '/api/news/page',
    data: { pageNum, pageSize }
  }).then((res) => {
    if (res.code !== SUCCESS_CODE) return res
    const page = normalizePage(res.data, pageNum, pageSize, mapNewsItem)
    return { code: SUCCESS_CODE, data: page }
  })
}

/** 快讯详情（含下一篇） */
export function fetchNewsDetail(id) {
  return request({
    url: `/api/news/${id}`,
    showError: true
  }).then((res) => {
    if (res.code !== SUCCESS_CODE) return res
    return { code: SUCCESS_CODE, data: mapNewsDetail(res.data || {}) }
  })
}

function mapNewsComment(row) {
  return {
    id: row.id,
    nickname: row.nickname || '氪友',
    avatar: resolveMediaUrl(row.avatar || ''),
    content: row.content || '',
    createTime: formatRelativeTime(row.createTime || row.create_time)
  }
}

/** 资讯评论列表 */
export function fetchNewsComments(newsId, limit = 100) {
  return request({
    url: `/api/news/${newsId}/comments`,
    data: { limit },
    showError: false
  }).then((res) => {
    if (res.code !== SUCCESS_CODE) return res
    const list = (res.data || []).map(mapNewsComment)
    return { code: SUCCESS_CODE, data: list }
  })
}

/** 发表资讯评论（需登录） */
export function submitNewsComment(newsId, content) {
  return request({
    url: `/api/news/${newsId}/comments`,
    method: 'POST',
    auth: true,
    data: { content },
    showError: true
  }).then((res) => {
    if (res.code !== SUCCESS_CODE) return res
    return { code: SUCCESS_CODE, data: mapNewsComment(res.data || {}) }
  })
}
