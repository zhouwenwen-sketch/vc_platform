<template>
  <view class="project-search-item" :class="{ compact }">
    <ProjectLogo :item="item" />
    <view class="item-body">
      <view class="item-top">
        <HighlightText
          :text="item.name"
          :keyword="keyword"
          custom-class="item-name"
        />
        <text v-if="item.round" class="round-tag">{{ item.round }}</text>
      </view>
      <HighlightText
        v-if="item.companyDesc"
        :text="item.companyDesc"
        :keyword="keyword"
        custom-class="item-desc"
      />
      <view class="entity-row">
        <text class="entity-label">企业主体：</text>
        <HighlightText
          v-if="item.entityName"
          :text="item.entityName"
          :keyword="keyword"
          custom-class="entity-name"
        />
        <text v-else class="entity-name entity-empty">—</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import HighlightText from '@/components/HighlightText/HighlightText.vue'
import ProjectLogo from '@/components/ProjectLogo/ProjectLogo.vue'

defineProps({
  item: { type: Object, required: true },
  keyword: { type: String, default: '' },
  /** 下拉模式：描述单行省略，便于固定三条可视高度 */
  compact: { type: Boolean, default: false }
})
</script>

<style scoped lang="scss">
.project-search-item {
  display: flex;
  gap: 20rpx;
  padding: 24rpx;
  box-sizing: border-box;
}

.compact {
  min-height: 192rpx;
}

.item-body {
  flex: 1;
  min-width: 0;
}

.item-top {
  display: flex;
  align-items: center;
  margin-bottom: 8rpx;
}

:deep(.item-name) {
  flex: 1;
  min-width: 0;
  font-size: 30rpx;
  font-weight: 600;
  color: #222;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.round-tag {
  flex: none;
  margin-left: 12rpx;
  padding: 4rpx 12rpx;
  font-size: 22rpx;
  line-height: 1.4;
  color: #78B9B1;
  background: rgba(41, 121, 255, 0.1);
  border-radius: 6rpx;
}

:deep(.item-desc) {
  display: block;
  overflow: hidden;
  font-size: 26rpx;
  line-height: 1.5;
  color: #666;
}

.compact :deep(.item-desc) {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.entity-row {
  display: flex;
  align-items: flex-start;
  margin-top: 8rpx;
  font-size: 24rpx;
  line-height: 1.5;
}

.entity-label {
  flex: none;
  color: #999;
}

:deep(.entity-name) {
  flex: 1;
  min-width: 0;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.entity-empty {
  flex: 1;
}
</style>
