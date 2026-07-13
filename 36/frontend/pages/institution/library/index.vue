<template>
  <view class="library-page" :class="{ 'filter-open': activePanel }">
    <LibraryPageHero title="大学生创投机构库" />

    <view class="library-filter-anchor">
      <view class="search-bar">
        <view class="search-input-wrap">
          <u-icon name="search" size="28rpx" color="#999" class="search-icon" />
          <input
            v-model="searchKeyword"
            class="search-input"
            placeholder="请输入机构名称、主体"
            @input="onKeywordInput"
            @confirm="applyFilters"
          />
        </view>
      </view>

      <view class="filter-bar">
        <view
          class="filter-item"
          :class="{ active: activePanel === 'field' || hasFilterSelection(filters.investmentField) }"
          @click="togglePanel('field')"
        >
          <text class="filter-text">{{ fieldBarText }}</text>
          <u-icon
            :name="activePanel === 'field' ? 'arrow-up' : 'arrow-down'"
            size="22rpx"
            :color="activePanel === 'field' || hasFilterSelection(filters.investmentField) ? FILTER_TAN : '#999'"
          />
        </view>
        <view
          class="filter-item"
          :class="{ active: activePanel === 'type' || hasFilterSelection(filters.instType) }"
          @click="togglePanel('type')"
        >
          <text class="filter-text">{{ typeBarText }}</text>
          <u-icon
            :name="activePanel === 'type' ? 'arrow-up' : 'arrow-down'"
            size="22rpx"
            :color="activePanel === 'type' || hasFilterSelection(filters.instType) ? FILTER_TAN : '#999'"
          />
        </view>
        <view
          class="filter-item"
          :class="{ active: activePanel === 'year' || hasFilterSelection(filters.foundedYear) }"
          @click="togglePanel('year')"
        >
          <text class="filter-text">{{ yearBarText }}</text>
          <u-icon
            :name="activePanel === 'year' ? 'arrow-up' : 'arrow-down'"
            size="22rpx"
            :color="activePanel === 'year' || hasFilterSelection(filters.foundedYear) ? FILTER_TAN : '#999'"
          />
        </view>
      </view>

      <view class="sort-bar">
        <view class="result-count">
          <text>共 </text>
          <text class="result-num">{{ displayTotal }}</text>
          <text> 筛选结果</text>
        </view>
      </view>

      <LibraryFilterDropdown
        :show="!!activePanel"
        @close="closePanel"
        @reset="resetCurrentPanel"
        @confirm="confirmCurrentPanel"
      >
          <scroll-view
            v-if="activePanel === 'field'"
            scroll-y
            class="dropdown-scroll single-col"
            :show-scrollbar="false"
          >
            <view
              class="dropdown-option"
              :class="{ selected: !hasFilterSelection(temp.investmentField) }"
              @click="temp.investmentField = []"
            >
              不限
            </view>
            <view
              v-for="item in investmentFieldOptions"
              :key="item"
              class="dropdown-option"
              :class="{ selected: isFilterSelected(temp.investmentField, item) }"
              @click="temp.investmentField = toggleFilterValue(temp.investmentField, item)"
            >
              {{ item }}
            </view>
          </scroll-view>

          <scroll-view
            v-if="activePanel === 'type'"
            scroll-y
            class="dropdown-scroll single-col"
          >
            <view
              class="dropdown-option"
              :class="{ selected: !hasFilterSelection(temp.instType) }"
              @click="temp.instType = []"
            >
              不限
            </view>
            <view
              v-for="item in institutionTypeOptions"
              :key="item"
              class="dropdown-option"
              :class="{ selected: isFilterSelected(temp.instType, item) }"
              @click="temp.instType = toggleFilterValue(temp.instType, item)"
            >
              {{ item }}
            </view>
          </scroll-view>

          <scroll-view
            v-if="activePanel === 'year'"
            scroll-y
            class="dropdown-scroll single-col"
          >
            <view
              class="dropdown-option"
              :class="{ selected: !hasFilterSelection(temp.foundedYear) }"
              @click="temp.foundedYear = []"
            >
              不限
            </view>
            <view
              v-for="item in INSTITUTION_YEAR_OPTIONS"
              :key="item"
              class="dropdown-option"
              :class="{ selected: isFilterSelected(temp.foundedYear, item) }"
              @click="temp.foundedYear = toggleFilterValue(temp.foundedYear, item)"
            >
              {{ item }}
            </view>
          </scroll-view>
      </LibraryFilterDropdown>
    </view>

    <view class="list-area">
    <scroll-view
      scroll-y
      class="list-scroll inst-list"
      :scroll-y="!activePanel"
      :scroll-top="scrollTop"
      @scrolltolower="loadMore"
    >
      <view v-for="item in institutionList" :key="item.id" class="inst-item" @click="goDetail(item)">
        <ProjectLogo :item="item" />
        <view class="inst-body">
          <view class="inst-head">
            <text class="inst-name">{{ item.name }}</text>
            <text v-if="item.instType" class="inst-type-tag">{{ item.instType }}</text>
          </view>
          <text class="inst-recent">最近投资：{{ item.recentInvestment || '-' }}</text>
        </view>
        <view class="inst-stat">
          <text class="stat-text">投资事件数：</text>
          <text class="stat-num">{{ item.eventCount }}</text>
        </view>
      </view>
      <u-loadmore :status="loadStatus" margin-top="20" margin-bottom="30" />
      <u-empty v-if="!loading && institutionList.length === 0" text="暂无匹配机构" mode="list" />
    </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { fetchInstitutionLibrary } from '@/api/institution.js'
import { fetchFilterBundle } from '@/api/filter.js'
import { SUCCESS_CODE } from '@/utils/request.js'
import LibraryFilterDropdown from '@/components/LibraryFilterDropdown/LibraryFilterDropdown.vue'
import LibraryPageHero from '@/components/LibraryPageHero/LibraryPageHero.vue'
import ProjectLogo from '@/components/ProjectLogo/ProjectLogo.vue'
import {
  FILTER_TAN,
  INVESTMENT_FIELD_OPTIONS as DEFAULT_INVESTMENT_FIELD_OPTIONS,
  INSTITUTION_TYPE_OPTIONS as DEFAULT_INSTITUTION_TYPE_OPTIONS,
  INSTITUTION_YEAR_OPTIONS
} from '@/utils/institutionFilterData.js'
import {
  toggleFilterValue,
  isFilterSelected,
  hasFilterSelection,
  serializeFilterValues
} from '@/utils/filterMulti.js'

const investmentFieldOptions = ref([...DEFAULT_INVESTMENT_FIELD_OPTIONS])
const institutionTypeOptions = ref([...DEFAULT_INSTITUTION_TYPE_OPTIONS])

const searchKeyword = ref('')
const activePanel = ref('')
const loading = ref(false)

const filters = reactive({
  investmentField: [],
  instType: [],
  foundedYear: []
})

const temp = reactive({
  investmentField: [],
  instType: [],
  foundedYear: []
})

const institutionList = ref([])
const displayTotal = ref(0)
const page = ref(1)
const hasMore = ref(true)
const loadStatus = ref('loadmore')
const scrollTop = ref(0)

let countTimer = null

const fieldBarText = computed(() => '投资领域')
const typeBarText = computed(() => '机构类型')
const yearBarText = computed(() => '成立时间')

function buildPayload(extra = {}) {
  return {
    keyword: searchKeyword.value.trim(),
    investmentField: serializeFilterValues(filters.investmentField),
    instType: serializeFilterValues(filters.instType),
    foundedYear: serializeFilterValues(filters.foundedYear),
    ...extra
  }
}

function buildEffectivePayload() {
  let investmentField = filters.investmentField
  let instType = filters.instType
  let foundedYear = filters.foundedYear
  if (activePanel.value === 'field') investmentField = temp.investmentField
  if (activePanel.value === 'type') instType = temp.instType
  if (activePanel.value === 'year') foundedYear = temp.foundedYear
  return buildPayload({
    investmentField: serializeFilterValues(investmentField),
    instType: serializeFilterValues(instType),
    foundedYear: serializeFilterValues(foundedYear)
  })
}

async function refreshDisplayTotal() {
  clearTimeout(countTimer)
  countTimer = setTimeout(async () => {
    const res = await fetchInstitutionLibrary(1, 1, buildEffectivePayload())
    if (res.code === SUCCESS_CODE) {
      displayTotal.value = res.data.total ?? 0
    }
  }, 120)
}

watch(
  () => [
    serializeFilterValues(temp.investmentField),
    serializeFilterValues(temp.instType),
    serializeFilterValues(temp.foundedYear),
    searchKeyword.value,
    activePanel.value
  ],
  refreshDisplayTotal
)

function syncTempFromFilters() {
  temp.investmentField = [...filters.investmentField]
  temp.instType = [...filters.instType]
  temp.foundedYear = [...filters.foundedYear]
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
  if (activePanel.value === 'field') temp.investmentField = []
  if (activePanel.value === 'type') temp.instType = []
  if (activePanel.value === 'year') temp.foundedYear = []
  confirmCurrentPanel()
}

function confirmCurrentPanel() {
  if (activePanel.value === 'field') filters.investmentField = [...temp.investmentField]
  if (activePanel.value === 'type') filters.instType = [...temp.instType]
  if (activePanel.value === 'year') filters.foundedYear = [...temp.foundedYear]
  closePanel()
  applyFilters()
}

function onKeywordInput() {
  refreshDisplayTotal()
}

function applyFilters() {
  loadList(true)
}

function goDetail(item) {
  if (!item?.id) return
  uni.navigateTo({ url: `/pages/institution/detail/index?id=${item.id}` })
}

async function loadList(reset = false) {
  if (reset) {
    page.value = 1
    hasMore.value = true
    institutionList.value = []
    scrollTop.value = 0
  }
  if (!hasMore.value && !reset) return

  loading.value = true
  loadStatus.value = 'loading'

  const pageNum = reset ? 1 : page.value
  const res = await fetchInstitutionLibrary(pageNum, 10, buildEffectivePayload())

  loading.value = false
  if (res.code === SUCCESS_CODE) {
    const { list, total, hasMore: more } = res.data
    institutionList.value = reset ? list : [...institutionList.value, ...list]
    displayTotal.value = total
    hasMore.value = more
    page.value = pageNum + 1
    loadStatus.value = more ? 'loadmore' : 'nomore'
  } else {
    loadStatus.value = 'loadmore'
  }
}

function loadMore() {
  if (loadStatus.value === 'loading') return
  loadList()
}

async function loadFilterOptions() {
  const res = await fetchFilterBundle('institution-library')
  if (res.code !== SUCCESS_CODE) return
  const data = res.data || {}
  if (Array.isArray(data.investmentField) && data.investmentField.length) {
    investmentFieldOptions.value = data.investmentField
  }
  if (Array.isArray(data.institutionType) && data.institutionType.length) {
    institutionTypeOptions.value = data.institutionType
  }
}

onLoad(async () => {
  syncTempFromFilters()
  await loadFilterOptions()
  loadList(true)
})
</script>

<style lang="scss" scoped>
@import '@/styles/library-page.scss';
@import '@/styles/theme.scss';

.inst-item {
  display: flex;
  align-items: flex-start;
  padding: 28rpx 24rpx;
  background: #fff;
  border-bottom: 1rpx solid #f0f0f0;
}

.inst-body {
  flex: 1;
  margin-left: 20rpx;
  min-width: 0;
  padding-right: 12rpx;
}

.inst-head {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 10rpx;
}

.inst-name {
  font-size: 32rpx;
  font-weight: 700;
  color: #222;
}

.inst-type-tag {
  font-size: 22rpx;
  color: $theme-primary;
  border: 1rpx solid $theme-primary;
  border-radius: 6rpx;
  padding: 2rpx 12rpx;
}

.inst-recent {
  font-size: 26rpx;
  color: #999;
  line-height: 1.4;
}

.inst-stat {
  flex-shrink: 0;
  text-align: right;
  max-width: 200rpx;
}

.stat-text {
  font-size: 24rpx;
  color: $theme-primary;
}

.stat-num {
  font-size: 28rpx;
  color: $theme-primary;
  font-weight: 600;
}
</style>
