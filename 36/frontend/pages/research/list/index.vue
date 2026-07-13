<template>
  <view class="library-page" :class="{ 'filter-open': showFilter }">
    <LibraryPageHero title="大学生创投研究院" />

    <view class="library-filter-anchor">
      <view class="filter-bar">
        <view
          class="filter-item"
          :class="{ active: showFilter && currentFilterType === 'reportType' || hasFilterSelection(filters.reportType) }"
          @click="openFilter('reportType')"
        >
          <text class="filter-text">报告类型</text>
          <u-icon
            :name="showFilter && currentFilterType === 'reportType' ? 'arrow-up' : 'arrow-down'"
            size="22rpx"
            :color="hasFilterSelection(filters.reportType) ? FILTER_TAN : '#999'"
          />
        </view>
        <view class="filter-divider" />
        <view
          class="filter-item"
          :class="{ active: showFilter && currentFilterType === 'industry' || hasFilterSelection(filters.industry) }"
          @click="openFilter('industry')"
        >
          <text class="filter-text">所属行业</text>
          <u-icon
            :name="showFilter && currentFilterType === 'industry' ? 'arrow-up' : 'arrow-down'"
            size="22rpx"
            :color="hasFilterSelection(filters.industry) ? FILTER_TAN : '#999'"
          />
        </view>
        <view class="filter-divider" />
        <view
          class="filter-item"
          :class="{ active: showFilter && currentFilterType === 'year' || hasFilterSelection(filters.year) }"
          @click="openFilter('year')"
        >
          <text class="filter-text">发布时间</text>
          <u-icon
            :name="showFilter && currentFilterType === 'year' ? 'arrow-up' : 'arrow-down'"
            size="22rpx"
            :color="hasFilterSelection(filters.year) ? FILTER_TAN : '#999'"
          />
        </view>
        <view class="filter-divider" />
        <view
          class="filter-item"
          :class="{ active: showFilter && currentFilterType === 'tag' || hasFilterSelection(filters.tag) }"
          @click="openFilter('tag')"
        >
          <text class="filter-text">特色分类</text>
          <u-icon
            :name="showFilter && currentFilterType === 'tag' ? 'arrow-up' : 'arrow-down'"
            size="22rpx"
            :color="hasFilterSelection(filters.tag) ? FILTER_TAN : '#999'"
          />
        </view>
      </view>

      <view class="sort-bar">
        <view class="result-count">
          <text>共 </text>
          <text class="result-num">{{ totalCount }}</text>
          <text> 项研究报告</text>
        </view>
      </view>

      <LibraryFilterDropdown
        :show="showFilter"
        @close="showFilter = false"
        @reset="resetFilter"
        @confirm="confirmFilter"
      >
        <scroll-view scroll-y class="dropdown-scroll single-col" :show-scrollbar="false">
          <view class="filter-section">
            <text class="section-title">{{ currentFilterTitle }}</text>
            <view
              v-for="option in currentFilterOptions"
              :key="option.value"
              class="dropdown-option"
              :class="{ selected: isCurrentOptionSelected(option) }"
              @click="selectFilterOption(option)"
            >
              {{ option.label }}
            </view>
          </view>
        </scroll-view>
      </LibraryFilterDropdown>
    </view>

    <view class="list-area">
      <scroll-view
        scroll-y
        class="list-scroll"
        @scrolltolower="loadMore"
        :scroll-with-animation="true"
      >
        <view
          v-for="item in reportList"
          :key="item.id"
          class="report-card"
          @click="goToDetail(item)"
        >
          <text class="card-title">{{ item.title }}</text>
          <view v-if="item.tagList?.length" class="card-tags">
            <text
              v-for="tag in item.tagList"
              :key="tag"
              class="report-tag"
              :class="tagClass(tag)"
            >{{ tag }}</text>
          </view>
          <text class="publish-date">{{ formatDate(item.publishDate) }}</text>
        </view>

        <u-empty v-if="!loading && reportList.length === 0" mode="list" text="暂无研究报告" />
        <u-loadmore v-else :status="loadStatus" margin-top="20" margin-bottom="30" />
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import LibraryFilterDropdown from '@/components/LibraryFilterDropdown/LibraryFilterDropdown.vue'
import LibraryPageHero from '@/components/LibraryPageHero/LibraryPageHero.vue'
import { fetchReportList } from '@/api/research.js'
import { fetchFilterBundle } from '@/api/filter.js'
import { SUCCESS_CODE } from '@/utils/request.js'
import { FILTER_TAN } from '@/utils/institutionFilterData.js'
import { INDUSTRY_OPTIONS as DEFAULT_INDUSTRY_OPTIONS } from '@/utils/projectFilterData.js'
import {
  toggleFilterValue,
  isFilterSelected,
  hasFilterSelection
} from '@/utils/filterMulti.js'

const DEFAULT_CATEGORIES = {
  reportTypes: [
    { name: '行业研究' },
    { name: '专题报告' },
    { name: '数据洞察' },
    { name: '深度分析' }
  ],
  industries: DEFAULT_INDUSTRY_OPTIONS.map((name) => ({ name })),
  tags: [
    { name: '热门赛道' },
    { name: '产业洞察' },
    { name: '前沿技术' },
    { name: '短篇洞察' },
    { name: '其他' }
  ]
}

const TAG_CLASS_MAP = {
  热门赛道: 'tag-hot',
  短研洞察: 'tag-hot',
  短篇洞察: 'tag-hot',
  产业洞察: 'tag-insight',
  前沿技术: 'tag-tech',
  其他: 'tag-tech'
}

const filters = reactive({
  reportType: [],
  industry: [],
  year: [],
  tag: []
})

const temp = reactive({
  reportType: [],
  industry: [],
  year: [],
  tag: []
})

const currentFilterType = ref('')
const showFilter = ref(false)
const loading = ref(false)
const reportList = ref([])
const totalCount = ref(0)
const page = ref(1)
const loadStatus = ref('loadmore')

const reportTypes = ref([])
const industries = ref([])
const tags = ref([])

const yearOptions = [
  { label: '全部年份', value: '' },
  { label: '2025年', value: '2025' },
  { label: '2024年', value: '2024' },
  { label: '2023年', value: '2023' },
  { label: '2022年', value: '2022' },
  { label: '2021年', value: '2021' }
]

const currentFilterTitle = computed(() => {
  const titles = {
    reportType: '报告类型',
    industry: '所属行业',
    year: '发布时间',
    tag: '特色分类'
  }
  return titles[currentFilterType.value] || ''
})

const currentFilterOptions = computed(() => {
  const allOption = { label: '全部', value: '' }
  switch (currentFilterType.value) {
    case 'reportType':
      return [allOption, ...reportTypes.value.map((item) => ({ label: item.name, value: item.name }))]
    case 'industry':
      return [allOption, ...industries.value.map((item) => ({ label: item.name, value: item.name }))]
    case 'year':
      return yearOptions
    case 'tag':
      return [allOption, ...tags.value.map((item) => ({ label: item.name, value: item.name }))]
    default:
      return []
  }
})

function isCurrentOptionSelected(option) {
  const type = currentFilterType.value
  if (!type) return false
  if (!option.value) {
    return !hasFilterSelection(temp[type])
  }
  return isFilterSelected(temp[type], option.value)
}

function syncTempFromFilters() {
  temp.reportType = [...filters.reportType]
  temp.industry = [...filters.industry]
  temp.year = [...filters.year]
  temp.tag = [...filters.tag]
}

function tagClass(tag) {
  return TAG_CLASS_MAP[tag] || 'tag-default'
}

function openFilter(type) {
  if (showFilter.value && currentFilterType.value === type) {
    showFilter.value = false
    return
  }
  syncTempFromFilters()
  currentFilterType.value = type
  showFilter.value = true
}

function selectFilterOption(option) {
  const type = currentFilterType.value
  if (!type) return
  if (!option.value) {
    temp[type] = []
    return
  }
  temp[type] = toggleFilterValue(temp[type], option.value)
}

function resetFilter() {
  const type = currentFilterType.value
  if (type) temp[type] = []
  confirmFilter()
}

function confirmFilter() {
  const type = currentFilterType.value
  if (type) filters[type] = [...temp[type]]
  showFilter.value = false
  loadList(true)
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return String(dateStr).slice(0, 10)
}

async function loadList(reset = false) {
  if (loadStatus.value === 'loading' && !reset) return

  if (reset) {
    page.value = 1
    reportList.value = []
    loadStatus.value = 'loading'
  }

  loading.value = true
  try {
    const res = await fetchReportList(page.value, 10, filters)
    if (res.code === SUCCESS_CODE) {
      const { list, total, hasMore } = res.data
      reportList.value = reset ? list : [...reportList.value, ...list]
      totalCount.value = total
      loadStatus.value = hasMore ? 'loadmore' : 'nomore'
      if (hasMore) page.value++
    } else {
      loadStatus.value = 'loadmore'
    }
  } finally {
    loading.value = false
  }
}

function loadMore() {
  if (loadStatus.value !== 'loadmore') return
  loadList()
}

function goToDetail(item) {
  if (!item?.id) return
  uni.navigateTo({ url: `/pages/research/detail/index?id=${item.id}` })
}

function applyDefaultCategories() {
  reportTypes.value = DEFAULT_CATEGORIES.reportTypes
  industries.value = DEFAULT_CATEGORIES.industries
  tags.value = DEFAULT_CATEGORIES.tags
}

async function loadFilterOptions() {
  const res = await fetchFilterBundle('research-list')
  if (res.code !== SUCCESS_CODE) return
  const data = res.data || {}
  if (Array.isArray(data.reportType) && data.reportType.length) {
    reportTypes.value = data.reportType.map((name) => ({ name }))
  }
  if (Array.isArray(data.industry) && data.industry.length) {
    industries.value = data.industry.map((name) => ({ name }))
  }
  if (Array.isArray(data.tag) && data.tag.length) {
    tags.value = data.tag.map((name) => ({ name }))
  }
}

onMounted(async () => {
  applyDefaultCategories()
  syncTempFromFilters()
  await loadFilterOptions()
  loadList(true)
})
</script>

<style lang="scss" scoped>
@import '@/styles/library-page.scss';

.report-card {
  padding: 32rpx 28rpx;
  background: #fff;
  border-bottom: 1rpx solid #f0f0f0;
}

.card-title {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: #222;
  line-height: 1.55;
  margin-bottom: 20rpx;
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.report-tag {
  font-size: 22rpx;
  line-height: 1;
  padding: 8rpx 16rpx;
  border-radius: 6rpx;
}

.tag-hot {
  color: #fa8c16;
  background: #fff7e6;
}

.tag-insight {
  color: #78B9B1;
  background: #e6f4ff;
}

.tag-tech {
  color: #52c41a;
  background: #f6ffed;
}

.tag-default {
  color: #666;
  background: #f5f5f5;
}

.publish-date {
  display: block;
  font-size: 24rpx;
  color: #999;
}

.filter-section {
  padding: 8rpx 0 16rpx;
}

.section-title {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 8rpx;
  padding: 0 24rpx;
}

.dropdown-option {
  padding: 24rpx;
  font-size: 28rpx;
  color: #333;

  &.selected {
    color: var(--filter-accent, #78B9B1);
    font-weight: 500;
    background: #C9E5E1;
  }
}
</style>
