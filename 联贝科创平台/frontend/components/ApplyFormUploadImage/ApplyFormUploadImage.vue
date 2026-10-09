<template>
  <view class="field">
    <view class="label-row">
      <text v-if="required" class="required">*</text>
      <text class="label">{{ label }}</text>
    </view>
    <view class="upload-row">
      <view class="upload-box" @click="chooseImage">
        <image v-if="modelValue" :src="modelValue" class="upload-preview" mode="aspectFill" />
        <u-icon v-else name="plus" color="#ccc" size="48rpx" />
      </view>
      <text class="upload-tip">{{ tip }}</text>
    </view>
  </view>
</template>

<script setup>
defineProps({
  label: { type: String, default: '项目LOGO' },
  modelValue: { type: String, default: '' },
  required: { type: Boolean, default: false },
  tip: {
    type: String,
    default: '请上传jpg/png文件，文件大小不超过5MB，尽量为正方形'
  },
  maxSize: { type: Number, default: 5 * 1024 * 1024 }
})
const emit = defineEmits(['update:modelValue'])

function chooseImage() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success(res) {
      const file = res.tempFiles?.[0] || { path: res.tempFilePaths?.[0], size: 0 }
      const path = file.path || res.tempFilePaths?.[0]
      if (!path) return
      if (file.size && file.size > 5 * 1024 * 1024) {
        uni.showToast({ title: '图片大小不能超过5MB', icon: 'none' })
        return
      }
      emit('update:modelValue', path)
    }
  })
}
</script>

<style scoped lang="scss">
@import '@/styles/apply-form.scss';

.upload-row {
  display: flex;
  align-items: flex-start;
  gap: 24rpx;
}

.upload-box {
  width: 160rpx;
  height: 160rpx;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fb;
  border-radius: 12rpx;
  overflow: hidden;
}

.upload-preview {
  width: 100%;
  height: 100%;
}

.upload-tip {
  flex: 1;
  font-size: 24rpx;
  color: #999;
  line-height: 1.6;
  padding-top: 8rpx;
}
</style>

