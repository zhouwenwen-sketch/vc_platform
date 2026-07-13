import { request } from '@/utils/request.js'

/** 提交项目入驻申请 */
export function submitProjectOnboard(payload) {
  return request({
    url: '/api/projects/onboard',
    method: 'POST',
    auth: true,
    data: payload
  })
}

/** 查询当前用户最新入驻申请状态 */
export function fetchOnboardStatus() {
  return request({
    url: '/api/projects/onboard/status',
    auth: true,
    showError: false
  })
}
