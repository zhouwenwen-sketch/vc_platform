/** 中国大陆手机号：1 开头，第二位 3-9，共 11 位 */
export const PHONE_REG = /^1[3-9]\d{9}$/

export function isValidPhone(phone) {
  return PHONE_REG.test(String(phone || '').trim())
}
