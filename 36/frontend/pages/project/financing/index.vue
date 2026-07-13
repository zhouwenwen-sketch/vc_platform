<template>
  <view class="library-page">
    <LibraryPageHero title="在融项目" />

    <view class="library-filter-anchor">
      <view class="search-bar">
        <view class="search-input-wrap">
          <u-icon name="search" size="28rpx" color="#999" class="search-icon" />
          <input
            v-model="searchKeyword"
            class="search-input"
            placeholder="请输入项目名称关键字"
            @confirm="applySearch"
          />
        </view>
      </view>

      <view class="sort-bar">
        <view class="result-count">
          <text>共 </text>
          <text class="result-num">{{ displayTotal }}</text>
          <text> 在融项目</text>
        </view>
      </view>
    </view>

    <view class="list-area">
      <scroll-view
        scroll-y
        class="list-scroll"
        :show-scrollbar="false"
        @scrolltolower="loadMore"
      >
        <ProjectLibraryCard
          v-for="item in projectList"
          :key="item.id"
          :item="item"
          @click="onProjectTap"
        />
        <u-loadmore :status="loadStatus" margin-top="20" margin-bottom="30" />
        <u-empty v-if="!loading && projectList.length === 0" text="暂无在融项目" mode="list" />
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import LibraryPageHero from '@/components/LibraryPageHero/LibraryPageHero.vue'
import ProjectLibraryCard from '@/components/ProjectLibraryCard/ProjectLibraryCard.vue'
import { fetchFinancingProjectPage } from '@/api/project.js'
import { SUCCESS_CODE } from '@/utils/request.js'
import { navigateFromProjectLibrary } from '@/utils/projectNavigate.js'

const searchKeyword = ref('')
const projectList = ref([])
const displayTotal = ref(0)
const page = ref(1)
const hasMore = ref(true)
const loadStatus = ref('loadmore')
const loading = ref(false)

async function loadList(reset = false) {
  if (loading.value) return
  if (!reset && !hasMore.value) return

  loading.value = true
  loadStatus.value = 'loading'
  const pageNum = reset ? 1 : page.value

  const res = await fetchFinancingProjectPage(pageNum, 10, {
    keyword: searchKeyword.value.trim()
  })

  loading.value = false
  if (res.code === SUCCESS_CODE) {
    const { list, total, hasMore: more } = res.data
    projectList.value = reset ? list : [...projectList.value, ...list]
    displayTotal.value = total
    hasMore.value = more
    page.value = pageNum + 1
    loadStatus.value = more ? 'loadmore' : 'nomore'
  } else {
    loadStatus.value = 'loadmore'
  }
}

function loadMore() {
  loadList()
}

function applySearch() {
  loadList(true)
}

function onProjectTap(item) {
  navigateFromProjectLibrary(item)
}

onLoad(() => {
  loadList(true)
})
</script>

<style scoped lang="scss">
@import '@/styles/library-page.scss';
</style>
