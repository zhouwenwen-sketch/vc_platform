/** 项目详情统一跳转（列表项 id / projectId 均可） */

export const PROJECT_DETAIL_PATH = '/pages/project/company/index'

export const PROJECT_ENTRY = {
  LIBRARY: 'library',
  FINANCING: 'financing',
  HOME: 'home',
  SEARCH: 'search',
  NEWS: 'news',
  COLLECTION: 'collection',
  INSTITUTION: 'institution'
}

/**
 * @param {number|string} id - projectId
 * @param {{ from?: string, anchor?: string }} [options]
 *   from: 来源模块；anchor: 详情页锚点（如 financing）
 */
export function navigateToProjectDetail(id, options = {}) {
  if (id == null || id === '') return
  const params = [`id=${id}`]
  if (options.from) params.push(`from=${encodeURIComponent(options.from)}`)
  if (options.anchor) params.push(`anchor=${encodeURIComponent(options.anchor)}`)
  uni.navigateTo({ url: `${PROJECT_DETAIL_PATH}?${params.join('&')}` })
}

/** 融资事件列表 → 详情并定位融资历史 */
export function navigateFromFinancingEvent(item) {
  const projectId = item?.projectId ?? item?.id
  navigateToProjectDetail(projectId, {
    from: PROJECT_ENTRY.FINANCING,
    anchor: 'financing'
  })
}

/** 项目库列表 → 详情 */
export function navigateFromProjectLibrary(item) {
  navigateToProjectDetail(item?.id ?? item?.projectId, {
    from: PROJECT_ENTRY.LIBRARY
  })
}
