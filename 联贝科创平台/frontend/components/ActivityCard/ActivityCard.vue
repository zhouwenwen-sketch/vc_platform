<template>
  <view class="activity-card" @click="$emit('click', item)">
    <u-image :src="item.cover" width="100%" height="280rpx" radius="16rpx 16rpx 0 0" mode="aspectFill" />
    <view class="body">
      <view class="status-row">
        <u-tag
          :text="statusText"
          size="mini"
          :type="tagType"
          plain
        />
        <text class="participants">{{ item.participants }}人参与</text>
      </view>
      <text class="title">{{ item.title }}</text>
      <view class="info-line">
        <u-icon name="clock" size="28rpx" color="#999" />
        <text>{{ timeText }}</text>
      </view>
      <view class="info-line">
        <u-icon name="map" size="28rpx" color="#999" />
        <text>{{ item.location }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  item: { type: Object, required: true }
})
defineEmits(['click'])

const statusText = computed(() => props.item.statusText || (props.item.status === 'registering' ? '正在报名' : props.item.status === 'ended' ? '已结束' : '进行中'))

const tagType = computed(() => {
  if (props.item.status === 'registering') return 'success'
  if (props.item.status === 'ended') return 'info'
  return 'primary'
})

const timeText = computed(() => {
  const s = props.item.startTime || ''
  return s.length > 16 ? s.slice(0, 16) : s
})
</script>

<style lang="scss" scoped>
.activity-card {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  margin: 0 24rpx 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.body {
  padding: 24rpx 28rpx 28rpx;
}

.status-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;

  .participants {
    font-size: 24rpx;
    color: #999;
  }
}

.title {
  font-size: 32rpx;
  font-weight: 700;
  color: #333;
  line-height: 1.45;
  display: block;
  margin-bottom: 16rpx;
}

.info-line {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-top: 10rpx;
  font-size: 26rpx;
  color: #666;
}
</style>
