import { request } from '@/utils/request.js'

/** 提交寻求报道 */
export function submitCoverageApply(payload) {
  return request({
    url: '/api/coverage/apply',
    method: 'POST',
    auth: true,
    data: payload,
    showError: true
  })
}

/** 检查当前用户是否已对该项目提交过报道申请 */
export function checkCoverageApplied(projectId) {
  return request({
    url: '/api/coverage/check',
    data: { projectId },
    auth: true,
    showError: false
  })
}
