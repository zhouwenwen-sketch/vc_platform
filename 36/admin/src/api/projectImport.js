import request from './request'

export function importProjectExcel(formData) {
  return request.post('/admin/project-import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 300000
  })
}
