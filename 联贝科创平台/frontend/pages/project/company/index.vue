<template>
  <view class="company-page" v-if="detail">
    <scroll-view scroll-y class="page-scroll" :scroll-into-view="scrollIntoView" scroll-with-animation>
      <view class="overview-card">
        <view class="overview-top">
          <ProjectLogo :item="detail" size="xl" />
          <view class="overview-main">
            <view class="name-row">
              <text class="company-name">{{ detail.name }}</text>
              <text v-if="detail.isCertified" class="cert-badge">认证</text>
            </view>
            <text class="slogan">{{ detail.slogan }}</text>
          </view>
        </view>
        <view class="meta-row">
          <view class="meta-item">
            <u-icon name="level" size="28rpx" color="#999" />
            <text>{{ detail.round || '-' }}</text>
          </view>
          <view class="meta-item">
            <u-icon name="map" size="28rpx" color="#999" />
            <text>{{ detail.location || '-' }}</text>
          </view>
          <view class="meta-item">
            <u-icon name="calendar" size="28rpx" color="#999" />
            <text>{{ detail.establishDate || '-' }}</text>
          </view>
        </view>
        <view class="meta-row action-row">
          <view class="meta-item website-item" @click="onOpenWebsite">
            <u-icon name="link" size="28rpx" color="#999" />
            <text class="website-text">{{ displayWebsite }}</text>
          </view>
          <text class="seek-link" @click="onSeekReport">寻求报道 {{ LINK_ARROW }}</text>
        </view>
        <view class="tag-row">
          <text v-if="detail.isFinancing" class="tag tag-warn">正在融资</text>
          <text v-for="t in detail.tags" :key="t" class="tag tag-green">{{ t }}</text>
        </view>
      </view>

      <view class="tabs">
        <view class="tab-item" :class="{ active: activeTab === 'info' }" @click="activeTab = 'info'">
          项目信息
        </view>
        <view class="tab-item" :class="{ active: activeTab === 'news' }" @click="activeTab = 'news'">
          行业资讯
        </view>
      </view>

      <view v-if="activeTab === 'info'" class="tab-body">
        <view class="block-card">
          <text class="block-title">项目介绍</text>
          <text class="block-text">{{ detail.intro || '暂无介绍' }}</text>
        </view>

        <view v-if="detail.financingHistory?.length" id="anchor-financing" class="block-card">
          <text class="block-title">融资历史</text>
          <view v-for="(f, idx) in detail.financingHistory" :key="idx" class="timeline-item">
            <view class="tl-left">
              <text class="tl-date">{{ f.date }}</text>
              <text class="tl-round">{{ f.round }}</text>
            </view>
            <view class="tl-dot" />
            <view class="tl-right">
              <text class="tl-amount">
                融资金额：<text class="amount-val">{{ f.amount || '未透露' }}</text>
              </text>
              <view v-if="f.investors?.length" class="investor-tags">
                <text v-for="inv in f.investors" :key="inv" class="inv-tag">{{ inv }}</text>
              </view>
            </view>
          </view>
        </view>

        <view v-if="detail.businessInfo" class="block-card">
          <text class="block-title">工商信息</text>
          <view class="info-table">
            <view class="info-row">
              <text class="info-label">工商全称</text>
              <text class="info-value">{{ detail.businessInfo.fullName }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">英文全称</text>
              <text class="info-value">{{ detail.businessInfo.englishName }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">法定代表人</text>
              <text class="info-value">{{ detail.businessInfo.legalPerson }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">注册地址</text>
              <text class="info-value">{{ detail.businessInfo.address }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">成立时间</text>
              <text class="info-value">{{ detail.businessInfo.establishDate }}</text>
            </view>
          </view>
        </view>

        <view v-if="detail.shareholders?.length" class="block-card">
          <text class="block-title">股东（发起人）</text>
          <view
            v-for="(s, idx) in displayShareholders"
            :key="idx"
            class="sub-card"
          >
            <view class="sub-row"><text class="sub-label">股东（发起人）：</text><text>{{ s.name }}</text></view>
            <view class="sub-row"><text class="sub-label">持股比例：</text><text>{{ s.ratio }}</text></view>
            <view class="sub-row"><text class="sub-label">认缴出资额：</text><text>{{ s.capital }}</text></view>
            <view class="sub-row"><text class="sub-label">认缴出资日期：</text><text>{{ s.capitalDate }}</text></view>
          </view>
          <view
            v-if="detail.shareholders.length > 1"
            class="more-link"
            @click="shareholderExpanded = !shareholderExpanded"
          >
            <text>{{ shareholderExpanded ? '收起' : '查看更多' }}</text>
            <u-icon :name="shareholderExpanded ? 'arrow-up' : 'arrow-down'" size="24rpx" color="#78B9B1" />
          </view>
        </view>

        <view v-if="detail.teamMembers?.length" class="block-card">
          <text class="block-title">团队成员</text>
          <view v-for="(m, idx) in displayTeam" :key="idx" class="member-item">
            <view class="member-head">
              <view class="member-avatar">
                <image v-if="m.avatar" :src="m.avatar" mode="aspectFill" />
                <text v-else>{{ m.name?.slice(0, 1) }}</text>
              </view>
              <view class="member-title-wrap">
                <text class="member-name">{{ m.name }}</text>
                <text class="member-role">{{ m.title }}</text>
              </view>
              <text class="detail-pill" @click="onMemberDetail(m)">详情</text>
            </view>
            <text class="member-bio">{{ m.bio }}</text>
          </view>
          <view
            v-if="detail.teamMembers.length > 1"
            class="more-link"
            @click="teamExpanded = !teamExpanded"
          >
            <text>{{ teamExpanded ? '收起' : '查看更多' }}</text>
            <u-icon :name="teamExpanded ? 'arrow-up' : 'arrow-down'" size="24rpx" color="#78B9B1" />
          </view>
        </view>
      </view>

      <view v-else class="tab-body">
        <view v-if="detail.industryNews?.length" class="block-card news-card">
          <view
            v-for="n in displayIndustryNews"
            :key="`${n.source || 'news'}-${n.id}`"
            class="news-item"
            :class="{ 'news-item-static': n.source === 'dynamic' }"
            @click="goNewsDetail(n)"
          >
            <view class="news-head">
              <text class="news-type-tag" :class="{ 'news-type-dynamic': n.source === 'dynamic' }">
                {{ n.newsType || '快讯' }}
              </text>
              <text class="news-title">{{ n.title }}</text>
            </view>
            <text v-if="n.date" class="news-date">{{ n.date }}</text>
          </view>
          <view v-if="showMoreNewsLink" class="more-link" @click="onMoreNews">
            <text>查看更多</text>
            <u-icon name="arrow-down" size="24rpx" color="#78B9B1" />
          </view>
        </view>
        <u-empty v-else text="暂无行业资讯" mode="list" margin-top="40" />
      </view>

      <view class="scroll-bottom" />
    </scroll-view>

    <view class="footer-bar">
      <view class="footer-edit" @click="onEdit">
        <u-icon name="edit-pen" size="36rpx" color="#666" />
        <text>编辑维护</text>
      </view>
      <view class="footer-contact" @click="onContact">我要联系</view>
    </view>
  </view>
  <view v-else class="loading-wrap">
    <u-loading-icon mode="circle" />
  </view>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { fetchProjectDetail } from '@/api/project.js'
import { SUCCESS_CODE } from '@/utils/request.js'
import { openCoverageApply } from '@/utils/coverageGuard.js'
import { openProjectContact } from '@/utils/contactGuard.js'
import { LINK_ARROW } from '@/utils/linkText.js'
import ProjectLogo from '@/components/ProjectLogo/ProjectLogo.vue'
import { newsListTitle } from '@/utils/transform.js'

const detail = ref(null)
const activeTab = ref('info')
const shareholderExpanded = ref(false)
const teamExpanded = ref(false)
const newsVisibleCount = ref(3)
const projectId = ref('')
const scrollIntoView = ref('')

const displayShareholders = computed(() => {
  const list = detail.value?.shareholders || []
  return shareholderExpanded.value ? list : list.slice(0, 1)
})

const displayTeam = computed(() => {
  const list = detail.value?.teamMembers || []
  return teamExpanded.value ? list : list.slice(0, 1)
})

const displayIndustryNews = computed(() => {
  const list = detail.value?.industryNews || []
  return list.slice(0, newsVisibleCount.value)
})

const showMoreNewsLink = computed(() => {
  const total = detail.value?.industryNews?.length || 0
  return total > 3 && newsVisibleCount.value < total
})

const displayWebsite = computed(() => {
  const url = detail.value?.website
  return url && url !== '-' ? url : '-'
})

async function loadDetail(id) {
  try {
    const res = await fetchProjectDetail(id)
    if (res.code === SUCCESS_CODE && res.data) {
      const data = res.data
      const news = (data.industryNews || data.industry_news || []).map((n) => ({
        id: n.id,
        title: newsListTitle(n.title || ''),
        summary: n.summary || '',
        date: n.date || '',
        newsType: n.newsType || n.news_type || '快讯',
        source: n.source || 'news'
      }))
      detail.value = {
        ...data,
        website: data.website || data.website_url || '',
        industryNews: news
      }
      newsVisibleCount.value = 3
    } else {
      uni.showToast({ title: '加载失败', icon: 'none' })
    }
  } catch {
    uni.showToast({ title: '网络异常', icon: 'none' })
  }
}

function scrollToAnchor(anchor) {
  if (anchor !== 'financing') return
  activeTab.value = 'info'
  nextTick(() => {
    setTimeout(() => {
      scrollIntoView.value = 'anchor-financing'
    }, 320)
  })
}

function onSeekReport() {
  openCoverageApply(projectId.value)
}

function onOpenWebsite() {
  const url = detail.value?.website
  if (!url || url === '-') return
  const href = /^https?:\/\//i.test(url) ? url : `https://${url}`
  // #ifdef H5
  window.open(href, '_blank')
  // #endif
  // #ifndef H5
  uni.setClipboardData({
    data: href,
    success: () => uni.showToast({ title: '链接已复制', icon: 'none' })
  })
  // #endif
}

function onMemberDetail(m) {
  uni.showToast({ title: m.name, icon: 'none' })
}

function onMoreNews() {
  const total = detail.value?.industryNews?.length || 0
  newsVisibleCount.value = Math.min(newsVisibleCount.value + 5, total)
}

function goNewsDetail(item) {
  if (!item?.id) return
  if (item.source === 'dynamic') {
    const full = item.summary ? `${item.title}\n${item.summary}` : item.title
    uni.showModal({ title: '动态', content: full, showCancel: false })
    return
  }
  uni.navigateTo({ url: `/pages/news/detail?id=${item.id}` })
}

function onEdit() {
  uni.showToast({ title: '编辑维护功能开发中', icon: 'none' })
}

function onContact() {
  openProjectContact(projectId.value)
}

onLoad(async (query) => {
  projectId.value = query.id || ''
  const anchor = query.anchor || ''
  if (projectId.value) {
    await loadDetail(projectId.value)
    scrollToAnchor(anchor)
  }
})
</script>

<style lang="scss" scoped>
$blue: #78B9B1;

.company-page {
  height: 100vh;
  background: #f0f2f5;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.page-scroll {
  flex: 1;
  min-height: 0;
  height: 0;
}

.overview-card {
  margin: 20rpx 24rpx;
  padding: 28rpx;
  background: #fff;
  border-radius: 16rpx;
}

.overview-top {
  display: flex;
  gap: 20rpx;
}

.overview-main {
  flex: 1;
  min-width: 0;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex-wrap: wrap;
}

.company-name {
  font-size: 36rpx;
  font-weight: 700;
  color: #222;
}

.cert-badge {
  font-size: 22rpx;
  color: $blue;
  border: 1rpx solid $blue;
  border-radius: 8rpx;
  padding: 4rpx 12rpx;
}

.slogan {
  font-size: 26rpx;
  color: #666;
  margin-top: 12rpx;
  line-height: 1.5;
  display: block;
}

.meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 24rpx;
  margin-top: 24rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 24rpx;
  color: #999;
}

.action-row {
  justify-content: space-between;
  align-items: center;
}

.website-item {
  flex: 1;
  min-width: 0;
  margin-right: 16rpx;
}

.website-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.seek-link {
  flex-shrink: 0;
  font-size: 24rpx;
  color: $blue;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 16rpx;
}

.tag {
  font-size: 22rpx;
  padding: 6rpx 16rpx;
  border-radius: 6rpx;
}

.tag-warn {
  color: #e65100;
  background: #fff3e0;
}

.tag-green {
  color: #2e7d32;
  background: #e8f5e9;
}

.tabs {
  display: flex;
  background: #fff;
  margin: 0 24rpx;
  border-radius: 12rpx 12rpx 0 0;
  border-bottom: 1rpx solid #eee;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 24rpx 0;
  font-size: 30rpx;
  color: #999;

  &.active {
    color: $blue;
    font-weight: 600;
    border-bottom: 4rpx solid $blue;
  }
}

.tab-body {
  margin: 0 24rpx 24rpx;
  background: #fff;
  border-radius: 0 0 16rpx 16rpx;
  padding: 8rpx 0 24rpx;
}

.block-card {
  padding: 24rpx 28rpx;
  border-bottom: 12rpx solid #f5f6f8;

  &:last-child {
    border-bottom: none;
  }
}

.block-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #222;
  display: block;
  margin-bottom: 20rpx;
}

.block-text {
  font-size: 28rpx;
  color: #444;
  line-height: 1.7;
}

.timeline-item {
  display: flex;
  padding-bottom: 28rpx;
  position: relative;
}

.tl-left {
  width: 140rpx;
  flex-shrink: 0;
}

.tl-date {
  display: block;
  font-size: 26rpx;
  color: #333;
  font-weight: 600;
}

.tl-round {
  font-size: 24rpx;
  color: #999;
}

.tl-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: $blue;
  margin: 8rpx 16rpx 0;
  flex-shrink: 0;
}

.tl-right {
  flex: 1;
}

.tl-amount {
  font-size: 28rpx;
  color: #333;
}

.amount-val {
  color: #e65100;
}

.investor-tags {
  margin-top: 12rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.inv-tag {
  font-size: 24rpx;
  color: $blue;
  background: #C9E5E1;
  padding: 8rpx 20rpx;
  border-radius: 8rpx;
}

.info-table {
  border: 1rpx solid #eee;
  border-radius: 8rpx;
  overflow: hidden;
}

.info-row {
  display: flex;
  border-bottom: 1rpx solid #eee;

  &:last-child {
    border-bottom: none;
  }
}

.info-label {
  width: 200rpx;
  flex-shrink: 0;
  padding: 20rpx;
  background: #fafafa;
  font-size: 26rpx;
  color: #666;
}

.info-value {
  flex: 1;
  padding: 20rpx;
  font-size: 26rpx;
  color: #333;
  line-height: 1.5;
}

.sub-card {
  background: #f8f9fb;
  border-radius: 12rpx;
  padding: 20rpx;
  margin-bottom: 16rpx;
}

.sub-row {
  font-size: 26rpx;
  color: #333;
  line-height: 1.8;
}

.sub-label {
  color: #999;
}

.more-link {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  padding: 16rpx;
  color: $blue;
  font-size: 28rpx;
}

.member-item {
  margin-bottom: 28rpx;
}

.member-head {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.member-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: #e3f2fd;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $blue;
  font-size: 32rpx;
  font-weight: 700;

  image {
    width: 100%;
    height: 100%;
  }
}

.member-title-wrap {
  flex: 1;
}

.member-name {
  font-size: 30rpx;
  font-weight: 700;
  color: #222;
  display: block;
}

.member-role {
  font-size: 24rpx;
  color: #999;
}

.detail-pill {
  font-size: 24rpx;
  color: $blue;
  background: #C9E5E1;
  padding: 8rpx 20rpx;
  border-radius: 8rpx;
}

.member-bio {
  margin-top: 16rpx;
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
  display: block;
}

.news-item {
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;

  &:active {
    opacity: 0.75;
  }

  &.news-item-static:active {
    opacity: 1;
  }
}

.news-head {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
}

.news-type-tag {
  flex: none;
  margin-top: 4rpx;
  padding: 2rpx 10rpx;
  font-size: 20rpx;
  line-height: 1.4;
  color: #78B9B1;
  background: rgba(41, 121, 255, 0.1);
  border-radius: 6rpx;

  &.news-type-dynamic {
    color: #e65100;
    background: #fff3e0;
  }
}

.news-title {
  flex: 1;
  font-size: 30rpx;
  font-weight: 400;
  color: #222;
  line-height: 1.5;
  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.news-date {
  font-size: 24rpx;
  color: #999;
  margin-top: 12rpx;
  display: block;
}

.scroll-bottom {
  height: 160rpx;
}

.footer-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  padding: 16rpx 24rpx calc(16rpx + env(safe-area-inset-bottom));
  display: flex;
  align-items: center;
  gap: 20rpx;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.06);
}

.footer-edit {
  display: flex;
  flex-direction: column;
  align-items: center;
  font-size: 22rpx;
  color: #666;
  width: 120rpx;
}

.footer-contact {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  text-align: center;
  background: $blue;
  color: #fff;
  font-size: 32rpx;
  border-radius: 44rpx;
}

.loading-wrap {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
