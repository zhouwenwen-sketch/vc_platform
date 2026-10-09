import { ref, onMounted, onBeforeUnmount, getCurrentInstance } from 'vue'
import { getTabNavHeightStyle } from '@/utils/navBarHeight.js'

/**
 * 首页 / 活动页：头部滚出视口后，顶部固定留白（对齐项目 Tab 默认导航栏区域高度）。
 *
 * 基于 uni.createIntersectionObserver 实现，比 scroll 事件监听方案：
 * - 异步回调，不阻塞主线程
 * - 底层映射各平台原生 API（小程序 Native / H5 IntersectionObserver）
 * - 无需手动计算阈值，避免 layout thrashing
 */
export function useScrollTopSpacer(options = {}) {
  const {
    headerSelector = '',
    scrollViewSelector = '',
    /** 头部剩余 visibleRatio（比例 0~1）时显示 spacer */
    visibleRatio = 0.15
  } = options
  const showTopSpacer = ref(false)
  const tabNavStyle = ref(getTabNavHeightStyle())

  let observer = null

  onMounted(() => {
    tabNavStyle.value = getTabNavHeightStyle()
    if (!headerSelector) return

    const instance = getCurrentInstance()

    // 创建 IntersectionObserver，监听可见比例变化
    observer = uni.createIntersectionObserver(instance?.proxy, {
      thresholds: [0, visibleRatio]
    })

    // scrollViewSelector 传了就用它作为参考容器，否则相对视口
    const target = scrollViewSelector
      ? observer.relativeTo(scrollViewSelector)
      : observer.relativeToViewport()

    target.observe(headerSelector, (res) => {
      // intersectionRatio 从 1（完全可见）→ 0（完全不可见）
      showTopSpacer.value = res.intersectionRatio < visibleRatio
    })
  })

  onBeforeUnmount(() => {
    if (observer) {
      observer.disconnect()
      observer = null
    }
  })

  /**
   * 兼容旧模板 @scroll 绑定 —— 现在由 IntersectionObserver 驱动，
   * 此函数仅防止调用方未移除绑定时报错
   */
  function onScroll() {}

  return { showTopSpacer, onScroll, tabNavStyle }
}
