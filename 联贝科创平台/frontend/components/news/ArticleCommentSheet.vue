<template>
  <u-popup :show="show" mode="bottom" round="16" @close="$emit('close')">
    <view class="comment-sheet">
      <view class="sheet-head">
        <text class="sheet-title">写评论</text>
        <u-icon name="close" size="40rpx" color="#999" @click="$emit('close')" />
      </view>
      <textarea
        v-model="content"
        class="comment-textarea"
        placeholder="说说你的看法..."
        :maxlength="500"
        :focus="show"
        :show-confirm-bar="false"
        auto-height
      />
      <view class="sheet-foot">
        <text class="char-count">{{ content.length }}/500</text>
        <view class="send-btn" :class="{ disabled: !canSend || submitting }" @click="onSend">
          {{ submitting ? '发送中' : '发送' }}
        </view>
      </view>
    </view>
  </u-popup>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  show: { type: Boolean, default: false },
  submitting: { type: Boolean, default: false }
})
const emit = defineEmits(['close', 'submit'])

const content = ref('')

const canSend = computed(() => content.value.trim().length > 0)

watch(
  () => props.show,
  (visible) => {
    if (!visible) {
      content.value = ''
    }
  }
)

function onSend() {
  const text = content.value.trim()
  if (!text || props.submitting) return
  emit('submit', text)
}
</script>

<style scoped lang="scss">
.comment-sheet {
  padding: 28rpx 28rpx calc(28rpx + env(safe-area-inset-bottom));
  background: #fff;
}

.sheet-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
}

.sheet-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #222;
}

.comment-textarea {
  width: 100%;
  min-height: 180rpx;
  max-height: 320rpx;
  padding: 20rpx 24rpx;
  background: #f5f6f8;
  border-radius: 12rpx;
  font-size: 30rpx;
  color: #333;
  line-height: 1.6;
  box-sizing: border-box;
}

.sheet-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 24rpx;
}

.char-count {
  font-size: 24rpx;
  color: #999;
}

.send-btn {
  min-width: 140rpx;
  height: 64rpx;
  line-height: 64rpx;
  text-align: center;
  padding: 0 32rpx;
  background: #78B9B1;
  color: #fff;
  border-radius: 32rpx;
  font-size: 28rpx;

  &.disabled {
    opacity: 0.45;
  }
}
</style>
