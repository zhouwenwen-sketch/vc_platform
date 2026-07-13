<template>
  <view class="detail-page">
    <view class="page-nav">
      <view class="back-btn" @click="onBack">
        <u-icon name="arrow-left" color="#333" size="40rpx" />
      </view>
      <text class="nav-title">{{ navTitle }}</text>
      <view class="nav-placeholder" />
    </view>

    <scroll-view
      v-if="detail"
      scroll-y
      class="content-scroll"
      :show-scrollbar="false"
    >
      <ResearchReportDetail :detail="detail" />
    </scroll-view>

    <ArticleBottomBar
      v-if="detail"
      :liked="liked"
      @like="onBottomLike"
    />

    <view v-else-if="loading" class="loading-wrap">
      <u-loading-icon mode="circle" color="#78B9B1" size="40" />
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import ResearchReportDetail from '@/components/ResearchReportDetail/ResearchReportDetail.vue'
import ArticleBottomBar from '@/components/news/ArticleBottomBar.vue'
import { fetchReportDetail } from '@/api/research.js'
import { SUCCESS_CODE } from '@/utils/request.js'

const detail = ref(null)
const loading = ref(true)
const liked = ref(false)

const navTitle = computed(() => {
  const title = detail.value?.title || ''
  return title.length > 14 ? `${title.slice(0, 14)}...` : title
})

onLoad((query) => {
  loadDetail(query?.id)
})

function onBack() {
  uni.navigateBack({ fail: () => uni.navigateTo({ url: '/pages/research/list/index' }) })
}

async function loadDetail(id) {
  if (!id) {
    loading.value = false
    return
  }
  loading.value = true
  liked.value = false
  try {
    const res = await fetchReportDetail(id)
    if (res.code === SUCCESS_CODE) {
      detail.value = res.data
    }
  } finally {
    loading.value = false
  }
}

function onBottomLike() {
  liked.value = !liked.value
  uni.showToast({ title: liked.value ? '已点赞' : '已取消', icon: 'none' })
}
</script>

<style scoped lang="scss">
.detail-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #fff;
}

.page-nav {
  flex: none;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: calc(88rpx + env(safe-area-inset-top));
  padding: env(safe-area-inset-top) 16rpx 0;
  background: #fff;
  border-bottom: 1rpx solid #f0f0f0;
}

.back-btn,
.nav-placeholder {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-title {
  flex: 1;
  text-align: center;
  font-size: 30rpx;
  font-weight: 600;
  color: #222;
}

.content-scroll {
  flex: 1;
  min-height: 0;
  height: 0;
}

.loading-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
