import request from './request'

export function login(data) {
  return request.post('/admin/auth/login', data)
}

export function fetchMe() {
  return request.get('/admin/auth/me')
}

export function logout() {
  return request.post('/admin/auth/logout')
}
