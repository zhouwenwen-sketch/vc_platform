import request from './request'

export function uploadFile(file, category = 'banner') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('category', category)
  return request.post('/admin/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
