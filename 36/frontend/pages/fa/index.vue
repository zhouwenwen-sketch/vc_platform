<template>
  <view class="fa-page">
    <scroll-view scroll-y class="fa-scroll" :show-scrollbar="false">
      <view class="hero">
        <text class="hero-title">{{ content.title }}</text>
        <text class="hero-sub">{{ content.subtitle }}</text>
      </view>

      <view class="intro-card">
        <text class="intro-text">{{ content.intro }}</text>
      </view>

      <view class="section">
        <text class="section-title">服务内容</text>
        <view v-for="(item, index) in content.services" :key="index" class="service-card">
          <view class="service-index">{{ index + 1 }}</view>
          <view class="service-body">
            <text class="service-name">{{ item.title }}</text>
            <text class="service-desc">{{ item.desc }}</text>
          </view>
        </view>
      </view>

      <view class="section">
        <text class="section-title">服务流程</text>
        <view class="process-list">
          <view v-for="(step, index) in content.process" :key="index" class="process-item">
            <view class="process-dot">{{ index + 1 }}</view>
            <text class="process-text">{{ step }}</text>
          </view>
        </view>
      </view>

      <view class="contact-card">
        <u-icon name="chat" color="#78B9B1" size="40rpx" />
        <text class="contact-text">{{ content.contactTip }}</text>
      </view>

      <view class="action-wrap">
        <u-button
          type="primary"
          text="联系 FA 顾问"
          custom-style="background:#5A9A92;border-color:#5A9A92;border-radius:48rpx;height:88rpx"
          @click="onContact"
        />
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { FA_SERVICES } from '@/content/fa-services.js'

const content = FA_SERVICES

function onContact() {
  uni.showToast({ title: '请联系平台运营咨询 FA 服务', icon: 'none' })
}
</script>

<style scoped lang="scss">
@import '@/styles/theme.scss';

.fa-page {
  box-sizing: border-box;
  min-height: 100%;
  background: #f5f6f8;
}

.fa-scroll {
  height: 100vh;
  padding-bottom: calc(48rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
}

.hero {
  background: $theme-hero-gradient;
  padding: 48rpx 32rpx 56rpx;
}

.hero-title {
  display: block;
  font-size: 44rpx;
  font-weight: 700;
  color: #fff;
  line-height: 1.4;
}

.hero-sub {
  display: block;
  margin-top: 16rpx;
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.85);
  line-height: 1.6;
}

.intro-card {
  margin: -32rpx 24rpx 24rpx;
  padding: 32rpx;
  background: #fff;
  border-radius: 16rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.06);
}

.intro-text {
  font-size: 28rpx;
  color: #555;
  line-height: 1.8;
}

.section {
  margin: 0 24rpx 24rpx;
}

.section-title {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #222;
  margin-bottom: 20rpx;
}

.service-card {
  display: flex;
  gap: 20rpx;
  padding: 28rpx;
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 16rpx;
}

.service-index {
  flex: none;
  width: 48rpx;
  height: 48rpx;
  line-height: 48rpx;
  text-align: center;
  background: #C9E5E1;
  color: #78B9B1;
  font-size: 26rpx;
  font-weight: 700;
  border-radius: 12rpx;
}

.service-body {
  flex: 1;
  min-width: 0;
}

.service-name {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: #222;
  margin-bottom: 8rpx;
}

.service-desc {
  display: block;
  font-size: 26rpx;
  color: #666;
  line-height: 1.65;
}

.process-list {
  background: #fff;
  border-radius: 16rpx;
  padding: 8rpx 28rpx;
}

.process-item {
  display: flex;
  align-items: flex-start;
  gap: 20rpx;
  padding: 24rpx 0;

  &:not(:last-child) {
    border-bottom: 1rpx solid #f0f0f0;
  }
}

.process-dot {
  flex: none;
  width: 40rpx;
  height: 40rpx;
  line-height: 40rpx;
  text-align: center;
  background: #5A9A92;
  color: #fff;
  font-size: 22rpx;
  font-weight: 600;
  border-radius: 50%;
}

.process-text {
  flex: 1;
  font-size: 28rpx;
  color: #444;
  line-height: 1.6;
  padding-top: 4rpx;
}

.contact-card {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
  margin: 0 24rpx 32rpx;
  padding: 28rpx;
  background: #fff;
  border-radius: 16rpx;
}

.contact-text {
  flex: 1;
  font-size: 26rpx;
  color: #666;
  line-height: 1.65;
}

.action-wrap {
  padding: 0 24rpx;
}
</style>
