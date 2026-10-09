/**
 * 本地存储封装
 */
const TOKEN_KEY = 'vc_token'
const USER_KEY = 'vc_user'

export function getToken() {
  return uni.getStorageSync(TOKEN_KEY) || ''
}

export function setToken(token) {
  uni.setStorageSync(TOKEN_KEY, token || '')
}

export function removeToken() {
  uni.removeStorageSync(TOKEN_KEY)
  uni.removeStorageSync(USER_KEY)
}

export function setUserInfo(user) {
  uni.setStorageSync(USER_KEY, user || null)
}

export function getUserInfo() {
  return uni.getStorageSync(USER_KEY) || null
}

export function isLoggedIn() {
  return !!getToken()
}
