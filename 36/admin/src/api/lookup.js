import request from './request'

export function lookupProjects(keyword, limit = 10) {
  return request.get('/admin/lookup/projects', {
    params: { keyword, limit }
  })
}
