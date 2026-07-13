<template>
  <view class="article-detail">
    <text class="article-title">{{ detail.title }}</text>

    <!-- 来源行 -->
    <view class="source-row">
      <view class="source-left">
        <view class="avatar-wrap">
          <image v-if="detail.sourceAvatar" :src="detail.sourceAvatar" class="avatar-img" mode="aspectFill" />
          <text v-else class="avatar-letter">{{ sourceLetter }}</text>
        </view>
        <view class="source-meta">
          <text class="source-name">{{ detail.author || '联贝科创平台' }}</text>
          <text class="source-time">{{ detail.publishTime }}</text>
        </view>
      </view>
      <view class="follow-btn" @click="onFollow">
        <text>+ 关注</text>
      </view>
    </view>

    <view class="divider" />

    <!-- 摘要框 -->
    <view v-if="detail.summary" class="summary-box">
      <u-icon name="chat" color="#666" size="32rpx" />
      <text class="summary-text">{{ detail.summary }}</text>
    </view>

    <!-- 封面图 -->
    <image
      v-if="detail.coverUrl"
      :src="detail.coverUrl"
      class="cover-img"
      mode="widthFix"
    />

    <!-- 正文 -->
    <rich-text v-if="detail.contentFormat === 'html'" class="article-body" :nodes="detail.content" />
    <text v-else class="article-body">{{ detail.content }}</text>

    <!-- 关联项目 -->
    <view
      v-if="detail.projectName"
      class="project-mini"
      @click="onProjectTap"
    >
      <ProjectLogo :logo-url="detail.logoUrl" :name="detail.projectName" size="sm" />
      <text class="p-name">{{ detail.projectName }}</text>
      <text v-if="detail.round" class="p-tag">{{ detail.round }}</text>
      <text v-if="detail.region" class="p-tag">{{ detail.region }}</text>
    </view>

    <!-- 版权声明 -->
    <view v-if="detail.attribution" class="attribution">
      <text v-for="(line, idx) in attributionLines" :key="idx" class="attr-line">{{ line }}</text>
    </view>

    <!-- 居中点赞 -->
    <view class="like-block" @click="toggleLike">
      <view class="like-circle" :class="{ active: liked }">
        <u-icon name="thumb-up" :color="liked ? '#78B9B1' : '#bbb'" size="48rpx" />
        <text class="like-num">{{ displayLikeCount }}</text>
      </view>
      <text class="like-tip">好文章，需要你的鼓励</text>
    </view>

    <!-- 评论区 -->
    <view id="comment-section" class="comment-section">
      <text class="comment-heading">评论区</text>
      <view v-if="comments.length" class="comment-list">
        <view v-for="item in comments" :key="item.id" class="comment-item">
          <view class="comment-avatar">
            <image v-if="item.avatar" :src="item.avatar" class="avatar-img" mode="aspectFill" />
            <text v-else class="avatar-letter">{{ item.nickname.slice(0, 1) }}</text>
          </view>
          <view class="comment-body">
            <view class="comment-meta">
              <text class="comment-name">{{ item.nickname }}</text>
              <text class="comment-time">{{ item.createTime }}</text>
            </view>
            <text class="comment-content">{{ item.content }}</text>
          </view>
        </view>
      </view>
      <view v-else class="comment-empty">
        <view class="empty-icon-wrap">
          <u-icon name="chat" color="#c5d4e8" size="80rpx" />
        </view>
        <text class="empty-text">暂无评论</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import ProjectLogo from '@/components/ProjectLogo/ProjectLogo.vue'

const props = defineProps({
  detail: { type: Object, required: true },
  comments: { type: Array, default: () => [] }
})
const emit = defineEmits(['project', 'like-change'])

const liked = ref(false)
const localLikeDelta = ref(0)

const sourceLetter = computed(() => {
  const name = props.detail.author || '联贝科创平台'
  return name.slice(0, 1)
})

const attributionLines = computed(() => {
  if (!props.detail.attribution) return []
  return props.detail.attribution.split('\n').filter(Boolean)
})

const displayLikeCount = computed(() => {
  const base = props.detail.likeCount || 0
  return base + localLikeDelta.value
})

watch(
  () => props.detail.id,
  () => {
    liked.value = false
    localLikeDelta.value = 0
  }
)

function onFollow() {
  uni.showToast({ title: '关注功能敬请期待', icon: 'none' })
}

function onProjectTap() {
  if (props.detail.projectId) {
    emit('project', props.detail)
  }
}

function toggleLike() {
  if (liked.value) {
    liked.value = false
    localLikeDelta.value = 0
  } else {
    liked.value = true
    localLikeDelta.value = 1
  }
  emit('like-change', { liked: liked.value, count: displayLikeCount.value })
}

defineExpose({ toggleLike, liked, displayLikeCount })
</script>

<style scoped lang="scss">
.article-detail {
  padding: 32rpx 32rpx 24rpx;
}

.article-title {
  display: block;
  font-size: 44rpx;
  font-weight: 400;
  color: #222;
  line-height: 1.45;
}

.source-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 28rpx;
}

.source-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex: 1;
  min-width: 0;
}

.avatar-wrap {
  flex: none;
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  overflow: hidden;
  background: linear-gradient(135deg, #6a5acd, #9370db);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-img {
  width: 100%;
  height: 100%;
}

.avatar-letter {
  font-size: 28rpx;
  font-weight: 600;
  color: #fff;
}

.source-meta {
  flex: 1;
  min-width: 0;
}

.source-name {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #222;
}

.source-time {
  display: block;
  margin-top: 6rpx;
  font-size: 24rpx;
  color: #999;
}

.follow-btn {
  flex: none;
  padding: 10rpx 24rpx;
  border: 1rpx solid #222;
  border-radius: 8rpx;

  text {
    font-size: 24rpx;
    color: #222;
  }
}

.divider {
  margin: 28rpx 0;
  border-bottom: 2rpx dashed #e8e8e8;
}

.summary-box {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
  padding: 24rpx 28rpx;
  margin-bottom: 32rpx;
  background: #f3f0fa;
  border-radius: 12rpx;
}

.summary-text {
  flex: 1;
  font-size: 28rpx;
  color: #444;
  line-height: 1.6;
}

.cover-img {
  width: 100%;
  border-radius: 8rpx;
  margin-bottom: 32rpx;
}

.article-body {
  display: block;
  font-size: 32rpx;
  font-weight: 400;
  color: #333;
  line-height: 1.85;
  white-space: pre-wrap;
}

.project-mini {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 40rpx;
  padding: 20rpx 24rpx;
  background: #f5f8fc;
  border-radius: 12rpx;
}

.p-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
}

.p-tag {
  padding: 4rpx 12rpx;
  font-size: 22rpx;
  line-height: 1.4;
  color: #78B9B1;
  background: rgba(41, 121, 255, 0.1);
  border-radius: 6rpx;
}

.attribution {
  margin-top: 48rpx;
  padding-top: 32rpx;
  border-top: 1rpx solid #f0f0f0;
}

.attr-line {
  display: block;
  font-size: 24rpx;
  color: #999;
  line-height: 1.7;

  & + & {
    margin-top: 8rpx;
  }
}

.like-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48rpx 0 40rpx;
}

.like-circle {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  border: 2rpx solid #e8e8e8;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4rpx;

  &.active {
    border-color: rgba(41, 121, 255, 0.4);
    background: rgba(41, 121, 255, 0.06);
  }
}

.like-num {
  font-size: 24rpx;
  color: #999;
}

.like-tip {
  margin-top: 16rpx;
  font-size: 24rpx;
  color: #bbb;
}

.comment-section {
  padding: 32rpx 0 16rpx;
  border-top: 16rpx solid #f5f6f8;
  margin: 0 -32rpx;
  padding-left: 32rpx;
  padding-right: 32rpx;
}

.comment-heading {
  display: block;
  font-size: 34rpx;
  font-weight: 700;
  color: #222;
  margin-bottom: 32rpx;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

.comment-item {
  display: flex;
  align-items: flex-start;
  gap: 20rpx;
}

.comment-avatar {
  flex: none;
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  overflow: hidden;
  background: linear-gradient(135deg, #7eb8f7, #b8d9f8);
  display: flex;
  align-items: center;
  justify-content: center;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 10rpx;
}

.comment-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #222;
}

.comment-time {
  font-size: 24rpx;
  color: #999;
}

.comment-content {
  display: block;
  font-size: 30rpx;
  color: #333;
  line-height: 1.6;
  word-break: break-all;
}

.comment-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40rpx 0 48rpx;
}

.empty-icon-wrap {
  opacity: 0.6;
}

.empty-text {
  margin-top: 16rpx;
  font-size: 28rpx;
  color: #bbb;
}

</style>
