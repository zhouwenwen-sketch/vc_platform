<template>
  <view class="report-detail">
    <text class="report-title">{{ detail.title }}</text>

    <view class="source-row">
      <view class="source-left">
        <view class="avatar-wrap">
          <image
            v-if="detail.publisher?.avatarUrl"
            :src="detail.publisher.avatarUrl"
            class="avatar-img"
            mode="aspectFill"
          />
          <text v-else class="avatar-letter">36</text>
        </view>
        <view class="source-meta">
          <text class="source-name">{{ detail.publisher?.name || '联贝科创研究院' }}</text>
          <text class="source-time">{{ detail.publishTime }}</text>
        </view>
      </view>
      <view class="follow-btn" @click="onFollow">
        <text>+ 关注</text>
      </view>
    </view>

    <view class="divider" />

    <view v-if="detail.tagList?.length" class="tag-row">
      <text
        v-for="tag in detail.tagList"
        :key="tag"
        class="report-tag"
        :class="tagClass(tag)"
      >{{ tag }}</text>
    </view>

    <text class="report-body">{{ detail.content }}</text>

    <view class="comment-section">
      <text class="comment-heading">评论区</text>
      <view class="comment-empty">
        <view class="empty-icon-wrap">
          <u-icon name="chat" color="#c5d4e8" size="80rpx" />
        </view>
        <text class="empty-text">暂无评论</text>
      </view>
    </view>
  </view>
</template>

<script setup>
const TAG_CLASS_MAP = {
  热门赛道: 'tag-hot',
  短研洞察: 'tag-hot',
  短篇洞察: 'tag-hot',
  产业洞察: 'tag-insight',
  前沿技术: 'tag-tech',
  其他: 'tag-tech'
}

defineProps({
  detail: { type: Object, required: true }
})

function tagClass(tag) {
  return TAG_CLASS_MAP[tag] || 'tag-default'
}

function onFollow() {
  uni.showToast({ title: '关注功能敬请期待', icon: 'none' })
}
</script>

<style scoped lang="scss">
@import '@/styles/theme.scss';

.report-detail {
  padding: 32rpx 32rpx 24rpx;
}

.report-title {
  display: block;
  font-size: 44rpx;
  font-weight: 700;
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
  background: $theme-logo-gradient;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-img {
  width: 100%;
  height: 100%;
}

.avatar-letter {
  font-size: 24rpx;
  font-weight: 700;
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
  background: #222;
  border-radius: 8rpx;

  text {
    font-size: 24rpx;
    color: #fff;
  }
}

.divider {
  margin: 28rpx 0;
  border-bottom: 1rpx dashed #e8e8e8;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 24rpx;
}

.report-tag {
  font-size: 22rpx;
  line-height: 1;
  padding: 8rpx 16rpx;
  border-radius: 6rpx;
}

.tag-hot {
  color: #fa8c16;
  background: #fff7e6;
}

.tag-insight {
  color: #78B9B1;
  background: #e6f4ff;
}

.tag-tech {
  color: #52c41a;
  background: #f6ffed;
}

.tag-default {
  color: #666;
  background: #f5f5f5;
}

.report-body {
  display: block;
  font-size: 32rpx;
  color: #333;
  line-height: 1.85;
  white-space: pre-wrap;
}

.comment-section {
  margin-top: 48rpx;
  padding-top: 32rpx;
  border-top: 1rpx solid #f0f0f0;
}

.comment-heading {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #222;
  margin-bottom: 32rpx;
}

.comment-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40rpx 0 20rpx;
}

.empty-icon-wrap {
  margin-bottom: 16rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #bbb;
}
</style>
