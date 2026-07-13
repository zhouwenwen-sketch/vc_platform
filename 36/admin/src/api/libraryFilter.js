import request from './request'

export function fetchLibraryFilterMeta() {
  return request.get('/admin/library-filters/meta')
}

export function fetchLibraryFilterOptions(scene, filterKey) {
  return request.get('/admin/library-filters', { params: { scene, filterKey } })
}

export function createLibraryFilterOption(data) {
  return request.post('/admin/library-filters', data)
}

export function updateLibraryFilterOption(id, data) {
  return request.put(`/admin/library-filters/${id}`, data)
}

export function deleteLibraryFilterOption(id) {
  return request.delete(`/admin/library-filters/${id}`)
}
