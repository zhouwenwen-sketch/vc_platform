<template>
  <view class="inst-detail" v-if="detail">
    <view class="hero">
      <view class="profile-card">
        <view class="profile-top">
          <ProjectLogo :item="detail" size="md" />
          <view class="profile-main">
            <view class="name-row">
              <text class="inst-name">{{ detail.name }}</text>
              <text v-for="t in detail.instTypes" :key="t" class="type-tag">{{ t }}</text>
            </view>
          </view>
        </view>
        <view class="profile-meta">
          <view class="meta-item">
            <u-icon name="calendar" size="26rpx" color="#999" />
            <text>{{ detail.foundedYear || '-' }}</text>
          </view>
          <view class="meta-item">
            <u-icon name="map" size="26rpx" color="#999" />
            <text>{{ detail.region || '-' }}</text>
          </view>
        </view>
      </view>
    </view>

    <scroll-view scroll-x class="tab-scroll" :show-scrollbar="false">
      <view class="tab-inner">
        <view
          v-for="tab in tabs"
          :key="tab.key"
          class="tab-item"
          :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
        </view>
      </view>
    </scroll-view>

    <scroll-view scroll-y class="body-scroll" :show-scrollbar="false">
      <!-- 机构信息 -->
      <view v-if="activeTab === 'info'" class="tab-panel">
        <view class="stats-card">
          <view class="stat-item">
            <text class="stat-num">{{ detail.eventCount }}</text>
            <text class="stat-label">投资事件数</text>
          </view>
          <view class="stat-item">
            <text class="stat-num">{{ detail.manageScale }}</text>
            <text class="stat-label">管理规模(人民币)</text>
          </view>
          <view class="stat-item">
            <text class="stat-num">{{ detail.teamCount }}</text>
            <text class="stat-label">投资团队</text>
          </view>
        </view>

        <view class="block-card">
          <text class="block-title">机构介绍</text>
          <text class="block-text" :class="{ collapsed: !introExpanded }">{{ detail.intro }}</text>
          <view v-if="detail.intro" class="more-btn" @click="introExpanded = !introExpanded">
            <text>{{ introExpanded ? '收起' : '查看更多' }}</text>
            <u-icon :name="introExpanded ? 'arrow-up' : 'arrow-down'" size="24rpx" color="#78B9B1" />
          </view>
        </view>
      </view>

      <!-- 投资信息 -->
      <view v-if="activeTab === 'invest'" class="tab-panel">
        <view v-if="detail.investmentFields?.length" class="block-card">
          <text class="block-title">投资领域</text>
          <view class="tag-grid">
            <text v-for="f in displayFields" :key="'inv-' + f" class="field-tag">{{ f }}</text>
          </view>
          <view
            v-if="detail.investmentFields.length > fieldLimit"
            class="more-btn"
            @click="fieldsExpanded = !fieldsExpanded"
          >
            <text>{{ fieldsExpanded ? '收起' : '查看更多' }}</text>
            <u-icon :name="fieldsExpanded ? 'arrow-up' : 'arrow-down'" size="24rpx" color="#78B9B1" />
          </view>
        </view>

        <view class="block-card event-card">
          <view class="block-head">
            <view class="head-left">
              <text class="block-title inline-title">投资事件</text>
              <u-icon name="question-circle" size="30rpx" color="#ccc" @click="onEventTip" />
            </view>
            <text class="block-sub">
              共 <text class="count-num">{{ detail.investmentEvents?.length || 0 }}</text> 个
            </text>
          </view>
          <view v-if="displayEvents.length" class="event-list">
            <view
              v-for="ev in displayEvents"
              :key="ev.projectId + ev.date + ev.round"
              class="event-item"
              @click="goProject(ev)"
            >
              <ProjectLogo :item="ev" />
              <view class="ev-body">
                <view class="ev-head">
                  <view class="ev-title-row">
                    <text class="ev-name">{{ ev.companyName }}</text>
                    <text v-if="ev.round" class="ev-round">{{ ev.round }}</text>
                  </view>
                  <text v-if="ev.industry" class="ev-industry">{{ ev.industry }}</text>
                </view>
                <text class="ev-desc">{{ ev.description }}</text>
                <view class="ev-meta">
                  <u-icon name="level" size="26rpx" color="#78B9B1" />
                  <text class="meta-text">
                    {{ ev.date }}<text class="sep">|</text>{{ ev.round }}<text class="sep">|</text>{{ ev.amount }}
                  </text>
                </view>
              </view>
            </view>
          </view>
          <view v-else class="empty-box">暂无数据</view>
          <view
            v-if="(detail.investmentEvents?.length || 0) > eventLimit"
            class="more-btn"
            @click="eventsExpanded = !eventsExpanded"
          >
            <text>{{ eventsExpanded ? '收起' : '查看更多' }}</text>
            <u-icon :name="eventsExpanded ? 'arrow-up' : 'arrow-down'" size="24rpx" color="#78B9B1" />
          </view>
        </view>
      </view>

      <!-- 机构明细 -->
      <view v-if="activeTab === 'biz'" class="tab-panel">
        <view class="block-card">
          <view class="block-head">
            <text class="block-title">基金管理人</text>
            <text class="block-sub">共 {{ detail.fundManagers?.length || 0 }} 个</text>
          </view>
          <view v-if="detail.fundManagers?.length" class="fund-manager-list">
            <view v-for="(fm, idx) in detail.fundManagers" :key="idx" class="fund-manager-card">
              <text class="fund-manager-title">{{ fm.fullName || fm.name || '-' }}</text>
              <view class="info-row">
                <text class="info-label">法人代表</text>
                <text class="info-value">{{ fm.legalPerson || '-' }}</text>
              </view>
              <view class="info-row">
                <text class="info-label">机构类型</text>
                <text class="info-value">{{ fm.instType || '-' }}</text>
              </view>
              <view class="info-row">
                <text class="info-label">办公地址</text>
                <text class="info-value">{{ fm.officeAddress || '-' }}</text>
              </view>
              <view class="info-row">
                <text class="info-label">注册资本</text>
                <text class="info-value">{{ fm.registeredCapital || '-' }}</text>
              </view>
              <view class="info-row">
                <text class="info-label">实缴资本</text>
                <text class="info-value">{{ fm.paidInCapital || '-' }}</text>
              </view>
              <view class="info-row">
                <text class="info-label">实缴比例</text>
                <text class="info-value">{{ fm.paidInRatio || '-' }}</text>
              </view>
              <view class="info-row">
                <text class="info-label">登记编号</text>
                <text class="info-value">{{ fm.registrationNo || '-' }}</text>
              </view>
              <view class="info-row">
                <text class="info-label">成立时间</text>
                <text class="info-value">{{ fm.establishDate || '-' }}</text>
              </view>
              <view class="info-row">
                <text class="info-label">登记时间</text>
                <text class="info-value">{{ fm.registerDate || '-' }}</text>
              </view>
            </view>
          </view>
          <view v-else class="empty-box">暂无数据</view>
        </view>

        <view v-if="detail.businessInfo" class="block-card">
          <text class="block-title">机构明细</text>
          <view class="info-row">
            <text class="info-label">主体名称</text>
            <text class="info-value">{{ detail.businessInfo.fullName || '-' }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">法人代表</text>
            <text class="info-value">{{ detail.businessInfo.legalPerson || '-' }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">成立时间</text>
            <text class="info-value">{{ detail.businessInfo.establishDate || '-' }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">注册地址</text>
            <text class="info-value">{{ detail.businessInfo.address || '-' }}</text>
          </view>
        </view>
      </view>

      <!-- 投资团队 -->
      <view v-if="activeTab === 'team'" class="tab-panel">
        <view class="block-card">
          <view class="block-head">
            <text class="block-title">投资团队</text>
            <text class="block-sub">共 {{ detail.teamMembers?.length || 0 }} 位</text>
          </view>
          <view v-if="detail.teamMembers?.length" class="team-list">
            <view v-for="(m, idx) in detail.teamMembers" :key="idx" class="team-item">
              <view class="team-avatar">
                <image v-if="m.avatar" :src="m.avatar" mode="aspectFill" class="team-avatar-img" />
                <text v-else>{{ m.name?.slice(0, 1) }}</text>
              </view>
              <view class="team-body">
                <text class="team-name">{{ m.name }}</text>
                <text class="team-title">{{ m.title }}</text>
                <text v-if="m.bio" class="team-bio">{{ m.bio }}</text>
              </view>
            </view>
          </view>
          <view v-else class="empty-box">暂无数据</view>
        </view>
      </view>

      <!-- 联系方式 -->
      <view v-if="activeTab === 'contact'" class="tab-panel">
        <view class="block-card">
          <text class="block-title">联系方式</text>
          <view class="info-row">
            <text class="info-label">官网</text>
            <text class="info-value">{{ detail.contact?.website || '-' }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">电话</text>
            <text class="info-value">{{ detail.contact?.phone || '-' }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">邮箱</text>
            <text class="info-value">{{ detail.contact?.email || '-' }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">地址</text>
            <text class="info-value">{{ detail.contact?.address || '-' }}</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>

  <view v-else-if="loading" class="loading-wrap">
    <u-loading-icon mode="circle" color="#78B9B1" size="40" />
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { fetchInstitutionDetail } from '@/api/institution.js'
import { SUCCESS_CODE } from '@/utils/request.js'
import { navigateToProjectDetail, PROJECT_ENTRY } from '@/utils/projectNavigate.js'
import ProjectLogo from '@/components/ProjectLogo/ProjectLogo.vue'

const detail = ref(null)
const loading = ref(true)
const activeTab = ref('info')
const introExpanded = ref(false)
const fieldsExpanded = ref(false)
const eventsExpanded = ref(false)
const fieldLimit = 8
const eventLimit = 3

const tabs = [
  { key: 'info', label: '机构信息' },
  { key: 'invest', label: '投资信息' },
  { key: 'biz', label: '机构明细' },
  { key: 'team', label: '投资团队' },
  { key: 'contact', label: '联系方式' }
]

const displayFields = computed(() => {
  const list = detail.value?.investmentFields || []
  return fieldsExpanded.value ? list : list.slice(0, fieldLimit)
})

const displayEvents = computed(() => {
  const list = detail.value?.investmentEvents || []
  return eventsExpanded.value ? list : list.slice(0, eventLimit)
})

onLoad((query) => {
  loadDetail(query?.id)
})

async function loadDetail(id) {
  if (!id) {
    loading.value = false
    return
  }
  loading.value = true
  try {
    const res = await fetchInstitutionDetail(id)
    if (res.code === SUCCESS_CODE) {
      detail.value = res.data
    }
  } catch (e) {
    console.error('[inst-detail] load failed', e)
  } finally {
    loading.value = false
  }
}

function goProject(ev) {
  if (!ev?.projectId) return
  navigateToProjectDetail(ev.projectId, { from: PROJECT_ENTRY.INSTITUTION })
}

function onEventTip() {
  uni.showToast({ title: '投资事件为平台收录的该机构投资过的项目', icon: 'none' })
}
</script>

<style scoped lang="scss">
@import '@/styles/theme.scss';

.inst-detail {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f6f8;
}

.hero {
  flex: none;
  background: $theme-card-hero-gradient;
  padding: 24rpx 24rpx 32rpx;
}

.profile-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.06);
}

.profile-top {
  display: flex;
  gap: 20rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.profile-main {
  flex: 1;
  min-width: 0;
}

.name-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12rpx;
}

.inst-name {
  font-size: 36rpx;
  font-weight: 700;
  color: #222;
}

.type-tag {
  font-size: 22rpx;
  color: $theme-primary;
  border: 1rpx solid $theme-primary;
  border-radius: 6rpx;
  padding: 2rpx 12rpx;
}

.profile-meta {
  display: flex;
  gap: 32rpx;
  margin-top: 20rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 26rpx;
  color: #666;
}

.tab-scroll {
  flex: none;
  background: #fff;
  border-bottom: 1rpx solid #f0f0f0;
}

.tab-inner {
  display: flex;
  padding: 0 16rpx;
  white-space: nowrap;
}

.tab-item {
  flex: none;
  padding: 24rpx 28rpx;
  font-size: 28rpx;
  color: #666;

  &.active {
    color: #222;
    font-weight: 600;
    border-bottom: 4rpx solid $theme-primary;
  }
}

.body-scroll {
  flex: 1;
  height: 0;
}

.tab-panel {
  padding: 24rpx;
}

.stats-card {
  display: flex;
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx 16rpx;
  margin-bottom: 24rpx;
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-num {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #222;
  margin-bottom: 8rpx;
}

.stat-label {
  font-size: 22rpx;
  color: #999;
}

.block-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
}

.block-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.block-title {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #222;
  margin-bottom: 16rpx;
}

.block-head .block-title {
  margin-bottom: 0;
}

.head-left {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.inline-title {
  margin-bottom: 0;
}

.block-sub {
  font-size: 24rpx;
  color: #999;
}

.count-num {
  color: $theme-primary;
  font-weight: 600;
}

.block-text {
  font-size: 28rpx;
  color: #555;
  line-height: 1.75;

  &.collapsed {
    display: -webkit-box;
    overflow: hidden;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 4;
  }
}

.tag-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.field-tag {
  padding: 8rpx 20rpx;
  font-size: 24rpx;
  color: $theme-primary;
  border: 1rpx solid rgba(196, 154, 108, 0.5);
  background: rgba(196, 154, 108, 0.08);
  border-radius: 8rpx;
}

.more-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  margin-top: 24rpx;
  font-size: 26rpx;
  color: $theme-primary;
}

.event-list {
  display: flex;
  flex-direction: column;
}

.event-item {
  display: flex;
  gap: 20rpx;
  padding: 28rpx 0;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.ev-body {
  flex: 1;
  min-width: 0;
}

.ev-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12rpx;
  margin-bottom: 8rpx;
}

.ev-title-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10rpx;
  flex: 1;
  min-width: 0;
}

.ev-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #222;
}

.ev-round {
  flex: none;
  font-size: 22rpx;
  color: #78B9B1;
  background: rgba(41, 121, 255, 0.1);
  padding: 2rpx 10rpx;
  border-radius: 6rpx;
}

.ev-industry {
  flex: none;
  font-size: 22rpx;
  color: #999;
  background: #f5f5f5;
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
}

.ev-desc {
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
  display: -webkit-box;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.ev-meta {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-top: 12rpx;
  font-size: 24rpx;
  color: $theme-primary;

  .meta-text {
    color: $theme-primary;
  }

  .sep {
    margin: 0 10rpx;
    color: rgba(196, 154, 108, 0.45);
  }
}

.empty-box {
  padding: 48rpx;
  text-align: center;
  font-size: 28rpx;
  color: #bbb;
  background: #f8f9fb;
  border-radius: 12rpx;
}

.fund-manager-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.fund-manager-card {
  padding: 8rpx 0 16rpx;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
    padding-bottom: 0;
  }
}

.fund-manager-title {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: #222;
  margin-bottom: 8rpx;
}

.info-row {
  display: flex;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f5f5f5;

  &:last-child {
    border-bottom: none;
  }
}

.info-label {
  width: 160rpx;
  flex: none;
  font-size: 28rpx;
  color: #999;
}

.info-value {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

.team-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.team-item {
  display: flex;
  gap: 20rpx;
}

.team-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: $theme-primary;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 28rpx;
  flex: none;
  overflow: hidden;
}

.team-avatar-img {
  width: 100%;
  height: 100%;
}

.team-name {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: #222;
}

.team-title {
  display: block;
  font-size: 24rpx;
  color: #999;
  margin-top: 4rpx;
}

.team-bio {
  display: block;
  font-size: 26rpx;
  color: #666;
  margin-top: 8rpx;
  line-height: 1.5;
}

.simple-item {
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}

.simple-name {
  display: block;
  font-size: 28rpx;
  color: #333;
}

.simple-sub {
  display: block;
  font-size: 24rpx;
  color: #999;
  margin-top: 6rpx;
}

.loading-wrap {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
