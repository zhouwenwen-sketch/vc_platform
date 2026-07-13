<template>
  <view class="field">
    <view class="label-row">
      <text v-if="required" class="required">*</text>
      <text class="label">{{ label }}</text>
    </view>
    <picker
      v-if="yearMonthOnly"
      mode="date"
      fields="month"
      :value="pickerMonthValue"
      start="1900-01"
      :end="maxYearMonthEnd"
      @change="onYearMonthChange"
    >
      <view class="picker-input">
        <text class="picker-value" :class="{ empty: !modelValue }">
          {{ displayValue || placeholder }}
        </text>
        <u-icon name="arrow-right" color="#ccc" size="28rpx" />
      </view>
    </picker>
    <picker v-else mode="date" :value="modelValue" :end="maxDate" @change="onDateChange">
      <view class="picker-input">
        <text class="picker-value" :class="{ empty: !modelValue }">
          {{ modelValue || placeholder }}
        </text>
        <u-icon name="arrow-right" color="#ccc" size="28rpx" />
      </view>
    </picker>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  label: { type: String, required: true },
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '请选择日期' },
  required: { type: Boolean, default: false },
  maxDate: { type: String, default: '' },
  yearMonthOnly: { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue'])

const maxYearMonth = computed(() => {
  const end = props.maxDate || new Date().toISOString().slice(0, 10)
  const [y, m] = end.split('-').map(Number)
  return { year: y || new Date().getFullYear(), month: m || 12 }
})

const maxYearMonthEnd = computed(() => {
  const { year, month } = maxYearMonth.value
  return `${year}-${String(month).padStart(2, '0')}`
})

const pickerMonthValue = computed(() => {
  if (props.modelValue) return props.modelValue
  return maxYearMonthEnd.value
})

const displayValue = computed(() => {
  if (!props.modelValue) return ''
  const [y, m] = props.modelValue.split('-')
  if (!y || !m) return props.modelValue
  return `${y}年${Number(m)}月`
})

function onDateChange(e) {
  emit('update:modelValue', e.detail.value)
}

function onYearMonthChange(e) {
  emit('update:modelValue', e.detail.value)
}
</script>

<style scoped lang="scss">
@import '@/styles/apply-form.scss';
</style>
