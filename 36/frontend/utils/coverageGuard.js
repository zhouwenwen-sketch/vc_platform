import { useUserStore } from '@/store/user.js'
import { requireLoginAtEntry, ensureLoggedIn } from '@/utils/authGuard.js'

export const COVERAGE_APPLY_PATH = '/pages/coverage/apply/index'

/** 打开寻求报道（需先登录） */
export function openCoverageApply(projectId) {
  let url = COVERAGE_APPLY_PATH
  if (projectId != null && projectId !== '') {
    url += `?projectId=${projectId}`
  }

  if (!requireLoginAtEntry(url)) {
    return
  }

  uni.navigateTo({ url })
}

/** 寻求报道页 onLoad 守卫 */
export function guardCoveragePage() {
  return ensureLoggedIn(COVERAGE_APPLY_PATH)
}
