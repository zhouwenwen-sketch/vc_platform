<template>
  <view
    class="detail-page"
    :class="{ 'filter-open': activePanel }"
    style="--filter-accent: #78B9B1"
  >
    <view class="search-bar">
      <view class="search-input-wrap">
        <u-icon name="search" size="28rpx" color="#999" class="search-icon" />
        <input
          v-model="searchKeyword"
          class="search-input"
          placeholder="公司/项目名/投资机构/赛道"
          @input="onKeywordInput"
          @confirm="applySearch"
        />
      </view>
    </view>

    <view class="page-body">
      <view class="hero-block">
        <image v-if="detail.cover" class="hero-cover" :src="detail.cover" mode="aspectFill" />
        <view v-if="detail.cover" class="hero-cover-mask" />
        <text v-if="detail.cover" class="hero-cover-title">{{ displayCoverTitle(detail) }}</text>
        <view v-if="detail.badge" class="hero-badge" @click.stop>
          <u-icon name="bookmark" size="24rpx" color="#78B9B1" />
          <text>{{ detail.badge }}</text>
          <u-icon name="arrow-right" size="22rpx" color="#78B9B1" />
        </view>
      </view>

      <view class="desc-block">
        <text class="desc-text" :class="{ collapsed: !descExpanded }">{{ detail.description }}</text>
        <text v-if="needExpand" class="expand-btn" @click="descExpanded = !descExpanded">
          {{ descExpanded ? '收起' : '展开' }}
        </text>
      </view>

      <view class="library-filter-anchor">
        <view class="filter-bar collection-filter-bar">
          <view
            class="filter-item"
            :class="{ active: activePanel === 'round' || hasFilterSelection(filters.round) }"
            @click="togglePanel('round')"
          >
            <text class="filter-text">{{ roundBarText }}</text>
            <u-icon
              :name="activePanel === 'round' ? 'arrow-up' : 'arrow-down'"
              size="22rpx"
              :color="activePanel === 'round' || hasFilterSelection(filters.round) ? '#78B9B1' : '#999'"
            />
          </view>
          <view
            class="filter-item"
            :class="{ active: activePanel === 'industry' || hasFilterSelection(filters.industry) }"
            @click="togglePanel('industry')"
          >
            <text class="filter-text">{{ industryBarText }}</text>
            <u-icon
              :name="activePanel === 'industry' ? 'arrow-up' : 'arrow-down'"
              size="22rpx"
              :color="activePanel === 'industry' || hasFilterSelection(filters.industry) ? '#78B9B1' : '#999'"
            />
          </view>
        </view>

        <view class="sort-bar">
          <view class="result-count">
            <text>共 </text>
            <text class="result-num">{{ displayTotal }}</text>
            <text> 个项目</text>
          </view>
        </view>

        <LibraryFilterDropdown
          :show="!!activePanel"
          @close="closePanel"
          @reset="resetCurrentPanel"
          @confirm="confirmCurrentPanel"
        >
          <scroll-view
            v-if="activePanel === 'round'"
            scroll-y
            class="dropdown-scroll single-col"
            :show-scrollbar="false"
          >
            <view
              class="dropdown-option"
              :class="{ selected: !hasFilterSelection(temp.round) }"
              @click="temp.round = []"
            >
              不限
            </view>
            <view
              v-for="item in roundOptions"
              :key="item"
              class="dropdown-option"
              :class="{ selected: isFilterSelected(temp.round, item) }"
              @click="temp.round = toggleFilterValue(temp.round, item)"
            >
              {{ item }}
            </view>
          </scroll-view>

          <scroll-view
            v-if="activePanel === 'industry'"
            scroll-y
            class="dropdown-scroll single-col"
            :show-scrollbar="false"
          >
            <view
              class="dropdown-option"
              :class="{ selected: !hasFilterSelection(temp.industry) }"
              @click="temp.industry = []"
            >
              不限
            </view>
            <view
              v-for="item in industryOptions"
              :key="item"
              class="dropdown-option"
              :class="{ selected: isFilterSelected(temp.industry, item) }"
              @click="temp.industry = toggleFilterValue(temp.industry, item)"
            >
              {{ item }}
            </view>
          </scroll-view>
        </LibraryFilterDropdown>
      </view>

      <view v-for="item in projectList" :key="item.id" class="proj-row">
        <view class="proj-main" @click="goCompany(item)">
          <ProjectLogo :item="item" />
          <view class="proj-info">
            <view class="proj-head">
              <text class="proj-name">{{ item.name }}</text>
              <text v-if="item.round" class="round-tag">{{ item.round }}</text>
            </view>
            <text class="proj-desc">{{ item.companyDesc }}</text>
            <text class="proj-meta">{{ item.metaLine }}</text>
          </view>
        </view>
        <view class="contact-btn" @click="onContact(item)">联系</view>
      </view>

      <u-loadmore :status="loadStatus" margin-top="12" margin-bottom="40" />
      <u-empty v-if="!loading && projectList.length === 0" text="暂无项目" mode="list" />
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { onLoad, onReachBottom } from '@dcloudio/uni-app'
import LibraryFilterDropdown from '@/components/LibraryFilterDropdown/LibraryFilterDropdown.vue'
import ProjectLogo from '@/components/ProjectLogo/ProjectLogo.vue'
import { fetchProjectCollectionById } from '@/api/project.js'
import { fetchFilterBundle } from '@/api/filter.js'
import { SUCCESS_CODE } from '@/utils/request.js'
import { displayCoverTitle } from '@/utils/projectCollection.js'
import { navigateToProjectDetail, PROJECT_ENTRY } from '@/utils/projectNavigate.js'
import {
  ROUND_OPTIONS as DEFAULT_ROUND_OPTIONS,
  INDUSTRY_OPTIONS as DEFAULT_INDUSTRY_OPTIONS
} from '@/utils/projectFilterData.js'
import {
  toggleFilterValue,
  isFilterSelected,
  hasFilterSelection,
  serializeFilterValues
} from '@/utils/filterMulti.js'

const roundOptions = ref([...DEFAULT_ROUND_OPTIONS])
const industryOptions = ref([...DEFAULT_INDUSTRY_OPTIONS])

const collectionId = ref('')
const searchKeyword = ref('')
const activePanel = ref('')
const detail = ref({
  title: '',
  cover: '',
  coverTitle: '',
  badge: '',
  description: ''
})
const projectList = ref([])
const displayTotal = ref(0)
const page = ref(1)
const hasMore = ref(true)
const loadStatus = ref('loadmore')
const loading = ref(false)
const descExpanded = ref(false)

const filters = reactive({
  round: [],
  industry: []
})

const temp = reactive({
  round: [],
  industry: []
})

let countTimer = null

const roundBarText = computed(() => '轮次')
const industryBarText = computed(() => '行业')
const needExpand = computed(() => (detail.value.description || '').length > 72)

function buildEffectivePayload() {
  let round = filters.round
  let industry = filters.industry
  if (activePanel.value === 'round') round = temp.round
  if (activePanel.value === 'industry') industry = temp.industry
  return {
    keyword: searchKeyword.value.trim(),
    round: serializeFilterValues(round),
    industry: serializeFilterValues(industry)
  }
}

async function refreshDisplayTotal() {
  if (!collectionId.value) return
  clearTimeout(countTimer)
  countTimer = setTimeout(async () => {
    const res = await fetchProjectCollectionById(collectionId.value, 1, 1, buildEffectivePayload())
    if (res.code === SUCCESS_CODE) {
      displayTotal.value = res.data.total ?? 0
    }
  }, 120)
}

watch(
  () => [
    serializeFilterValues(temp.round),
    serializeFilterValues(temp.industry),
    searchKeyword.value,
    activePanel.value
  ],
  refreshDisplayTotal
)

function syncTempFromFilters() {
  temp.round = [...filters.round]
  temp.industry = [...filters.industry]
}

function togglePanel(name) {
  if (activePanel.value === name) {
    closePanel()
    return
  }
  syncTempFromFilters()
  activePanel.value = name
}

function closePanel() {
  activePanel.value = ''
}

function resetCurrentPanel() {
  if (activePanel.value === 'round') temp.round = []
  if (activePanel.value === 'industry') temp.industry = []
  confirmCurrentPanel()
}

function confirmCurrentPanel() {
  if (activePanel.value === 'round') filters.round = [...temp.round]
  if (activePanel.value === 'industry') filters.industry = [...temp.industry]
  closePanel()
  loadDetail(true)
}

function onKeywordInput() {
  refreshDisplayTotal()
}

function applySearch() {
  loadDetail(true)
}

async function loadDetail(reset = false) {
  if (!collectionId.value) return
  if (reset) {
    page.value = 1
    hasMore.value = true
    projectList.value = []
  }
  if (!hasMore.value && !reset) return

  loading.value = true
  loadStatus.value = 'loading'

  const res = await fetchProjectCollectionById(collectionId.value, page.value, 20, buildEffectivePayload())

  loading.value = false
  if (res.code === SUCCESS_CODE) {
    const d = res.data
    if (reset) {
      detail.value = {
        title: d.title,
        cover: d.cover,
        coverTitle: d.coverTitle || '',
        badge: d.badge,
        description: d.description || d.summary || ''
      }
      uni.setNavigationBarTitle({ title: d.title || '项目集' })
    }
    const { list, total, hasMore: more } = res.data
    projectList.value = reset ? list : [...projectList.value, ...list]
    displayTotal.value = total
    hasMore.value = more
    page.value += 1
    loadStatus.value = more ? 'loadmore' : 'nomore'
  } else {
    loadStatus.value = 'loadmore'
  }
}

function loadMore() {
  if (loadStatus.value === 'loading' || activePanel.value) return
  loadDetail()
}

function goCompany(item) {
  if (!item?.id) return
  navigateToProjectDetail(item.id, { from: PROJECT_ENTRY.COLLECTION })
}

function onContact() {
  uni.showToast({ title: '联系功能开发中', icon: 'none' })
}

async function loadFilterOptions() {
  const res = await fetchFilterBundle('collection-detail')
  if (res.code !== SUCCESS_CODE) return
  const data = res.data || {}
  if (Array.isArray(data.industry) && data.industry.length) {
    industryOptions.value = data.industry
  }
  if (Array.isArray(data.round) && data.round.length) {
    roundOptions.value = data.round
  }
}

onLoad(async (query) => {
  collectionId.value = query?.id || ''
  syncTempFromFilters()
  await loadFilterOptions()
  loadDetail(true)
})

onReachBottom(() => loadMore())
</script>

<style lang="scss" scoped>
@import '@/styles/library-page.scss';

$blue: #78B9B1;

.detail-page {
  min-height: 100vh;
  background: #fff;
  padding-bottom: 40rpx;

  &.filter-open {
    overflow: hidden;
    height: 100vh;
  }
}

.search-bar {
  flex-shrink: 0;
  padding: 16rpx 24rpx;
  background: #fff;
}

.hero-block {
  position: relative;
  margin: 0 24rpx 20rpx;
  border-radius: 16rpx;
  overflow: hidden;
}

.hero-cover {
  width: 100%;
  height: 280rpx;
  display: block;
}

.hero-cover-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.12) 0%, rgba(0, 0, 0, 0.52) 100%);
}

.hero-cover-title {
  position: absolute;
  left: 28rpx;
  right: 28rpx;
  top: 50%;
  transform: translateY(-50%);
  font-size: 36rpx;
  font-weight: 800;
  color: #fff;
  line-height: 1.35;
  white-space: pre-line;
  text-align: center;
  z-index: 1;
}

.hero-badge {
  position: absolute;
  left: 16rpx;
  top: 16rpx;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 10rpx 20rpx;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 8rpx;
  font-size: 24rpx;
  color: $blue;
}

.desc-block {
  padding: 0 24rpx 24rpx;
  position: relative;
}

.desc-text {
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
  display: block;

  &.collapsed {
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
  }
}

.expand-btn {
  position: absolute;
  right: 24rpx;
  bottom: 24rpx;
  font-size: 28rpx;
  color: $blue;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0), #fff 30%);
  padding-left: 48rpx;
}

.library-filter-anchor {
  background: #fff;
}

.collection-filter-bar {
  justify-content: flex-end;
  padding-right: 24rpx;
  padding-left: 24rpx;

  .filter-item {
    flex: none;
    min-width: 140rpx;
  }
}

.sort-bar {
  border-bottom: 8rpx solid #f5f6f8;
}

.proj-row {
  display: flex;
  align-items: center;
  padding: 28rpx 24rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

.proj-main {
  flex: 1;
  display: flex;
  min-width: 0;
}

.proj-info {
  flex: 1;
  margin-left: 20rpx;
  min-width: 0;
}

.proj-head {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 8rpx;
}

.proj-name {
  font-size: 32rpx;
  font-weight: 700;
  color: #222;
}

.round-tag {
  font-size: 22rpx;
  color: $blue;
  border: 1rpx solid $blue;
  border-radius: 6rpx;
  padding: 2rpx 12rpx;
  flex-shrink: 0;
}

.proj-desc {
  font-size: 26rpx;
  color: #666;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.proj-meta {
  font-size: 24rpx;
  color: #999;
  margin-top: 6rpx;
  display: block;
}

.contact-btn {
  flex-shrink: 0;
  margin-left: 16rpx;
  padding: 12rpx 28rpx;
  font-size: 26rpx;
  color: $blue;
  background: #C9E5E1;
  border-radius: 32rpx;
}
</style>
