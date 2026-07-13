<template>
  <view class="coverage-hero">
    <text class="hero-brand">联贝科创平台</text>
    <text class="hero-title">寻求报道申请</text>
  </view>
</template>

<style scoped lang="scss">
@import '@/styles/theme.scss';

.coverage-hero {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  height: 100rpx;
  background: $theme-banner-gradient;
  padding: 0 32rpx;
}

.hero-brand {
  position: absolute;
  left: 32rpx;
  font-size: 28rpx;
  font-weight: 700;
  color: #fff;
  letter-spacing: 1rpx;
}

.hero-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #fff;
}
</style>
