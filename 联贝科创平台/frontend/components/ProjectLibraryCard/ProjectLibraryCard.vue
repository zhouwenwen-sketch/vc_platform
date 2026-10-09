<template>
  <view class="library-card" @click="onTap">
    <ProjectLogo :item="item" />
    <view class="item-info">
      <view class="item-header">
        <text class="item-name">{{ item.name }}</text>
        <u-tag v-if="item.round" :text="item.round" size="mini" plain type="primary" />
      </view>
      <text v-if="item.companyDesc" class="item-desc">{{ item.companyDesc }}</text>
      <text v-if="item.metaLine" class="item-meta">{{ item.metaLine }}</text>
    </view>
  </view>
</template>

<script setup>
import ProjectLogo from '@/components/ProjectLogo/ProjectLogo.vue'

const props = defineProps({
  item: { type: Object, required: true }
})
const emit = defineEmits(['click'])

function onTap() {
  emit('click', props.item)
}
</script>

<style scoped lang="scss">
@import '@/styles/theme.scss';

$blue: $theme-primary;

.library-card {
  display: flex;
  padding: 28rpx 24rpx;
  background: #fff;
  border-bottom: 1rpx solid #f5f5f5;
}

.item-info {
  flex: 1;
  margin-left: 20rpx;
  min-width: 0;
}

.item-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.item-name {
  flex: 1;
  font-size: 32rpx;
  font-weight: 700;
  color: #222;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-desc {
  display: block;
  margin-top: 10rpx;
  font-size: 26rpx;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #999;
}
</style>
