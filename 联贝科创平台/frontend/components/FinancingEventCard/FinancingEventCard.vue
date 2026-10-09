<template>
  <view class="event-card" :class="{ embedded }" @click="onTap">
    <view class="ev-main">
      <ProjectLogo :item="item" />
      <view class="ev-body">
        <view class="ev-row ev-row-title">
          <text class="ev-name">{{ item.companyName }}</text>
          <text class="ev-amount">{{ item.amount || '未透露' }}</text>
        </view>
        <view v-if="item.description || item.round" class="ev-row ev-row-meta">
          <text v-if="item.description" class="ev-desc">{{ item.description }}</text>
          <text v-if="item.round" class="ev-round">{{ item.round }}</text>
        </view>
      </view>
    </view>
    <view class="ev-foot">
      <text class="ev-inv">投资方：{{ item.investors || '-' }}</text>
      <text v-if="item.date" class="ev-date">{{ item.date }}</text>
    </view>
  </view>
</template>

<script setup>
import ProjectLogo from '@/components/ProjectLogo/ProjectLogo.vue'

const props = defineProps({
  item: { type: Object, required: true },
  /** 首页等卡片式嵌入：圆角、无分割线 */
  embedded: { type: Boolean, default: false }
})
const emit = defineEmits(['click'])

function onTap() {
  emit('click', props.item)
}
</script>

<style scoped lang="scss">
@import '@/styles/theme.scss';

$blue: $theme-primary;

.event-card {
  display: flex;
  flex-direction: column;
  padding: 28rpx 24rpx;
  background: #fff;
  border-bottom: 1rpx solid #f0f0f0;

  &.embedded {
    border-bottom: none;
    border-radius: 16rpx;
    margin: 0 24rpx 16rpx;
    padding: 28rpx;
  }
}

.ev-main {
  display: flex;
}

.ev-body {
  flex: 1;
  margin-left: 20rpx;
  min-width: 0;
}

.ev-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16rpx;
}

.ev-row-title {
  align-items: flex-start;
}

.ev-row-meta {
  margin-top: 6rpx;
}

.ev-name {
  flex: 1;
  font-size: 32rpx;
  font-weight: 700;
  color: #222;
  min-width: 0;
}

.ev-amount {
  flex-shrink: 0;
  font-size: 28rpx;
  color: $blue;
  font-weight: 400;
  text-align: right;
}

.ev-round {
  flex-shrink: 0;
  font-size: 24rpx;
  color: #999;
  text-align: right;
}

.ev-desc {
  flex: 1;
  font-size: 26rpx;
  color: #999;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ev-foot {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12rpx;
  margin-top: 16rpx;
  font-size: 24rpx;
}

.ev-inv {
  flex: 1;
  min-width: 0;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ev-date {
  flex-shrink: 0;
  color: #bbb;
}
</style>
