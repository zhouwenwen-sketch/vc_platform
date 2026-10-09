<template>
  <view class="legal-page">
    <view class="version-row">
      <text class="version-label">历史版本：</text>
      <view class="version-picker">
        <text>最新版</text>
        <u-icon name="arrow-down" size="24rpx" color="#666" />
      </view>
    </view>

    <scroll-view scroll-y class="legal-scroll" :show-scrollbar="false">
      <view class="legal-body">
        <text class="doc-title">{{ doc.title }}</text>
        <text class="doc-date">更新日期：{{ doc.updateDate }}</text>
        <text class="doc-date">生效日期：{{ doc.effectiveDate }}</text>

        <block v-for="(item, index) in doc.paragraphs" :key="index">
          <text v-if="isHeading(item)" class="section-title">{{ item }}</text>
          <text v-else class="section-text">{{ item }}</text>
        </block>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
defineProps({
  doc: {
    type: Object,
    required: true
  }
})

function isHeading(text) {
  if (!text) return false
  return (
    /^[\d一二三四五六七八九十]+[\.．、]/.test(text) ||
    text === '目录' ||
    /^第[\d一二三四五六七八九十]+[条章节]/.test(text)
  )
}
</script>

<style lang="scss" scoped>
.legal-page {
  min-height: 100vh;
  background: #fff;
  display: flex;
  flex-direction: column;
}

.version-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 24rpx 32rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.version-label {
  font-size: 28rpx;
  color: #333;
}

.version-picker {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 16rpx;
  border: 1rpx solid #e5e5e5;
  border-radius: 8rpx;
  font-size: 28rpx;
  color: #333;
}

.legal-scroll {
  flex: 1;
  height: 0;
}

.legal-body {
  padding: 32rpx;
  padding-bottom: 80rpx;
}

.doc-title {
  display: block;
  text-align: center;
  font-size: 36rpx;
  font-weight: 700;
  color: #111;
  line-height: 1.5;
  margin-bottom: 24rpx;
}

.doc-date {
  display: block;
  text-align: center;
  font-size: 26rpx;
  color: #666;
  line-height: 1.8;
}

.section-title {
  display: block;
  margin-top: 32rpx;
  font-size: 30rpx;
  font-weight: 700;
  color: #111;
  line-height: 1.7;
}

.section-text {
  display: block;
  margin-top: 20rpx;
  font-size: 28rpx;
  color: #333;
  line-height: 1.8;
  text-align: justify;
}
</style>
