<template>
  <view class="onboard-name-field">
    <view class="label-row">
      <text class="required">*</text>
      <text class="label">项目名称</text>
    </view>
    <view class="input-wrap">
      <input
        v-model="keyword"
        class="input"
        placeholder="请输入公司简称，如：大学生创投传媒/信息科技"
        placeholder-class="placeholder"
        @input="onInput"
        @blur="onBlur"
      />
      <view v-if="keyword" class="clear-btn" @click="clearKeyword">
        <u-icon name="close-circle-fill" color="#ccc" size="36rpx" />
      </view>
    </view>
    <text v-if="duplicateHint" class="duplicate-hint">{{ duplicateHint }}</text>
  </view>
</template>

<script setup>
import { ref, watch } from 'vue'
import { checkProjectNameExists } from '@/api/project.js'
import { SUCCESS_CODE } from '@/utils/request.js'
import { showOnboardDuplicateModal } from '@/utils/projectOnboardGuard.js'

const props = defineProps({
  modelValue: { type: String, default: '' }
})
const emit = defineEmits(['update:modelValue', 'name-valid', 'name-duplicate', 'blur-empty', 'name-reset'])

const keyword = ref(props.modelValue || '')
const duplicateHint = ref('')
const nameValid = ref(false)
const checking = ref(false)

watch(
  () => props.modelValue,
  (val) => {
    if (val !== keyword.value) {
      keyword.value = val || ''
    }
  }
)

watch(keyword, (val) => {
  emit('update:modelValue', val)
})

function onInput() {
  duplicateHint.value = ''
  if (nameValid.value) {
    nameValid.value = false
    emit('name-reset')
  }
}

async function onBlur() {
  const name = keyword.value.trim()
  if (!name) {
    duplicateHint.value = ''
    nameValid.value = false
    emit('blur-empty')
    return
  }

  keyword.value = name
  emit('update:modelValue', name)

  if (checking.value) return
  checking.value = true
  const res = await checkProjectNameExists(name)
  checking.value = false

  if (res.code !== SUCCESS_CODE) {
    uni.showToast({ title: '名称校验失败，请稍后重试', icon: 'none' })
    nameValid.value = false
    emit('name-reset')
    return
  }

  if (res.data?.exists) {
    duplicateHint.value = '该项目名称已被大学生创投收录，请更换名称'
    nameValid.value = false
    emit('name-duplicate', name)
    showOnboardDuplicateModal()
    return
  }

  duplicateHint.value = ''
  nameValid.value = true
  emit('name-valid', name)
}

function clearKeyword() {
  keyword.value = ''
  duplicateHint.value = ''
  nameValid.value = false
  emit('update:modelValue', '')
  emit('blur-empty')
}

async function validateName() {
  const name = keyword.value.trim()
  if (!name) return false

  if (nameValid.value && name === props.modelValue.trim()) {
    return true
  }

  const res = await checkProjectNameExists(name)
  if (res.code !== SUCCESS_CODE) {
    uni.showToast({ title: '名称校验失败，请稍后重试', icon: 'none' })
    return false
  }

  if (res.data?.exists) {
    duplicateHint.value = '该项目名称已被大学生创投收录，请更换名称'
    nameValid.value = false
    emit('name-duplicate', name)
    showOnboardDuplicateModal()
    return false
  }

  duplicateHint.value = ''
  nameValid.value = true
  emit('name-valid', name)
  return true
}

defineExpose({ validateName, isNameValid: () => nameValid.value })
</script>

<style scoped lang="scss">
@import '@/styles/apply-form.scss';

.onboard-name-field {
  margin-bottom: 32rpx;
}

.input-wrap {
  position: relative;

  .input {
    width: 100%;
    display: block;
  }
}

.clear-btn {
  position: absolute;
  right: 16rpx;
  top: 50%;
  transform: translateY(-50%);
  padding: 8rpx;
}

.duplicate-hint {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #e53935;
  line-height: 1.5;
}
</style>
