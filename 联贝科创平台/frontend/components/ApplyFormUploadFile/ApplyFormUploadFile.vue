<template>
  <view class="field">
    <view class="label-row">
      <text v-if="required" class="required">*</text>
      <text class="label">{{ label }}</text>
    </view>
    <view class="file-upload" @click="chooseFile">
      <text class="file-btn-text">{{ fileName || buttonText }}</text>
    </view>
    <text class="file-tip">{{ tip }}</text>
    <text v-if="disclaimer" class="file-disclaimer">{{ disclaimer }}</text>
  </view>
</template>

<script setup>
const props = defineProps({
  label: { type: String, default: '项目BP（非必传）' },
  modelValue: { type: String, default: '' },
  fileName: { type: String, default: '' },
  required: { type: Boolean, default: false },
  buttonText: { type: String, default: '上传BP' },
  tip: { type: String, default: '请上传PDF文件，文件大小不超过20MB。' },
  disclaimer: {
    type: String,
    default: '*投资人申请对接并获得同意后，BP可被查阅/下载'
  },
  maxSize: { type: Number, default: 20 * 1024 * 1024 }
})
const emit = defineEmits(['update:modelValue', 'update:fileName'])

function chooseFile() {
  // #ifdef MP-WEIXIN
  uni.chooseMessageFile({
    count: 1,
    type: 'file',
    extension: ['pdf'],
    success(res) {
      handleFile(res.tempFiles?.[0])
    }
  })
  // #endif
  // #ifndef MP-WEIXIN
  if (typeof uni.chooseFile === 'function') {
    uni.chooseFile({
      count: 1,
      extension: ['.pdf'],
      success(res) {
        handleFile(res.tempFiles?.[0])
      }
    })
    return
  }
  uni.showToast({ title: '当前环境暂不支持文件上传', icon: 'none' })
  // #endif
}

function handleFile(file) {
  if (!file) return
  if (file.size && file.size > props.maxSize) {
    uni.showToast({ title: '文件大小不能超过20MB', icon: 'none' })
    return
  }
  const name = file.name || ''
  if (name && !name.toLowerCase().endsWith('.pdf')) {
    uni.showToast({ title: '请上传PDF文件', icon: 'none' })
    return
  }
  emit('update:modelValue', file.path)
  emit('update:fileName', name || '已选择文件')
}
</script>

<style scoped lang="scss">
@import '@/styles/apply-form.scss';

.file-upload {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 200rpx;
  height: 72rpx;
  padding: 0 32rpx;
  border: 2rpx solid #78B9B1;
  border-radius: 12rpx;
}

.file-btn-text {
  font-size: 28rpx;
  color: #78B9B1;
}

.file-tip {
  display: block;
  margin-top: 16rpx;
  font-size: 24rpx;
  color: #999;
  line-height: 1.5;
}

.file-disclaimer {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #78B9B1;
  line-height: 1.5;
}
</style>