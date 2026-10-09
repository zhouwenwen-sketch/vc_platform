<template>
  <view class="detail-page" :class="{ 'is-article': isArticle }">
    <scroll-view
      v-if="detail"
      scroll-y
      class="content-scroll"
      :show-scrollbar="false"
      :scroll-into-view="scrollIntoView"
      scroll-with-animation
    >
      <NewsFlashDetail
        v-if="detail.type === '快讯'"
        :detail="detail"
        @project="goProject"
      />
      <NewsArticleDetail
        v-else
        ref="articleRef"
        :detail="detail"
        :comments="comments"
        @project="goProject"
        @like-change="onLikeChange"
      />

      <view v-if="detail.type === '快讯' && detail.nextNews" class="next-section">
        <view class="next-head">
          <text class="next-label">下一篇</text>
          <view class="next-line" />
        </view>
        <view class="next-card" @click="goNext">
          <text class="next-title">{{ nextNewsTitle }}</text>
        </view>
      </view>
    </scroll-view>

    <view v-if="loading && !detail" class="loading-wrap">
      <u-loading-icon mode="circle" color="#78B9B1" size="40" />
    </view>
  </view>

  <!-- 底部栏与评论弹层置于 detail-page 外，避免 u-popup 的 flex:1 挤压正文区 -->
  <ArticleBottomBar
    v-if="isArticle && detail"
    :liked="articleLiked"
    :comment-count="comments.length"
    @like="onBottomLike"
    @comment="onCommentTap"
  />

  <ArticleCommentSheet
    :show="showCommentSheet"
    :submitting="commentSubmitting"
    @close="showCommentSheet = false"
    @submit="onSubmitComment"
  />
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import NewsFlashDetail from '@/components/news/NewsFlashDetail.vue'
import NewsArticleDetail from '@/components/news/NewsArticleDetail.vue'
import ArticleBottomBar from '@/components/news/ArticleBottomBar.vue'
import ArticleCommentSheet from '@/components/news/ArticleCommentSheet.vue'
import { fetchNewsDetail, fetchNewsComments, submitNewsComment } from '@/api/news.js'
import { SUCCESS_CODE } from '@/utils/request.js'
import { requireLoginAtEntry } from '@/utils/authGuard.js'
import { navigateToProjectDetail, PROJECT_ENTRY } from '@/utils/projectNavigate.js'
import { newsListTitle } from '@/utils/transform.js'

const detail = ref(null)
const loading = ref(true)
const newsId = ref(null)
const articleRef = ref(null)
const articleLiked = ref(false)
const comments = ref([])
const showCommentSheet = ref(false)
const commentSubmitting = ref(false)
const scrollIntoView = ref('')

const isArticle = computed(() => detail.value?.type === '文章')

const nextNewsTitle = computed(() => newsListTitle(detail.value?.nextNews?.title || ''))

const navTitle = computed(() => {
  const title = detail.value?.title || ''
  return title.length > 14 ? `${title.slice(0, 14)}...` : title
})

watch(navTitle, (title) => {
  if (title) {
    uni.setNavigationBarTitle({ title })
  }
}, { immediate: true })

const commentPageUrl = computed(() =>
  newsId.value ? `/pages/news/detail?id=${newsId.value}` : '/pages/news/detail'
)

onLoad((query) => {
  newsId.value = query?.id
  loadDetail(query?.id)
})

async function loadDetail(id) {
  if (!id) {
    loading.value = false
    return
  }
  loading.value = true
  detail.value = null
  articleLiked.value = false
  comments.value = []
  try {
    const detailRes = await fetchNewsDetail(id)
    await loadComments(id)
    if (detailRes.code === SUCCESS_CODE) {
      detail.value = detailRes.data
    }
  } catch (err) {
    console.error('[news-detail] load failed', err)
  } finally {
    loading.value = false
  }
}

async function loadComments(id = newsId.value) {
  if (!id) return
  const res = await fetchNewsComments(id)
  if (res.code === SUCCESS_CODE) {
    comments.value = res.data || []
  }
}

function goProject(item) {
  if (!item?.projectId) return
  navigateToProjectDetail(item.projectId, { from: PROJECT_ENTRY.NEWS })
}

function goNext() {
  const nextId = detail.value?.nextNews?.id
  if (!nextId) return
  uni.redirectTo({ url: `/pages/news/detail?id=${nextId}` })
}

function onLikeChange({ liked }) {
  articleLiked.value = liked
}

function onBottomLike() {
  articleRef.value?.toggleLike()
}

function onCommentTap() {
  if (!requireLoginAtEntry(commentPageUrl.value)) return
  showCommentSheet.value = true
}

async function onSubmitComment(content) {
  if (!newsId.value || commentSubmitting.value) return
  commentSubmitting.value = true
  try {
    const res = await submitNewsComment(newsId.value, content)
    if (res.code === SUCCESS_CODE) {
      comments.value = [res.data, ...comments.value]
      showCommentSheet.value = false
      uni.showToast({ title: '评论成功', icon: 'success' })
      scrollIntoView.value = ''
      setTimeout(() => {
        scrollIntoView.value = 'comment-section'
      }, 200)
    }
  } finally {
    commentSubmitting.value = false
  }
}
</script>

<style scoped lang="scss">
.detail-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #fff;

  &.is-article .content-scroll {
    padding-bottom: calc(120rpx + env(safe-area-inset-bottom));
  }
}

.content-scroll {
  flex: 1;
  height: 0;
}

.next-section {
  padding: 0 32rpx 60rpx;
  border-top: 16rpx solid #f5f6f8;
}

.next-head {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 32rpx 0 24rpx;
}

.next-label {
  font-size: 30rpx;
  font-weight: 600;
  color: #222;
}

.next-line {
  flex: 1;
  height: 4rpx;
  max-width: 48rpx;
  background: #78B9B1;
  border-radius: 2rpx;
}

.next-card {
  padding-bottom: 32rpx;
}

.next-title {
  font-size: 34rpx;
  font-weight: 400;
  color: #222;
  line-height: 1.5;
  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.loading-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
