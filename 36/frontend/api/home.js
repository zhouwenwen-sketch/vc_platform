import { request } from '@/utils/request.js'
import { fetchFeaturedProjects } from '@/api/project.js'

/** 首页初始化 */
export function fetchHomeInit() {
  return request({
    url: '/api/home/init',
    showError: false
  })
}

/** 首页在融项目 */
export function fetchHomeFinancingProjects() {
  return fetchFeaturedProjects(6)
}
