<template>
  <view class="library-page">
    <LibraryPageHero title="项目商城" />

    <view class="page-top">
      <view class="search-bar">
        <view class="search-input-wrap">
          <u-icon name="search" size="32rpx" color="#999" class="search-icon" />
          <input
            v-model="keyword"
            class="search-input"
            placeholder="请输入项目名称搜索"
            confirm-type="search"
            @confirm="onSearch"
          />
        </view>
      </view>

      <view class="tab-row">
        <text
          v-for="tab in MA_DEAL_TABS"
          :key="tab.value"
          class="tab-item"
          :class="{ active: activeCategory === tab.value }"
          @click="onTabChange(tab.value)"
        >{{ tab.label }}</text>
      </view>
    </view>

    <view class="list-area">
      <scroll-view
        scroll-y
        class="list-scroll"
        :scroll-with-animation="true"
        @scrolltolower="loadMore"
      >
        <view class="card-grid">
          <MaDealCard
            v-for="item in dealList"
            :key="item.id"
            :item="item"
            @click="goDetail"
            @favorite="onFavorite"
            @share="onShare"
          />
        </view>

        <u-empty v-if="!loading && dealList.length === 0" mode="list" text="暂无融资并购项目" />
        <u-loadmore v-else :status="loadStatus" margin-top="20" margin-bottom="30" />
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import LibraryPageHero from '@/components/LibraryPageHero/LibraryPageHero.vue'
import MaDealCard from '@/components/MaDealCard/MaDealCard.vue'
import { fetchMaDealPage, MA_DEAL_TABS, shareMaDeal } from '@/api/maDeal.js'
import { SUCCESS_CODE } from '@/utils/request.js'

const keyword = ref('')
const activeCategory = ref('all')
const dealList = ref([])
const pageNum = ref(1)
const pageSize = 10
const hasMore = ref(true)
const loading = ref(false)
const loadStatus = ref('loadmore')
const listReady = ref(false)

onLoad(() => {
  listReady.value = true
  reloadList()
})

function onTabChange(value) {
  if (activeCategory.value === value) return
  activeCategory.value = value
  reloadList()
}

function onSearch() {
  reloadList()
}

async function reloadList() {
  pageNum.value = 1
  hasMore.value = true
  dealList.value = []
  await loadList()
}

async function loadList() {
  if (loading.value || !hasMore.value) return
  loading.value = true
  loadStatus.value = 'loading'
  try {
    const res = await fetchMaDealPage(pageNum.value, pageSize, {
      keyword: keyword.value.trim(),
      category: activeCategory.value
    })
    if (res.code === SUCCESS_CODE) {
      const { list, hasMore: more } = res.data
      dealList.value = pageNum.value === 1 ? list : dealList.value.concat(list)
      hasMore.value = more
      loadStatus.value = more ? 'loadmore' : 'nomore'
      if (more) pageNum.value += 1
    } else {
      loadStatus.value = 'loadmore'
    }
  } catch (e) {
    console.error('[ma/list] load failed', e)
    loadStatus.value = 'loadmore'
  } finally {
    loading.value = false
  }
}

function loadMore() {
  if (!listReady.value) return
  loadList()
}

function goDetail(item) {
  if (!item?.id) return
  uni.navigateTo({ url: `/pages/ma/detail/index?id=${item.id}` })
}

function onFavorite() {
  uni.showToast({ title: '收藏功能即将上线', icon: 'none' })
}

async function onShare(item) {
  if (!item?.id) return
  await shareMaDeal(item.id).catch(() => {})
  uni.showToast({ title: '已记录转发', icon: 'none' })
}
</script>

<style scoped lang="scss">
@import '@/styles/library-page.scss';
@import '@/styles/theme.scss';

.page-top {
  flex-shrink: 0;
  background: #fff;
}

.tab-row {
  display: flex;
  align-items: center;
  padding: 0 0 20rpx;
}

.tab-item {
  flex: 1;
  text-align: center;
  font-size: 28rpx;
  color: #666;
  padding: 12rpx 4rpx;
  position: relative;
  box-sizing: border-box;

  &.active {
    color: $theme-primary;
    font-weight: 700;

    &::after {
      content: '';
      position: absolute;
      left: 50%;
      transform: translateX(-50%);
      bottom: 0;
      width: 48rpx;
      height: 4rpx;
      background: $theme-primary;
      border-radius: 2rpx;
    }
  }
}

.card-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16rpx;
  padding: 16rpx 24rpx 0;
}
</style>
