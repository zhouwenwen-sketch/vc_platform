<template>
  <view class="detail-header">
    <swiper
      v-if="bannerUrls.length"
      class="banner-swiper"
      circular
      :indicator-dots="bannerUrls.length > 1"
      indicator-color="rgba(255,255,255,0.4)"
      indicator-active-color="#fff"
      :autoplay="bannerUrls.length > 1"
    >
      <swiper-item v-for="(url, i) in bannerUrls" :key="i">
        <image :src="url" class="banner-img" mode="aspectFill" />
      </swiper-item>
    </swiper>

    <view class="meta-block">
      <text class="title">{{ detail.title }}</text>
      <view class="status-row">
        <text class="status-tag" :class="statusClass">{{ detail.statusText }}</text>
      </view>
      <view class="info-line">
        <u-icon name="clock" size="28rpx" color="#999" />
        <text>{{ detail.startTime }}</text>
      </view>
      <view class="info-line">
        <u-icon name="map" size="28rpx" color="#999" />
        <text>{{ organizerDisplay }}</text>
      </view>
      <view class="info-line">
        <u-icon name="rmb-circle" size="28rpx" color="#999" />
        <text>{{ detail.priceText }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  detail: { type: Object, required: true }
})

const bannerUrls = computed(() => {
  const list = props.detail.bannerUrls || []
  if (list.length) return list
  return props.detail.coverUrl ? [props.detail.coverUrl] : []
})

const organizerDisplay = computed(() => props.detail.organizerName || props.detail.location || '-')

const statusClass = computed(() => {
  const s = props.detail.status
  if (s === 'registering') return 'tag-registering'
  if (s === 'ended') return 'tag-ended'
  return 'tag-ongoing'
})
</script>

<style scoped lang="scss">
.detail-header {
  background: #fff;
}

.banner-swiper {
  width: 100%;
  height: 420rpx;
}

.banner-img {
  width: 100%;
  height: 420rpx;
}

.meta-block {
  padding: 28rpx 24rpx 8rpx;
}

.title {
  display: block;
  font-size: 34rpx;
  font-weight: 700;
  color: #222;
  line-height: 1.45;
}

.status-row {
  margin: 16rpx 0 20rpx;
}

.status-tag {
  display: inline-block;
  padding: 4rpx 16rpx;
  border-radius: 6rpx;
  font-size: 22rpx;
  color: #fff;

  &.tag-registering {
    background: #00b33b;
  }

  &.tag-ongoing {
    background: #78B9B1;
  }

  &.tag-ended {
    background: #999;
  }
}

.info-line {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-top: 12rpx;
  font-size: 26rpx;
  color: #666;
}
</style>
