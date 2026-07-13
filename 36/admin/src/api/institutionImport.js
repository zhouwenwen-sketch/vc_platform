import request from './request'

export function importInstitutionExcel(formData) {
  return request.post('/admin/institution-import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 120000
  })
}
