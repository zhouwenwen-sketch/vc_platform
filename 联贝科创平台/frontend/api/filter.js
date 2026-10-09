import { request, SUCCESS_CODE } from '@/utils/request.js'

/** 按页面 bundle 拉取筛选项 */
export function fetchFilterBundle(bundleName) {
  return request({
    url: `/api/filters/bundle/${bundleName}`,
    showError: false
  }).then((res) => {
    if (res.code !== SUCCESS_CODE) return res
    return { code: SUCCESS_CODE, data: res.data || {} }
  })
}
