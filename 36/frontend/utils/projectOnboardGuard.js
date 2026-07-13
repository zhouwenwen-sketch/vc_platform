import { useUserStore } from '@/store/user.js'
import { fetchOnboardStatus } from '@/api/onboard.js'
import { SUCCESS_CODE } from '@/utils/request.js'
import { requireLoginAtEntry, ensureLoggedIn } from '@/utils/authGuard.js'

export const PROJECT_ONBOARD_PATH = '/pages/project/onboard/index'
export const PROJECT_ONBOARD_RESULT_PATH = '/pages/project/onboard/result'

export const ONBOARD_DUPLICATE_MODAL_CONTENT =
  '您选择的项目已被联贝科创收录，无需重复创建。'

/** 已收录项目被选中时的提示（不跳转） */
export function showOnboardDuplicateModal() {
  return new Promise((resolve) => {
    uni.showModal({
      title: '提示',
      content: ONBOARD_DUPLICATE_MODAL_CONTENT,
      showCancel: false,
      confirmText: '确定',
      success: () => resolve(true),
      fail: () => resolve(false)
    })
  })
}

function goOnboardResult() {
  uni.navigateTo({ url: PROJECT_ONBOARD_RESULT_PATH })
}

/** 查询当前登录用户的入驻申请状态 */
export async function loadCurrentOnboardStatus() {
  const res = await fetchOnboardStatus()
  if (res.code !== SUCCESS_CODE) {
    return null
  }
  return res.data || null
}

function shouldRedirectToResult(data) {
  if (!data || data.status === 'none') {
    return false
  }
  const userStore = useUserStore()
  if (data.userId && userStore.userInfo?.id && data.userId !== userStore.userInfo.id) {
    return false
  }
  return data.status === 'pending' || data.status === 'approved'
}

async function checkOnboardApplicationStatus() {
  try {
    const data = await loadCurrentOnboardStatus()
    if (shouldRedirectToResult(data)) {
      goOnboardResult()
      return true
    }
  } catch (e) {
    /* ignore */
  }
  return false
}

/** 打开项目入驻（必须先登录） */
export async function openProjectOnboard() {
  if (!requireLoginAtEntry(PROJECT_ONBOARD_PATH)) {
    return
  }

  const userStore = useUserStore()
  try {
    await userStore.refreshUserInfo()
  } catch (e) {
    /* ignore */
  }

  if (await checkOnboardApplicationStatus()) {
    return
  }

  uni.navigateTo({ url: PROJECT_ONBOARD_PATH })
}

/** 项目入驻页 onLoad 守卫 */
export async function guardOnboardPage() {
  if (!ensureLoggedIn(PROJECT_ONBOARD_PATH)) {
    return false
  }

  const userStore = useUserStore()
  userStore.hydrateFromStorage()
  try {
    await userStore.refreshUserInfo()
  } catch (e) {
    /* ignore */
  }

  if (await checkOnboardApplicationStatus()) {
    return false
  }
  return true
}

/** 入驻结果页 onLoad 守卫 */
export function guardOnboardResultPage() {
  return ensureLoggedIn(PROJECT_ONBOARD_RESULT_PATH)
}

export { goOnboardResult }
