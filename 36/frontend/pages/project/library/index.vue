<template>
  <view class="library-page" :class="{ 'filter-open': activePanel || showDrawer }">
    <LibraryPageHero title="大学生创投企业项目库" />

    <view class="library-filter-anchor">
      <view class="search-bar">
        <view class="search-input-wrap">
          <u-icon name="search" size="28rpx" color="#999" class="search-icon" />
          <input
            v-model="searchKeyword"
            class="search-input"
            placeholder="请输入项目名称关键字"
            @input="onKeywordInput"
            @confirm="applyFilters"
          />
        </view>
      </view>

      <view class="filter-bar">
        <view
          class="filter-item"
          :class="{ active: activePanel === 'industry' || hasFilterSelection(filters.industry) }"
          @click="togglePanel('industry')"
        >
          <text class="filter-text">{{ industryBarText }}</text>
          <u-icon
            :name="activePanel === 'industry' ? 'arrow-up' : 'arrow-down'"
            size="22rpx"
            :color="activePanel === 'industry' || hasFilterSelection(filters.industry) ? FILTER_TAN : '#999'"
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
            :color="activePanel === 'region' || hasFilterSelection(filters.region) || filters.regionScope === 'overseas' ? FILTER_TAN : '#999'"
          />
        </view>
        <view
          class="filter-item"
          :class="{ active: activePanel === 'round' || hasFilterSelection(filters.round) }"
          @click="togglePanel('round')"
        >
          <text class="filter-text">{{ roundBarText }}</text>
          <u-icon
            :name="activePanel === 'round' ? 'arrow-up' : 'arrow-down'"
            size="22rpx"
            :color="activePanel === 'round' || hasFilterSelection(filters.round) ? FILTER_TAN : '#999'"
          />
        </view>
        <view class="filter-divider" />
        <view
          class="filter-item filter-more"
          :class="{ active: showDrawer || drawerActive }"
          @click="openDrawer"
        >
          <text class="filter-text">筛选</text>
        </view>
      </view>

      <view class="sort-bar">
        <view class="result-count">
          <text>共 </text>
          <text class="result-num">{{ displayTotal }}</text>
          <text> 筛选结果</text>
        </view>
        <view class="sort-tabs">
          <view
            class="sort-tab"
            :class="{ active: activeSort === 'hot' }"
            @click="setHotSort"
          >
            热门对接
          </view>
          <view
            class="sort-tab"
            :class="{ active: activeSort !== 'hot' || activePanel === 'sort' }"
            @click="toggleSortPanel"
          >
            <text>{{ sortBarLabel }}</text>
            <u-icon
              :name="activePanel === 'sort' ? 'arrow-up' : 'arrow-down'"
              size="22rpx"
              :color="activeSort !== 'hot' || activePanel === 'sort' ? FILTER_TAN : '#999'"
            />
          </view>
        </view>
      </view>

      <LibraryFilterDropdown
        :show="!!activePanel"
        @close="closePanel"
        @reset="resetCurrentPanel"
        @confirm="confirmCurrentPanel"
      >
        <scroll-view v-if="activePanel === 'industry'" scroll-y class="dropdown-scroll single-col" :show-scrollbar="false">
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

        <!-- 所在地区 -->
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

        <!-- 融资轮次 -->
        <scroll-view v-if="activePanel === 'round'" scroll-y class="dropdown-scroll single-col">
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

        <!-- 项目推荐排序 -->
        <scroll-view v-if="activePanel === 'sort'" scroll-y class="dropdown-scroll single-col">
          <view
            v-for="item in SORT_OPTIONS"
            :key="item.value"
            class="dropdown-option"
            :class="{ selected: temp.sortBy === item.value }"
            @click="temp.sortBy = item.value"
          >
            {{ item.label }}
          </view>
        </scroll-view>
      </LibraryFilterDropdown>
    </view>

    <view class="list-area">
    <scroll-view
      scroll-y
      class="list-scroll"
      :scroll-y="!activePanel"
      :scroll-top="scrollTop"
      @scrolltolower="loadMore"
    >
      <ProjectLibraryCard
        v-for="item in projectList"
        :key="item.id"
        :item="item"
        @click="onProjectTap"
      />
      <u-loadmore :status="loadStatus" margin-top="20" margin-bottom="30" />
      <u-empty v-if="!loading && projectList.length === 0" text="暂无匹配项目" mode="list" />
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
            <text class="drawer-label">项目优势</text>
            <view class="tag-grid">
              <view
                class="tag-item"
                :class="{ active: !hasFilterSelection(temp.advantage) }"
                @click="temp.advantage = []"
              >
                不限
              </view>
              <view
                v-for="t in advantageOptions"
                :key="t"
                class="tag-item"
                :class="{ active: isFilterSelected(temp.advantage, t) }"
                @click="temp.advantage = toggleFilterValue(temp.advantage, t)"
              >
                {{ t }}
              </view>
            </view>
          </view>

          <view class="drawer-section">
            <view class="drawer-label-row">
              <text class="drawer-label">成立时间</text>
              <view class="expand-btn" @click="yearExpanded = !yearExpanded">
                <text>{{ yearExpanded ? '收起' : '展开' }}</text>
                <u-icon :name="yearExpanded ? 'arrow-up' : 'arrow-down'" size="24rpx" color="#999" />
              </view>
            </view>
            <view class="tag-grid">
              <view
                class="tag-item"
                :class="{ active: !hasFilterSelection(temp.foundedYear) }"
                @click="temp.foundedYear = []"
              >
                不限
              </view>
              <view
                v-for="y in displayYears"
                :key="y"
                class="tag-item"
                :class="{ active: isFilterSelected(temp.foundedYear, y) }"
                @click="temp.foundedYear = toggleFilterValue(temp.foundedYear, y)"
              >
                {{ y }}
              </view>
            </view>
          </view>

          <view class="drawer-section">
            <text class="drawer-label">大学生创投报道</text>
            <view class="tag-grid">
              <view
                class="tag-item"
                :class="{ active: !temp.lbReport }"
                @click="temp.lbReport = ''"
              >
                不限
              </view>
              <view
                v-for="r in LB_REPORT_OPTIONS"
                :key="r"
                class="tag-item"
                :class="{ active: temp.lbReport === r }"
                @click="temp.lbReport = r"
              >
                {{ r }}
              </view>
            </view>
          </view>

          <view class="drawer-section">
            <text class="drawer-label">正在融资</text>
            <view class="tag-grid">
              <view
                class="tag-item"
                :class="{ active: !temp.financing }"
                @click="temp.financing = ''"
              >
                不限
              </view>
              <view
                v-for="f in FINANCING_OPTIONS"
                :key="f"
                class="tag-item"
                :class="{ active: temp.financing === f }"
                @click="temp.financing = f"
              >
                {{ f }}
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
import { ref, reactive, computed, watch } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import LibraryFilterDropdown from '@/components/LibraryFilterDropdown/LibraryFilterDropdown.vue'
import LibraryPageHero from '@/components/LibraryPageHero/LibraryPageHero.vue'
import ProjectLibraryCard from '@/components/ProjectLibraryCard/ProjectLibraryCard.vue'
import { fetchProjectCollectionDetail } from '@/api/project.js'
import { fetchFilterBundle } from '@/api/filter.js'
import { SUCCESS_CODE } from '@/utils/request.js'
import { navigateFromProjectLibrary } from '@/utils/projectNavigate.js'
import { FILTER_TAN } from '@/utils/institutionFilterData.js'
import {
  toggleFilterValue,
  isFilterSelected,
  hasFilterSelection,
  serializeFilterValues
} from '@/utils/filterMulti.js'
import {
  INDUSTRY_OPTIONS as DEFAULT_INDUSTRY_OPTIONS,
  ROUND_OPTIONS as DEFAULT_ROUND_OPTIONS,
  CHINA_PROVINCES,
  ADVANTAGE_OPTIONS as DEFAULT_ADVANTAGE_OPTIONS,
  ESTABLISHMENT_YEAR_OPTIONS as DEFAULT_ESTABLISHMENT_YEAR_OPTIONS,
  LB_REPORT_OPTIONS,
  FINANCING_OPTIONS,
  SORT_OPTIONS
} from '@/utils/projectFilterData.js'

const industryOptions = ref([...DEFAULT_INDUSTRY_OPTIONS])
const roundOptions = ref([...DEFAULT_ROUND_OPTIONS])
const advantageOptions = ref([...DEFAULT_ADVANTAGE_OPTIONS])
const establishmentYearOptions = ref([...DEFAULT_ESTABLISHMENT_YEAR_OPTIONS])

const searchKeyword = ref('')
const activeSort = ref('recommend')
const activePanel = ref('')
const showDrawer = ref(false)
const yearExpanded = ref(false)
const loading = ref(false)

const filters = reactive({
  industry: [],
  region: [],
  regionScope: '',
  round: [],
  advantage: [],
  foundedYear: [],
  lbReport: '',
  financing: ''
})

const temp = reactive({
  industry: [],
  region: [],
  regionZone: 'china',
  round: [],
  advantage: [],
  foundedYear: [],
  lbReport: '',
  financing: '',
  sortBy: 'recommend'
})

const projectList = ref([])
const displayTotal = ref(0)
const page = ref(1)
const hasMore = ref(true)
const loadStatus = ref('loadmore')
const scrollTop = ref(0)

let countTimer = null

const drawerActive = computed(
  () =>
    !!(
      hasFilterSelection(filters.advantage) ||
      hasFilterSelection(filters.foundedYear) ||
      filters.lbReport ||
      filters.financing
    )
)

const industryBarText = computed(() => '所属行业')
const regionBarText = computed(() => '所在地区')
const roundBarText = computed(() => '融资轮次')

const sortBarLabel = computed(() => {
  if (activeSort.value === 'hot') return '项目推荐'
  const matched = SORT_OPTIONS.find((item) => item.value === activeSort.value)
  return matched?.label || '项目推荐'
})

const displayYears = computed(() =>
  yearExpanded.value ? establishmentYearOptions.value : establishmentYearOptions.value.slice(0, 5)
)

async function loadFilterOptions() {
  const res = await fetchFilterBundle('project-library')
  if (res.code !== SUCCESS_CODE) return
  const data = res.data || {}
  if (Array.isArray(data.industry) && data.industry.length) {
    industryOptions.value = data.industry
  }
  if (Array.isArray(data.round) && data.round.length) {
    roundOptions.value = data.round
  }
  if (Array.isArray(data.advantage) && data.advantage.length) {
    advantageOptions.value = data.advantage
  }
  if (Array.isArray(data.foundedYear) && data.foundedYear.length) {
    establishmentYearOptions.value = data.foundedYear
  }
}

function buildPayload(extra = {}) {
  let sortBy = activeSort.value
  if (activePanel.value === 'sort') {
    sortBy = temp.sortBy
  }
  const payload = {
    keyword: searchKeyword.value.trim(),
    industry: serializeFilterValues(filters.industry),
    region: serializeFilterValues(filters.region),
    regionScope: filters.regionScope,
    round: serializeFilterValues(filters.round),
    advantage: serializeFilterValues(filters.advantage),
    foundedYear: serializeFilterValues(filters.foundedYear),
    lbReport: serializeFilterValues(filters.lbReport),
    financing: filters.financing,
    sortBy,
    ...extra
  }
  payload.financing = mapFinancingParam(payload.financing)
  payload.lbReport = mapLbReportParam(payload.lbReport)
  return payload
}

/** 大学生创投报道：界面「是/否」→ 接口 yes/no（单选） */
function mapLbReportParam(val) {
  if (val === '是') return 'yes'
  if (val === '否') return 'no'
  return ''
}

/** 正在融资：界面「是/否」→ 接口 yes/no */
function mapFinancingParam(val) {
  if (val === '是') return 'yes'
  if (val === '否') return 'no'
  return ''
}

/** 当前生效的筛选条件（含下拉/抽屉中的临时选择） */
function resolveRegionScope(regionList, regionZone) {
  return hasFilterSelection(regionList) ? '' : regionZone === 'overseas' ? 'overseas' : ''
}

function buildEffectivePayload() {
  let industry = filters.industry
  let region = filters.region
  let regionScope = filters.regionScope
  let round = filters.round
  let advantage = filters.advantage
  let foundedYear = filters.foundedYear
  let lbReport = filters.lbReport
  let financing = filters.financing

  if (activePanel.value === 'industry') {
    industry = temp.industry
  }
  if (activePanel.value === 'region') {
    region = temp.region
    regionScope = resolveRegionScope(temp.region, temp.regionZone)
  }
  if (activePanel.value === 'round') {
    round = temp.round
  }
  if (showDrawer.value) {
    advantage = temp.advantage
    foundedYear = temp.foundedYear
    lbReport = temp.lbReport
    financing = temp.financing
  }

  return buildPayload({
    industry: serializeFilterValues(industry),
    region: serializeFilterValues(region),
    regionScope,
    round: serializeFilterValues(round),
    advantage: serializeFilterValues(advantage),
    foundedYear: serializeFilterValues(foundedYear),
    lbReport: serializeFilterValues(lbReport),
    financing
  })
}

async function refreshDisplayTotal() {
  clearTimeout(countTimer)
  countTimer = setTimeout(async () => {
    const res = await fetchProjectCollectionDetail(1, 1, buildEffectivePayload())
    if (res.code === SUCCESS_CODE) {
      displayTotal.value = res.data.total ?? 0
    }
  }, 120)
}

/** 下拉/抽屉/搜索关键字变更时实时预览条数 */
watch(
  () => [
    serializeFilterValues(temp.industry),
    serializeFilterValues(temp.region),
    temp.regionZone,
    serializeFilterValues(temp.round),
    serializeFilterValues(temp.advantage),
    serializeFilterValues(temp.foundedYear),
    temp.lbReport,
    temp.financing,
    temp.sortBy,
    searchKeyword.value,
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
  temp.advantage = [...filters.advantage]
  temp.foundedYear = [...filters.foundedYear]
  temp.lbReport = filters.lbReport
  temp.financing = filters.financing
  temp.sortBy = activeSort.value === 'hot' ? 'recommend' : activeSort.value
}

function syncSortTemp() {
  temp.sortBy = activeSort.value === 'hot' ? 'recommend' : activeSort.value
}

function toggleSortPanel() {
  if (activePanel.value === 'sort') {
    closePanel()
    return
  }
  showDrawer.value = false
  syncSortTemp()
  activePanel.value = 'sort'
}

function setHotSort() {
  closePanel()
  if (activeSort.value === 'hot') return
  activeSort.value = 'hot'
  refreshDisplayTotal()
  applyFilters()
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
  if (activePanel.value === 'round') temp.round = []
  if (activePanel.value === 'sort') temp.sortBy = 'recommend'
  confirmCurrentPanel()
}

function confirmCurrentPanel() {
  if (activePanel.value === 'industry') {
    filters.industry = [...temp.industry]
  }
  if (activePanel.value === 'region') {
    filters.region = [...temp.region]
    filters.regionScope = resolveRegionScope(temp.region, temp.regionZone)
  }
  if (activePanel.value === 'round') {
    filters.round = [...temp.round]
  }
  if (activePanel.value === 'sort') {
    activeSort.value = temp.sortBy
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
  temp.advantage = []
  temp.foundedYear = []
  temp.lbReport = ''
  temp.financing = ''
  confirmDrawer()
}

function confirmDrawer() {
  filters.advantage = [...temp.advantage]
  filters.foundedYear = [...temp.foundedYear]
  filters.lbReport = temp.lbReport
  filters.financing = temp.financing
  showDrawer.value = false
  applyFilters()
}

function onKeywordInput() {
  refreshDisplayTotal()
}

function applyFilters() {
  loadList(true)
}

async function loadList(reset = false) {
  if (reset) {
    page.value = 1
    hasMore.value = true
    projectList.value = []
    scrollTop.value = 0
  }
  if (!hasMore.value && !reset) return

  loading.value = true
  loadStatus.value = 'loading'

  const payload = buildEffectivePayload()
  const pageNum = reset ? 1 : page.value
  const res = await fetchProjectCollectionDetail(pageNum, 10, payload)

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
  if (loadStatus.value === 'loading') return
  loadList()
}

function onProjectTap(item) {
  navigateFromProjectLibrary(item)
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
</style>
