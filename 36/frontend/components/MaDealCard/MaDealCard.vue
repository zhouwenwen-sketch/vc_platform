<template>
  <view class="ma-card" @click="onTap">
    <view class="card-head">
      <view class="brand">
        <ProjectLogo :logo-url="item.logoUrl" :name="item.brandName" size="xs" round />
        <text class="brand-name">{{ item.brandName }}</text>
      </view>
      <text v-if="item.relativeTime" class="time-tag">{{ item.relativeTime }}</text>
    </view>

    <view class="hero">
      <text class="hero-title">{{ item.title }}</text>
      <view v-if="item.tagList?.length" class="hero-tags">
        <text v-for="tag in item.tagList.slice(0, 3)" :key="tag" class="hero-tag">{{ tag }}</text>
      </view>
      <text v-if="item.projectNo" class="project-no">项目编号：{{ item.projectNo }}</text>
    </view>

    <view class="body">
      <view class="amount-row">
        <text class="amount">{{ item.dealAmountText }}</text>
        <text v-if="item.categoryLabel" class="category-pill">{{ item.categoryLabel }}</text>
      </view>
      <text class="summary">{{ item.summary }}</text>
    </view>

    <view class="foot" @click.stop>
      <view class="stat">
        <u-icon name="eye" size="28rpx" color="#666" />
        <text>预览({{ item.viewCount }})</text>
      </view>
      <view class="stat">
        <u-icon name="calendar" size="28rpx" color="#666" />
        <text>预约({{ item.appointmentCount }})</text>
      </view>
      <view class="stat" @click.stop="$emit('favorite', item)">
        <u-icon name="star" size="28rpx" color="#666" />
        <text>收藏({{ item.favoriteCount }})</text>
      </view>
      <view class="stat" @click.stop="$emit('share', item)">
        <u-icon name="share" size="28rpx" color="#666" />
        <text>转发({{ item.shareCount }})</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import ProjectLogo from '@/components/ProjectLogo/ProjectLogo.vue'

const props = defineProps({
  item: { type: Object, required: true }
})
const emit = defineEmits(['click', 'favorite', 'share'])

function onTap() {
  emit('click', props.item)
}
</script>

<style scoped lang="scss">
@import '@/styles/theme.scss';

.ma-card {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 16rpx 0;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10rpx;
  min-width: 0;
}

.brand-name {
  font-size: 22rpx;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.time-tag {
  flex-shrink: 0;
  background: #ff6b52;
  color: #fff;
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
}

.hero {
  margin: 12rpx 16rpx 0;
  padding: 20rpx 16rpx;
  background: linear-gradient(180deg, #f3f6fa 0%, #eef3f8 100%);
  border-radius: 12rpx;
  position: relative;
}

.hero-title {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: #222;
  line-height: 1.35;
}

.hero-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
  margin-top: 10rpx;
}

.hero-tag {
  font-size: 22rpx;
  font-weight: 700;
  color: #333;
}

.project-no {
  display: block;
  margin-top: 12rpx;
  font-size: 20rpx;
  color: #aaa;
}

.body {
  padding: 16rpx;
}

.amount-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.amount {
  font-size: 40rpx;
  font-weight: 700;
  color: #ff6b52;
  line-height: 1.1;
}

.category-pill {
  font-size: 20rpx;
  color: #333;
  border: 1rpx solid #ddd;
  border-radius: 8rpx;
  padding: 4rpx 10rpx;
}

.summary {
  display: block;
  margin-top: 10rpx;
  font-size: 24rpx;
  color: #333;
  font-weight: 600;
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12rpx 16rpx 16rpx;
  border-top: 1rpx solid #f5f5f5;
}

.stat {
  display: flex;
  align-items: center;
  gap: 4rpx;
  font-size: 20rpx;
  color: #666;
}
</style>
