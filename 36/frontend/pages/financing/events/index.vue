<template>
  <view class="library-page" :class="{ 'filter-open': activePanel || showDrawer }">
    <LibraryPageHero title="大学生创投融资事件库" />

    <view class="library-filter-anchor">
      <view class="filter-bar financing-filter-bar">
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
        <view
          class="filter-item"
          :class="{ active: activePanel === 'region' || hasFilterSelection(filters.region) || filters.regionScope === 'overseas' }"
          @click="togglePanel('region')"
        >
          <text class="filter-text">{{ regionBarText }}</text>
          <u-icon
            :name="activePanel === 'region' ? 'arrow-up' : 'arrow-down'"
            size="22rpx"
            :color="activePanel === 'region' || hasFilterSelection(filters.region) || filters.regionScope === 'overseas' ? '#78B9B1' : '#999'"
          />
        </view>
        <view class="filter-divider" />
        <view
          class="filter-item filter-more"
          :class="{ active: showDrawer || drawerActive }"
          @click="openDrawer"
        >
          <text class="filter-text">筛选</text>
          <u-icon name="list" size="28rpx" :color="showDrawer || drawerActive ? '#78B9B1' : '#999'" />
        </view>
      </view>

      <view class="result-banner">
        <text>共 </text>
        <text class="result-num">{{ displayTotal }}</text>
        <text> 筛选结果</text>
      </view>

      <LibraryFilterDropdown
        :show="!!activePanel"
        @close="closePanel"
        @reset="resetCurrentPanel"
        @confirm="confirmCurrentPanel"
      >
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

        <view v-if="activePanel === 'region'" class="region-panel">
          <scroll-view scroll-y class="region-left">
            <view
              class="region-zone"
              :class="{ selected: temp.regionZone === 'china' }"
              @click="onRegionZone('china')"
            >
              中国
            </view>
            <view
              class="region-zone"
              :class="{ selected: temp.regionZone === 'overseas' }"
              @click="onRegionZone('overseas')"
            >
              海外
            </view>
          </scroll-view>
          <scroll-view v-if="temp.regionZone === 'china'" scroll-y class="region-right">
            <view
              class="dropdown-option"
              :class="{ selected: !hasFilterSelection(temp.region) }"
              @click="clearRegionSelection"
            >
              不限
            </view>
            <view
              v-for="item in CHINA_PROVINCES"
              :key="item"
              class="dropdown-option"
              :class="{ selected: isFilterSelected(temp.region, item) }"
              @click="toggleRegionProvince(item)"
            >
              {{ item }}
            </view>
          </scroll-view>
          <view v-else class="region-right region-right-empty" />
        </view>
      </LibraryFilterDropdown>
    </view>

    <view class="list-area">
      <scroll-view
        scroll-y
        class="list-scroll"
        :scroll-y="!activePanel"
        :scroll-top="scrollTop"
        :lower-threshold="120"
        @scrolltolower="onScrollToLower"
      >
        <FinancingEventCard
          v-for="item in eventList"
          :key="item.id"
          :item="item"
          @click="onEventTap"
        />
        <u-loadmore :status="loadStatus" margin-top="20" margin-bottom="30" />
        <u-empty v-if="!loading && eventList.length === 0" text="暂无融资事件" mode="list" />
      </scroll-view>
    </view>
  </view>

  <!-- 右侧高级筛选：置于 library-page 外，避免 u-popup 的 flex:1 挤压列表区 -->
  <u-popup
    :show="showDrawer"
    mode="right"
    :safe-area-inset-bottom="true"
    @close="showDrawer = false"
  >
    <view class="drawer-panel">
      <view class="drawer-head">
        <text class="drawer-title">筛选</text>
        <u-icon name="close" size="40rpx" color="#666" @click="showDrawer = false" />
      </view>
      <scroll-view scroll-y class="drawer-body">
        <view class="drawer-body-inner">
          <view class="drawer-section">
          <view class="drawer-label-row">
            <text class="drawer-label">融资轮次</text>
            <view class="expand-btn" @click="roundExpanded = !roundExpanded">
              <text>{{ roundExpanded ? '收起' : '展开' }}</text>
              <u-icon :name="roundExpanded ? 'arrow-up' : 'arrow-down'" size="24rpx" color="#999" />
            </view>
          </view>
          <view class="tag-grid">
            <view
              class="tag-item"
              :class="{ active: !hasFilterSelection(temp.round) }"
              @click="temp.round = []"
            >
              不限
            </view>
            <view
              v-for="r in displayRounds"
              :key="r"
              class="tag-item"
              :class="{ active: isFilterSelected(temp.round, r) }"
              @click="temp.round = toggleFilterValue(temp.round, r)"
            >
              {{ r }}
            </view>
          </view>
          </view>

          <view class="drawer-section">
          <view class="drawer-label-row">
            <text class="drawer-label">融资时间</text>
            <view class="expand-btn" @click="yearExpanded = !yearExpanded">
              <text>{{ yearExpanded ? '收起' : '展开' }}</text>
              <u-icon :name="yearExpanded ? 'arrow-up' : 'arrow-down'" size="24rpx" color="#999" />
            </view>
          </view>
          <view class="tag-grid">
            <view
              class="tag-item"
              :class="{ active: !hasFilterSelection(temp.financingYear) }"
              @click="temp.financingYear = []"
            >
              不限
            </view>
            <view
              v-for="y in displayYears"
              :key="y"
              class="tag-item"
              :class="{ active: isFilterSelected(temp.financingYear, y) }"
              @click="temp.financingYear = toggleFilterValue(temp.financingYear, y)"
            >
              {{ y }}
            </view>
          </view>
        </view>

        <view class="drawer-section">
          <text class="drawer-label">货币币种</text>
          <view class="tag-grid">
            <view
              class="tag-item"
              :class="{ active: !hasFilterSelection(temp.currency) }"
              @click="temp.currency = []"
            >
              不限
            </view>
            <view
              v-for="c in currencyOptions"
              :key="c"
              class="tag-item"
              :class="{ active: isFilterSelected(temp.currency, c) }"
              @click="temp.currency = toggleFilterValue(temp.currency, c)"
            >
              {{ c }}
            </view>
          </view>
        </view>
        </view>
      </scroll-view>
      <view class="drawer-footer">
        <view class="foot-btn reset" @click="resetDrawer">重置</view>
        <view class="foot-btn confirm" @click="confirmDrawer">确定</view>
      </view>
    </view>
  </u-popup>
</template>

<script setup>
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import LibraryFilterDropdown from '@/components/LibraryFilterDropdown/LibraryFilterDropdown.vue'
import LibraryPageHero from '@/components/LibraryPageHero/LibraryPageHero.vue'
import FinancingEventCard from '@/components/FinancingEventCard/FinancingEventCard.vue'
import { fetchFinancingEvents } from '@/api/financing.js'
import { fetchFilterBundle } from '@/api/filter.js'
import { SUCCESS_CODE } from '@/utils/request.js'
import { navigateFromFinancingEvent } from '@/utils/projectNavigate.js'
import { INDUSTRY_OPTIONS as DEFAULT_INDUSTRY_OPTIONS, CHINA_PROVINCES } from '@/utils/projectFilterData.js'
import {
  EVENT_ROUND_OPTIONS as DEFAULT_EVENT_ROUND_OPTIONS,
  FINANCING_YEAR_OPTIONS as DEFAULT_FINANCING_YEAR_OPTIONS,
  CURRENCY_OPTIONS as DEFAULT_CURRENCY_OPTIONS
} from '@/utils/financingFilterData.js'
import {
  toggleFilterValue,
  isFilterSelected,
  hasFilterSelection,
  serializeFilterValues
} from '@/utils/filterMulti.js'

const industryOptions = ref([...DEFAULT_INDUSTRY_OPTIONS])
const eventRoundOptions = ref([...DEFAULT_EVENT_ROUND_OPTIONS])
const financingYearOptions = ref([...DEFAULT_FINANCING_YEAR_OPTIONS])
const currencyOptions = ref([...DEFAULT_CURRENCY_OPTIONS])

const activePanel = ref('')
const showDrawer = ref(false)
const roundExpanded = ref(false)
const yearExpanded = ref(false)
const loading = ref(false)

const filters = reactive({
  industry: [],
  region: [],
  regionScope: '',
  round: [],
  financingYear: [],
  currency: []
})

const temp = reactive({
  industry: [],
  region: [],
  regionZone: 'china',
  round: [],
  financingYear: [],
  currency: []
})

const eventList = ref([])
const displayTotal = ref(0)
const page = ref(1)
const hasMore = ref(true)
const loadStatus = ref('loadmore')
const scrollTop = ref(0)
/** 首屏渲染完成前忽略 scrolltolower，避免未滑动就连续加载到底 */
const listReady = ref(false)

let countTimer = null

const drawerActive = computed(
  () =>
    !!(
      hasFilterSelection(filters.round) ||
      hasFilterSelection(filters.financingYear) ||
      hasFilterSelection(filters.currency)
    )
)

const industryBarText = computed(() => '所属行业')
const regionBarText = computed(() => '所在地区')

const displayRounds = computed(() =>
  roundExpanded.value ? eventRoundOptions.value : eventRoundOptions.value.slice(0, 9)
)

const displayYears = computed(() =>
  yearExpanded.value ? financingYearOptions.value : financingYearOptions.value.slice(0, 5)
)

function resolveRegionScope(regionList, regionZone) {
  return hasFilterSelection(regionList) ? '' : regionZone === 'overseas' ? 'overseas' : ''
}

function buildPayload(extra = {}) {
  return {
    industry: serializeFilterValues(filters.industry),
    region: serializeFilterValues(filters.region),
    regionScope: filters.regionScope,
    round: serializeFilterValues(filters.round),
    financingYear: serializeFilterValues(filters.financingYear),
    currency: serializeFilterValues(filters.currency),
    ...extra
  }
}

function buildEffectivePayload() {
  let industry = filters.industry
  let region = filters.region
  let regionScope = filters.regionScope
  let round = filters.round
  let financingYear = filters.financingYear
  let currency = filters.currency

  if (activePanel.value === 'industry') industry = temp.industry
  if (activePanel.value === 'region') {
    region = temp.region
    regionScope = resolveRegionScope(temp.region, temp.regionZone)
  }
  if (showDrawer.value) {
    round = temp.round
    financingYear = temp.financingYear
    currency = temp.currency
  }

  return buildPayload({
    industry: serializeFilterValues(industry),
    region: serializeFilterValues(region),
    regionScope,
    round: serializeFilterValues(round),
    financingYear: serializeFilterValues(financingYear),
    currency: serializeFilterValues(currency)
  })
}

async function refreshDisplayTotal() {
  clearTimeout(countTimer)
  countTimer = setTimeout(async () => {
    const res = await fetchFinancingEvents(1, 1, buildEffectivePayload())
    if (res.code === SUCCESS_CODE) {
      displayTotal.value = res.data.total ?? 0
    }
  }, 120)
}

watch(
  () => [
    serializeFilterValues(temp.industry),
    serializeFilterValues(temp.region),
    temp.regionZone,
    serializeFilterValues(temp.round),
    serializeFilterValues(temp.financingYear),
    serializeFilterValues(temp.currency),
    activePanel.value,
    showDrawer.value
  ],
  refreshDisplayTotal
)

function syncTempFromFilters() {
  temp.industry = [...filters.industry]
  temp.region = [...filters.region]
  temp.regionZone = filters.regionScope === 'overseas' ? 'overseas' : 'china'
  temp.round = [...filters.round]
  temp.financingYear = [...filters.financingYear]
  temp.currency = [...filters.currency]
}

function togglePanel(name) {
  if (activePanel.value === name) {
    closePanel()
    return
  }
  showDrawer.value = false
  syncTempFromFilters()
  activePanel.value = name
}

function closePanel() {
  activePanel.value = ''
}

function onRegionZone(zone) {
  temp.regionZone = zone
  temp.region = []
  refreshDisplayTotal()
}

function clearRegionSelection() {
  temp.region = []
  temp.regionZone = 'china'
  refreshDisplayTotal()
}

function toggleRegionProvince(item) {
  temp.regionZone = 'china'
  temp.region = toggleFilterValue(temp.region, item)
  refreshDisplayTotal()
}

function resetCurrentPanel() {
  if (activePanel.value === 'industry') temp.industry = []
  if (activePanel.value === 'region') {
    temp.region = []
    temp.regionZone = 'china'
  }
  confirmCurrentPanel()
}

function confirmCurrentPanel() {
  if (activePanel.value === 'industry') filters.industry = [...temp.industry]
  if (activePanel.value === 'region') {
    filters.region = [...temp.region]
    filters.regionScope = resolveRegionScope(temp.region, temp.regionZone)
  }
  closePanel()
  applyFilters()
}

function openDrawer() {
  closePanel()
  syncTempFromFilters()
  showDrawer.value = true
}

function resetDrawer() {
  temp.round = []
  temp.financingYear = []
  temp.currency = []
  confirmDrawer()
}

function confirmDrawer() {
  filters.round = [...temp.round]
  filters.financingYear = [...temp.financingYear]
  filters.currency = [...temp.currency]
  showDrawer.value = false
  applyFilters()
}

function applyFilters() {
  loadList(true)
}

async function loadList(reset = false) {
  if (reset) {
    page.value = 1
    hasMore.value = true
    eventList.value = []
    listReady.value = false
  }
  if (!hasMore.value && !reset) return

  loading.value = true
  loadStatus.value = 'loading'

  const res = await fetchFinancingEvents(page.value, 10, buildEffectivePayload())

  loading.value = false
  if (res.code === SUCCESS_CODE) {
    const { list, total, hasMore: more } = res.data
    eventList.value = reset ? list : [...eventList.value, ...list]
    displayTotal.value = total
    hasMore.value = more
    page.value += 1
    loadStatus.value = more ? 'loadmore' : 'nomore'
    if (reset) {
      await nextTick()
      scrollTop.value = 0
      setTimeout(() => {
        listReady.value = true
      }, 300)
    }
  } else {
    loadStatus.value = 'loadmore'
    if (reset) {
      listReady.value = true
    }
  }
}

function onScrollToLower() {
  if (!listReady.value || activePanel.value) return
  loadMore()
}

function loadMore() {
  if (loadStatus.value === 'loading' || !listReady.value) return
  loadList()
}

function onEventTap(item) {
  navigateFromFinancingEvent(item)
}

async function loadFilterOptions() {
  const res = await fetchFilterBundle('financing-events')
  if (res.code !== SUCCESS_CODE) return
  const data = res.data || {}
  if (Array.isArray(data.industry) && data.industry.length) {
    industryOptions.value = data.industry
  }
  if (Array.isArray(data.round) && data.round.length) {
    eventRoundOptions.value = data.round
  }
  if (Array.isArray(data.financingYear) && data.financingYear.length) {
    financingYearOptions.value = data.financingYear
  }
  if (Array.isArray(data.currency) && data.currency.length) {
    currencyOptions.value = data.currency
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

.financing-filter-bar .filter-item {
  flex: 1;
}

.filter-more {
  flex: 0.9;
  gap: 8rpx;
}
</style>
