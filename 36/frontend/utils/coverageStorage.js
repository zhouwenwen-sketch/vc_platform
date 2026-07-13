/** 寻求报道 Mock 本地存储（一期不落库，二期迁移至 coverage_apply_record） */

const STORAGE_KEY = 'vc_coverage_applications'

export function listCoverageApplications() {
  try {
    return uni.getStorageSync(STORAGE_KEY) || []
  } catch {
    return []
  }
}

/** 每用户每项目仅一条（含 pending / approved） */
export function hasCoverageApplication(userId, projectId) {
  if (!userId || !projectId) return false
  return listCoverageApplications().some(
    (item) => String(item.userId) === String(userId) && String(item.projectId) === String(projectId)
  )
}

export function saveCoverageApplication(record) {
  const list = listCoverageApplications()
  list.unshift({
    ...record,
    id: Date.now(),
    status: 'pending',
    createTime: new Date().toISOString()
  })
  uni.setStorageSync(STORAGE_KEY, list)
}
