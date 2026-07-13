<template>
  <view class="home-page" :style="tabNavStyle">
    <view class="top-spacer" :class="{ visible: showTopSpacer }" />

    <scroll-view
      scroll-y
      class="page-scroll"
      :show-scrollbar="false"
      enhanced
      @scroll="onScroll"
    >
      <!-- 顶部品牌区 + 搜索 -->
      <view id="home-header" class="header-wrap">
        <view class="brand">
          <text class="brand-title">大学生创投平台</text>
          <text class="brand-slogan">科技创新生态优质连接服务平台</text>
        </view>
        <view class="search-bar" @click="onSearch">
          <u-icon name="search" size="32rpx" color="#999" />
          <text class="placeholder">公司/项目名/投资机构/赛道</text>
        </view>
      </view>

      <view class="scroll-body">
      <!-- 功能入口 -->
      <view class="menu-grid">
        <view
          v-for="item in menuList"
          :key="item.id"
          class="menu-item"
          @click="onMenuTap(item)"
        >
          <view class="menu-icon">
            <image
              class="menu-icon-img"
              :src="menuIconSrc(item)"
              mode="aspectFit"
            />
          </view>
          <text class="menu-text">{{ item.title }}</text>
        </view>
      </view>

      <!-- Banner -->
      <swiper class="banner-swiper" circular indicator-dots indicator-color="rgba(255,255,255,.4)" indicator-active-color="#fff">
        <swiper-item v-for="b in bannerList" :key="b.id">
          <view class="banner-item" @click="onBannerTap(b)">
            <u-image :src="b.imageUrl" width="100%" height="280rpx" radius="16rpx" mode="aspectFill" />
            <view class="banner-mask">
              <text class="b-title">{{ b.title }}</text>
              <text class="b-sub">{{ b.subtitle }}</text>
            </view>
          </view>
        </swiper-item>
      </swiper>

      <!-- 融资快报 -->
      <SectionHeader title="融资快报" action-text="更多" @action="goNewsList" />
      <NewsCard v-for="n in displayNews" :key="n.id" :item="n" @click="onNewsTap" />

      <!-- 融资事件 -->
      <SectionHeader
        title="融资事件"
        :subtitle="`本月投资事件数 ${eventCount} 起`"
        action-text="更多"
        @action="goFinancingEvents"
      />
      <FinancingEventCard
        v-for="ev in financingEvents"
        :key="ev.id"
        :item="ev"
        embedded
        @click="onFinancingEventTap"
      />

      <!-- 在融项目 -->
      <view class="financing-block">
        <SectionHeader
          title="在融项目"
          link-text="我要融资"
          gradient
          action-text="更多"
          @action="goFinancingProjects"
        />
        <view class="fin-list">
          <view
            v-for="p in displayFinancingProjects"
            :key="p.id"
            class="fin-item"
            @click="goProjectDetail(p)"
          >
            <ProjectLogo :item="p" size="sm" />
            <view class="fin-info">
              <view class="fin-top">
                <text class="fin-title">{{ p.title }}</text>
                <u-tag :text="p.round" size="mini" plain type="primary" />
              </view>
              <text class="fin-desc">{{ p.desc }}</text>
            </view>
          </view>
        </view>
        <view class="view-all-btn inner" @click="goFinancingProjects">
          <text>查看全部 {{ LINK_ARROW }}</text>
        </view>
      </view>

      <!-- 项目集 -->
      <view id="project-collection" class="anchor-section">
      <SectionHeader title="项目集" action-text="更多" @action="goCollectionList" />
      <view class="collection-feature" @click="goCollectionDetail(projectCollection)">
        <u-image :src="projectCollection.bannerImage" width="100%" height="320rpx" radius="16rpx" mode="aspectFill" />
        <view class="cf-badge">{{ projectCollection.projectCount }}个项目 · {{ projectCollection.date }}</view>
        <text class="cf-title">{{ projectCollection.title }}</text>
        <text class="cf-summary">{{ projectCollection.summary }}</text>
      </view>
      <view v-for="p in displayCollectionProjects" :key="p.id" class="coll-proj">
        <ProjectLogo :item="p" size="sm" />
        <view class="cp-info">
          <view class="cp-top">
            <text class="cp-name">{{ p.name }}</text>
            <u-tag :text="p.round" size="mini" plain type="primary" />
          </view>
          <text class="cp-desc">{{ p.desc }}</text>
        </view>
      </view>
      </view>

      <!-- 活动推荐 -->
      <view class="activity-block">
        <SectionHeader title="活动推荐" gradient action-text="更多" @action="goActivityList" />
        <view class="activity-list">
          <view
            v-for="a in recommendedActivities"
            :key="a.id"
            class="activity-item"
            @click="onActivityTap(a)"
          >
            <view class="act-cover">
              <u-image :src="a.cover" width="240rpx" height="135rpx" radius="12rpx" mode="aspectFill" />
            </view>
            <text class="act-title">{{ a.title }}</text>
          </view>
        </view>
        <view class="view-all-btn inner" @click="goActivityList">
          <text>查看全部 {{ LINK_ARROW }}</text>
        </view>
      </view>

      <!-- 最新入驻 -->
      <SectionHeader title="最新入驻" action-text="更多" @action="goProjectLibrary" />
      <view class="entry-grid">
        <view v-for="e in latestEntries" :key="e.id" class="entry-card">
          <ProjectLogo :item="e" size="sm" />
          <view class="entry-info">
            <view class="entry-top">
              <text class="entry-name">{{ e.name }}</text>
              <u-tag :text="e.round" size="mini" plain type="primary" />
            </view>
          </view>
          <text class="entry-desc">{{ e.desc }}</text>
        </view>
      </view>

    </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useScrollTopSpacer } from '@/utils/useScrollTopSpacer.js'

const { showTopSpacer, onScroll, tabNavStyle } = useScrollTopSpacer({ headerSelector: '#home-header' })
import SectionHeader from '@/components/SectionHeader/SectionHeader.vue'
import NewsCard from '@/components/NewsCard/NewsCard.vue'
import FinancingEventCard from '@/components/FinancingEventCard/FinancingEventCard.vue'
import ProjectLogo from '@/components/ProjectLogo/ProjectLogo.vue'
import { fetchHomeInit, fetchHomeFinancingProjects } from '@/api/home.js'
import { LINK_ARROW } from '@/utils/linkText.js'
import { fetchFeaturedFinancingEvents } from '@/api/financing.js'
import { fetchNewsPage } from '@/api/news.js'
import { fetchRecommendedActivities, fetchActivityPage } from '@/api/activity.js'
import { openInvestorAuth, requireLogin } from '@/utils/authGuard.js'
import { openCoverageApply } from '@/utils/coverageGuard.js'
import { openProjectOnboard } from '@/utils/projectOnboardGuard.js'
import {
  navigateFromFinancingEvent,
  navigateToProjectDetail,
  PROJECT_ENTRY
} from '@/utils/projectNavigate.js'
import { SUCCESS_CODE } from '@/utils/request.js'
import { resolveMediaUrl } from '@/utils/mediaUrl.js'

const MENU_ICON_KEYS = {
  1: 'news',
  2: 'events',
  3: 'library',
  4: 'institution',
  5: 'collection',
  6: 'research',
  7: 'investor',
  8: 'onboard',
  9: 'coverage',
  10: 'fa',
  11: 'ma'
}

/** 兼容接口仍返回 uview 图标名的情况 */
const LEGACY_ICON_KEYS = {
  'file-text': 'news',
  calendar: 'events',
  folder: 'library',
  home: 'institution',
  grid: 'collection',
  bookmark: 'research',
  account: 'investor',
  'plus-circle': 'onboard',
  'edit-pen': 'coverage',
  'rmb-circle': 'fa',
  order: 'ma'
}

const MENU_ICON_MAP = {
  news: '/static/home-icons/news.png',
  events: '/static/home-icons/events.png',
  library: '/static/home-icons/library.png',
  institution: '/static/home-icons/institution.png',
  collection: '/static/home-icons/collection.png',
  research: '/static/home-icons/research.png',
  investor: '/static/home-icons/investor.png',
  onboard: '/static/home-icons/onboard.png',
  coverage: '/static/home-icons/coverage.png',
  fa: '/static/home-icons/fa.png',
  ma: '/static/home-icons/ma.png'
}

function resolveMenuIconKey(item) {
  return MENU_ICON_KEYS[item?.id]
    || LEGACY_ICON_KEYS[item?.icon]
    || item?.icon
    || 'news'
}

function menuIconSrc(item) {
  const key = resolveMenuIconKey(item)
  return MENU_ICON_MAP[key] || MENU_ICON_MAP.news
}

function normalizeMenuList(list) {
  return (list || []).map((item) => ({
    ...item,
    icon: resolveMenuIconKey(item)
  }))
}

const menuList = ref([])
const bannerList = ref([])
const displayNews = ref([])
const financingEvents = ref([])
const financingProjects = ref([])
const projectCollection = ref({ projects: [] })
const latestEntries = ref([])
const recommendedActivities = ref([])
const eventCount = ref(16)

const HOME_NEWS_LIMIT = 3
const HOME_EVENT_LIMIT = 3
const HOME_COLLECTION_LIMIT = 3
const HOME_FINANCING_LIMIT = 3
const HOME_ACTIVITY_LIMIT = 3

const displayCollectionProjects = computed(() =>
  (projectCollection.value.projects || []).slice(0, HOME_COLLECTION_LIMIT)
)

const displayFinancingProjects = computed(() =>
  financingProjects.value.slice(0, HOME_FINANCING_LIMIT)
)

async function loadInit() {
  const res = await fetchHomeInit()
  if (res.code === SUCCESS_CODE) {
    const d = res.data
    menuList.value = normalizeMenuList(d.menuList)
    bannerList.value = (d.bannerList || []).map((b) => ({
      ...b,
      imageUrl: resolveMediaUrl(b.imageUrl)
    }))
    financingEvents.value = (d.financingEvents || []).slice(0, HOME_EVENT_LIMIT)
    projectCollection.value = d.projectCollection
    latestEntries.value = d.latestEntries
    eventCount.value = d.eventCount
  }
  const evRes = await fetchFeaturedFinancingEvents(HOME_EVENT_LIMIT)
  if (evRes.code === SUCCESS_CODE && evRes.data?.length) {
    financingEvents.value = evRes.data.slice(0, HOME_EVENT_LIMIT)
  }
  const finRes = await fetchHomeFinancingProjects()
  if (finRes.code === SUCCESS_CODE) {
    financingProjects.value = finRes.data
  }
  await Promise.all([loadNews(), loadActivities()])
}

async function loadActivities() {
  let list = []
  const recommendedRes = await fetchRecommendedActivities(HOME_ACTIVITY_LIMIT)
  if (recommendedRes.code === SUCCESS_CODE && recommendedRes.data?.length) {
    list = recommendedRes.data
  } else {
    const pageRes = await fetchActivityPage(1, HOME_ACTIVITY_LIMIT)
    if (pageRes.code === SUCCESS_CODE) {
      list = pageRes.data?.list || []
    }
  }
  recommendedActivities.value = list.slice(0, HOME_ACTIVITY_LIMIT)
}

async function loadNews() {
  const res = await fetchNewsPage(1, HOME_NEWS_LIMIT)
  if (res.code === SUCCESS_CODE) {
    displayNews.value = (res.data?.list || []).slice(0, HOME_NEWS_LIMIT)
  }
}

onMounted(() => {
  loadInit().catch((err) => console.error('[home] loadInit failed', err))
})

function onSearch() {
  uni.navigateTo({ url: '/pages/search/index' })
}

function onBannerTap(banner) {
  const link = banner?.linkUrl
  if (!link) return
  if (link.startsWith('/pages')) {
    if (TAB_PAGE_PATHS.has(link)) {
      uni.switchTab({ url: link })
    } else {
      uni.navigateTo({ url: link })
    }
    return
  }
  if (/^https?:\/\//i.test(link)) {
    // #ifdef H5
    window.open(link, '_blank')
    // #endif
    // #ifndef H5
    uni.setClipboardData({
      data: link,
      success: () => uni.showToast({ title: '链接已复制', icon: 'none' })
    })
    // #endif
  }
}

const TAB_PAGE_PATHS = new Set([
  '/pages/home/index',
  '/pages/project/index',
  '/pages/activity/index',
  '/pages/mine/index'
])

function onMenuTap(item) {
  if (item.path === 'project-collection') {
    uni.navigateTo({ url: '/pages/project/collection/index' })
    return
  }
  if (item.path === '/pages/auth/investor/index') {
    openInvestorAuth()
    return
  }
  if (item.path === '/pages/coverage/apply/index') {
    openCoverageApply()
    return
  }
  if (item.path === '/pages/project/onboard/index') {
    openProjectOnboard()
    return
  }
  if (item.requireLogin && item.path?.startsWith('/pages')) {
    requireLogin(item.path)
    return
  }
  if (item.path?.startsWith('/pages')) {
    if (TAB_PAGE_PATHS.has(item.path)) {
      uni.switchTab({ url: item.path })
    } else {
      uni.navigateTo({ url: item.path })
    }
    return
  }
  uni.showToast({ title: item.title, icon: 'none' })
}

function goProjectDetail(p) {
  navigateToProjectDetail(p?.id, { from: PROJECT_ENTRY.HOME })
}

function onFinancingEventTap(ev) {
  navigateFromFinancingEvent(ev)
}

function goCollectionList() {
  uni.navigateTo({ url: '/pages/project/collection/index' })
}

function goProjectLibrary() {
  uni.navigateTo({ url: '/pages/project/library/index' })
}

function goFinancingProjects() {
  uni.navigateTo({ url: '/pages/project/financing/index' })
}

function goFinancingEvents() {
  uni.navigateTo({ url: '/pages/financing/events/index' })
}

function goCollectionDetail(c) {
  const id = c?.id
  if (!id) {
    goCollectionList()
    return
  }
  uni.navigateTo({ url: `/pages/project/collection/detail?id=${id}` })
}

function goNewsList() {
  uni.navigateTo({ url: '/pages/news/index' })
}

function onNewsTap(item) {
  if (!item?.id) return
  uni.navigateTo({ url: `/pages/news/detail?id=${item.id}` })
}

function goActivityList() {
  uni.switchTab({ url: '/pages/activity/index' })
}

function onActivityTap(item) {
  if (!item?.id) return
  uni.navigateTo({ url: `/pages/activity/detail/index?id=${item.id}` })
}

function toastMore(name) {
  uni.showToast({ title: `${name} - 更多`, icon: 'none' })
}
</script>

<style lang="scss" scoped>
@import '@/styles/theme.scss';
@import '@/styles/scroll-top-spacer.scss';

.home-page {
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

.header-wrap {
  background: $theme-home-header-gradient;
  padding: $home-header-padding-y $home-header-padding-x $home-header-padding-bottom;
  padding-top: calc(#{$tab-nav-height} + #{$home-header-status-extra});
}

.brand-title {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  color: #fff;
}

.brand-slogan {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.85);
  margin-top: 8rpx;
  display: block;
}

.search-bar {
  margin-top: $home-header-search-gap;
  background: #fff;
  border-radius: 28rpx;
  height: 55rpx;
  display: flex;
  align-items: center;
  padding: 0 28rpx;
  gap: 14rpx;

  .placeholder {
    font-size: 28rpx;
    color: #999;
  }
}

.scroll-body {
  padding-bottom: calc(100rpx + env(safe-area-inset-bottom));
}

.menu-grid {
  display: flex;
  flex-wrap: wrap;
  background: #fff;
  margin: 20rpx 24rpx;
  border-radius: 16rpx;
  padding: 24rpx 0 8rpx;
}

.menu-item {
  width: 20%;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 24rpx;
}

.menu-icon {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.menu-icon-img {
  width: 56rpx;
  height: 56rpx;
}

.menu-text {
  font-size: 22rpx;
  color: #333;
  margin-top: 12rpx;
  text-align: center;
}

.banner-swiper {
  height: 300rpx;
  margin: 0 24rpx 16rpx;
}

.banner-item {
  position: relative;
  height: 280rpx;
}

.banner-mask {
  position: absolute;
  left: 24rpx;
  bottom: 24rpx;
  right: 24rpx;

  .b-title {
    color: #fff;
    font-size: 34rpx;
    font-weight: 700;
    display: block;
  }

  .b-sub {
    color: rgba(255, 255, 255, 0.85);
    font-size: 24rpx;
    margin-top: 8rpx;
    display: block;
  }
}

.financing-block {
  margin: 0 24rpx 24rpx;
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;

  .view-all-btn.inner {
    margin: 0;
    border-radius: 0;
    background: #fff;
    border-top: 1rpx solid #f0f0f0;
  }
}

.fin-list {
  padding: 0 28rpx;
}

.fin-item {
  display: flex;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.fin-info {
  margin-left: 20rpx;
  flex: 1;
  min-width: 0;
}

.fin-top {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.fin-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #333;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.fin-desc {
  font-size: 26rpx;
  color: #999;
  margin-top: 8rpx;
  line-height: 1.4;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-block {
  margin: 0 24rpx 24rpx;
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;

  .view-all-btn.inner {
    margin: 0;
    border-radius: 0;
    background: #fff;
    border-top: 1rpx solid #f0f0f0;
  }
}

.activity-list {
  padding: 0 28rpx;
}

.activity-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.act-cover {
  flex-shrink: 0;
  width: 240rpx;
  height: 135rpx;
}

.act-title {
  flex: 1;
  margin-left: 20rpx;
  font-size: 28rpx;
  font-weight: 700;
  color: #333;
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.collection-feature {
  margin: 0 24rpx 16rpx;
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  position: relative;
}

.cf-badge {
  position: absolute;
  right: 40rpx;
  bottom: 200rpx;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  font-size: 22rpx;
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
}

.cf-title {
  font-size: 32rpx;
  font-weight: 700;
  margin-top: 16rpx;
  display: block;
}

.cf-summary {
  font-size: 26rpx;
  color: #666;
  margin-top: 12rpx;
  line-height: 1.5;
  display: block;
}

.coll-proj {
  display: flex;
  background: #fff;
  margin: 0 24rpx 12rpx;
  padding: 24rpx 28rpx;
  border-radius: 16rpx;
}

.cp-info {
  margin-left: 20rpx;
  flex: 1;
}

.cp-top {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.cp-name {
  font-size: 30rpx;
  font-weight: 700;
}

.cp-desc {
  font-size: 26rpx;
  color: #999;
  margin-top: 8rpx;
}

.view-all-btn {
  margin: 8rpx 24rpx 24rpx;
  text-align: center;
  padding: 20rpx;
  background: #f0f0f0;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #666;

  &.primary {
    background: #C9E5E1;
    color: #78B9B1;
  }
}

.entry-grid {
  display: flex;
  flex-wrap: wrap;
  padding: 0 24rpx;
  gap: 16rpx;
}

.entry-card {
  display: flex;
  flex-wrap: wrap;
  width: calc(50% - 8rpx);
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  box-sizing: border-box;
}

.entry-info {
  margin-left: 12rpx;
  flex: 1;
  min-width: 0;
}

.entry-top {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.entry-name {
  font-size: 28rpx;
  font-weight: 700;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.entry-desc {
  width: 100%;
  font-size: 24rpx;
  color: #999;
  margin-top: 12rpx;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

</style>
