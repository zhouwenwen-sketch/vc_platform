import { defineStore } from 'pinia'
import { fetchUserInfo } from '@/api/user.js'
import {
  getToken,
  setToken,
  removeToken,
  getUserInfo,
  setUserInfo,
  isLoggedIn as checkLogin
} from '@/utils/storage.js'
import { SUCCESS_CODE } from '@/utils/request.js'

/**
 * 用户状态（登录态、个人信息）
 */
export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    userInfo: getUserInfo(),
    isLoggedIn: checkLogin()
  }),
  actions: {
    setLogin(token, userInfo) {
      this.token = token
      this.userInfo = userInfo
      this.isLoggedIn = true
      setToken(token)
      setUserInfo(userInfo)
    },
    logout() {
      this.token = ''
      this.userInfo = null
      this.isLoggedIn = false
      removeToken()
    },
    async refreshUserInfo() {
      if (!this.isLoggedIn && !getToken()) return
      const res = await fetchUserInfo()
      if (res.code === SUCCESS_CODE && res.data) {
        this.userInfo = res.data
        this.isLoggedIn = true
        setUserInfo(res.data)
        return
      }
      if (res.code === 401) {
        this.logout()
      }
    },
    hydrateFromStorage() {
      this.token = getToken()
      this.userInfo = getUserInfo()
      this.isLoggedIn = checkLogin()
    }
  }
})
