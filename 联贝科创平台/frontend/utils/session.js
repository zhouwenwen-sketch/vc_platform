import { useUserStore } from '@/store/user.js'
import { LOGIN_PATH } from '@/utils/authGuard.js'

let handling401 = false

/** 清除本地登录态 */
export function clearSession() {
  useUserStore().logout()
}

/**
 * Token 失效处理：清本地态并跳转登录
 * @param {boolean} showToast 是否提示
 */
export function handleUnauthorized(showToast = true) {
  if (handling401) return

  const pages = getCurrentPages()
  const current = pages[pages.length - 1]
  const route = current?.route ? `/${current.route}` : ''
  if (
    route === LOGIN_PATH
    || route.endsWith('pages/login/login')
    || route.endsWith('pages/login/sms')
  ) {
    clearSession()
    return
  }

  handling401 = true
  clearSession()

  if (showToast) {
    uni.showToast({ title: '登录已过期，请重新登录', icon: 'none' })
  }

  const redirectPath = route || '/pages/mine/index'
  const delay = showToast ? 500 : 0
  setTimeout(() => {
    handling401 = false
    uni.navigateTo({
      url: `${LOGIN_PATH}?redirect=${encodeURIComponent(redirectPath)}`
    })
  }, delay)
}

export function isUnauthorizedResponse(body) {
  return body?.code === 401
}
