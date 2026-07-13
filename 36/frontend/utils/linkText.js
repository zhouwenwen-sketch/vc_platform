/** 链接文案末尾箭头（小程序静态 text 内勿直接写 >，会显示为 &gt;） */
export const LINK_ARROW = '>'

/** 双箭头，如「快速创建>>」 */
export const DOUBLE_LINK_ARROW = '>>'

/** @param {string} label */
export function withLinkArrow(label) {
  return `${label} ${LINK_ARROW}`
}
