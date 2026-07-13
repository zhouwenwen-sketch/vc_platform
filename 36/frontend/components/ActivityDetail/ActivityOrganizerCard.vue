<template>
  <view v-if="organizer" class="organizer-section">
    <text class="section-title">主办方</text>
    <view class="organizer-card" @click="$emit('click', organizer)">
      <ProjectLogo :logo-url="organizer.logoUrl" :name="organizer.name" size="md" round />
      <view class="card-body">
        <text class="org-name">{{ organizer.name }}</text>
        <text v-if="organizer.metaText" class="org-meta">{{ organizer.metaText }}</text>
        <view v-if="organizer.tags?.length" class="tag-row">
          <text v-for="tag in organizer.tags" :key="tag" class="tag-item">{{ tag }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import ProjectLogo from '@/components/ProjectLogo/ProjectLogo.vue'

const props = defineProps({
  organizer: { type: Object, default: null }
})
defineEmits(['click'])
</script>

<style scoped lang="scss">
.organizer-section {
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

.organizer-card {
  display: flex;
  gap: 20rpx;
  padding: 24rpx;
  background: #fff;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
}

.card-body {
  flex: 1;
  min-width: 0;
}

.org-name {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #222;
  line-height: 1.4;
}

.org-meta {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #999;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 12rpx;
}

.tag-item {
  padding: 4rpx 14rpx;
  background: #f5f6f8;
  border-radius: 6rpx;
  font-size: 22rpx;
  color: #666;
}
</style>
