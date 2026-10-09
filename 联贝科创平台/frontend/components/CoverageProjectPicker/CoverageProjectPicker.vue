<template>
  <view class="picker-block">
    <ApplySectionBar title="您即将申请报道的项目" />

    <view class="coverage-section">
      <text class="hint-text">
        请先输入项目名称，查询项目是否有收录，引用已收录的项目可提高审核速度
      </text>

      <view class="search-wrap">
        <input
          v-model="keyword"
          class="search-input-outline"
          placeholder="请输入公司简称，如联贝科创传媒/信息科技"
          placeholder-class="placeholder"
          @input="onInput"
          @focus="showResults = true"
        />
        <view v-if="keyword" class="clear-btn" @click="clearKeyword">
          <u-icon name="close-circle-fill" color="#ccc" size="36rpx" />
        </view>
      </view>

      <ProjectSearchDropdown
        :keyword="keyword"
        :list="searchList"
        :show-panel="showResults"
        :loading="searching"
        :searched="searched"
        @select="selectProject"
      />

      <view class="quick-create" @click="$emit('create-project')">
        <text class="link">未找到我要寻求报道的项目，快速创建{{ DOUBLE_LINK_ARROW }}</text>
      </view>

      <view class="tips-box">
        <text class="tips-title">温馨提示:</text>
        <text class="tips-line">1. 请填写规范的项目名称，便于审核人员快速识别，提高曝光与关注度。</text>
        <text class="tips-line">2. 请勿在「项目名称」中填写项目描述、介绍或报道需求。</text>
      </view>

      <view v-if="selected" class="coverage-preview-card">
        <ProjectLogo :item="selected" />
        <view class="preview-body">
          <view class="preview-head">
            <text class="preview-name">{{ selected.name }}</text>
            <u-tag v-if="selected.round" :text="selected.round" size="mini" plain type="primary" />
          </view>
          <text v-if="selected.companyDesc" class="preview-desc">{{ selected.companyDesc }}</text>
          <text v-if="selected.metaLine" class="preview-meta">{{ selected.metaLine }}</text>
        </view>
      </view>
      <view v-else class="coverage-preview-card preview-skeleton">
        <view class="sk-logo" />
        <view class="sk-lines">
          <view class="sk-line long" />
          <view class="sk-line mid" />
          <view class="sk-line short" />
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, watch } from 'vue'
import ApplySectionBar from '@/components/ApplySectionBar/ApplySectionBar.vue'
import ProjectSearchDropdown from '@/components/ProjectSearchDropdown/ProjectSearchDropdown.vue'
import ProjectLogo from '@/components/ProjectLogo/ProjectLogo.vue'
import { fetchProjectSearch } from '@/api/search.js'
import { SUCCESS_CODE } from '@/utils/request.js'
import { DOUBLE_LINK_ARROW } from '@/utils/linkText.js'

const props = defineProps({
  modelValue: { type: Object, default: null }
})
const emit = defineEmits(['update:modelValue', 'create-project'])

const keyword = ref('')
const searchList = ref([])
const showResults = ref(false)
const searching = ref(false)
const searched = ref(false)
const selected = ref(props.modelValue)

let debounceTimer = null

watch(
  () => props.modelValue,
  (val) => {
    selected.value = val
    if (val?.name) {
      keyword.value = val.name
    }
  },
  { immediate: true }
)

function onInput() {
  showResults.value = true
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(doSearch, 280)
}

async function doSearch() {
  const kw = keyword.value.trim()
  if (!kw) {
    searchList.value = []
    searched.value = false
    return
  }
  searching.value = true
  searched.value = false
  const res = await fetchProjectSearch(kw, 1, 8)
  searching.value = false
  searched.value = true
  if (res.code === SUCCESS_CODE) {
    searchList.value = res.data?.list || []
  } else {
    searchList.value = []
  }
}

function selectProject(item) {
  selected.value = {
    id: item.id,
    name: item.name,
    round: item.round,
    companyDesc: item.companyDesc,
    logoUrl: item.logoUrl,
    entityName: item.entityName,
    metaLine: item.entityName ? `企业主体：${item.entityName}` : ''
  }
  keyword.value = item.name
  showResults.value = false
  emit('update:modelValue', selected.value)
}

function clearKeyword() {
  keyword.value = ''
  searchList.value = []
  searched.value = false
  selected.value = null
  emit('update:modelValue', null)
}

function setProject(project) {
  if (!project) return
  selected.value = project
  keyword.value = project.name || ''
  emit('update:modelValue', selected.value)
}

defineExpose({ setProject })
</script>

<style scoped lang="scss">
@import '@/styles/theme.scss';
@import '@/styles/apply-form.scss';
@import '@/styles/coverage-form.scss';
@import '@/styles/project-search-dropdown.scss';

.picker-block {
  margin-bottom: 0;
}

.search-wrap {
  position: relative;
}

.clear-btn {
  position: absolute;
  right: 16rpx;
  top: 50%;
  transform: translateY(-50%);
  padding: 8rpx;
}

.quick-create {
  margin-top: 16rpx;
}

.sk-logo {
  width: 88rpx;
  height: 88rpx;
  border-radius: 12rpx;
  background: $theme-logo-gradient;
  flex-shrink: 0;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;

  image {
    width: 100%;
    height: 100%;
  }

  text {
    color: #fff;
    font-size: 32rpx;
    font-weight: 700;
  }
}

.preview-body,
.sk-lines {
  flex: 1;
  margin-left: 20rpx;
  min-width: 0;
}

.preview-head {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.preview-name {
  font-size: 30rpx;
  font-weight: 700;
  color: #222;
}

.preview-desc {
  display: block;
  margin-top: 8rpx;
  font-size: 26rpx;
  color: #666;
}

.preview-meta {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #999;
}

.sk-line {
  height: 24rpx;
  background: #f0f0f0;
  border-radius: 4rpx;
  margin-bottom: 16rpx;

  &.long {
    width: 70%;
  }

  &.mid {
    width: 90%;
  }

  &.short {
    width: 50%;
    margin-bottom: 0;
  }
}
</style>
