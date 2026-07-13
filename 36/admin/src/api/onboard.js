import request from './request'

export function approveOnboard(id) {
  return request.post(`/admin/onboard/${id}/approve`)
}

export function rejectOnboard(id, auditRemark) {
  return request.post(`/admin/onboard/${id}/reject`, { auditRemark })
}
