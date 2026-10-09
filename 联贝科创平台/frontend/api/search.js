import { request, SUCCESS_CODE } from '@/utils/request.js'
import { fetchInstitutionLibrary } from '@/api/institution.js'
import { mapSearchProjectItem, splitByKeyword } from '@/utils/highlight.js'
import { normalizePage } from '@/utils/transform.js'

/** 首页搜索 - 实时匹配项目 */
export function fetchProjectSearch(keyword, pageNum = 1, pageSize = 20) {
  const kw = (keyword || '').trim()
  if (!kw) {
    return Promise.resolve({ code: SUCCESS_CODE, data: { list: [], total: 0, hasMore: false } })
  }

  return request({
    url: '/api/projects/search',
    data: { keyword: kw, pageNum, pageSize },
    showError: false
  }).then((res) => {
    if (res.code !== SUCCESS_CODE) return res
    const page = normalizePage(res.data, pageNum, pageSize, mapSearchProjectItem)
    return { code: SUCCESS_CODE, data: page }
  })
}

/** 首页搜索 - 实时匹配机构 */
export function fetchInstitutionSearch(keyword, pageNum = 1, pageSize = 20) {
  const kw = (keyword || '').trim()
  if (!kw) {
    return Promise.resolve({ code: SUCCESS_CODE, data: { list: [], total: 0, hasMore: false } })
  }
  return fetchInstitutionLibrary(pageNum, pageSize, { keyword: kw })
}

export { splitByKeyword, mapSearchProjectItem }
