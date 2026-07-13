/**

 * 统一请求封装：对接 Spring Boot 后端

 */

import { getToken } from '@/utils/storage.js'

import { handleUnauthorized, isUnauthorizedResponse } from '@/utils/session.js'

import { BASE_URL } from '@/utils/devConfig.js'



export { BASE_URL }



export const SUCCESS_CODE = 200



/**

 * @param {Object} options

 * @param {string} options.url - 以 /api 开头的路径

 * @param {string} [options.method='GET']

 * @param {Object} [options.data]

 * @param {boolean} [options.auth=false] - 是否携带 token

 * @param {boolean} [options.showError=true] - 是否 toast 错误

 * @param {boolean} [options.skipAuthRedirect=false] - 401 时不自动跳登录

 */

export function request(options) {

  const {

    url,

    method = 'GET',

    data = {},

    auth = false,

    showError = true,

    skipAuthRedirect = false

  } = options



  const header = { 'Content-Type': 'application/json' }

  const token = getToken()

  if (auth && token) {

    header.Authorization = `Bearer ${token}`

    header['X-Token'] = token

  }



  return new Promise((resolve, reject) => {

    uni.request({

      url: BASE_URL + url,

      method,

      data,

      header,

      success: (res) => {

        const body = res.data

        if (res.statusCode >= 200 && res.statusCode < 300 && body) {

          if (body.code === SUCCESS_CODE) {

            resolve(body)

            return

          }

          if (isUnauthorizedResponse(body) && !skipAuthRedirect) {

            handleUnauthorized(showError)

            reject(body)

            return

          }

          const msg = body.message || '请求失败'

          if (showError) {

            uni.showToast({ title: msg, icon: 'none' })

          }

          reject(body)

          return

        }

        if (showError) {

          uni.showToast({ title: '网络异常，请检查后端是否启动', icon: 'none' })

        }

        reject(new Error('network error'))

      },

      fail: () => {

        if (showError) {

          uni.showToast({ title: '网络异常，请检查后端是否启动', icon: 'none' })

        }

        reject(new Error('network error'))

      }

    })

  })

}


