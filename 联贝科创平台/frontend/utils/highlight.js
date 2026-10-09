/** 按关键字拆分文本，用于搜索高亮 */
import { resolveMediaUrl } from '@/utils/mediaUrl.js'

export function splitByKeyword(text, keyword) {
  const source = text || ''
  const kw = (keyword || '').trim()
  if (!source || !kw) {
    return [{ text: source, match: false }]
  }

  const lowerSource = source.toLowerCase()
  const lowerKw = kw.toLowerCase()
  const segments = []
  let start = 0
  let idx = lowerSource.indexOf(lowerKw, start)

  while (idx !== -1) {
    if (idx > start) {
      segments.push({ text: source.slice(start, idx), match: false })
    }
    segments.push({ text: source.slice(idx, idx + kw.length), match: true })
    start = idx + kw.length
    idx = lowerSource.indexOf(lowerKw, start)
  }

  if (start < source.length) {
    segments.push({ text: source.slice(start), match: false })
  }

  return segments.length ? segments : [{ text: source, match: false }]
}

export function mapSearchProjectItem(row) {
  return {
    id: row.id,
    name: row.name || '',
    round: row.round || '未披露',
    companyDesc: row.companyDesc || row.company_desc || '',
    logoUrl: resolveMediaUrl(row.logoUrl || row.logo_url || ''),
    entityName: row.entityName || row.entity_name || ''
  }
}
