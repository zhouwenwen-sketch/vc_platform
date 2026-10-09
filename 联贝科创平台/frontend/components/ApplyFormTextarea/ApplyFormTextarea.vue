<template>
  <view class="field">
    <view class="label-row">
      <text v-if="required" class="required">*</text>
      <text class="label">{{ label }}</text>
    </view>
    <view class="textarea-wrap">
      <textarea
        :value="modelValue"
        class="textarea"
        :maxlength="maxlength"
        :placeholder="placeholder"
        placeholder-class="placeholder"
        @input="onInput"
      />
      <text class="char-count">{{ (modelValue || '').length }}/{{ maxlength }}</text>
    </view>
  </view>
</template>

<script setup>
defineProps({
  label: { type: String, required: true },
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '' },
  maxlength: { type: Number, default: 200 },
  required: { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue'])

function onInput(e) {
  emit('update:modelValue', e.detail.value)
}
</script>

<style scoped lang="scss">
@import '@/styles/apply-form.scss';
</style>
