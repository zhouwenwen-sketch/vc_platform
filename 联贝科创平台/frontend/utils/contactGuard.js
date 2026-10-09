import { useUserStore } from '@/store/user.js'
import { requireLoginAtEntry } from '@/utils/authGuard.js'

const CERTIFIED_ROLES = new Set(['investor', 'entrepreneur'])

/** 是否已通过投资人或创业者认证 */
export function isUserCertified(userInfo) {
  if (!userInfo) return false
  return userInfo.authStatus === 'approved' && CERTIFIED_ROLES.has(userInfo.role)
}

function buildProjectDetailUrl(projectId) {
  const base = '/pages/project/company/index'
  return projectId ? `${base}?id=${projectId}` : base
}

function promptCertification(authStatus) {
  if (authStatus === 'pending') {
    uni.showModal({
      title: '提示',
      content: '您的认证正在审核中，审核通过后即可联系项目方',
      showCancel: false,
      confirmText: '我知道了'
    })
    return
  }

  uni.showModal({
    title: '提示',
    content: '联系项目方需要先完成投资人或创业者认证',
    confirmText: '去认证',
    cancelText: '取消',
    success(res) {
      if (res.confirm) {
        uni.switchTab({ url: '/pages/mine/index' })
      }
    }
  })
}

/**
 * 打开「我要联系」（需先登录，且已认证投资人或创业者）
 * @param {number|string} [projectId]
 * @returns {Promise<boolean>} 是否通过校验
 */
export async function openProjectContact(projectId) {
  const targetUrl = buildProjectDetailUrl(projectId)

  if (!requireLoginAtEntry(targetUrl)) {
    return false
  }

  const userStore = useUserStore()
  try {
    await userStore.refreshUserInfo()
  } catch (e) {
    /* ignore */
  }

  if (!isUserCertified(userStore.userInfo)) {
    promptCertification(userStore.userInfo?.authStatus)
    return false
  }

  uni.showToast({ title: '联系功能开发中', icon: 'none' })
  return true
}
