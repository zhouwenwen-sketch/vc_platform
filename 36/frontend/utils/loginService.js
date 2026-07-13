import {
  loginByPhone as apiLoginByPhone,
  loginByWxPhone as apiLoginByWxPhone,
  logout as apiLogout,
  fetchUserInfo
} from '@/api/user.js'
import { useUserStore } from '@/store/user.js'
import { SUCCESS_CODE } from '@/utils/request.js'
import { setUserInfo } from '@/utils/storage.js'
import { clearSession, handleUnauthorized, isUnauthorizedResponse } from '@/utils/session.js'

function applyLoginResult(res) {
  if (res.code === SUCCESS_CODE && res.data) {
    clearSession()
    useUserStore().setLogin(res.data.token, res.data.userInfo)
  }
  return res
}

/** 手机号验证码登录 */
export async function loginByPhone(phone, code) {
  const res = await apiLoginByPhone(phone, code)
  return applyLoginResult(res)
}

/** 微信小程序手机号一键登录 */
export async function loginByWxPhone(phoneCode) {
  const res = await apiLoginByWxPhone(phoneCode)
  return applyLoginResult(res)
}

/** 退出登录 */
export async function logout() {
  try {
    await apiLogout()
  } catch (e) {
    /* 本地仍清掉登录态 */
  }
  clearSession()
  uni.showToast({ title: '已退出登录', icon: 'none' })
  setTimeout(() => {
    uni.switchTab({ url: '/pages/home/index' })
  }, 400)
}

/**
 * 启动时校验 session 是否有效
 * @param {{ redirect?: boolean }} options redirect=true 时失效则跳登录页
 */
export async function checkSession(options = {}) {
  const { redirect = false } = options
  const userStore = useUserStore()
  userStore.hydrateFromStorage()

  if (!userStore.isLoggedIn) {
    return false
  }

  try {
    const res = await fetchUserInfo()
    if (res.code === SUCCESS_CODE && res.data) {
      userStore.userInfo = res.data
      userStore.isLoggedIn = true
      setUserInfo(res.data)
      return true
    }
    if (isUnauthorizedResponse(res)) {
      clearSession()
      if (redirect) {
        handleUnauthorized(false)
      }
      return false
    }
  } catch (e) {
    if (isUnauthorizedResponse(e)) {
      clearSession()
      if (redirect) {
        handleUnauthorized(false)
      }
      return false
    }
    /* 网络异常：保留本地登录态 */
    return true
  }

  return false
}

/** 根据 redirect 路径生成登录页提示文案 */
export function getLoginHint(redirectUrl) {
  if (!redirectUrl) {
    return '登录后享受更多创投服务'
  }
  if (redirectUrl.includes('onboard')) {
    return '登录后继续完成项目入驻'
  }
  if (redirectUrl.includes('investor')) {
    return '登录后继续投资人认证'
  }
  if (redirectUrl.includes('coverage')) {
    return '登录后继续寻求报道'
  }
  if (redirectUrl.includes('project/company')) {
    return '登录后继续联系项目方'
  }
  return '登录后继续操作'
}
