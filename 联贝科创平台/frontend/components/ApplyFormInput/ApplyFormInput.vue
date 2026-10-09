<template>
  <view class="field">
    <view class="label-row">
      <text v-if="required" class="required">*</text>
      <text class="label">{{ label }}</text>
    </view>
    <input
      :value="modelValue"
      class="input"
      :type="type"
      :maxlength="maxlength"
      :placeholder="placeholder"
      placeholder-class="placeholder"
      @input="onInput"
    />
  </view>
</template>

<script setup>
defineProps({
  label: { type: String, required: true },
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '' },
  maxlength: { type: [Number, String], default: 140 },
  required: { type: Boolean, default: false },
  type: { type: String, default: 'text' }
})
const emit = defineEmits(['update:modelValue'])

function onInput(e) {
  emit('update:modelValue', e.detail.value)
}
</script>

<style scoped lang="scss">
@import '@/styles/apply-form.scss';
</style>

