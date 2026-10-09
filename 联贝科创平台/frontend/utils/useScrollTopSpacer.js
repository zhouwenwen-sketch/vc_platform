import { ref, onMounted, getCurrentInstance } from 'vue'
import { getTabNavHeightStyle } from '@/utils/navBarHeight.js'

/**
 * 首页 / 活动页：头部滚出视口后，顶部固定留白（对齐项目 Tab 默认导航栏区域高度）。
 */
export function useScrollTopSpacer(options = {}) {
  const { headerSelector = '', defaultThreshold = 120 } = options
  const showTopSpacer = ref(false)
  const scrollThreshold = ref(defaultThreshold)
  const tabNavStyle = ref(getTabNavHeightStyle())

  onMounted(() => {
    tabNavStyle.value = getTabNavHeightStyle()

    if (!headerSelector) return
    const instance = getCurrentInstance()
    uni.createSelectorQuery()
      .in(instance?.proxy)
      .select(headerSelector)
      .boundingClientRect((rect) => {
        if (rect?.height) {
          scrollThreshold.value = Math.max(0, Math.floor(rect.height * 0.85))
        }
      })
      .exec()
  })

  function onScroll(e) {
    const scrollTop = e.detail?.scrollTop ?? 0
    showTopSpacer.value = scrollTop >= scrollThreshold.value
  }

  return { showTopSpacer, onScroll, tabNavStyle }
}
