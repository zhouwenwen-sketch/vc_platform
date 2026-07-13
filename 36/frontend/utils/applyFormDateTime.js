/** 表单日期时间工具（配合原生 picker，避免 scroll-view 内 u-picker 首次空白） */

export function pad2(num) {
  return String(num).padStart(2, '0')
}

export function getDaysInMonth(year, month) {
  return new Date(year, month, 0).getDate()
}

export function parseDateTimeParts(value) {
  const now = new Date()
  const fallback = {
    year: now.getFullYear(),
    month: now.getMonth() + 1,
    day: now.getDate(),
    hour: now.getHours(),
    minute: now.getMinutes()
  }
  if (!value) return fallback

  const normalized = value.trim().replace(' ', 'T')
  const date = new Date(normalized.length === 16 ? `${normalized}:00` : normalized)
  if (Number.isNaN(date.getTime())) return fallback

  return {
    year: date.getFullYear(),
    month: date.getMonth() + 1,
    day: date.getDate(),
    hour: date.getHours(),
    minute: date.getMinutes()
  }
}

export function formatDateTimeValue(parts) {
  return `${parts.year}-${pad2(parts.month)}-${pad2(parts.day)} ${pad2(parts.hour)}:${pad2(parts.minute)}:00`
}

export function formatDateTimeDisplay(value) {
  if (!value) return ''
  return value.slice(0, 16)
}

export function buildYearOptions(minYear, maxYear) {
  const years = []
  for (let y = minYear; y <= maxYear; y += 1) {
    years.push(String(y))
  }
  return years
}

export function buildMonthOptions() {
  return Array.from({ length: 12 }, (_, i) => pad2(i + 1))
}

export function buildDayOptions(year, month) {
  const count = getDaysInMonth(year, month)
  return Array.from({ length: count }, (_, i) => pad2(i + 1))
}

export function buildHourOptions() {
  return Array.from({ length: 24 }, (_, i) => pad2(i))
}

export function buildMinuteOptions() {
  return Array.from({ length: 60 }, (_, i) => pad2(i))
}

export function partsToIndexes(parts, yearOptions) {
  const yearIdx = Math.max(0, yearOptions.indexOf(String(parts.year)))
  const monthIdx = Math.max(0, parts.month - 1)
  const dayIdx = Math.max(0, parts.day - 1)
  const hourIdx = Math.max(0, parts.hour)
  const minuteIdx = Math.max(0, parts.minute)
  return [yearIdx, monthIdx, dayIdx, hourIdx, minuteIdx]
}

export function indexesToParts(indexes, columns) {
  const [yearIdx, monthIdx, dayIdx, hourIdx, minuteIdx] = indexes
  return {
    year: Number(columns[0][yearIdx] || columns[0][0]),
    month: Number(columns[1][monthIdx] || columns[1][0]),
    day: Number(columns[2][dayIdx] || columns[2][0]),
    hour: Number(columns[3][hourIdx] || columns[3][0]),
    minute: Number(columns[4][minuteIdx] || columns[4][0])
  }
}

export function buildDateTimeColumns(parts, minYear, maxYear) {
  const yearOptions = buildYearOptions(minYear, maxYear)
  const monthOptions = buildMonthOptions()
  const dayOptions = buildDayOptions(parts.year, parts.month)
  return [
    yearOptions,
    monthOptions,
    dayOptions,
    buildHourOptions(),
    buildMinuteOptions()
  ]
}
