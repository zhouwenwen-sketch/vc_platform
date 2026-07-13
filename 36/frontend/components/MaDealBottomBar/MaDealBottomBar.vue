<template>
  <view class="bottom-bar">
    <view class="actions">
      <view class="action-item" @click="$emit('appoint')">
        <u-icon name="calendar" color="#666" size="40rpx" />
        <text class="action-text">预约</text>
      </view>
      <view class="action-item" @click="$emit('call')">
        <u-icon name="phone" color="#666" size="40rpx" />
        <text class="action-text">通话</text>
      </view>
      <view class="action-item" @click="$emit('favorite')">
        <u-icon name="heart" color="#666" size="40rpx" />
        <text class="action-text">收藏</text>
      </view>
    </view>
    <view
      class="cta-btn"
      :class="{ disabled }"
      @click="onCta"
    >
      {{ btnText }}
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  btnText: { type: String, default: '立即预约沟通' },
  disabled: { type: Boolean, default: false }
})
const emit = defineEmits(['appoint', 'call', 'favorite', 'cta'])

function onCta() {
  if (props.disabled) return
  emit('cta')
}
</script>

<style scoped lang="scss">
@import '@/styles/theme.scss';

.bottom-bar {
  flex: none;
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx 24rpx calc(16rpx + env(safe-area-inset-bottom));
  background: #fff;
  border-top: 1rpx solid #f0f0f0;
}

.actions {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
  min-width: 72rpx;
}

.action-text {
  font-size: 20rpx;
  color: #666;
}

.cta-btn {
  flex: 1;
  height: 80rpx;
  line-height: 80rpx;
  text-align: center;
  background: $theme-button-gradient;
  color: #fff;
  font-size: 30rpx;
  font-weight: 600;
  border-radius: 8rpx;

  &.disabled {
    background: #ccc;
  }
}
</style>
