<template>
  <view class="bottom-bar">
    <view class="actions">
      <view class="action-item" :class="{ active: liked }" @click="$emit('like')">
        <u-icon name="thumb-up" :color="liked ? '#78B9B1' : '#666'" size="44rpx" />
        <text class="action-num">{{ likeCount }}</text>
      </view>
      <view class="action-item" @click="$emit('favorite')">
        <u-icon name="star" color="#666" size="44rpx" />
      </view>
      <view class="action-item" @click="$emit('share')">
        <u-icon name="share" color="#666" size="44rpx" />
      </view>
    </view>
    <view
      class="register-btn"
      :class="{ disabled: disabled }"
      @click="onRegister"
    >
      {{ btnText }}
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  likeCount: { type: Number, default: 0 },
  liked: { type: Boolean, default: false },
  btnText: { type: String, default: '立即报名' },
  disabled: { type: Boolean, default: false }
})
const emit = defineEmits(['like', 'favorite', 'share', 'register'])

function onRegister() {
  if (props.disabled) return
  emit('register')
}
</script>

<style scoped lang="scss">
@import '@/styles/theme.scss';

.bottom-bar {
  flex: none;
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 16rpx 24rpx calc(16rpx + env(safe-area-inset-bottom));
  background: #fff;
  border-top: 1rpx solid #f0f0f0;
}

.actions {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 4rpx;
  padding: 8rpx;
}

.action-num {
  font-size: 24rpx;
  color: #666;
}

.register-btn {
  flex: 1;
  height: 72rpx;
  line-height: 72rpx;
  text-align: center;
  background: $theme-button-gradient;
  color: #fff;
  font-size: 30rpx;
  font-weight: 600;
  border-radius: 36rpx;

  &.disabled {
    background: #ccc;
    color: #fff;
  }
}
</style>
