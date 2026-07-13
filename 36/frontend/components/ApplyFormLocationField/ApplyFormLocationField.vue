<template>
  <view class="field">
    <view class="label-row">
      <text v-if="required" class="required">*</text>
      <text class="label">{{ label }}</text>
    </view>
    <view class="location-row">
      <view class="location-cell location-cell-country">
        <view class="picker-input" @click="countryPickerShow = true">
          <text class="picker-value">{{ country || '中国' }}</text>
          <u-icon name="arrow-right" color="#ccc" size="28rpx" />
        </view>
      </view>
      <view class="location-cell location-cell-detail">
        <view v-if="isChina" class="picker-input" @click="openRegionPicker">
          <text class="picker-value" :class="{ empty: !locationDisplay }">
            {{ locationDisplay || '选择省份和城市' }}
          </text>
          <u-icon name="arrow-right" color="#ccc" size="28rpx" />
        </view>
        <input
          v-else
          :value="overseasLocation"
          class="input overseas-input"
          placeholder="请输入海外总部所在地"
          placeholder-class="placeholder"
          @input="onOverseasInput"
        />
      </view>
    </view>

    <u-picker
      ref="regionPickerRef"
      :show="regionPickerShow"
      :columns="regionColumns"
      :default-index="regionDefaultIndex"
      :title="label"
      @change="onRegionPickerChange"
      @confirm="onRegionConfirm"
      @cancel="regionPickerShow = false"
      @close="regionPickerShow = false"
    />

    <u-picker
      :show="countryPickerShow"
      :columns="[COUNTRY_OPTIONS]"
      :title="label"
      @confirm="onCountryConfirm"
      @cancel="countryPickerShow = false"
      @close="countryPickerShow = false"
    />
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import {
  buildProvinceCityColumns,
  getCitiesByProvince,
  getProvinceCityIndex
} from '@/utils/chinaRegions.js'

const COUNTRY_OPTIONS = ['中国', '海外']

const props = defineProps({
  label: { type: String, default: '总部所在地' },
  country: { type: String, default: '中国' },
  province: { type: String, default: '' },
  city: { type: String, default: '' },
  overseasLocation: { type: String, default: '' },
  required: { type: Boolean, default: false }
})
const emit = defineEmits([
  'update:country',
  'update:province',
  'update:city',
  'update:overseasLocation'
])

const countryPickerShow = ref(false)
const regionPickerShow = ref(false)
const regionPickerRef = ref(null)
const regionColumns = ref(buildProvinceCityColumns())
const regionDefaultIndex = ref([0, 0])

const isChina = computed(() => props.country !== '海外')

const locationDisplay = computed(() => {
  if (!props.province) return ''
  if (!props.city || props.province === props.city) return props.province
  return `${props.province} ${props.city}`
})

function openRegionPicker() {
  regionColumns.value = buildProvinceCityColumns(props.province, props.city)
  regionDefaultIndex.value = getProvinceCityIndex(props.province, props.city)
  regionPickerShow.value = true
}

function onRegionPickerChange(e) {
  if (e.columnIndex !== 0) return
  const province = e.value?.[0]
  if (!province) return
  regionPickerRef.value?.setColumnValues(1, getCitiesByProvince(province))
}

function onRegionConfirm(e) {
  const [province = '', city = ''] = e.value || []
  if (province) emit('update:province', province)
  if (city) emit('update:city', city)
  regionPickerShow.value = false
}

function onOverseasInput(e) {
  emit('update:overseasLocation', e.detail.value || '')
}

function onCountryConfirm(e) {
  const val = e.value?.[0]
  if (val && val !== props.country) {
    emit('update:country', val)
    emit('update:province', '')
    emit('update:city', '')
    emit('update:overseasLocation', '')
  }
  countryPickerShow.value = false
}
</script>

<style scoped lang="scss">
@import '@/styles/apply-form.scss';

.location-row {
  display: flex;
  gap: 16rpx;
}

.location-cell {
  min-width: 0;
}

.location-cell-country {
  flex: 0 0 176rpx;
}

.location-cell-detail {
  flex: 1;
}

.overseas-input {
  width: 100%;
  display: block;
}
</style>
