import { request, SUCCESS_CODE } from '@/utils/request.js'
import { mapActivityCard, mapActivityDetail, normalizePage } from '@/utils/transform.js'

/** 首页活动推荐：活动库 event，推荐优先，再按最新事件排序 */
export function fetchRecommendedActivities(limit = 6) {
  return request({
    url: '/api/activities/recommended',
    data: { limit },
    showError: false
  })
    .then((res) => {
      if (res.code !== SUCCESS_CODE) return res
      const list = (res.data || []).map(mapActivityCard)
      return { code: SUCCESS_CODE, data: list }
    })
    .catch(() => ({ code: 500, data: [] }))
}

/** 首页路演前 N 条 */
export function fetchFeaturedRoadshows(limit = 6) {
  return request({
    url: '/api/activities/featured',
    data: { limit },
    showError: false
  }).then((res) => {
    if (res.code !== SUCCESS_CODE) return res
    const list = (res.data || []).map(mapActivityCard)
    return { code: SUCCESS_CODE, data: list }
  })
}

/** 活动分页；status: all | ongoing | ended */
export function fetchActivityPage(pageNum = 1, pageSize = 4, status = 'all') {
  const params = { pageNum, pageSize }
  if (status && status !== 'all') {
    params.status = status
  }

  return request({
    url: '/api/activities/page',
    data: params
  }).then((res) => {
    if (res.code !== SUCCESS_CODE) return res
    const page = normalizePage(res.data, pageNum, pageSize, mapActivityCard)
    return { code: SUCCESS_CODE, data: page }
  })
}

/** 我参与的活动；tab: reserved | signed_up | attended */
export function fetchMyActivityPage({ pageNum = 1, pageSize = 10, tab = 'reserved', keyword = '' } = {}) {
  const params = { pageNum, pageSize, tab }
  if (keyword) {
    params.keyword = keyword
  }
  return request({
    url: '/api/activities/my/page',
    auth: true,
    data: params
  }).then((res) => {
    if (res.code !== SUCCESS_CODE) return res
    const page = normalizePage(res.data, pageNum, pageSize, mapActivityCard)
    return { code: SUCCESS_CODE, data: page }
  })
}

/** 活动详情 */
export function fetchActivityDetail(id) {
  return request({
    url: `/api/activities/${id}`,
    auth: false
  }).then((res) => {
    if (res.code !== SUCCESS_CODE) return res
    return { code: SUCCESS_CODE, data: mapActivityDetail(res.data) }
  })
}

/** 提交活动报名 */
export function submitActivityRegistration(id, payload) {
  return request({
    url: `/api/activities/${id}/register`,
    method: 'POST',
    auth: true,
    data: payload
  })
}

/** 提交活动发布申请 */
export function submitActivityPublish(payload) {
  return request({
    url: '/api/activities/publish',
    method: 'POST',
    auth: true,
    data: payload
  })
}
