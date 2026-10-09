<template>
  <view class="login-page">
    <view class="login-card">
      <text class="title">手机验证码登录</text>
      <text class="sub">{{ loginHint }}</text>
      <text v-if="smsMockHint" class="sub-env">{{ smsMockHint }}</text>

      <view class="field">
        <input
          v-model="phone"
          class="input"
          type="number"
          maxlength="11"
          placeholder="请输入手机号"
          placeholder-class="placeholder"
        />
      </view>

      <view class="field code-row">
        <input
          v-model="code"
          class="input code-input"
          type="number"
          maxlength="6"
          placeholder="请输入验证码"
          placeholder-class="placeholder"
        />
        <view
          class="code-btn"
          :class="{ disabled: countdown > 0 || sendingCode }"
          @click="onSendCode"
        >
          <text>{{ countdown > 0 ? `${countdown}s` : sendingCode ? '发送中' : '获取验证码' }}</text>
        </view>
      </view>

      <view class="login-btn" :class="{ loading }" @click="onLogin">
        <text>{{ loading ? '登录中...' : '登录' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad, onUnload } from '@dcloudio/uni-app'
import { sendSmsCode, fetchSmsConfig } from '@/api/user.js'
import { navigateAfterLogin } from '@/utils/authGuard.js'
import { loginByPhone, getLoginHint } from '@/utils/loginService.js'
import { isValidPhone } from '@/utils/phone.js'
import { SUCCESS_CODE } from '@/utils/request.js'

const phone = ref('')
const code = ref('')
const countdown = ref(0)
const loading = ref(false)
const sendingCode = ref(false)
const redirectUrl = ref('')
const smsMockHint = ref('')
let timer = null

const loginHint = computed(() => getLoginHint(redirectUrl.value))

onLoad((query) => {
  if (query?.agreed !== '1') {
    let url = '/pages/login/login'
    if (query?.redirect) {
      url += `?redirect=${encodeURIComponent(query.redirect)}`
    }
    uni.redirectTo({ url })
    return
  }
  if (query?.redirect) {
    redirectUrl.value = decodeRedirect(query.redirect)
  }
  loadSmsConfig()
})

function decodeRedirect(raw) {
  let url = decodeURIComponent(raw)
  if (url.includes('%')) {
    url = decodeURIComponent(url)
  }
  return url
}

async function loadSmsConfig() {
  try {
    const res = await fetchSmsConfig()
    if (res.code === SUCCESS_CODE && res.data?.mock && res.data.mockHint) {
      smsMockHint.value = res.data.mockHint
    }
  } catch (e) {
    /* 忽略，不影响登录 */
  }
}

onUnload(() => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
})

async function onSendCode() {
  if (countdown.value > 0 || sendingCode.value) return
  if (!isValidPhone(phone.value)) {
    uni.showToast({ title: '请输入正确手机号', icon: 'none' })
    return
  }
  sendingCode.value = true
  try {
    const res = await sendSmsCode(phone.value.trim())
    const msg = res?.data?.message || '验证码已发送，请注意查收短信'
    uni.showToast({ title: msg, icon: 'none' })
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value -= 1
      if (countdown.value <= 0) {
        clearInterval(timer)
        timer = null
      }
    }, 1000)
  } catch (e) {
    /* request 已 toast */
  } finally {
    sendingCode.value = false
  }
}

async function onLogin() {
  if (loading.value) return
  if (!isValidPhone(phone.value)) {
    uni.showToast({ title: '请输入正确手机号', icon: 'none' })
    return
  }
  if (!code.value) {
    uni.showToast({ title: '请输入验证码', icon: 'none' })
    return
  }
  loading.value = true
  try {
    const res = await loginByPhone(phone.value.trim(), code.value.trim())
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
.login-page {
  box-sizing: border-box;
  min-height: 100vh;
  background: #fff;
  padding: 80rpx 48rpx 48rpx;
}

.login-card {
  width: 100%;
}

.title {
  font-size: 40rpx;
  font-weight: 700;
  color: #333;
  display: block;
}

.sub {
  font-size: 26rpx;
  color: #666;
  margin-top: 12rpx;
  display: block;
  line-height: 1.5;
}

.sub-env {
  font-size: 24rpx;
  color: #999;
  margin: 8rpx 0 40rpx;
  display: block;
  line-height: 1.5;
}

.field {
  margin-top: 48rpx;
  margin-bottom: 28rpx;
}

.input {
  width: 100%;
  height: 88rpx;
  padding: 0 24rpx;
  font-size: 30rpx;
  color: #333;
  background: #f8f9fb;
  border-radius: 12rpx;
  box-sizing: border-box;
}

.placeholder {
  color: #bbb;
}

.code-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.code-input {
  flex: 1;
  min-width: 0;
}

.code-btn {
  flex: none;
  height: 88rpx;
  padding: 0 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #78B9B1;
  border-radius: 12rpx;

  text {
    font-size: 26rpx;
    color: #fff;
    white-space: nowrap;
  }

  &.disabled {
    background: #a0c4ff;
  }
}

.login-btn {
  margin-top: 40rpx;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #78B9B1;
  border-radius: 44rpx;

  text {
    font-size: 32rpx;
    color: #fff;
    font-weight: 600;
  }

  &.loading {
    opacity: 0.6;
  }
}
</style>
