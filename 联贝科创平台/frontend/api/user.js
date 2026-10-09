import { request, SUCCESS_CODE } from '@/utils/request.js'
import { mapUserInfo } from '@/utils/transform.js'

/** 发送验证码 */
export function sendSmsCode(mobile) {
  return request({
    url: '/api/user/sendCode',
    method: 'POST',
    data: { mobile }
  })
}

/** 短信验证码模式配置 */
export function fetchSmsConfig() {
  return request({
    url: '/api/user/sms/config',
    showError: false
  })
}

/** 微信小程序手机号一键登录 */
export function loginByWxPhone(phoneCode) {
  return request({
    url: '/api/user/wxLogin',
    method: 'POST',
    data: { phoneCode }
  }).then((res) => {
    if (res.code === SUCCESS_CODE && res.data?.userInfo) {
      res.data.userInfo = mapUserInfo(res.data.userInfo)
    }
    return res
  })
}

/** 手机号验证码登录 */
export function loginByPhone(mobile, captcha) {
  return request({
    url: '/api/user/login',
    method: 'POST',
    data: { mobile, captcha }
  }).then((res) => {
    if (res.code === SUCCESS_CODE && res.data?.userInfo) {
      res.data.userInfo = mapUserInfo(res.data.userInfo)
    }
    return res
  })
}

/** 当前用户信息 */
export function fetchUserInfo() {
  return request({
    url: '/api/user/info',
    auth: true,
    showError: false,
    skipAuthRedirect: true
  }).then((res) => {
    if (res.code === SUCCESS_CODE && res.data) {
      return { code: SUCCESS_CODE, data: mapUserInfo(res.data) }
    }
    return res
  })
}

/** 退出登录 */
export function logout() {
  return request({
    url: '/api/user/logout',
    method: 'POST',
    auth: true,
    showError: false,
    skipAuthRedirect: true
  })
}

/** 提交认证 */
export function submitAuth(payload) {
  return request({
    url: '/api/user/auth',
    method: 'POST',
    auth: true,
    data: payload
  })
}

/** 我的页菜单与认证入口 */
export function fetchMineStatic() {
  return request({
    url: '/api/user/mine/init',
    showError: false
  })
}
