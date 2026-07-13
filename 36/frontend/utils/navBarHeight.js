/**
 * Tab 页顶部留白高度（与项目页 system 默认 navigationBar 一致）
 * 微信默认导航栏 = statusBarHeight + 标题栏( iOS 44 / Android 48 )
 */
export function getTabNavBarHeight() {
  const sys = uni.getSystemInfoSync()
  const platform = (sys.platform || sys.osName || '').toLowerCase()
  const titleBarHeight = platform.includes('android') ? 48 : 44
  return (sys.statusBarHeight || 44) + titleBarHeight
}

export function getTabNavHeightStyle() {
  return { '--tab-nav-height': `${getTabNavBarHeight()}px` }
}

/** @deprecated 使用 getTabNavBarHeight */
export function getDefaultNavBarHeight() {
  return getTabNavBarHeight()
}
