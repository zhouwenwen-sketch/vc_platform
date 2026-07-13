<template>
  <u-popup :show="show" mode="bottom" round="16" @close="$emit('close')">
    <view class="form-picker-sheet">
      <view class="form-picker-head">
        <text class="form-picker-cancel" @click="$emit('close')">取消</text>
        <text class="form-picker-title">{{ title }}</text>
        <text class="form-picker-confirm" @click="$emit('confirm')">确定</text>
      </view>
      <scroll-view scroll-y class="form-picker-scroll" :show-scrollbar="false">
        <view
          v-for="item in options"
          :key="item"
          class="form-picker-option"
          :class="{ selected: selectedSet.has(item) }"
          @click="$emit('toggle', item)"
        >
          <text>{{ item }}</text>
          <u-icon
            v-if="selectedSet.has(item)"
            name="checkmark"
            color="#78B9B1"
            size="32rpx"
          />
        </view>
      </scroll-view>
    </view>
  </u-popup>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  show: { type: Boolean, default: false },
  title: { type: String, default: '' },
  options: { type: Array, default: () => [] },
  selected: { type: Array, default: () => [] }
})

defineEmits(['close', 'confirm', 'toggle'])

const selectedSet = computed(() => new Set(props.selected))
</script>

<style scoped lang="scss">
@import '@/styles/form-picker-sheet.scss';
</style>
