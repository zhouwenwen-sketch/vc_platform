<template>
  <view class="project-card" @click="handleClick">
    <ProjectLogo :item="item" size="lg" />
    <view class="info">
      <view class="row-top">
        <text class="name">{{ item.name }}</text>
        <u-tag :text="item.status" size="mini" plain type="primary" />
      </view>
      <text class="desc">{{ item.description }}</text>
      <text class="meta">{{ metaLine }}</text>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import ProjectLogo from '@/components/ProjectLogo/ProjectLogo.vue'
import { navigateToProjectDetail, PROJECT_ENTRY } from '@/utils/projectNavigate.js'
import { buildProjectCardMetaLine } from '@/utils/projectCardMeta.js'

const props = defineProps({
  item: { type: Object, required: true }
})
const emit = defineEmits(['click'])

const metaLine = computed(() => props.item.metaLine || buildProjectCardMetaLine(props.item))

function handleClick() {
  if (props.item?.id) {
    navigateToProjectDetail(props.item.id, { from: PROJECT_ENTRY.HOME })
  }
  emit('click', props.item)
}
</script>

<style lang="scss" scoped>
.project-card {
  display: flex;
  padding: 28rpx 30rpx;
  background: #fff;
  border-radius: 16rpx;
  margin: 0 24rpx 16rpx;
}

.info {
  flex: 1;
  margin-left: 24rpx;
  min-width: 0;
}

.row-top {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 8rpx;

  .name {
    font-size: 32rpx;
    font-weight: 700;
    color: #333;
  }
}

.desc {
  font-size: 26rpx;
  color: #666;
  line-height: 1.4;
  display: block;
  margin-bottom: 8rpx;
}

.meta {
  font-size: 24rpx;
  color: #999;
  line-height: 1.4;
}
</style>
