<template>
  <view class="mine-page">
    <view class="header">
      <view class="user-row" @click="onLoginTap">
        <u-avatar
          :src="userInfo.avatarUrl"
          size="120rpx"
          :text="isLoggedIn ? '' : '创投'"
          bg-color="#4a6fa5"
        />
        <view class="user-text">
          <text v-if="!isLoggedIn" class="login-tip">点击登录/注册</text>
          <template v-else>
            <text class="nickname">{{ userInfo.nickname }}</text>
            <text class="welcome">{{ authStatusText }}</text>
          </template>
        </view>
      </view>
    </view>

    <view class="cert-card">
      <view
        v-for="(c, index) in certificationList"
        :key="c.id"
        class="cert-item"
        @click="onCertTap(c)"
      >
        <u-icon :name="c.icon" size="48rpx" :color="index === 0 ? '#d4a84b' : '#78B9B1'" />
        <text class="cert-label">{{ c.label }}</text>
        <u-icon name="arrow-right" color="#ccc" size="28rpx" />
      </view>
    </view>

    <view class="menu-card">
      <u-cell-group :border="false">
        <u-cell
          v-for="m in menuList"
          :key="m.id"
          :title="m.label"
          :is-link="true"
          :border="false"
          @click="onMenuTap(m)"
        >
          <template #icon>
            <u-icon :name="m.icon" size="40rpx" color="#666" custom-style="margin-right: 16rpx" />
          </template>
        </u-cell>
      </u-cell-group>
    </view>

    <view v-if="isLoggedIn" class="logout-wrap" @click="onLogout">
      <text class="logout-text">退出登录</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { fetchMineStatic } from '@/api/user.js'
import { useUserStore } from '@/store/user.js'
import { SUCCESS_CODE } from '@/utils/request.js'
import { openInvestorAuth, requireLoginAtEntry } from '@/utils/authGuard.js'
import { openProjectOnboard } from '@/utils/projectOnboardGuard.js'
import { logout, checkSession } from '@/utils/loginService.js'

const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)
const userInfo = computed(() => userStore.userInfo || { nickname: '', avatarUrl: '' })
const certificationList = ref([])
const menuList = ref([])

const authStatusText = computed(() => {
  const s = userInfo.value.authStatus
  const map = { none: '未认证', pending: '认证审核中', approved: '已认证', rejected: '认证未通过' }
  return map[s] || '欢迎回来'
})

async function loadMine() {
  userStore.hydrateFromStorage()
  if (userStore.isLoggedIn) {
    await checkSession()
  }
  const staticRes = await fetchMineStatic()
  if (staticRes.code === SUCCESS_CODE) {
    certificationList.value = staticRes.data.certificationList
    menuList.value = staticRes.data.menuList
  }
}

function onLoginTap() {
  if (!isLoggedIn.value) {
    uni.navigateTo({ url: '/pages/login/login?redirect=' + encodeURIComponent('/pages/mine/index') })
  }
}

function onLogout() {
  uni.showModal({
    title: '提示',
    content: '确定退出登录吗？',
    success(res) {
      if (res.confirm) {
        logout()
      }
    }
  })
}

function onCertTap(c) {
  if (c.type === 'investor') {
    openInvestorAuth()
    return
  }
  if (c.type === 'entrepreneur') {
    openProjectOnboard()
    return
  }
  uni.showToast({ title: c.label || '功能开发中', icon: 'none' })
}

function onMenuTap(m) {
  if (m.label === '关注公众号') {
    uni.showToast({ title: '请关注联贝科创公众号', icon: 'none' })
    return
  }
  if (m.path) {
    if (m.requireLogin && !requireLoginAtEntry(m.path)) {
      return
    }
    if (m.linkType === 'tab') {
      uni.switchTab({ url: m.path })
    } else {
      uni.navigateTo({ url: m.path })
    }
    return
  }
  uni.showToast({ title: m.label, icon: 'none' })
}

onShow(() => loadMine())
</script>

<style lang="scss" scoped>
@import '@/styles/theme.scss';

.mine-page {
  box-sizing: border-box;
  min-height: 100%;
  background: #f0f2f5;
  padding-bottom: calc(100rpx + env(safe-area-inset-bottom));
}

.header {
  background: $theme-header-gradient;
  padding: $profile-header-padding-top $profile-header-padding-x $profile-header-padding-bottom;
}

.user-row {
  display: flex;
  align-items: center;
  gap: 28rpx;
}

.user-text {
  flex: 1;
}

.login-tip {
  font-size: 40rpx;
  font-weight: 600;
  color: #fff;
}

.nickname {
  font-size: 36rpx;
  font-weight: 700;
  color: #fff;
  display: block;
}

.welcome {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.75);
  margin-top: 8rpx;
}

.cert-card {
  display: flex;
  background: #fff;
  margin: -60rpx 24rpx 24rpx;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.06);
}

.cert-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 36rpx 20rpx;

  &:first-child {
    border-right: 1rpx solid #eee;
  }
}

.cert-label {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.menu-card {
  margin: 0 24rpx;
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  padding: 12rpx 0;

  :deep(.u-cell__body) {
    padding: 28rpx 32rpx;
  }
}

.logout-wrap {
  margin-top: 24rpx;
  padding: 16rpx 0 0;
  text-align: center;
}

.logout-text {
  font-size: 28rpx;
  color: #999;
}
</style>

