import request from './request'

export function approveUserAuth(id) {
  return request.post(`/admin/user-auth/${id}/approve`)
}

export function rejectUserAuth(id, auditRemark) {
  return request.post(`/admin/user-auth/${id}/reject`, { auditRemark })
}
