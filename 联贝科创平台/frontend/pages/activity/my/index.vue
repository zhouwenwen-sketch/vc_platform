<template>
  <view class="my-activity-page">
    <view class="search-wrap">
      <u-search
        v-model="keyword"
        placeholder="请输入要查询的内容"
        :show-action="false"
        shape="round"
        bg-color="#f5f6f8"
        @search="onSearch"
        @clear="onSearchClear"
      />
    </view>

    <view class="tab-bar">
      <view
        v-for="tab in tabs"
        :key="tab.value"
        class="tab-item"
        :class="{ active: currentTab === tab.value }"
        @click="onTabChange(tab.value)"
      >
        <text class="tab-text">{{ tab.label }}</text>
        <view v-if="currentTab === tab.value" class="tab-line" />
      </view>
    </view>

    <scroll-view
      scroll-y
      class="list-scroll"
      :show-scrollbar="false"
      @scrolltolower="loadMore"
    >
      <ActivityCard v-for="item in list" :key="item.id" :item="item" @click="onActivityTap" />
      <u-loadmore v-if="list.length > 0" :status="loadStatus" margin-top="16" margin-bottom="40" />
      <u-empty
        v-if="!loading && list.length === 0"
        text="暂无数据"
        mode="data"
        margin-top="120"
      />
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import ActivityCard from '@/components/ActivityCard/ActivityCard.vue'
import { fetchMyActivityPage } from '@/api/activity.js'
import { ensureLoggedIn } from '@/utils/authGuard.js'
import { SUCCESS_CODE } from '@/utils/request.js'

const MY_ACTIVITY_PATH = '/pages/activity/my/index'

const tabs = [
  { label: '已预约', value: 'reserved' },
  { label: '已报名', value: 'signed_up' },
  { label: '已参加', value: 'attended' }
]

const currentTab = ref('reserved')
const keyword = ref('')
const list = ref([])
const page = ref(1)
const pageSize = 10
const hasMore = ref(false)
const loading = ref(false)
const loadStatus = ref('loadmore')

onLoad(() => {
  if (!ensureLoggedIn(MY_ACTIVITY_PATH)) return
  loadList(true)
})

async function loadList(reset = false) {
  if (loading.value) return
  if (reset) {
    page.value = 1
    list.value = []
    hasMore.value = false
    loadStatus.value = 'loadmore'
  }
  loading.value = true
  try {
    const res = await fetchMyActivityPage({
      pageNum: page.value,
      pageSize,
      tab: currentTab.value,
      keyword: keyword.value.trim()
    })
    if (res.code !== SUCCESS_CODE) return
    const data = res.data || {}
    const nextList = data.list || []
    list.value = reset ? nextList : list.value.concat(nextList)
    hasMore.value = !!data.hasMore
    loadStatus.value = hasMore.value ? 'loadmore' : 'nomore'
  } finally {
    loading.value = false
  }
}

function loadMore() {
  if (loading.value || !hasMore.value) return
  page.value += 1
  loadList(false)
}

function onTabChange(value) {
  if (currentTab.value === value) return
  currentTab.value = value
  loadList(true)
}

function onSearch() {
  loadList(true)
}

function onSearchClear() {
  keyword.value = ''
  loadList(true)
}

function onActivityTap(item) {
  uni.navigateTo({ url: `/pages/activity/detail/index?id=${item.id}` })
}
</script>

<style lang="scss" scoped>
.my-activity-page {
  min-height: 100vh;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
}

.search-wrap {
  padding: 20rpx 24rpx 8rpx;
  background: #fff;
}

.tab-bar {
  display: flex;
  background: #fff;
  border-bottom: 1rpx solid #f0f0f0;
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24rpx 0 16rpx;
  position: relative;
}

.tab-text {
  font-size: 30rpx;
  color: #333;
}

.tab-item.active .tab-text {
  color: #78B9B1;
  font-weight: 600;
}

.tab-line {
  width: 48rpx;
  height: 6rpx;
  border-radius: 3rpx;
  background: #78B9B1;
  margin-top: 12rpx;
}

.list-scroll {
  flex: 1;
  height: 0;
  padding-top: 16rpx;
}
</style>
