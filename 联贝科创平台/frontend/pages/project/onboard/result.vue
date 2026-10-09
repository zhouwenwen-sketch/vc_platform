<template>
  <view class="result-page">
    <view class="result-card">
      <view class="icon-wrap">
        <u-icon
          :name="isApproved ? 'checkmark-circle-fill' : 'clock-fill'"
          :color="isApproved ? '#19be6b' : '#78B9B1'"
          size="96rpx"
        />
      </view>
      <text class="result-title">{{ isApproved ? '入驻成功' : '提交成功' }}</text>
      <text class="result-desc">{{ resultDesc }}</text>

      <view v-if="projectName" class="info-box">
        <view class="info-row">
          <text class="info-label">项目名称</text>
          <text class="info-value">{{ projectName }}</text>
        </view>
        <view v-if="applicationId" class="info-row">
          <text class="info-label">申请编号</text>
          <text class="info-value">{{ applicationId }}</text>
        </view>
        <view v-if="projectId" class="info-row">
          <text class="info-label">项目编号</text>
          <text class="info-value">{{ projectId }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">当前状态</text>
          <text class="info-value status" :class="{ approved: isApproved }">
            {{ isApproved ? '已通过' : '审核中' }}
          </text>
        </view>
      </view>

      <text class="result-tip">{{ resultTip }}</text>
    </view>

    <view class="actions">
      <u-button
        text="返回首页"
        custom-style="flex:1;height:88rpx;border-radius:48rpx"
        @click="goHome"
      />
      <u-button
        v-if="isApproved && projectId"
        type="primary"
        text="查看项目"
        custom-style="flex:1;background:#78B9B1;border-color:#78B9B1;height:88rpx;border-radius:48rpx"
        @click="goProjectDetail"
      />
      <u-button
        v-else
        type="primary"
        text="完成"
        custom-style="flex:1;background:#78B9B1;border-color:#78B9B1;height:88rpx;border-radius:48rpx"
        @click="goBack"
      />
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import {
  guardOnboardResultPage,
  loadCurrentOnboardStatus,
  PROJECT_ONBOARD_PATH
} from '@/utils/projectOnboardGuard.js'
import { navigateToProjectDetail } from '@/utils/projectNavigate.js'
import { useUserStore } from '@/store/user.js'

const projectName = ref('')
const applicationId = ref('')
const projectId = ref('')
const status = ref('pending')

const isApproved = computed(() => status.value === 'approved')
const resultDesc = computed(() =>
  isApproved.value
    ? '您的项目已通过审核并入库，可在项目库中查看'
    : '入驻申请已提交，请等待审核（预计 1-3 个工作日）'
)
const resultTip = computed(() =>
  isApproved.value
    ? '您现在可以使用寻求报道等功能。'
    : '审核结果将通过短信通知您。如有疑问，请联系平台运营。'
)

function applyStatusData(data) {
  projectName.value = data.projectName || ''
  applicationId.value = data.applicationId ? String(data.applicationId) : ''
  projectId.value = data.projectId ? String(data.projectId) : ''
  status.value = data.status || 'pending'
}

onLoad(async () => {
  if (!guardOnboardResultPage()) return

  const userStore = useUserStore()
  try {
    await userStore.refreshUserInfo()
    const data = await loadCurrentOnboardStatus()
    if (!data || data.status === 'none') {
      uni.redirectTo({ url: PROJECT_ONBOARD_PATH })
      return
    }
    if (data.userId && userStore.userInfo?.id && data.userId !== userStore.userInfo.id) {
      uni.redirectTo({ url: PROJECT_ONBOARD_PATH })
      return
    }
    applyStatusData(data)
  } catch (e) {
    uni.showToast({ title: '加载失败，请稍后重试', icon: 'none' })
    setTimeout(() => {
      uni.redirectTo({ url: PROJECT_ONBOARD_PATH })
    }, 600)
  }
})

function goHome() {
  uni.switchTab({ url: '/pages/home/index' })
}

function goProjectDetail() {
  if (!projectId.value) return
  navigateToProjectDetail(projectId.value)
}

function goBack() {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack({ delta: pages.length - 1 })
    return
  }
  uni.switchTab({ url: '/pages/mine/index' })
}
</script>

<style scoped lang="scss">
.result-page {
  min-height: 100vh;
  background: #f5f6f8;
  padding: 48rpx 32rpx;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.result-card {
  flex: 1;
  background: #fff;
  border-radius: 16rpx;
  padding: 64rpx 40rpx 48rpx;
  text-align: center;
}

.icon-wrap {
  display: flex;
  justify-content: center;
  margin-bottom: 32rpx;
}

.result-title {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  color: #222;
  margin-bottom: 16rpx;
}

.result-desc {
  display: block;
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
  margin-bottom: 40rpx;
}

.info-box {
  text-align: left;
  background: #f8f9fb;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 32rpx;
}

.info-row {
  display: flex;
  justify-content: space-between;
  gap: 24rpx;
  padding: 12rpx 0;

  &:not(:last-child) {
    border-bottom: 1rpx solid #eee;
  }
}

.info-label {
  font-size: 26rpx;
  color: #999;
  flex-shrink: 0;
}

.info-value {
  font-size: 26rpx;
  color: #333;
  text-align: right;
  word-break: break-all;

  &.status {
    color: #78B9B1;
    font-weight: 600;

    &.approved {
      color: #19be6b;
    }
  }
}

.result-tip {
  display: block;
  font-size: 24rpx;
  color: #999;
  line-height: 1.65;
}

.actions {
  display: flex;
  gap: 16rpx;
  margin-top: 32rpx;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
}
</style>
