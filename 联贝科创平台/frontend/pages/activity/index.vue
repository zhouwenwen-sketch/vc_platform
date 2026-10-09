<template>
  <view class="activity-page">
    <scroll-view
      scroll-y
      class="page-scroll"
      :show-scrollbar="false"
    >
      <view class="page-head">
        <text class="head-title">创投活动</text>
        <text class="head-sub">连接创业者与投资人的优质活动</text>
      </view>

      <!-- 状态筛选 -->
      <view class="filter-tabs">
        <view
          v-for="tab in statusTabs"
          :key="tab.value"
          class="filter-item"
          :class="{ active: currentStatus === tab.value }"
          @click="onStatusChange(tab.value)"
        >
          {{ tab.label }}
        </view>
      </view>

      <view class="list-scroll">
        <ActivityCard v-for="a in displayList" :key="a.id" :item="a" @click="onActivityTap" />
        <u-loadmore
          v-if="displayList.length"
          :status="loadStatus"
          margin-top="16"
          margin-bottom="40"
        />
        <u-empty v-if="!loading && displayList.length === 0" text="暂无活动" mode="list" />
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import ActivityCard from '@/components/ActivityCard/ActivityCard.vue'
import { fetchActivityPage } from '@/api/activity.js'
import { SUCCESS_CODE } from '@/utils/request.js'

const statusTabs = [
  { label: '全部', value: 'all' },
  { label: '进行中', value: 'ongoing' },
  { label: '已结束', value: 'ended' }
]

const FETCH_PAGE_SIZE = 50

const currentStatus = ref('all')
const displayList = ref([])
const loadStatus = ref('loading')
const loading = ref(false)

async function loadList() {
  loading.value = true
  loadStatus.value = 'loading'
  displayList.value = []

  let pageNum = 1
  let all = []
  let hasMore = true

  while (hasMore) {
    const res = await fetchActivityPage(pageNum, FETCH_PAGE_SIZE, currentStatus.value)
    if (res.code !== SUCCESS_CODE) break
    all = all.concat(res.data.list || [])
    hasMore = res.data.hasMore
    pageNum += 1
  }

  displayList.value = all
  loading.value = false
  loadStatus.value = 'nomore'
}

function onStatusChange(status) {
  if (currentStatus.value === status) return
  currentStatus.value = status
  loadList()
}

function onActivityTap(item) {
  uni.navigateTo({ url: `/pages/activity/detail/index?id=${item.id}` })
}

onMounted(() => loadList())
</script>

<style lang="scss" scoped>
@import '@/styles/theme.scss';

.activity-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #f5f6f8;
}

.page-scroll {
  flex: 1;
  height: 0;
}

.page-head {
  background: $theme-hero-gradient;
  padding: $hero-body-padding-top $hero-padding-x $hero-padding-bottom;
}

.head-title {
  font-size: 40rpx;
  font-weight: 700;
  color: #fff;
  display: block;
}

.head-sub {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 12rpx;
  display: block;
}

.filter-tabs {
  display: flex;
  background: #fff;
  padding: 16rpx 24rpx;
  gap: 16rpx;
}

.filter-item {
  flex: 1;
  text-align: center;
  padding: 16rpx 0;
  border-radius: 32rpx;
  font-size: 28rpx;
  color: #666;
  background: #f5f5f5;

  &.active {
    background: #C9E5E1;
    color: #78B9B1;
    font-weight: 600;
  }
}

.list-scroll {
  padding-top: 16rpx;
  padding-bottom: calc(100rpx + env(safe-area-inset-bottom));
}
</style>
