<template>
  <view class="library-page">
    <LibraryPageHero title="融资快报" />

    <view class="list-area">
      <scroll-view
        scroll-y
        class="list-scroll"
        :show-scrollbar="false"
        @scrolltolower="loadMore"
      >
        <NewsCard
          v-for="item in newsList"
          :key="item.id"
          :item="item"
          plain
          @click="onNewsTap"
          @project="onProjectTap"
        />

        <u-loadmore :status="loadStatus" margin-top="16" margin-bottom="32" />
        <u-empty
          v-if="!loading && newsList.length === 0"
          text="暂无快讯"
          mode="news"
          margin-top="120"
        />
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import LibraryPageHero from '@/components/LibraryPageHero/LibraryPageHero.vue'
import NewsCard from '@/components/NewsCard/NewsCard.vue'
import { fetchNewsPage } from '@/api/news.js'
import { SUCCESS_CODE } from '@/utils/request.js'
import { navigateToProjectDetail, PROJECT_ENTRY } from '@/utils/projectNavigate.js'

const newsList = ref([])
const pageNum = ref(1)
const hasMore = ref(true)
const loading = ref(false)
const loadStatus = ref('loadmore')
const pageSize = 10

onLoad(() => {
  loadNews(true)
})

async function loadNews(reset = false) {
  if (loading.value) return
  if (!reset && !hasMore.value) return

  if (reset) {
    pageNum.value = 1
    hasMore.value = true
    newsList.value = []
  }

  loading.value = true
  loadStatus.value = 'loading'

  try {
    const res = await fetchNewsPage(pageNum.value, pageSize)
    if (res.code === SUCCESS_CODE) {
      const list = res.data?.list || []
      const more = res.data?.hasMore ?? false
      newsList.value = reset ? list : newsList.value.concat(list)
      hasMore.value = more
      pageNum.value += 1
      loadStatus.value = more ? 'loadmore' : 'nomore'
    } else {
      loadStatus.value = 'loadmore'
    }
  } catch (err) {
    console.error('[news] load failed', err)
    loadStatus.value = 'loadmore'
  } finally {
    loading.value = false
  }
}

function loadMore() {
  loadNews(false)
}

function onNewsTap(item) {
  if (!item?.id) return
  uni.navigateTo({ url: `/pages/news/detail?id=${item.id}` })
}

function onProjectTap(item) {
  if (!item?.projectId) return
  navigateToProjectDetail(item.projectId, { from: PROJECT_ENTRY.NEWS })
}
</script>

<style scoped lang="scss">
@import '@/styles/library-page.scss';
</style>
