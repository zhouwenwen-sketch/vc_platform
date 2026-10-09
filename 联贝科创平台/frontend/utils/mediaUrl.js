import { BASE_URL } from '@/utils/request.js'

/** 将 /uploads 相对路径补全为可访问的绝对地址（统一走当前 BASE_URL） */
export function resolveMediaUrl(url) {
  if (!url || typeof url !== 'string') return url || ''
  const trimmed = url.trim()
  if (!trimmed) return ''
  const base = BASE_URL.replace(/\/$/, '')
  // 无论 API 返回 localhost 还是线上域名，只要含 /uploads/ 就按当前 BASE_URL 重写
  const uploadsIdx = trimmed.indexOf('/uploads/')
  if (uploadsIdx >= 0) {
    return `${base}${trimmed.slice(uploadsIdx)}`
  }
  if (/^https?:\/\//i.test(trimmed)) return trimmed
  if (trimmed.startsWith('/uploads/')) {
    return `${base}${trimmed}`
  }
  return trimmed
}

export function resolveMediaUrls(urls) {
  if (!Array.isArray(urls)) return urls || []
  return urls.map(resolveMediaUrl)
}
