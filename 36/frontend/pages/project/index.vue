<template>
  <view class="project-page">
    <view class="scroll-body">
      <!-- 项目集 -->
      <view class="section-block">
        <text class="block-title">项目集</text>
        <scroll-view
          scroll-x
          class="tab-scroll"
          :scroll-into-view="tabScrollIntoView"
          scroll-with-animation
          :show-scrollbar="false"
        >
          <view class="tab-inner">
            <view
              v-for="(tab, idx) in collectionTabs"
              :id="'coll-tab-' + idx"
              :key="tab"
              class="tab-pill"
              :class="{ active: activeTab === idx }"
              @click="onCollectionTabChange(idx)"
            >
              {{ tab }}
            </view>
          </view>
        </scroll-view>
        <swiper
          class="coll-swiper"
          :current="activeTab"
          :duration="300"
          @change="onCollectionSwiperChange"
        >
          <swiper-item v-for="(tab, idx) in collectionTabs" :key="tab">
            <view class="coll-row">
              <view
                v-for="c in collectionPages[idx] || []"
                :key="c.id"
                class="coll-card"
                @click="goCollectionDetail(c)"
              >
                <view class="coll-cover-wrap">
                  <image class="coll-cover" :src="c.cover" mode="aspectFill" />
                  <view class="coll-cover-mask" />
                  <text class="coll-cover-title">{{ displayCoverTitle(c) }}</text>
                  <view class="coll-cover-badge">
                    <text>{{ c.projectCount }}个项目 | {{ formatCollectionDate(c.date) }}</text>
                  </view>
                </view>
                <text class="coll-title">{{ displayIndustryLabel(c) }}</text>
              </view>
            </view>
          </swiper-item>
        </swiper>
        <view class="link-more" @click="goCollectionList">查看更多 {{ LINK_ARROW }}</view>
      </view>

      <!-- 快捷入口 -->
      <view class="quick-row">
        <view class="quick-card" @click="goProjectLibrary">
          <text class="qc-title">项目库</text>
          <text class="qc-sub">查看全部项目</text>
          <u-icon name="folder" size="56rpx" color="#78B9B1" class="qc-icon" />
        </view>
        <view class="quick-card" @click="openProjectOnboard">
          <text class="qc-title">项目入驻</text>
          <text class="qc-sub">链接资本获得曝光</text>
          <u-icon name="plus-circle" size="56rpx" color="#78B9B1" class="qc-icon" />
        </view>
      </view>

      <!-- 热门项目 -->
      <view class="hot-header">
        <text class="hot-title">热门项目</text>
        <text class="hot-sub">每30分钟更新</text>
      </view>
      <ProjectCard v-for="p in displayProjects" :key="p.id" :item="p" />
      <view class="link-more hot-more" @click="goProjectLibrary">查看更多 {{ LINK_ARROW }}</view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import ProjectCard from '@/components/ProjectCard/ProjectCard.vue'
import { fetchProjectPageInit, fetchProjectPage, fetchProjectCollections } from '@/api/project.js'
import { SUCCESS_CODE } from '@/utils/request.js'
import { openProjectOnboard } from '@/utils/projectOnboardGuard.js'
import { LINK_ARROW } from '@/utils/linkText.js'
import { displayCoverTitle, displayIndustryLabel } from '@/utils/projectCollection.js'

const collectionTabs = ref([])
const collectionPages = ref([])
const activeTab = ref(0)
const tabScrollIntoView = ref('')
const displayProjects = ref([])

const HOT_PROJECT_LIMIT = 15
const DEFAULT_COLLECTION_TABS = ['全部', '最受关注', '热门赛道', '大赛路演', '榜单名册', '政策支持']

function tabParam(tab) {
  if (!tab || tab === '全部') return ''
  return tab
}

function formatCollectionDate(dateStr) {
  if (!dateStr) return ''
  const s = String(dateStr)
  return s.length >= 10 ? s.slice(5, 10) : s
}

function scrollTabIntoView(idx) {
  tabScrollIntoView.value = ''
  nextTick(() => {
    tabScrollIntoView.value = `coll-tab-${idx}`
  })
}

async function loadCollectionsForTab(idx) {
  const tab = collectionTabs.value[idx]
  if (!tab) return []
  const res = await fetchProjectCollections(1, 2, tabParam(tab))
  if (res.code === SUCCESS_CODE) {
    return res.data.list || []
  }
  return []
}

async function loadAllCollections() {
  if (!collectionTabs.value.length) return
  collectionPages.value = await Promise.all(
    collectionTabs.value.map((_, idx) => loadCollectionsForTab(idx))
  )
}

function onCollectionTabChange(idx) {
  if (activeTab.value === idx) return
  activeTab.value = idx
  scrollTabIntoView(idx)
}

function onCollectionSwiperChange(e) {
  const idx = e.detail?.current ?? 0
  if (activeTab.value === idx) return
  activeTab.value = idx
  scrollTabIntoView(idx)
}

async function loadInit() {
  const res = await fetchProjectPageInit()
  if (res.code === SUCCESS_CODE) {
    collectionTabs.value = res.data.collectionTabs?.length
      ? res.data.collectionTabs
      : [...DEFAULT_COLLECTION_TABS]
  } else {
    collectionTabs.value = [...DEFAULT_COLLECTION_TABS]
  }
  await loadAllCollections()
  await loadHotProjects()
}

async function loadHotProjects() {
  const res = await fetchProjectPage(1, HOT_PROJECT_LIMIT)
  if (res.code === SUCCESS_CODE) {
    displayProjects.value = (res.data.list || []).slice(0, HOT_PROJECT_LIMIT)
  }
}

onMounted(() => loadInit())

function goProjectLibrary() {
  uni.navigateTo({ url: '/pages/project/library/index' })
}

function goCollectionList() {
  uni.navigateTo({ url: '/pages/project/collection/index' })
}

function goCollectionDetail(c) {
  if (!c?.id) return
  uni.navigateTo({ url: `/pages/project/collection/detail?id=${c.id}` })
}
</script>

<style lang="scss" scoped>
.project-page {
  min-height: 100vh;
  background: #f5f6f8;
}

.scroll-body {
  padding-bottom: 32rpx;
}

.section-block {
  background: #fff;
  margin: 24rpx 24rpx 24rpx;
  border-radius: 16rpx;
  padding: 28rpx 0 16rpx;
}

.block-title {
  font-size: 34rpx;
  font-weight: 700;
  padding: 0 28rpx 20rpx;
  display: block;
}

.tab-scroll {
  white-space: nowrap;
}

.tab-inner {
  display: inline-flex;
  padding: 0 28rpx 20rpx;
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
    background: #C9E5E1;
    color: #78B9B1;
  }
}

.coll-swiper {
  height: 280rpx;
}

.coll-row {
  display: flex;
  gap: 20rpx;
  padding: 0 28rpx;
  box-sizing: border-box;
}

.coll-card {
  flex: 1;
  min-width: 0;
}

.coll-cover-wrap {
  position: relative;
  height: 200rpx;
  border-radius: 12rpx;
  overflow: hidden;
}

.coll-cover {
  width: 100%;
  height: 100%;
  display: block;
}

.coll-cover-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.12) 0%, rgba(0, 0, 0, 0.52) 100%);
}

.coll-cover-title {
  position: absolute;
  left: 16rpx;
  right: 16rpx;
  top: 50%;
  transform: translateY(-50%);
  font-size: 24rpx;
  font-weight: 800;
  color: #fff;
  line-height: 1.35;
  white-space: pre-line;
  text-align: center;
}

.coll-cover-badge {
  position: absolute;
  right: 12rpx;
  bottom: 12rpx;
  left: auto;
  padding: 4rpx 12rpx;
  background: rgba(0, 0, 0, 0.42);
  border-radius: 20rpx;

  text {
    font-size: 20rpx;
    color: rgba(255, 255, 255, 0.95);
  }
}

.coll-title {
  display: block;
  width: 100%;
  font-size: 26rpx;
  color: #333;
  margin-top: 12rpx;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
  text-align: center;
}

.link-more {
  text-align: center;
  padding: 20rpx;
  font-size: 28rpx;
  color: #78B9B1;
}

.quick-row {
  display: flex;
  gap: 20rpx;
  padding: 0 24rpx 24rpx;
}

.quick-card {
  flex: 1;
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  position: relative;
  min-height: 140rpx;
}

.qc-title {
  font-size: 32rpx;
  font-weight: 700;
  display: block;
}

.qc-sub {
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
  display: block;
}

.qc-icon {
  position: absolute;
  right: 24rpx;
  bottom: 24rpx;
  opacity: 0.3;
}

.hot-header {
  padding: 8rpx 30rpx 16rpx;
}

.hot-title {
  font-size: 34rpx;
  font-weight: 700;
  margin-right: 16rpx;
}

.hot-sub {
  font-size: 24rpx;
  color: #999;
}

.hot-more {
  margin-bottom: 16rpx;
}
</style>
