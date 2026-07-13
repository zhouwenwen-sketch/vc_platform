import { useUserStore } from '@/store/user.js'

export const LOGIN_PATH = '/pages/login/login'
export const INVESTOR_AUTH_PATH = '/pages/auth/investor/index'

const TAB_PAGE_PATHS = new Set([
  '/pages/home/index',
  '/pages/project/index',
  '/pages/activity/index',
  '/pages/mine/index'
])

/** 跳转登录页，登录成功后回到 targetUrl */
export function redirectToLogin(targetUrl, replace = false) {
  const url = `${LOGIN_PATH}?redirect=${encodeURIComponent(targetUrl)}`
  if (replace) {
    uni.redirectTo({ url })
    return
  }
  uni.navigateTo({ url })
}

/**
 * 页面级守卫（onLoad）：未登录则替换当前页为登录页
 * @returns {boolean} 是否已登录
 */
export function ensureLoggedIn(targetUrl) {
  const userStore = useUserStore()
  userStore.hydrateFromStorage()
  if (userStore.isLoggedIn) {
    return true
  }
  redirectToLogin(targetUrl, true)
  return false
}

/**
 * 入口级守卫（菜单/按钮）：未登录则打开登录页，不进入目标页
 * @returns {boolean} 是否已登录
 */
export function requireLoginAtEntry(targetUrl) {
  const userStore = useUserStore()
  userStore.hydrateFromStorage()
  if (userStore.isLoggedIn) {
    return true
  }
  redirectToLogin(targetUrl, false)
  return false
}

/** 打开需登录页面：先验登录，通过后再 navigateTo */
export function requireLogin(targetUrl) {
  if (!requireLoginAtEntry(targetUrl)) {
    return false
  }
  uni.navigateTo({ url: targetUrl })
  return true
}

/** 登录成功后跳转到 redirect 或返回上一页 */
export function navigateAfterLogin(redirectUrl) {
  if (!redirectUrl) {
    const pages = getCurrentPages()
    if (pages.length > 1) {
      uni.navigateBack()
      return
    }
    uni.switchTab({ url: '/pages/mine/index' })
    return
  }

  const url = redirectUrl.startsWith('/') ? redirectUrl : `/${redirectUrl}`
  const path = url.split('?')[0]
  if (TAB_PAGE_PATHS.has(path)) {
    uni.switchTab({ url: path })
    return
  }
  uni.redirectTo({ url })
}

/** 打开投资人认证（先登录，再校验认证状态） */
export async function openInvestorAuth() {
  if (!requireLoginAtEntry(INVESTOR_AUTH_PATH)) {
    return
  }

  const userStore = useUserStore()
  try {
    await userStore.refreshUserInfo()
  } catch (e) {
    /* ignore */
  }

  const status = userStore.userInfo?.authStatus
  if (status === 'pending') {
    uni.showToast({ title: '您已提交，请等待审核', icon: 'none' })
    return
  }
  if (status === 'approved') {
    uni.showToast({ title: '您已是认证投资人', icon: 'none' })
    return
  }

  uni.navigateTo({ url: INVESTOR_AUTH_PATH })
}

/** 投资人认证页 onLoad 守卫 */
export function guardInvestorAuthPage() {
  if (!ensureLoggedIn(INVESTOR_AUTH_PATH)) {
    return false
  }

  const userStore = useUserStore()
  userStore.hydrateFromStorage()
  const status = userStore.userInfo?.authStatus
  if (status === 'pending') {
    uni.showToast({ title: '您已提交，请等待审核', icon: 'none' })
    setTimeout(() => uni.navigateBack(), 400)
    return false
  }
  if (status === 'approved') {
    uni.showToast({ title: '您已是认证投资人', icon: 'none' })
    setTimeout(() => uni.navigateBack(), 400)
    return false
  }
  return true
}
