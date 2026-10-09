/** 后端字段 → 前端展示字段 */

import { resolveMediaUrl, resolveMediaUrls } from '@/utils/mediaUrl.js'
import {
  buildProjectCardMetaLine,
  formatProjectFoundingYear,
  resolveProjectIndustries,
  resolveProjectLocation
} from '@/utils/projectCardMeta.js'

export function formatDateTime(dateStr) {
  if (!dateStr) return ''
  return String(dateStr).replace('T', ' ').slice(0, 16)
}

export function formatRelativeTime(dateStr) {
  if (!dateStr) return ''
  const date = new Date(String(dateStr).replace(/-/g, '/'))
  const diff = Date.now() - date.getTime()
  const hour = 3600000
  const day = 86400000
  const minute = 60000
  if (diff < minute) return '刚刚'
  if (diff < hour) return `${Math.max(1, Math.floor(diff / minute))}分钟前`
  if (diff < 24 * hour) return `${Math.floor(diff / hour)}小时前`
  if (diff < 7 * day) return `${Math.floor(diff / day)}天前`
  return String(dateStr).slice(0, 10)
}

/** 融资快报列表标题：取首行并截断，不展示正文 */
export function newsListTitle(title, maxLen = 100) {
  const raw = (title || '').trim()
  if (!raw) return ''
  const firstLine = raw.split(/\r?\n/)[0].trim()
  if (firstLine.length <= maxLen) return firstLine
  return `${firstLine.slice(0, maxLen)}…`
}

export function newsTextsEqual(a, b) {
  return (a || '').trim() === (b || '').trim()
}

/** 详情正文：去掉与标题重复的首行/前缀 */
export function newsDetailBody(title, content) {
  const body = (content || '').trim()
  const head = (title || '').trim()
  if (!body) return head
  if (!head || newsTextsEqual(head, body)) return body
  if (body.startsWith(head)) {
    const rest = body.slice(head.length).replace(/^[\s:：\-—]+/, '').trimStart()
    return rest || body
  }
  const lines = body.split(/\r?\n/)
  if (lines[0].trim() === head) {
    const rest = lines.slice(1).join('\n').trimStart()
    return rest || body
  }
  return body
}

export function mapNewsItem(row) {
  const round = row.round || row.tag || ''
  return {
    id: row.id,
    type: row.newsType || row.news_type || '快讯',
    title: newsListTitle(row.title),
    author: row.source || '',
    publishTime: formatRelativeTime(row.createTime || row.create_time),
    projectName: row.projectName || row.project_name || '',
    projectId: row.projectId ?? row.project_id ?? null,
    logoUrl: resolveMediaUrl(row.logoUrl || row.logo_url || ''),
    roundTag: round,
    regionTag: row.region || ''
  }
}

export function mapProjectCard(row) {
  return {
    id: row.id,
    name: row.name,
    status: row.statusLabel || row.status_label || row.round || '',
    description: row.companyDesc || row.company_desc || '',
    tags: resolveProjectIndustries(row, 2),
    location: resolveProjectLocation(row),
    foundingYear: formatProjectFoundingYear(row),
    metaLine: buildProjectCardMetaLine(row),
    logoUrl: resolveMediaUrl(row.logoUrl || row.logo_url || row.logo || '')
  }
}

/** 企业项目库列表项（公司视角：轮次 + 描述 + 行业/地区/成立年） */
export function mapLibraryProjectItem(row) {
  return {
    id: row.id,
    name: row.name,
    round: row.round || row.statusLabel || row.status_label || row.status || '',
    companyDesc: row.companyDesc || row.company_desc || row.description || '',
    logoUrl: resolveMediaUrl(row.logoUrl || row.logo_url || row.logo || ''),
    metaLine: buildProjectCardMetaLine(row)
  }
}

/** 机构库列表项 */
export function mapInstitutionItem(row) {
  return {
    id: row.id,
    name: row.name || '',
    entityName: row.entityName || row.entity_name || '',
    instType: row.instType || row.inst_type || '',
    recentInvestment: row.recentInvestment || row.recent_investment || '',
    eventCount: row.eventCount ?? row.event_count ?? 0,
    logoUrl: resolveMediaUrl(row.logoUrl || row.logo_url || '')
  }
}

export function mapFinancingCard(row) {
  return {
    id: row.id,
    title: row.name || row.title || '',
    round: row.round || row.status || '',
    desc: row.companyDesc || row.company_desc || row.description || row.desc || ''
  }
}

export function mapRoadshowCard(row) {
  return {
    id: row.id,
    title: row.title,
    cover: resolveMediaUrl(row.coverUrl || row.cover_url || '')
  }
}

export function mapActivityCard(row) {
  const start = row.startTime || row.start_time || ''
  const status = row.status || 'ongoing'
  return {
    id: row.id,
    title: row.title,
    cover: resolveMediaUrl(row.coverUrl || row.cover_url || ''),
    location: row.location || '',
    startTime: typeof start === 'string' ? start.replace('T', ' ').slice(0, 16) : start,
    status,
    statusText: activityStatusText(status),
    participants: row.participantCount ?? row.participant_count ?? 0
  }
}

export function activityStatusText(status) {
  if (status === 'registering') return '正在报名'
  if (status === 'ended') return '已结束'
  return '进行中'
}

export function mapActivityDetail(row) {
  if (!row) return null
  const start = row.startTime || row.start_time || ''
  const status = row.status || 'ongoing'
  const coverUrl = resolveMediaUrl(row.coverUrl || row.cover_url || '')
  const extraBanners = resolveMediaUrls(row.bannerUrls || row.banner_urls || [])
  const bannerUrls = []
  if (coverUrl) bannerUrls.push(coverUrl)
  for (const url of extraBanners) {
    if (url && !bannerUrls.includes(url)) bannerUrls.push(url)
  }
  return {
    id: row.id,
    title: row.title,
    coverUrl,
    bannerUrls,
    detailImages: resolveMediaUrls(row.detailImages || row.detail_images || []),
    location: row.location || '',
    startTime: typeof start === 'string' ? start.replace('T', ' ').slice(0, 19) : start,
    endTime: formatDateTime(row.endTime || row.end_time),
    status,
    statusText: row.statusText || row.status_text || activityStatusText(status),
    priceText: row.priceText || row.price_text || '免费',
    participantCount: row.participantCount ?? row.participant_count ?? 0,
    likeCount: row.likeCount ?? row.like_count ?? 0,
    organizerName: row.organizerName || row.organizer_name || '',
    organizer: row.organizer || null,
    signedUp: !!(row.signedUp ?? row.signed_up),
    liked: !!(row.liked)
  }
}

export function mapUserInfo(row) {
  if (!row) return null
  return {
    id: row.id,
    phone: row.phone,
    nickname: row.nickname || '用户',
    avatarUrl: resolveMediaUrl(row.avatar || row.avatarUrl || ''),
    role: row.role,
    authStatus: row.authStatus || row.auth_status || 'none'
  }
}

/** 将后端分页转为前端 hasMore 结构 */
export function normalizePage(data, pageNum, pageSize, mapper) {
  const rawList = data?.list || []
  const total = Number(data?.total ?? 0)
  const list = mapper ? rawList.map(mapper) : rawList
  const hasMore = pageNum * pageSize < total
  return { list, total, hasMore }
}
