<template>
  <view class="field">
    <view class="label-row">
      <text v-if="required" class="required">*</text>
      <text class="label">{{ label }}</text>
    </view>
    <picker
      mode="multiSelector"
      :range="pickerColumns"
      :value="pickerIndexes"
      @change="onPickerChange"
      @columnchange="onColumnChange"
    >
      <view class="picker-input">
        <text class="picker-value" :class="{ empty: !modelValue }">
          {{ displayValue || placeholder }}
        </text>
        <u-icon name="arrow-right" color="#ccc" size="28rpx" />
      </view>
    </picker>
  </view>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import {
  buildDateTimeColumns,
  buildDayOptions,
  formatDateTimeDisplay,
  formatDateTimeValue,
  indexesToParts,
  parseDateTimeParts,
  partsToIndexes
} from '@/utils/applyFormDateTime.js'

const props = defineProps({
  label: { type: String, required: true },
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '请选择日期时间' },
  required: { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue'])

const nowYear = new Date().getFullYear()
const minYear = nowYear - 5
const maxYear = nowYear + 10

const currentParts = ref(parseDateTimeParts(props.modelValue))
const pickerColumns = ref(buildDateTimeColumns(currentParts.value, minYear, maxYear))
const pickerIndexes = ref(partsToIndexes(currentParts.value, pickerColumns.value[0]))

const displayValue = computed(() => formatDateTimeDisplay(props.modelValue))

watch(
  () => props.modelValue,
  (value) => {
    syncPickerState(parseDateTimeParts(value))
  }
)

function syncPickerState(parts) {
  currentParts.value = parts
  pickerColumns.value = buildDateTimeColumns(parts, minYear, maxYear)
  pickerIndexes.value = partsToIndexes(parts, pickerColumns.value[0])
}

function onColumnChange(e) {
  const { column, value: columnValue } = e.detail
  const nextIndexes = [...pickerIndexes.value]
  nextIndexes[column] = columnValue

  if (column === 0 || column === 1) {
    const year = Number(pickerColumns.value[0][nextIndexes[0]])
    const month = Number(pickerColumns.value[1][nextIndexes[1]])
    const dayOptions = buildDayOptions(year, month)
    pickerColumns.value = [
      pickerColumns.value[0],
      pickerColumns.value[1],
      dayOptions,
      pickerColumns.value[3],
      pickerColumns.value[4]
    ]
    if (nextIndexes[2] >= dayOptions.length) {
      nextIndexes[2] = dayOptions.length - 1
    }
  }

  pickerIndexes.value = nextIndexes
}

function onPickerChange(e) {
  const indexes = e.detail.value
  const parts = indexesToParts(indexes, pickerColumns.value)
  syncPickerState(parts)
  emit('update:modelValue', formatDateTimeValue(parts))
}
</script>

<style scoped lang="scss">
@import '@/styles/apply-form.scss';
</style>
