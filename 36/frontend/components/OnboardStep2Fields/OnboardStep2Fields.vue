<template>
  <view class="step2-fields">
    <text class="section-title">核心团队成员</text>

    <view class="add-member-btn" @click="openAdd">
      <text class="add-member-text">+ 新增核心成员</text>
    </view>

    <view v-if="members.length" class="member-list">
      <view
        v-for="(m, idx) in members"
        :key="idx"
        class="member-card"
        @click="openEdit(idx)"
      >
        <view class="member-head">
          <view class="member-avatar">
            <image v-if="m.avatar" :src="m.avatar" class="avatar-img" mode="aspectFill" />
            <text v-else class="avatar-fallback">{{ m.name?.slice(0, 1) }}</text>
          </view>
          <view class="member-info">
            <text class="member-name">{{ m.name }}</text>
            <text class="member-title">{{ m.title }}</text>
          </view>
          <view class="member-actions" @click.stop>
            <text class="action-edit" @click="openEdit(idx)">编辑</text>
            <text class="action-delete" @click="removeMember(idx)">删除</text>
          </view>
        </view>
        <text v-if="m.bio" class="member-bio">{{ m.bio }}</text>
      </view>
    </view>

    <OnboardTeamMemberSheet
      :show="sheetShow"
      :member="editingMember"
      @close="sheetShow = false"
      @confirm="onMemberConfirm"
    />
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import OnboardTeamMemberSheet from '@/components/OnboardTeamMemberSheet/OnboardTeamMemberSheet.vue'

const members = defineModel({ type: Array, default: () => [] })

const sheetShow = ref(false)
const editingIndex = ref(-1)

const editingMember = computed(() => {
  if (editingIndex.value < 0) return null
  return members.value[editingIndex.value] || null
})

function openAdd() {
  editingIndex.value = -1
  sheetShow.value = true
}

function openEdit(idx) {
  editingIndex.value = idx
  sheetShow.value = true
}

function removeMember(idx) {
  uni.showModal({
    title: '提示',
    content: '确定删除该核心成员吗？',
    success(res) {
      if (res.confirm) {
        members.value.splice(idx, 1)
      }
    }
  })
}

function onMemberConfirm(member) {
  if (editingIndex.value >= 0) {
    members.value.splice(editingIndex.value, 1, member)
  } else {
    members.value.push(member)
  }
  sheetShow.value = false
}
</script>

<style scoped lang="scss">
@import '@/styles/apply-form.scss';

.add-member-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 88rpx;
  margin-bottom: 32rpx;
  background: rgba(41, 121, 255, 0.08);
  border-radius: 48rpx;
}

.add-member-text {
  font-size: 30rpx;
  font-weight: 500;
  color: #78B9B1;
}

.member-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.member-card {
  padding: 24rpx;
  background: #f8f9fb;
  border-radius: 12rpx;
}

.member-head {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.member-avatar {
  width: 80rpx;
  height: 80rpx;
  flex-shrink: 0;
  border-radius: 50%;
  background: #e8eef8;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
}

.avatar-fallback {
  font-size: 32rpx;
  font-weight: 600;
  color: #78B9B1;
}

.member-info {
  flex: 1;
  min-width: 0;
}

.member-name {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: #222;
  margin-bottom: 4rpx;
}

.member-title {
  display: block;
  font-size: 24rpx;
  color: #999;
}

.member-actions {
  display: flex;
  gap: 20rpx;
  flex-shrink: 0;
}

.action-edit,
.action-delete {
  font-size: 26rpx;
}

.action-edit {
  color: #78B9B1;
}

.action-delete {
  color: #e53935;
}

.member-bio {
  margin-top: 16rpx;
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
</style>
