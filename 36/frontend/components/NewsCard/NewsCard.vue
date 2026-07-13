<template>
  <view class="news-card" :class="{ plain }" @click="$emit('click', item)">
    <!-- 快讯：橙色标签 + 相对时间 -->
    <view v-if="item.type === '快讯'" class="flash-row">
      <text class="flash-tag">快讯</text>
      <text class="flash-time">{{ item.publishTime }}</text>
    </view>

    <text class="news-title" :class="{ article: item.type === '文章' }">{{ item.title }}</text>

    <!-- 文章：作者 + 时间 -->
    <view v-if="item.type === '文章'" class="meta-row">
      <text v-if="item.author" class="author">{{ item.author }}</text>
      <text class="time">{{ item.publishTime }}</text>
    </view>

    <!-- 关联项目条（有项目名时展示） -->
    <view
      v-if="item.projectName"
      class="project-mini"
      @click.stop="onProjectTap"
    >
      <ProjectLogo :logo-url="item.logoUrl" :name="item.projectName" size="sm" />
      <text class="p-name">{{ item.projectName }}</text>
      <text v-if="item.roundTag" class="p-tag">{{ item.roundTag }}</text>
      <text v-if="item.regionTag" class="p-tag">{{ item.regionTag }}</text>
    </view>
  </view>
</template>

<script setup>
import ProjectLogo from '@/components/ProjectLogo/ProjectLogo.vue'

const props = defineProps({
  item: { type: Object, required: true },
  plain: { type: Boolean, default: false }
})
const emit = defineEmits(['click', 'project'])

function onProjectTap() {
  if (props.item.projectId) {
    emit('project', props.item)
  }
}
</script>

<style lang="scss" scoped>
.news-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx 32rpx;
  margin: 0 24rpx 16rpx;

  &.plain {
    margin: 0;
    border-radius: 0;
    padding: 32rpx;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }
  }
}

.flash-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 16rpx;
}

.flash-tag {
  padding: 4rpx 12rpx;
  font-size: 22rpx;
  line-height: 1.4;
  color: #ff6a00;
  background: rgba(255, 106, 0, 0.12);
  border-radius: 6rpx;
}

.flash-time {
  font-size: 24rpx;
  color: #999;
}

.news-title {
  font-size: 32rpx;
  font-weight: 400;
  color: #222;
  line-height: 1.5;
  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;

  &.article {
    font-size: 34rpx;
    line-height: 1.45;
  }
}

.meta-row {
  display: flex;
  align-items: center;
  margin-top: 16rpx;
  font-size: 24rpx;
  color: #999;

  .author {
    margin-right: 16rpx;
  }
}

.project-mini {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 24rpx;
  padding: 20rpx 24rpx;
  background: #f5f8fc;
  border-radius: 12rpx;
}

.p-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  margin-right: 4rpx;
}

.p-tag {
  padding: 4rpx 12rpx;
  font-size: 22rpx;
  line-height: 1.4;
  color: #78B9B1;
  background: rgba(41, 121, 255, 0.1);
  border-radius: 6rpx;
}
</style>
