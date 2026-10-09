/** 列表筛选项多选：同列 OR、列间 AND */

export function toggleFilterValue(arr, value) {
  const list = Array.isArray(arr) ? arr : []
  const idx = list.indexOf(value)
  if (idx >= 0) {
    const next = [...list]
    next.splice(idx, 1)
    return next
  }
  return [...list, value]
}

export function isFilterSelected(arr, value) {
  return Array.isArray(arr) && arr.includes(value)
}

export function hasFilterSelection(arr) {
  return Array.isArray(arr) && arr.length > 0
}

export function serializeFilterValues(value) {
  if (Array.isArray(value)) {
    return value.filter(Boolean).join(',')
  }
  return value ? String(value).trim() : ''
}

export function parseFilterValues(raw) {
  if (!raw) return []
  if (Array.isArray(raw)) return raw.filter(Boolean)
  return String(raw)
    .split(',')
    .map((s) => s.trim())
    .filter(Boolean)
}
