<template>
  <view class="stepper">
    <view class="stepper-track">
      <view
        v-for="n in total"
        :key="n"
        class="step-node"
        :class="{ active: n === current, done: n < current }"
      >
        <text class="step-num">{{ n }}</text>
      </view>
    </view>
    <text class="stepper-tip">仅需三步完成您的申请</text>
  </view>
</template>

<script setup>
defineProps({
  current: { type: Number, default: 1 },
  total: { type: Number, default: 3 }
})
</script>

<style scoped lang="scss">
.stepper {
  margin-bottom: 32rpx;
}

.stepper-track {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 48rpx;
  margin-bottom: 16rpx;
}

.step-node {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e8e8e8;
  transform: rotate(45deg);
  border-radius: 8rpx;

  &.active {
    background: #78B9B1;
  }

  &.done {
    background: rgba(41, 121, 255, 0.35);
  }
}

.step-num {
  transform: rotate(-45deg);
  font-size: 28rpx;
  font-weight: 600;
  color: #999;
}

.active .step-num,
.done .step-num {
  color: #fff;
}

.stepper-tip {
  display: block;
  text-align: center;
  font-size: 26rpx;
  color: #666;
}
</style>
