<template>
  <view
    v-if="show"
    class="dropdown-mask"
    :style="{ top: maskTop + 'px' }"
    @click="$emit('close')"
    @touchmove.stop.prevent
  >
    <view class="dropdown-panel" @click.stop @touchmove.stop>
      <view class="dropdown-body">
        <slot />
      </view>
      <view class="dropdown-footer">
        <view class="foot-btn reset" @click="$emit('reset')">重置</view>
        <view class="foot-btn confirm" @click="$emit('confirm')">确定</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'

defineOptions({
  options: {
    // 允许页面 scss 与组件样式共同作用于 slot 内容（H5 兼容）
    styleIsolation: 'shared'
  }
})

const props = defineProps({
  show: { type: Boolean, default: false },
  /** 用于计算遮罩 top，紧贴筛选栏底部（需在页面内唯一） */
  anchorSelector: { type: String, default: '.library-filter-anchor' }
})

defineEmits(['close', 'reset', 'confirm'])

const maskTop = ref(0)

function measureMaskTop() {
  uni
    .createSelectorQuery()
    .select(props.anchorSelector)
    .boundingClientRect((rect) => {
      if (rect && rect.bottom > 0) {
        maskTop.value = rect.bottom
      }
    })
    .exec()
}

watch(
  () => props.show,
  (visible) => {
    if (visible) {
      nextTick(() => {
        measureMaskTop()
        setTimeout(measureMaskTop, 50)
      })
    }
  }
)
</script>

<style lang="scss" scoped>
@import '@/styles/form-picker-sheet.scss';

.dropdown-mask {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 200;
  background: rgba(0, 0, 0, 0.35);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.dropdown-panel {
  width: 100%;
  background: #fff;
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.dropdown-body {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.dropdown-footer {
  flex-shrink: 0;
  display: flex;
  padding: 24rpx 32rpx calc(24rpx + env(safe-area-inset-bottom));
  gap: 0;
  border-top: 1rpx solid #f0f0f0;
  background: #fff;
}

.foot-btn {
  flex: 1;
  height: 80rpx;
  line-height: 80rpx;
  text-align: center;
  font-size: 30rpx;

  &.reset {
    color: var(--filter-accent, #78B9B1);
    background: #fff;
    border: 2rpx solid var(--filter-accent, #78B9B1);
    border-radius: 40rpx 0 0 40rpx;
  }

  &.confirm {
    color: #fff;
    background: var(--filter-accent, #78B9B1);
    border-radius: 0 40rpx 40rpx 0;
  }
}

:deep(.dropdown-scroll) {
  flex: 1;
  min-height: 0;
  height: 0;
}

:deep(.dropdown-scroll.single-col),
:deep(.region-right) {
  padding: 16rpx 24rpx;
  box-sizing: border-box;
}

:deep(.dropdown-option) {
  @include form-picker-option-base;

  &.selected {
    color: var(--filter-accent, #78B9B1);
  }
}

:deep(.region-panel) {
  display: flex;
  flex: 1;
  min-height: 0;
  height: 0;
}

:deep(.region-left) {
  width: 200rpx;
  background: #f7f8fa;
  height: 100%;
}

:deep(.region-right) {
  flex: 1;
  height: 100%;
}

:deep(.region-right-empty) {
  flex: 1;
  background: #fff;
}

:deep(.region-zone) {
  padding: 32rpx 24rpx;
  font-size: 30rpx;
  color: #333;

  &.selected {
    color: var(--filter-accent, #78B9B1);
    font-weight: 600;
    background: #fff;
  }
}
</style>
