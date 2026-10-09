<template>
  <view class="preview-block">
    <view class="preview-head-bar">
      <text class="preview-head-text">项目卡片预览（移动端）</text>
    </view>
    <view class="preview-card">
      <ProjectLogo :logo-url="form.logoUrl" :name="form.projectName" />
      <view class="preview-body">
        <view class="preview-title-row">
          <text class="preview-name">{{ form.projectName || '项目名称' }}</text>
          <u-tag
            v-if="form.financingRound"
            :text="form.financingRound"
            size="mini"
            plain
            type="primary"
          />
        </view>
        <text class="preview-desc">{{ form.oneLiner || '一句话介绍将展示在这里' }}</text>
        <text v-if="metaLine" class="preview-meta">{{ metaLine }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import ProjectLogo from '@/components/ProjectLogo/ProjectLogo.vue'

const props = defineProps({
  form: { type: Object, required: true }
})

const metaLine = computed(() => {
  const parts = []
  if (props.form.industries?.length) {
    parts.push(props.form.industries.join(' | '))
  }
  if (props.form.country === '海外') {
    if (props.form.overseasLocation) parts.push(props.form.overseasLocation)
  } else {
    const loc = [props.form.province, props.form.city].filter(Boolean).join(' ')
    if (loc) parts.push(loc)
  }
  if (props.form.establishDate) {
    parts.push(props.form.establishDate.slice(0, 4))
  }
  return parts.join(' | ')
})
</script>

<style scoped lang="scss">
@import '@/styles/theme.scss';

.preview-block {
  margin-top: 8rpx;
}

.preview-head-bar {
  padding: 20rpx 0;
  border-top: 1rpx solid #f0f0f0;
}

.preview-head-text {
  font-size: 26rpx;
  color: #999;
}

.preview-card {
  display: flex;
  padding: 28rpx 24rpx;
  background: #f8f9fb;
  border-radius: 12rpx;
}

.preview-body {
  flex: 1;
  margin-left: 20rpx;
  min-width: 0;
}

.preview-title-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 8rpx;
}

.preview-name {
  flex: 1;
  font-size: 30rpx;
  font-weight: 700;
  color: #222;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.preview-desc {
  display: block;
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
  margin-bottom: 8rpx;
}

.preview-meta {
  display: block;
  font-size: 24rpx;
  color: #999;
  line-height: 1.4;
}
</style>

