<template>
  <view class="flash-detail">
    <text v-if="displayTitle" class="flash-title">{{ displayTitle }}</text>

    <view class="flash-row">
      <text class="flash-tag">快讯</text>
      <text class="flash-time">{{ detail.publishTime }}</text>
    </view>

    <text v-if="showBody" class="article-body">{{ bodyText }}</text>

    <view
      v-if="detail.projectName"
      class="project-mini"
      @click="onProjectTap"
    >
      <ProjectLogo :logo-url="detail.logoUrl" :name="detail.projectName" size="sm" />
      <text class="p-name">{{ detail.projectName }}</text>
      <text v-if="detail.round" class="p-tag">{{ detail.round }}</text>
      <text v-if="detail.region" class="p-tag">{{ detail.region }}</text>
    </view>

    <view v-if="detail.sourceUrl" class="source-link">
      <u-icon name="attach" color="#78B9B1" size="32rpx" />
      <text class="link-text">原文链接</text>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import ProjectLogo from '@/components/ProjectLogo/ProjectLogo.vue'
import { newsDetailBody, newsTextsEqual } from '@/utils/transform.js'

const props = defineProps({
  detail: { type: Object, required: true }
})
const emit = defineEmits(['project'])

const displayTitle = computed(() => {
  const raw = (props.detail.title || '').trim()
  if (raw) return raw.split(/\r?\n/)[0].trim()
  const content = (props.detail.content || '').trim()
  if (!content) return ''
  return content.split(/\r?\n/)[0].trim()
})

const bodyText = computed(() =>
  newsDetailBody(displayTitle.value, props.detail.content)
)

const showBody = computed(() => {
  const body = (bodyText.value || '').trim()
  if (!body) return false
  return !newsTextsEqual(body, displayTitle.value)
})

function onProjectTap() {
  if (props.detail.projectId) {
    emit('project', props.detail)
  }
}
</script>

<style scoped lang="scss">
.flash-detail {
  padding: 32rpx 32rpx 48rpx;
}

.flash-title {
  display: block;
  font-size: 40rpx;
  font-weight: 600;
  color: #222;
  line-height: 1.45;
  margin-bottom: 20rpx;
}

.flash-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 24rpx;
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
  font-size: 26rpx;
  color: #999;
}

.article-body {
  display: block;
  font-size: 32rpx;
  font-weight: 400;
  color: #333;
  line-height: 1.75;
  white-space: pre-wrap;
}

.project-mini {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 32rpx;
  padding: 20rpx 24rpx;
  background: #f5f8fc;
  border-radius: 12rpx;
}

.p-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
}

.p-tag {
  padding: 4rpx 12rpx;
  font-size: 22rpx;
  line-height: 1.4;
  color: #78B9B1;
  background: rgba(41, 121, 255, 0.1);
  border-radius: 6rpx;
}

.source-link {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-top: 40rpx;
}

.link-text {
  font-size: 28rpx;
  color: #78B9B1;
}
</style>
