import request from './request'

export function fetchResources() {
  return request.get('/admin/crud/resources')
}

export function fetchResourceMeta(resource) {
  return request.get(`/admin/crud/${resource}/meta`)
}

export function fetchProjectTagOptions() {
  return request.get('/admin/crud/project/tag-options')
}

export function fetchProjectBusiness(projectId) {
  return request.get(`/admin/crud/project/${projectId}/business`)
}

export function fetchPage(resource, params) {
  return request.get(`/admin/crud/${resource}`, { params })
}

export function fetchDetail(resource, id) {
  return request.get(`/admin/crud/${resource}/${id}`)
}

export function createRecord(resource, data) {
  return request.post(`/admin/crud/${resource}`, data)
}

export function updateRecord(resource, id, data) {
  return request.put(`/admin/crud/${resource}/${id}`, data)
}

export function deleteRecord(resource, id) {
  return request.delete(`/admin/crud/${resource}/${id}`)
}
