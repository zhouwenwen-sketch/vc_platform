<template>
  <view class="detail-page">
    <scroll-view v-if="detail" scroll-y class="content-scroll" :show-scrollbar="false">
      <view class="hero">
        <view class="brand-row">
          <ProjectLogo :logo-url="detail.logoUrl" :name="detail.brandName" size="xs" round />
          <text class="brand-name">{{ detail.brandName }}</text>
        </view>
        <text class="hero-title">{{ detail.title }}</text>
        <view v-if="detail.tagList?.length" class="hero-tags">
          <text v-for="tag in detail.tagList" :key="tag" class="hero-tag">{{ tag }}</text>
        </view>
        <text v-if="detail.projectNo" class="project-no">项目编号：{{ detail.projectNo }}</text>
      </view>

      <view class="main-card">
        <view class="main-top">
          <text class="amount">¥{{ detail.dealAmountText }}</text>
          <view class="share-btn" @click="onShare">
            <u-icon name="share" size="32rpx" color="#666" />
            <text>分享</text>
          </view>
        </view>
        <text class="summary">{{ detail.summary }}</text>
      </view>

      <view class="section-divider">
        <text>- 宝贝详情 -</text>
      </view>

      <view class="detail-list">
        <view v-for="row in detailRows" :key="row.label" class="detail-row">
          <text class="detail-key">【{{ row.label }}】</text>
          <text class="detail-value">{{ row.value }}</text>
        </view>
        <view v-if="detail.cooperationIntent" class="detail-row block">
          <text class="detail-key">【合作意向】</text>
          <text class="detail-value">{{ detail.cooperationIntent }}</text>
        </view>
      </view>

      <view class="end-tip">
        <text>- 到底了，看看其他的吧 -</text>
      </view>
    </scroll-view>

    <MaDealBottomBar
      v-if="detail"
      :btn-text="appointBtnText"
      :disabled="detail.appointed"
      @appoint="onAppoint"
      @call="onCall"
      @favorite="onFavorite"
      @cta="onAppoint"
    />

    <view v-else-if="loading" class="loading-wrap">
      <u-loading-icon mode="circle" color="#78B9B1" size="40" />
    </view>

    <view v-else class="empty-wrap">
      <u-empty text="项目加载失败" mode="data" />
      <view class="retry-btn" @click="loadDetail(dealId)">重新加载</view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad, onShareAppMessage } from '@dcloudio/uni-app'
import MaDealBottomBar from '@/components/MaDealBottomBar/MaDealBottomBar.vue'
import ProjectLogo from '@/components/ProjectLogo/ProjectLogo.vue'
import { appointMaDeal, fetchMaDealDetail, shareMaDeal } from '@/api/maDeal.js'
import { requireLoginAtEntry } from '@/utils/authGuard.js'
import { SUCCESS_CODE } from '@/utils/request.js'
import { resolveMediaUrl } from '@/utils/mediaUrl.js'

const detail = ref(null)
const loading = ref(true)
const dealId = ref('')

const appointBtnText = computed(() => (detail.value?.appointed ? '已预约' : '立即预约沟通'))

const detailRows = computed(() => {
  const d = detail.value
  if (!d) return []
  const rows = [
    { label: '项目编号', value: d.projectNo },
    { label: '项目名称', value: d.projectName },
    { label: '主营业务', value: d.mainBusiness },
    { label: '实控股权', value: d.controllingStake },
    { label: '目前市值', value: d.marketValue },
    { label: '营业收入', value: d.revenueData },
    { label: '年净利润', value: d.netProfitData },
    { label: '负债率', value: d.debtRatio },
    { label: '总资产', value: d.totalAssets },
    { label: '净资产', value: d.netAssets },
    { label: '账面资金', value: d.bookFunds }
  ]
  return rows.filter((row) => row.value)
})

onLoad((query) => {
  dealId.value = query?.id || ''
  if (dealId.value) {
    loadDetail(dealId.value)
  } else {
    loading.value = false
  }
})

onShareAppMessage(() => ({
  title: detail.value?.summary || detail.value?.title || '融资并购项目',
  path: `/pages/ma/detail/index?id=${dealId.value}`
}))

function mapDetail(row) {
  const tagList = row.tagList || (row.tags ? String(row.tags).split(/[,，]/).map((t) => t.trim()).filter(Boolean) : [])
  return {
    ...row,
    tagList,
    logoUrl: resolveMediaUrl(row.logoUrl || row.logo_url || ''),
    projectName: row.projectName || row.project_name || '',
    mainBusiness: row.mainBusiness || row.main_business || '',
    controllingStake: row.controllingStake || row.controlling_stake || '',
    marketValue: row.marketValue || row.market_value || '',
    revenueData: row.revenueData || row.revenue_data || '',
    netProfitData: row.netProfitData || row.net_profit_data || '',
    debtRatio: row.debtRatio || row.debt_ratio || '',
    totalAssets: row.totalAssets || row.total_assets || '',
    netAssets: row.netAssets || row.net_assets || '',
    bookFunds: row.bookFunds || row.book_funds || '',
    cooperationIntent: row.cooperationIntent || row.cooperation_intent || '',
    contactPhone: row.contactPhone || row.contact_phone || '',
    appointed: !!row.appointed
  }
}

async function loadDetail(id) {
  loading.value = true
  try {
    const res = await fetchMaDealDetail(id)
    if (res.code === SUCCESS_CODE) {
      detail.value = mapDetail(res.data)
    } else {
      detail.value = null
    }
  } catch (e) {
    console.error('[ma/detail] load failed', e)
    detail.value = null
  } finally {
    loading.value = false
  }
}

function appointTargetUrl() {
  return `/pages/ma/detail/index?id=${dealId.value}`
}

async function onAppoint() {
  if (!dealId.value) return
  if (detail.value?.appointed) {
    uni.showToast({ title: '您已预约该项目', icon: 'none' })
    return
  }
  if (!requireLoginAtEntry(appointTargetUrl())) return
  try {
    const res = await appointMaDeal(dealId.value)
    if (res.code === SUCCESS_CODE) {
      detail.value.appointed = true
      detail.value.appointmentCount = (detail.value.appointmentCount || 0) + 1
      uni.showToast({ title: '预约成功', icon: 'success' })
    }
  } catch (e) {
    console.error('[ma/detail] appoint failed', e)
  }
}

function onCall() {
  const phone = detail.value?.contactPhone
  if (!phone) {
    uni.showToast({ title: '暂无联系电话', icon: 'none' })
    return
  }
  uni.makePhoneCall({ phoneNumber: phone })
}

function onFavorite() {
  uni.showToast({ title: '收藏功能即将上线', icon: 'none' })
}

async function onShare() {
  if (!dealId.value) return
  await shareMaDeal(dealId.value).catch(() => {})
  if (detail.value) {
    detail.value.shareCount = (detail.value.shareCount || 0) + 1
  }
  uni.showToast({ title: '分享已记录', icon: 'none' })
}
</script>

<style scoped lang="scss">
@import '@/styles/theme.scss';

.detail-page {
  height: 100vh;
  overflow: hidden;
  background: #f5f6f8;
  display: flex;
  flex-direction: column;
}

.content-scroll {
  flex: 1;
  min-height: 0;
  height: 0;
}

.hero {
  background: #f0f2f5;
  padding: 32rpx 28rpx 40rpx;
}

.brand-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 20rpx;
}

.brand-name {
  font-size: 28rpx;
  color: #333;
  font-weight: 600;
}

.hero-title {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  color: #222;
  line-height: 1.35;
}

.hero-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 16rpx;
}

.hero-tag {
  font-size: 28rpx;
  font-weight: 700;
  color: #222;
}

.project-no {
  display: block;
  margin-top: 20rpx;
  font-size: 24rpx;
  color: #999;
}

.main-card {
  background: #fff;
  padding: 28rpx;
}

.main-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16rpx;
}

.amount {
  font-size: 48rpx;
  font-weight: 700;
  color: #ff6b52;
  line-height: 1.2;
  flex: 1;
}

.share-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  font-size: 26rpx;
  color: #666;
  flex-shrink: 0;
}

.summary {
  display: block;
  margin-top: 20rpx;
  font-size: 30rpx;
  font-weight: 700;
  color: #222;
  line-height: 1.5;
}

.section-divider {
  text-align: center;
  padding: 28rpx;
  background: #fff;
  border-top: 1rpx solid #f0f0f0;
  color: #999;
  font-size: 26rpx;
}

.detail-list {
  background: #fff;
  padding: 0 28rpx 32rpx;
}

.detail-row {
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f5f5f5;

  &.block {
    border-bottom: none;
  }
}

.detail-key {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #222;
  margin-bottom: 8rpx;
}

.detail-value {
  font-size: 28rpx;
  color: #333;
  line-height: 1.55;
}

.end-tip {
  text-align: center;
  padding: 24rpx 28rpx 16rpx;
  color: #bbb;
  font-size: 24rpx;
}

.loading-wrap,
.empty-wrap {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 0;
}

.retry-btn {
  margin-top: 24rpx;
  padding: 16rpx 40rpx;
  background: #78B9B1;
  color: #fff;
  border-radius: 40rpx;
  font-size: 28rpx;
}
</style>
