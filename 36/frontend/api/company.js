import { request, SUCCESS_CODE } from '@/utils/request.js'

/** 企业主体模糊搜索（入驻表单） */
export function fetchCompanySearch(keyword, limit = 10) {
  const kw = (keyword || '').trim()
  if (!kw) {
    return Promise.resolve({ code: SUCCESS_CODE, data: [] })
  }

  return request({
    url: '/api/companies/search',
    data: { keyword: kw, limit },
    showError: false
  })
}

/** 校验企业主体是否在工商库中（与后端 isValidEntityName 一致） */
export async function validateEntityName(entityName) {
  const name = (entityName || '').trim()
  if (!name) return false

  const res = await fetchCompanySearch(name, 20)
  if (res.code !== SUCCESS_CODE) return false
  return (res.data || []).some((item) => item === name)
}
