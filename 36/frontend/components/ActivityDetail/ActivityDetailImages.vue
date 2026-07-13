<template>
  <view class="detail-section">
    <text class="section-title">活动详情</text>
    <view v-if="images.length" class="image-list">
      <image
        v-for="(url, i) in images"
        :key="i"
        :src="url"
        class="detail-img"
        mode="widthFix"
        @click="preview(i)"
      />
    </view>
    <view v-else class="empty-tip">
      <text>暂无详情内容</text>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  images: { type: Array, default: () => [] }
})

function preview(index) {
  uni.previewImage({
    current: index,
    urls: props.images
  })
}
</script>

<style scoped lang="scss">
.detail-section {
  margin-top: 16rpx;
  background: #fff;
  padding: 28rpx 24rpx 32rpx;
}

.section-title {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #222;
  margin-bottom: 20rpx;
}

.image-list {
  display: flex;
  flex-direction: column;
}

.detail-img {
  width: 100%;
  display: block;
  border-radius: 8rpx;
}

.detail-img + .detail-img {
  margin-top: 4rpx;
}

.empty-tip {
  padding: 80rpx 0;
  text-align: center;
  font-size: 28rpx;
  color: #999;
}
</style>
