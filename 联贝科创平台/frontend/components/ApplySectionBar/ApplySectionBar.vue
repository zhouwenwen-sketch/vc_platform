<template>
  <view class="section-bar">
    <text class="section-bar-text">{{ title }}</text>
  </view>
</template>

<script setup>
defineProps({
  title: { type: String, required: true }
})
</script>

<style scoped lang="scss">
.section-bar {
  background: #f5f5f5;
  padding: 24rpx 32rpx;
}

.section-bar-text {
  font-size: 30rpx;
  font-weight: 700;
  color: #222;
}
</style>
