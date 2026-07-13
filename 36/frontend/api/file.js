import { BASE_URL, SUCCESS_CODE } from '@/utils/request.js'
import { getToken } from '@/utils/storage.js'
import { handleUnauthorized, isUnauthorizedResponse } from '@/utils/session.js'

/**
 * 上传文件到后端
 * @param {string} filePath 本地临时路径
 * @param {string} category image / document / onboard-logo / onboard-cert / onboard-avatar
 */
export function uploadFile(filePath, category = 'image') {
  return new Promise((resolve, reject) => {
    const token = getToken()
    uni.uploadFile({
      url: `${BASE_URL}/api/files/upload`,
      filePath,
      name: 'file',
      formData: { category },
      header: {
        Authorization: token ? `Bearer ${token}` : '',
        'X-Token': token || ''
      },
      success(res) {
        try {
          const body = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
          if (body?.code === SUCCESS_CODE) {
            resolve(body)
            return
          }
          if (isUnauthorizedResponse(body)) {
            handleUnauthorized(true)
            reject(body)
            return
          }
          reject(body || { message: '上传失败' })
        } catch (e) {
          reject({ message: '上传响应解析失败' })
        }
      },
      fail(err) {
        reject(err || { message: '上传失败' })
      }
    })
  })
}
