<template>
  <view class="search-page">
    <view class="page-nav">
      <view class="back-btn" @click="onBack">
        <u-icon name="arrow-left" color="#333" size="40rpx" />
      </view>
      <text class="nav-title">搜索</text>
      <view class="nav-placeholder" />
    </view>

    <view class="search-bar">
      <view class="search-input-wrap">
        <u-icon name="search" size="32rpx" color="#999" class="search-icon" />
        <input
          v-model="keyword"
          class="search-input"
          type="text"
          confirm-type="search"
          placeholder="公司/项目名/投资机构/赛道"
          :focus="inputFocus"
          @input="onInput"
          @confirm="runSearch"
        />
        <u-icon
          v-if="keyword"
          name="close-circle-fill"
          size="32rpx"
          color="#ccc"
          class="clear-icon"
          @click="clearKeyword"
        />
      </view>
    </view>

    <scroll-view scroll-y class="result-scroll" :show-scrollbar="false">
      <view v-if="keyword.trim() && projectList.length" class="section">
        <text class="section-title">项目</text>
        <view
          v-for="item in projectList"
          :key="'p-' + item.id"
          class="project-item"
          @click="goProjectDetail(item)"
        >
          <ProjectLogo :item="item" />
          <view class="item-body">
            <view class="item-top">
              <HighlightText
                :text="item.name"
                :keyword="keyword"
                custom-class="item-name"
              />
              <text class="round-tag">{{ item.round }}</text>
            </view>
            <HighlightText
              v-if="item.companyDesc"
              :text="item.companyDesc"
              :keyword="keyword"
              custom-class="item-desc"
            />
            <view class="entity-row">
              <text class="entity-label">企业主体：</text>
              <HighlightText
                v-if="item.entityName"
                :text="item.entityName"
                :keyword="keyword"
                custom-class="entity-name"
              />
              <text v-else class="entity-name entity-empty">—</text>
            </view>
          </view>
        </view>
      </view>

      <view v-if="keyword.trim() && institutionList.length" class="section">
        <text class="section-title">机构</text>
        <view
          v-for="item in institutionList"
          :key="'i-' + item.id"
          class="inst-item"
          @click="goInstitutionDetail(item)"
        >
          <ProjectLogo :item="item" />
          <view class="item-body">
            <view class="item-top">
              <HighlightText
                :text="item.name"
                :keyword="keyword"
                custom-class="item-name"
              />
              <text v-if="item.instType" class="inst-type-tag">{{ item.instType }}</text>
            </view>
            <view class="entity-row">
              <text class="entity-label">机构主体：</text>
              <HighlightText
                v-if="item.entityName"
                :text="item.entityName"
                :keyword="keyword"
                custom-class="entity-name"
              />
              <text v-else class="entity-name entity-empty">—</text>
            </view>
            <text class="inst-meta">最近投资：{{ item.recentInvestment || '—' }} · 投资事件 {{ item.eventCount }}</text>
          </view>
        </view>
      </view>

      <view v-if="loading" class="loading-wrap">
        <u-loading-icon mode="circle" color="#78B9B1" size="36" />
      </view>

      <u-empty
        v-if="keyword.trim() && !loading && !hasResults"
        text="暂无匹配结果"
        mode="search"
        margin-top="120"
      />
    </scroll-view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import HighlightText from '@/components/HighlightText/HighlightText.vue'
import ProjectLogo from '@/components/ProjectLogo/ProjectLogo.vue'
import { fetchProjectSearch, fetchInstitutionSearch } from '@/api/search.js'
import { SUCCESS_CODE } from '@/utils/request.js'
import { navigateToProjectDetail, PROJECT_ENTRY } from '@/utils/projectNavigate.js'

const keyword = ref('')
const projectList = ref([])
const institutionList = ref([])
const loading = ref(false)
const inputFocus = ref(true)
let debounceTimer = null
let requestSeq = 0

const hasResults = computed(() => projectList.value.length > 0 || institutionList.value.length > 0)

onLoad((query) => {
  if (query?.keyword) {
    keyword.value = decodeURIComponent(query.keyword)
    runSearch()
  }
})

function onInput() {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(runSearch, 200)
}

function clearKeyword() {
  keyword.value = ''
  projectList.value = []
  institutionList.value = []
  clearTimeout(debounceTimer)
}

async function runSearch() {
  const kw = keyword.value.trim()
  if (!kw) {
    projectList.value = []
    institutionList.value = []
    loading.value = false
    return
  }

  const seq = ++requestSeq
  loading.value = true
  try {
    const [projectRes, institutionRes] = await Promise.all([
      fetchProjectSearch(kw, 1, 30),
      fetchInstitutionSearch(kw, 1, 30)
    ])
    if (seq !== requestSeq) return
    if (projectRes.code === SUCCESS_CODE) {
      projectList.value = projectRes.data?.list || []
    }
    if (institutionRes.code === SUCCESS_CODE) {
      institutionList.value = institutionRes.data?.list || []
    }
  } catch (err) {
    console.error('[search] failed', err)
  } finally {
    if (seq === requestSeq) {
      loading.value = false
    }
  }
}

function goProjectDetail(item) {
  navigateToProjectDetail(item.id, { from: PROJECT_ENTRY.SEARCH })
}

function goInstitutionDetail(item) {
  uni.navigateTo({ url: `/pages/institution/detail/index?id=${item.id}` })
}

function onBack() {
  uni.navigateBack({
    fail: () => {
      uni.switchTab({ url: '/pages/home/index' })
    }
  })
}
</script>

<style scoped lang="scss">
.search-page {
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
}

.back-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 72rpx;
  height: 72rpx;
}

.nav-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #222;
}

.nav-placeholder {
  width: 72rpx;
  height: 72rpx;
}

.search-bar {
  flex: none;
  padding: 16rpx 24rpx 20rpx;
  background: #fff;
}

.search-input-wrap {
  display: flex;
  align-items: center;
  height: 72rpx;
  padding: 0 24rpx;
  background: #f5f6f8;
  border-radius: 36rpx;
}

.search-icon {
  flex: none;
  margin-right: 12rpx;
}

.search-input {
  flex: 1;
  height: 72rpx;
  font-size: 28rpx;
  color: #333;
}

.clear-icon {
  flex: none;
  margin-left: 12rpx;
}

.result-scroll {
  flex: 1;
  height: 0;
}

.section {
  padding: 0 24rpx 40rpx;
}

.section-title {
  display: block;
  padding: 8rpx 0 20rpx;
  font-size: 32rpx;
  font-weight: 600;
  color: #222;
}

.project-item,
.inst-item {
  display: flex;
  gap: 20rpx;
  padding: 28rpx 0;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.item-body {
  flex: 1;
  min-width: 0;
}

.item-top {
  display: flex;
  align-items: center;
  margin-bottom: 10rpx;
}

:deep(.item-name) {
  flex: 1;
  min-width: 0;
  font-size: 30rpx;
  font-weight: 600;
  color: #222;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.round-tag {
  flex: none;
  margin-left: 12rpx;
  padding: 4rpx 12rpx;
  font-size: 22rpx;
  line-height: 1.4;
  color: #78B9B1;
  background: rgba(41, 121, 255, 0.1);
  border-radius: 6rpx;
}

.inst-type-tag {
  flex: none;
  margin-left: 12rpx;
  padding: 4rpx 12rpx;
  font-size: 22rpx;
  line-height: 1.4;
  color: #78B9B1;
  background: rgba(41, 121, 255, 0.1);
  border-radius: 6rpx;
}

.inst-meta {
  display: block;
  margin-top: 10rpx;
  font-size: 24rpx;
  color: #999;
  line-height: 1.5;
}

:deep(.item-desc) {
  display: -webkit-box;
  overflow: hidden;
  font-size: 26rpx;
  line-height: 1.5;
  color: #666;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.entity-row {
  display: flex;
  align-items: flex-start;
  margin-top: 10rpx;
  font-size: 24rpx;
  line-height: 1.5;
}

.entity-label {
  flex: none;
  color: #999;
}

:deep(.entity-name) {
  flex: 1;
  min-width: 0;
  color: #999;
}

.entity-empty {
  flex: 1;
  color: #999;
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 40rpx 0;
}
</style>
