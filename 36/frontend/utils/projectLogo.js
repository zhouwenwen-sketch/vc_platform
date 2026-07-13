/** 解析项目/公司 logo 地址（忽略 mock 中的纯色 hex） */
export function resolveProjectLogoUrl(item) {
  const raw = item?.logoUrl || item?.logo || ''
  if (!raw || typeof raw !== 'string') return ''
  if (/^https?:\/\//i.test(raw) || raw.startsWith('/')) return raw
  return ''
}

/** 无 logo 占位：浅色随机渐变（同项目/公司稳定不变） */
const LOGO_FALLBACK_GRADIENTS = [
  'linear-gradient(180deg, #e87860 0%, #f8c8bc 100%)',
  'linear-gradient(180deg, #e88898 0%, #f5ccd8 100%)',
  'linear-gradient(180deg, #e8a060 0%, #f8dcc0 100%)',
  'linear-gradient(180deg, #6eb8b0 0%, #cceae6 100%)',
  'linear-gradient(180deg, #6a9fd4 0%, #c8dff0 100%)',
  'linear-gradient(180deg, #9a88d4 0%, #ddd4f0 100%)',
  'linear-gradient(180deg, #6db888 0%, #cce8d4 100%)',
  'linear-gradient(180deg, #d4a858 0%, #f0e0c0 100%)'
]

function hashString(str) {
  let hash = 0
  const text = String(str || '')
  for (let i = 0; i < text.length; i += 1) {
    hash = ((hash << 5) - hash + text.charCodeAt(i)) | 0
  }
  return Math.abs(hash)
}

export function resolveProjectLogoSeed(item, name = '') {
  if (item?.id != null && item.id !== '') return String(item.id)
  const label = name
    || item?.name
    || item?.companyName
    || item?.title
    || item?.brandName
    || ''
  return label || '项'
}

export function resolveProjectLogoFallbackGradient(seed) {
  const index = hashString(seed) % LOGO_FALLBACK_GRADIENTS.length
  return LOGO_FALLBACK_GRADIENTS[index]
}
