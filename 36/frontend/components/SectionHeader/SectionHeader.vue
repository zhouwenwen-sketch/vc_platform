<template>
  <view class="section-header" :class="{ gradient: gradient }">
    <view class="left">
      <text class="title">{{ title }}</text>
      <text v-if="subtitle" class="subtitle">{{ subtitle }}</text>
    </view>
    <view v-if="linkText || actionText" class="action">
      <text v-if="linkText" class="link" @click.stop="$emit('link')">{{ linkText }}</text>
      <view v-if="actionText" class="more-wrap" @click="$emit('action')">
        <text class="more">{{ actionText }}</text>
        <u-icon v-if="showArrow" name="arrow-right" size="24rpx" color="#999" />
      </view>
    </view>
  </view>
</template>

<script setup>
defineProps({
  title: { type: String, default: '' },
  subtitle: { type: String, default: '' },
  actionText: { type: String, default: '更多' },
  linkText: { type: String, default: '' },
  showArrow: { type: Boolean, default: true },
  gradient: { type: Boolean, default: false }
})
defineEmits(['action', 'link'])
</script>

<style lang="scss" scoped>
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 30rpx 20rpx;

  &.gradient {
    background: linear-gradient(180deg, #fff5ee 0%, #ffffff 60%);
    border-radius: 20rpx 20rpx 0 0;
  }

  .left {
    display: flex;
    flex-direction: column;
    gap: 6rpx;
  }

  .title {
    font-size: 34rpx;
    font-weight: 700;
    color: #333;
  }

  .subtitle {
    font-size: 24rpx;
    color: #999;
  }

  .action {
    display: flex;
    align-items: center;
    gap: 8rpx;
  }

  .link {
    font-size: 24rpx;
    color: #666;
    margin-right: 16rpx;
  }

  .more-wrap {
    display: flex;
    align-items: center;
    gap: 8rpx;
  }

  .more {
    font-size: 26rpx;
    color: #999;
  }
}
</style>
