<template>
  <view class="detail-page">
    <scroll-view
      v-if="detail"
      scroll-y
      class="content-scroll"
      :show-scrollbar="false"
    >
      <ActivityDetailHeader :detail="detail" />
      <ActivityDetailImages :images="detail.detailImages" />
      <ActivityOrganizerCard :organizer="detail.organizer" @click="onOrganizerTap" />

      <view class="recommend-section">
        <view class="tab-row">
          <text
            class="tab-item"
            :class="{ active: activeTab === 'demand' }"
            @click="activeTab = 'demand'"
          >推荐需求</text>
          <text
            class="tab-item"
            :class="{ active: activeTab === 'partner' }"
            @click="activeTab = 'partner'"
          >推荐业务伙伴</text>
        </view>
        <view class="tab-empty">
          <text>{{ activeTab === 'demand' ? '暂无推荐需求' : '暂无推荐业务伙伴' }}</text>
        </view>
      </view>

      <view class="scroll-bottom" />
    </scroll-view>

    <ActivityBottomBar
      v-if="detail"
      :like-count="likeCount"
      :liked="liked"
      :btn-text="registerBtnText"
      :disabled="registerDisabled"
      @like="onLike"
      @favorite="onFavorite"
      @share="onShare"
      @register="onRegister"
    />

    <view v-else-if="loading" class="loading-wrap">
      <u-loading-icon mode="circle" color="#78B9B1" size="40" />
    </view>

    <view v-else class="empty-wrap">
      <u-empty text="活动加载失败" mode="data" />
      <view class="retry-btn" @click="loadDetail(activityId)">重新加载</view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad, onShareAppMessage } from '@dcloudio/uni-app'
import ActivityDetailHeader from '@/components/ActivityDetail/ActivityDetailHeader.vue'
import ActivityDetailImages from '@/components/ActivityDetail/ActivityDetailImages.vue'
import ActivityOrganizerCard from '@/components/ActivityDetail/ActivityOrganizerCard.vue'
import ActivityBottomBar from '@/components/ActivityBottomBar/ActivityBottomBar.vue'
import { fetchActivityDetail } from '@/api/activity.js'
import { requireLoginAtEntry } from '@/utils/authGuard.js'
import { SUCCESS_CODE } from '@/utils/request.js'

const detail = ref(null)
const loading = ref(true)
const liked = ref(false)
const likeCount = ref(0)
const activityId = ref('')
const activeTab = ref('demand')

const registerBtnText = computed(() => {
  if (!detail.value) return '立即报名'
  if (detail.value.signedUp) return '已报名'
  if (detail.value.status === 'ended') return '已结束'
  if (detail.value.status !== 'registering') return '暂未开放'
  return '立即报名'
})

const registerDisabled = computed(() => {
  if (!detail.value) return true
  return (
    detail.value.signedUp
    || detail.value.status === 'ended'
    || detail.value.status !== 'registering'
  )
})

onLoad((query) => {
  activityId.value = query?.id || ''
  loadDetail(activityId.value)
})

onShareAppMessage(() => ({
  title: detail.value?.title || '活动详情',
  path: `/pages/activity/detail/index?id=${activityId.value}`
}))

async function loadDetail(id) {
  if (!id) {
    loading.value = false
    return
  }
  loading.value = true
  liked.value = false
  try {
    const res = await fetchActivityDetail(id)
    if (res.code === SUCCESS_CODE) {
      detail.value = res.data
      likeCount.value = res.data.likeCount || 0
    } else {
      uni.showToast({ title: res.message || '加载失败', icon: 'none' })
    }
  } catch (e) {
    uni.showToast({ title: '加载失败，请检查网络', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function onLike() {
  liked.value = !liked.value
  likeCount.value += liked.value ? 1 : -1
}

function onFavorite() {
  uni.showToast({ title: '收藏功能敬请期待', icon: 'none' })
}

function onShare() {
  uni.showToast({ title: '请点击右上角分享', icon: 'none' })
}

function onRegister() {
  if (registerDisabled.value || !activityId.value) return
  const registerUrl = `/pages/activity/register/index?id=${activityId.value}`
  if (!requireLoginAtEntry(registerUrl)) return
  uni.navigateTo({ url: registerUrl })
}

function onOrganizerTap(org) {
  if (!org?.id) return
  uni.navigateTo({ url: `/pages/institution/detail/index?id=${org.id}` })
}
</script>

<style scoped lang="scss">
.detail-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background: #f5f6f8;
}

.content-scroll {
  flex: 1;
  min-height: 0;
  height: 0;
}

.recommend-section {
  margin-top: 16rpx;
  background: #fff;
  padding: 0 24rpx 32rpx;
}

.tab-row {
  display: flex;
  gap: 40rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.tab-item {
  padding: 24rpx 0 20rpx;
  font-size: 28rpx;
  color: #999;

  &.active {
    color: #78B9B1;
    font-weight: 600;
    border-bottom: 4rpx solid #78B9B1;
    margin-bottom: -1rpx;
  }
}

.tab-empty {
  padding: 60rpx 0;
  text-align: center;
  font-size: 26rpx;
  color: #bbb;
}

.scroll-bottom {
  height: 16rpx;
}

.loading-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-wrap {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 24rpx;
}

.retry-btn {
  padding: 16rpx 48rpx;
  background: #78B9B1;
  color: #fff;
  font-size: 28rpx;
  border-radius: 32rpx;
}
</style>
