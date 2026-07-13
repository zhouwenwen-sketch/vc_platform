/** 项目集封面叠字（支持 \n 换行） */
export function displayCoverTitle(item) {
  if (item?.coverTitle) {
    return String(item.coverTitle).replace(/\\n/g, '\n')
  }
  return item?.title || ''
}

/** 封面下方短标题：只展示一个行业/赛道名 */
export function displayIndustryLabel(item) {
  if (item?.industryLabel) {
    return String(item.industryLabel).trim()
  }
  const title = String(item?.title || '').trim()
  const colonMatch = title.match(/[：:]([^：:]+?)(?:专题)?$/)
  if (colonMatch) {
    return colonMatch[1].trim()
  }
  const annualMatch = title.match(/年度(.+?)(?:标杆企业|价值共创|企业榜|合集)/)
  if (annualMatch) {
    return annualMatch[1].trim()
  }
  const coverLines = displayCoverTitle(item).split('\n').map((s) => s.trim()).filter(Boolean)
  const lastLine = coverLines[coverLines.length - 1] || ''
  const annualFromCover = lastLine.match(/年度(.+?)(?:标杆企业|价值共创|企业榜|合集)?$/)
  if (annualFromCover) {
    return annualFromCover[1].trim()
  }
  if (lastLine && lastLine.length <= 12) {
    return lastLine
  }
  return String(item?.category || '').trim()
}
