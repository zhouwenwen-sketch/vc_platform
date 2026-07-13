import request from './request'

export function fetchAdminUsers(params) {
  return request.get('/admin/users', { params })
}

export function createAdminUser(data) {
  return request.post('/admin/users', data)
}

export function updateAdminUser(id, data) {
  return request.put(`/admin/users/${id}`, data)
}

export function deleteAdminUser(id) {
  return request.delete(`/admin/users/${id}`)
}

export function fetchRoles(params) {
  return request.get('/admin/roles', { params })
}

export function fetchRoleDetail(id) {
  return request.get(`/admin/roles/${id}`)
}

export function fetchRoleOptions() {
  return request.get('/admin/roles/options')
}

export function fetchAllPermissions() {
  return request.get('/admin/roles/permissions/all')
}

export function createRole(data) {
  return request.post('/admin/roles', data)
}

export function updateRole(id, data) {
  return request.put(`/admin/roles/${id}`, data)
}

export function deleteRole(id) {
  return request.delete(`/admin/roles/${id}`)
}
