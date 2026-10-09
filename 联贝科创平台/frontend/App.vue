<script setup>
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'
import { checkSession } from '@/utils/loginService.js'
import { getTabNavHeightStyle } from '@/utils/navBarHeight.js'

onLaunch(() => {
  checkSession()
  const app = getApp()
  if (app) {
    app.globalData = app.globalData || {}
    app.globalData.tabNavStyle = getTabNavHeightStyle()
  }
})

onShow(() => {})
onHide(() => {})
</script>

<style lang="scss">
@import 'uview-plus/index.scss';

page {
  height: 100%;
  background-color: #f5f6f8;
  --tab-nav-height: calc(var(--status-bar-height, 44px) + 44px);
  font-family: -apple-system, BlinkMacSystemFont, 'Helvetica Neue', Helvetica, 'PingFang SC',
    'Microsoft YaHei', sans-serif;
}
</style>
