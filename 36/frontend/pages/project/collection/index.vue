<template>
  <view class="collection-page">
    <scroll-view scroll-x class="tab-scroll" :show-scrollbar="false">
      <view class="tab-inner">
        <view
          v-for="(tab, idx) in tabs"
          :key="tab"
          class="tab-pill"
          :class="{ active: activeTab === idx }"
          @click="onTabChange(idx)"
        >
          {{ tab }}
        </view>
      </view>
    </scroll-view>

    <view class="list-body">
      <view
        v-for="item in collections"
        :key="item.id"
        class="coll-card"
        @click="goDetail(item)"
      >
        <view class="cover-wrap">
          <image class="cover-img" :src="item.cover" mode="aspectFill" />
          <view class="cover-mask" />
          <text class="cover-title">{{ displayCoverTitle(item) }}</text>
          <view class="cover-badge">
            <text>{{ item.projectCount }}个项目 | {{ item.date }}</text>
          </view>
        </view>
        <text class="card-summary">{{ displayIndustryLabel(item) }}</text>
      </view>
      <u-loadmore :status="loadStatus" margin-top="16" margin-bottom="32" />
      <u-empty v-if="!loading && collections.length === 0" text="暂无项目集" mode="list" />
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onReachBottom } from '@dcloudio/uni-app'
import { fetchCollectionTabs, fetchProjectCollections } from '@/api/project.js'
import { SUCCESS_CODE } from '@/utils/request.js'
import { displayCoverTitle, displayIndustryLabel } from '@/utils/projectCollection.js'

const tabs = ref(['全部', '最受关注', '热门赛道', '大赛路演', '榜单名册', '政策支持'])
const activeTab = ref(0)
const collections = ref([])
const page = ref(1)
const hasMore = ref(true)
const loadStatus = ref('loadmore')
const loading = ref(false)
function currentTab() {
  return tabs.value[activeTab.value] || '全部'
}

async function loadTabs() {
  const res = await fetchCollectionTabs()
  if (res.code === SUCCESS_CODE && res.data?.length) {
    tabs.value = res.data
  }
}

async function loadList(reset = false) {
  if (reset) {
    page.value = 1
    hasMore.value = true
    collections.value = []
  }
  if (!hasMore.value && !reset) return

  loading.value = true
  loadStatus.value = 'loading'
  const tab = currentTab()
  const tabParam = tab === '全部' ? '' : tab
  const res = await fetchProjectCollections(page.value, 10, tabParam)
  loading.value = false

  if (res.code === SUCCESS_CODE) {
    const { list, hasMore: more } = res.data
    collections.value = reset ? list : [...collections.value, ...list]
    hasMore.value = more
    page.value += 1
    loadStatus.value = more ? 'loadmore' : 'nomore'
  } else {
    loadStatus.value = 'loadmore'
  }
}

function onTabChange(idx) {
  if (activeTab.value === idx) return
  activeTab.value = idx
  loadList(true)
}

function loadMore() {
  if (loadStatus.value === 'loading') return
  loadList()
}

function goDetail(item) {
  if (!item?.id) return
  uni.navigateTo({ url: `/pages/project/collection/detail?id=${item.id}` })
}

onMounted(async () => {
  await loadTabs()
  await loadList(true)
})

onReachBottom(() => loadMore())
</script>

<style lang="scss" scoped>
.collection-page {
  min-height: 100vh;
  background: #f5f6f8;
  padding-bottom: 32rpx;
}

.tab-scroll {
  flex-shrink: 0;
  white-space: nowrap;
  background: #fff;
  border-bottom: 1rpx solid #f0f0f0;
}

.tab-inner {
  display: inline-flex;
  padding: 20rpx 24rpx;
  gap: 16rpx;
}

.tab-pill {
  display: inline-block;
  padding: 12rpx 28rpx;
  background: #f5f5f5;
  border-radius: 32rpx;
  font-size: 26rpx;
  color: #666;
  flex-shrink: 0;

  &.active {
    background: #78B9B1;
    color: #fff;
    font-weight: 500;
  }
}

.list-body {
  padding: 24rpx;
}

.coll-card {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  margin-bottom: 24rpx;
  padding-bottom: 24rpx;
}

.cover-wrap {
  position: relative;
  height: 320rpx;
  margin: 0 0 20rpx;
}

.cover-img {
  width: 100%;
  height: 100%;
}

.cover-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.15) 0%, rgba(0, 0, 0, 0.55) 100%);
}

.cover-title {
  position: absolute;
  left: 28rpx;
  right: 28rpx;
  top: 50%;
  transform: translateY(-50%);
  font-size: 40rpx;
  font-weight: 800;
  color: #fff;
  line-height: 1.35;
  white-space: pre-line;
  text-align: center;
}

.cover-badge {
  position: absolute;
  right: 20rpx;
  bottom: 20rpx;
  padding: 8rpx 20rpx;
  background: rgba(0, 0, 0, 0.45);
  border-radius: 24rpx;

  text {
    font-size: 22rpx;
    color: rgba(255, 255, 255, 0.95);
  }
}

.card-summary {
  display: block;
  width: 100%;
  padding: 0 24rpx;
  box-sizing: border-box;
  font-size: 28rpx;
  color: #333;
  line-height: 1.55;
  font-weight: 500;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
