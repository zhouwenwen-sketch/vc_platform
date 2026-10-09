<template>
  <view class="rules-page">
    <scroll-view scroll-y class="rules-scroll" :show-scrollbar="false">
      <text class="intro">{{ rules.intro }}</text>

      <view v-for="section in rules.sections" :key="section.title" class="section">
        <text class="section-title">{{ section.title }}</text>
        <view v-for="(item, index) in section.items" :key="index" class="rule-item">
          <text class="rule-index">{{ index + 1 }}.</text>
          <text class="rule-text">{{ item }}</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { INVESTOR_AUTH_RULES } from '@/content/investor-auth-rules.js'

const rules = INVESTOR_AUTH_RULES
</script>

<style scoped lang="scss">
.rules-page {
  box-sizing: border-box;
  min-height: 100%;
  background: #fff;
}

.rules-scroll {
  height: 100vh;
  padding: 32rpx;
  box-sizing: border-box;
}

.intro {
  display: block;
  font-size: 28rpx;
  color: #333;
  line-height: 1.8;
}

.section {
  margin-top: 40rpx;
}

.section-title {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: #222;
  line-height: 1.6;
  margin-bottom: 24rpx;
}

.rule-item {
  display: flex;
  align-items: flex-start;
  gap: 8rpx;
  margin-bottom: 20rpx;
}

.rule-index {
  flex: none;
  font-size: 28rpx;
  color: #666;
  line-height: 1.8;
}

.rule-text {
  flex: 1;
  font-size: 28rpx;
  color: #666;
  line-height: 1.8;
}
</style>
