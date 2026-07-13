<template>
  <view class="project-search-dropdown">
    <view v-if="showPanel && keyword.trim() && list.length" class="search-dropdown-panel">
      <scroll-view scroll-y class="search-dropdown-scroll" :show-scrollbar="false">
        <view
          v-for="item in list"
          :key="item.id"
          class="search-dropdown-item-wrap"
          @click="$emit('select', item)"
        >
          <ProjectSearchResultItem :item="item" :keyword="keyword" compact />
        </view>
      </scroll-view>
    </view>
    <view v-else-if="showPanel && keyword.trim() && searched && !loading" class="result-empty">
      <text>未找到匹配项目</text>
    </view>
  </view>
</template>

<script setup>
import ProjectSearchResultItem from '@/components/ProjectSearchResultItem/ProjectSearchResultItem.vue'

defineProps({
  keyword: { type: String, default: '' },
  list: { type: Array, default: () => [] },
  showPanel: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  searched: { type: Boolean, default: false }
})

defineEmits(['select'])
</script>

<style scoped lang="scss">
@import '@/styles/project-search-dropdown.scss';

.result-empty {
  margin-top: 16rpx;
  padding: 24rpx;
  font-size: 26rpx;
  color: #999;
  text-align: center;
  background: #fff;
  border: 1rpx solid #eee;
  border-radius: 12rpx;
}
</style>
