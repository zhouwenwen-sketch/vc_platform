<template>
  <view class="auth-login-page">
    <view class="brand-area">
      <text class="logo">联贝科创平台</text>
      <text class="headline">请允许联贝科创平台授权手机号</text>
      <text class="desc">获取您的手机号，仅用于注册并登录小程序</text>
    </view>

    <view class="action-area">
      <!-- #ifdef MP-WEIXIN -->
      <button
        v-if="agreed"
        class="btn btn-primary"
        open-type="getPhoneNumber"
        :loading="loading"
        @getphonenumber="onGetPhoneNumber"
      >
        手机号一键登录
      </button>
      <button v-else class="btn btn-primary" @click="onRequireAgreement">
        手机号一键登录
      </button>
      <!-- #endif -->
      <!-- #ifndef MP-WEIXIN -->
      <view class="btn btn-primary" @click="onNonWeixinTip">
        <text>手机号一键登录</text>
      </view>
      <!-- #endif -->

      <view class="btn btn-secondary" @click="onGoSmsLogin">
        <text>手机验证码登录</text>
      </view>
    </view>

    <view class="agreement-row" @click="agreed = !agreed">
      <view class="checkbox" :class="{ checked: agreed }">
        <u-icon v-if="agreed" name="checkmark" color="#fff" size="22rpx" />
      </view>
      <text class="agreement-text">
        我已阅读并同意
        <text class="link" @click.stop="onAgreementTap('user')">《联贝科创用户服务协议》</text>
        、
        <text class="link" @click.stop="onAgreementTap('privacy')">《联贝科创隐私政策》</text>
      </text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { navigateAfterLogin } from '@/utils/authGuard.js'
import { loginByWxPhone } from '@/utils/loginService.js'
import { SUCCESS_CODE } from '@/utils/request.js'

const agreed = ref(false)
const loading = ref(false)
const redirectUrl = ref('')

onLoad((query) => {
  if (query?.redirect) {
    redirectUrl.value = decodeRedirect(query.redirect)
  }
})

function decodeRedirect(raw) {
  let url = decodeURIComponent(raw)
  if (url.includes('%')) {
    url = decodeURIComponent(url)
  }
  return url
}

function onRequireAgreement() {
  uni.showToast({ title: '请先同意用户协议和隐私政策', icon: 'none' })
}

function onNonWeixinTip() {
  uni.showToast({ title: '请在微信小程序中使用一键登录', icon: 'none' })
  setTimeout(() => onGoSmsLogin(), 600)
}

function onAgreementTap(type) {
  const url =
    type === 'privacy'
      ? '/pages/legal/privacy-policy'
      : '/pages/legal/user-agreement'
  uni.navigateTo({ url })
}

function onGoSmsLogin() {
  if (!agreed.value) {
    onRequireAgreement()
    return
  }
  let url = '/pages/login/sms?agreed=1'
  if (redirectUrl.value) {
    url += `&redirect=${encodeURIComponent(redirectUrl.value)}`
  }
  uni.navigateTo({ url })
}

async function onGetPhoneNumber(e) {
  if (loading.value) return
  if (!agreed.value) {
    onRequireAgreement()
    return
  }

  const detail = e?.detail || {}
  if (detail.errMsg !== 'getPhoneNumber:ok' || !detail.code) {
    if (detail.errMsg && !detail.errMsg.includes('deny') && !detail.errMsg.includes('cancel')) {
      uni.showToast({ title: '授权失败，请重试或使用验证码登录', icon: 'none' })
    }
    return
  }

  loading.value = true
  try {
    const res = await loginByWxPhone(detail.code)
    if (res.code === SUCCESS_CODE) {
      uni.showToast({ title: '登录成功', icon: 'success' })
      setTimeout(() => navigateAfterLogin(redirectUrl.value), 500)
    }
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.auth-login-page {
  box-sizing: border-box;
  min-height: 100vh;
  background: #fff;
  padding: 120rpx 48rpx 48rpx;
  display: flex;
  flex-direction: column;
}

.brand-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 80rpx;
}

.logo {
  font-size: 52rpx;
  font-weight: 800;
  color: #78B9B1;
  letter-spacing: 1rpx;
  text-align: center;
}

.headline {
  margin-top: 80rpx;
  font-size: 40rpx;
  font-weight: 700;
  color: #222;
  text-align: center;
}

.desc {
  margin-top: 20rpx;
  font-size: 26rpx;
  color: #999;
  text-align: center;
  line-height: 1.6;
  padding: 0 24rpx;
}

.action-area {
  margin-top: auto;
  padding-bottom: 48rpx;
}

.btn {
  width: 100%;
  height: 96rpx;
  border-radius: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  font-weight: 600;
  box-sizing: border-box;
  margin: 0;
  padding: 0;
  line-height: 96rpx;

  &::after {
    border: none;
  }
}

.btn-primary {
  background: #07c160;
  color: #fff;
  border: none;
}

.btn-secondary {
  margin-top: 28rpx;
  background: #fff;
  color: #666;
  border: 2rpx solid #e5e5e5;
}

.agreement-row {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
}

.checkbox {
  width: 32rpx;
  height: 32rpx;
  margin-top: 4rpx;
  flex-shrink: 0;
  border: 2rpx solid #ccc;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;

  &.checked {
    background: #78B9B1;
    border-color: #78B9B1;
  }
}

.agreement-text {
  flex: 1;
  font-size: 24rpx;
  color: #666;
  line-height: 1.6;
}

.link {
  color: #78B9B1;
}
</style>
