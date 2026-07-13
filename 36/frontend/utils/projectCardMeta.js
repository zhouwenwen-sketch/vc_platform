import regionMap from './chinaRegions.json'

const HONOR_TAG_PATTERN = /独角兽|GEI|胡润|榜单|名单|百强|TOP\s?\d|福布斯|德勤|毕马威|i\s?NDEX/i
const YEAR_TAG_PATTERN = /^(19|20)\d{2}(年)?$/

function parseList(val) {
  if (Array.isArray(val)) return val.map((t) => String(t).trim()).filter(Boolean)
  if (val == null || val === '') return []
  return String(val)
    .split(/[,，|]/)
    .map((t) => t.trim())
    .filter(Boolean)
}

function isLocationTag(tag, location) {
  if (!tag) return false
  const normalizedTag = String(tag).trim()
  const normalizedLocation = String(location || '').trim()
  if (normalizedLocation && (normalizedTag === normalizedLocation || normalizedLocation.includes(normalizedTag))) {
    return true
  }
  for (const [province, cities] of Object.entries(regionMap)) {
    if (normalizedTag === province || normalizedTag.endsWith(province)) return true
    if (Array.isArray(cities) && cities.some((city) => normalizedTag === city || normalizedTag.endsWith(city))) {
      return true
    }
  }
  return false
}

function isNonIndustryTag(tag, location) {
  if (!tag) return true
  if (YEAR_TAG_PATTERN.test(tag)) return true
  if (HONOR_TAG_PATTERN.test(tag)) return true
  if (isLocationTag(tag, location)) return true
  return false
}

function collectUniqueIndustries(max, ...sources) {
  const seen = new Set()
  const result = []
  const add = (item) => {
    const text = (item || '').trim()
    if (!text || seen.has(text)) return
    seen.add(text)
    result.push(text)
  }
  for (const source of sources) {
    for (const item of parseList(source)) {
      add(item)
      if (result.length >= max) return result
    }
  }
  return result
}

/** 项目卡片行业类型，最多 2 项 */
export function resolveProjectIndustries(row, max = 2) {
  const location = row.location || row.region || ''
  const industries = collectUniqueIndustries(
    max,
    row.category,
    row.nationalEconomyIndustry || row.national_economy_industry,
    row.strategicEmergingIndustry || row.strategic_emerging_industry,
    row.highPrecisionIndustry || row.high_precision_industry
  )
  if (industries.length >= max) return industries

  for (const tag of parseList(row.tags)) {
    if (industries.length >= max) break
    if (isNonIndustryTag(tag, location)) continue
    if (!industries.includes(tag)) industries.push(tag)
  }
  return industries.slice(0, max)
}

export function resolveProjectLocation(row) {
  return String(row.location || row.region || '').trim()
}

export function formatProjectFoundingYear(row) {
  const raw = row.foundingYear || row.founding_year || row.establishDate || row.establish_date || ''
  if (!raw) return ''
  const text = String(raw).trim()
  const matched = text.match(/(19|20)\d{2}/)
  return matched ? matched[0] : text.replace(/年$/, '')
}

/** 项目卡片底部 meta：行业(最多2) | 城市 | 成立年份 */
export function buildProjectCardMetaLine(row) {
  const industries = resolveProjectIndustries(row, 2)
  const location = resolveProjectLocation(row)
  const foundingYear = formatProjectFoundingYear(row)
  return [
    industries.length ? industries.join(' | ') : '',
    location,
    foundingYear
  ]
    .filter(Boolean)
    .join(' | ')
}
