import { defineStore } from 'pinia'
import { ref } from 'vue'
import { fetchMe, login as loginApi, logout as logoutApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const profile = ref(null)
  const menus = ref([])

  async function login(form) {
    const data = await loginApi(form)
    token.value = data.token
    localStorage.setItem('admin_token', data.token)
    profile.value = data
    menus.value = data.menus || []
    return data
  }

  async function loadProfile() {
    const data = await fetchMe()
    profile.value = data
    menus.value = data.menus || []
    return data
  }

  async function logout() {
    try {
      await logoutApi()
    } finally {
      token.value = ''
      profile.value = null
      menus.value = []
      localStorage.removeItem('admin_token')
    }
  }

  function hasPermission(code) {
    if (profile.value?.roleCode === 'super_admin') return true
    return profile.value?.permissions?.includes(code)
  }

  return { token, profile, menus, login, loadProfile, logout, hasPermission }
})
