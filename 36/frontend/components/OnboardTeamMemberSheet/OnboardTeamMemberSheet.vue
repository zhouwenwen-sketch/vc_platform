<template>
  <u-popup :show="show" mode="bottom" round="16" @close="$emit('close')">
    <view class="member-sheet">
      <view class="member-sheet-head">
        <text class="member-sheet-title">核心团队成员</text>
        <view class="member-sheet-close" @click="$emit('close')">
          <u-icon name="close" color="#999" size="36rpx" />
        </view>
      </view>

      <scroll-view scroll-y class="member-sheet-body" :show-scrollbar="false">
        <ApplyFormInput
          v-model="draft.name"
          required
          label="姓名"
          placeholder="请输入成员姓名"
        />

        <ApplyFormInput
          v-model="draft.title"
          required
          label="职务"
          placeholder="请输入成员职务"
        />

        <ApplyFormTextarea
          v-model="draft.bio"
          required
          label="个人经历"
          placeholder="请描述学历、从业经历等相关内容，尽量简要。100字以内"
          :maxlength="100"
        />

        <ApplyFormUploadImage
          v-model="draft.avatar"
          label="头像"
          tip="仅支持jpg/png文件，文件大小不超过5MB。推荐为正方形"
        />
      </scroll-view>

      <view class="member-sheet-footer">
        <u-button
          type="primary"
          text="确定"
          custom-style="width:100%;background:#78B9B1;border-color:#78B9B1;height:88rpx;border-radius:48rpx"
          @click="onConfirm"
        />
      </view>
    </view>
  </u-popup>
</template>

<script setup>
import { reactive, watch } from 'vue'
import ApplyFormInput from '@/components/ApplyFormInput/ApplyFormInput.vue'
import ApplyFormTextarea from '@/components/ApplyFormTextarea/ApplyFormTextarea.vue'
import ApplyFormUploadImage from '@/components/ApplyFormUploadImage/ApplyFormUploadImage.vue'

const props = defineProps({
  show: { type: Boolean, default: false },
  member: { type: Object, default: null }
})

const emit = defineEmits(['close', 'confirm'])

function createEmptyMember() {
  return {
    name: '',
    title: '',
    bio: '',
    avatar: ''
  }
}

const draft = reactive(createEmptyMember())

watch(
  () => props.show,
  (visible) => {
    if (!visible) return
    const src = props.member || createEmptyMember()
    Object.assign(draft, createEmptyMember(), src)
  }
)

function validate() {
  if (!draft.name.trim()) {
    uni.showToast({ title: '请填写成员姓名', icon: 'none' })
    return false
  }
  if (!draft.title.trim()) {
    uni.showToast({ title: '请填写成员职务', icon: 'none' })
    return false
  }
  if (!draft.bio.trim()) {
    uni.showToast({ title: '请填写个人经历', icon: 'none' })
    return false
  }
  return true
}

function onConfirm() {
  if (!validate()) return
  emit('confirm', {
    name: draft.name.trim(),
    title: draft.title.trim(),
    bio: draft.bio.trim(),
    avatar: draft.avatar
  })
}
</script>

<style scoped lang="scss">
.member-sheet {
  background: #fff;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
}

.member-sheet-head {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  padding: 28rpx 32rpx;
  border-bottom: 1rpx solid #f0f0f0;
  flex-shrink: 0;
}

.member-sheet-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #222;
}

.member-sheet-close {
  position: absolute;
  right: 32rpx;
  top: 50%;
  transform: translateY(-50%);
  padding: 8rpx;
}

.member-sheet-body {
  flex: 1;
  min-height: 0;
  max-height: calc(85vh - 200rpx);
  padding: 24rpx 32rpx;
  box-sizing: border-box;
}

.member-sheet-footer {
  flex-shrink: 0;
  padding: 16rpx 32rpx calc(16rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid #f0f0f0;
}
</style>
