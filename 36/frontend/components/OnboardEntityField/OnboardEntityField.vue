<template>
  <view class="onboard-entity-field">
    <view class="label-row">
      <text v-if="required" class="required">*</text>
      <text class="label">{{ label }}</text>
    </view>
    <view class="search-wrap">
      <input
        v-model="keyword"
        class="input"
        :placeholder="placeholder"
        placeholder-class="placeholder"
        @input="onInput"
        @focus="onFocus"
        @blur="onBlur"
      />
      <view v-if="keyword" class="clear-btn" @click="clearKeyword">
        <u-icon name="close-circle-fill" color="#ccc" size="36rpx" />
      </view>
    </view>

    <view v-if="showResults && keyword.trim() && list.length" class="entity-dropdown-panel">
      <scroll-view scroll-y class="entity-dropdown-scroll" :show-scrollbar="false">
        <view
          v-for="(name, idx) in list"
          :key="`${name}-${idx}`"
          class="entity-dropdown-item"
          @click="onSelect(name)"
        >
          <text>{{ name }}</text>
        </view>
      </scroll-view>
    </view>
    <view
      v-else-if="showResults && keyword.trim() && searched && !searching"
      class="entity-dropdown-empty"
    >
      <text>未找到匹配企业</text>
    </view>
  </view>
</template>

<script setup>
import { ref, watch } from 'vue'
import { fetchCompanySearch } from '@/api/company.js'
import { SUCCESS_CODE } from '@/utils/request.js'

const props = defineProps({
  modelValue: { type: String, default: '' },
  label: { type: String, default: '企业主体' },
  placeholder: { type: String, default: '请输入您的企业名称' },
  required: { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue'])

const keyword = ref(props.modelValue || '')
const list = ref([])
const showResults = ref(false)
const searching = ref(false)
const searched = ref(false)

let debounceTimer = null
let blurTimer = null

watch(
  () => props.modelValue,
  (val) => {
    if (val !== keyword.value) {
      keyword.value = val || ''
    }
  }
)

function onFocus() {
  showResults.value = true
  if (keyword.value.trim()) {
    doSearch()
  }
}

function onInput() {
  emit('update:modelValue', keyword.value)
  showResults.value = true
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(doSearch, 280)
}

function onBlur() {
  clearTimeout(blurTimer)
  blurTimer = setTimeout(() => {
    showResults.value = false
    const trimmed = keyword.value.trim()
    keyword.value = trimmed
    emit('update:modelValue', trimmed)
  }, 200)
}

async function doSearch() {
  const kw = keyword.value.trim()
  if (!kw) {
    list.value = []
    searched.value = false
    return
  }
  searching.value = true
  searched.value = false
  const res = await fetchCompanySearch(kw, 10)
  searching.value = false
  searched.value = true
  list.value = res.code === SUCCESS_CODE ? res.data || [] : []
}

function onSelect(name) {
  clearTimeout(blurTimer)
  showResults.value = false
  keyword.value = name
  emit('update:modelValue', name)
}

function clearKeyword() {
  keyword.value = ''
  list.value = []
  searched.value = false
  emit('update:modelValue', '')
}
</script>

<style scoped lang="scss">
@import '@/styles/apply-form.scss';

.onboard-entity-field {
  margin-bottom: 32rpx;
}

.search-wrap {
  position: relative;

  .input {
    width: 100%;
    display: block;
    padding-right: 64rpx;
  }
}

.clear-btn {
  position: absolute;
  right: 16rpx;
  top: 50%;
  transform: translateY(-50%);
  padding: 8rpx;
}

.entity-dropdown-panel {
  margin-top: 16rpx;
  background: #fff;
  border: 1rpx solid #eee;
  border-radius: 12rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.entity-dropdown-scroll {
  max-height: 288rpx;
}

.entity-dropdown-item {
  padding: 24rpx;
  font-size: 28rpx;
  color: #333;
  line-height: 1.5;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  &:active {
    background: #f8f9fb;
  }
}

.entity-dropdown-empty {
  margin-top: 16rpx;
  padding: 24rpx;
  font-size: 26rpx;
  color: #999;
  text-align: center;
  background: #fff;
  border: 1rpx solid #eee;
  border-radius: 12rpx;
}
</style>
