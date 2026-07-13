<template>
  <view class="bottom-bar">
    <view class="comment-input" @click="onComment">
      <text class="placeholder">写评论...</text>
    </view>
    <view class="action-icons">
      <view class="icon-btn" @click="onComment">
        <u-icon name="chat" color="#666" size="44rpx" />
        <view v-if="commentCount > 0" class="badge">{{ commentCount > 99 ? '99+' : commentCount }}</view>
      </view>
      <view class="icon-btn" :class="{ active: liked }" @click="$emit('like')">
        <u-icon name="thumb-up" :color="liked ? '#78B9B1' : '#666'" size="44rpx" />
      </view>
      <view class="icon-btn" @click="onFavorite">
        <u-icon name="star" color="#666" size="44rpx" />
      </view>
    </view>
  </view>
</template>

<script setup>
defineProps({
  liked: { type: Boolean, default: false },
  commentCount: { type: Number, default: 0 }
})
const emit = defineEmits(['like', 'comment'])

function onComment() {
  emit('comment')
}

function onFavorite() {
  uni.showToast({ title: '收藏功能敬请期待', icon: 'none' })
}
</script>

<style scoped lang="scss">
.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 16rpx 24rpx calc(16rpx + env(safe-area-inset-bottom));
  background: #fff;
  border-top: 1rpx solid #f0f0f0;
}

.comment-input {
  flex: 1;
  height: 72rpx;
  padding: 0 28rpx;
  background: #f5f6f8;
  border-radius: 36rpx;
  display: flex;
  align-items: center;
}

.placeholder {
  font-size: 28rpx;
  color: #bbb;
}

.action-icons {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.icon-btn {
  position: relative;
  padding: 8rpx;

  &.active {
    opacity: 1;
  }
}

.badge {
  position: absolute;
  top: 0;
  right: 0;
  min-width: 28rpx;
  height: 28rpx;
  line-height: 28rpx;
  padding: 0 6rpx;
  background: #ff4d4f;
  color: #fff;
  font-size: 18rpx;
  text-align: center;
  border-radius: 14rpx;
}
</style>
