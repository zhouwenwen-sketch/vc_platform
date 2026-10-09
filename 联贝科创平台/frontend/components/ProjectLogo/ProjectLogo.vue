<template>
  <view
    class="project-logo"
    :class="[`project-logo--${size}`, { 'project-logo--round': round }]"
    :style="fallbackStyle"
  >
    <image v-if="resolvedUrl" :src="resolvedUrl" class="project-logo__img" mode="aspectFill" />
    <text v-else class="project-logo__letter">{{ displayLetter }}</text>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { resolveProjectLogoUrl, resolveProjectLogoSeed, resolveProjectLogoFallbackGradient } from '@/utils/projectLogo.js'

const props = defineProps({
  logoUrl: { type: String, default: '' },
  name: { type: String, default: '' },
  /** 兼容 home mock 等仍使用 logo 字段的数据 */
  item: { type: Object, default: null },
  size: {
    type: String,
    default: 'md',
    validator: (v) => ['xs', 'sm', 'md', 'lg', 'xl'].includes(v)
  },
  round: { type: Boolean, default: false }
})

const resolvedUrl = computed(() => {
  if (props.logoUrl) return props.logoUrl
  if (props.item) return resolveProjectLogoUrl(props.item)
  return ''
})

const displayLetter = computed(() => {
  const n = props.name || props.item?.name || props.item?.companyName || props.item?.title || props.item?.brandName || '项'
  return String(n).slice(0, 1)
})

const fallbackStyle = computed(() => {
  if (resolvedUrl.value) return {}
  const seed = resolveProjectLogoSeed(props.item, props.name)
  return { background: resolveProjectLogoFallbackGradient(seed) }
})
</script>

<style lang="scss" scoped>
.project-logo {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;

  &--xs {
    width: 40rpx;
    height: 40rpx;
    border-radius: 50%;

    .project-logo__letter {
      font-size: 22rpx;
    }
  }

  &--sm {
    width: 56rpx;
    height: 56rpx;
    border-radius: 10rpx;

    .project-logo__letter {
      font-size: 26rpx;
    }
  }

  &--md {
    width: 88rpx;
    height: 88rpx;
    border-radius: 12rpx;

    .project-logo__letter {
      font-size: 32rpx;
    }
  }

  &--lg {
    width: 96rpx;
    height: 96rpx;
    border-radius: 16rpx;

    .project-logo__letter {
      font-size: 36rpx;
    }
  }

  &--xl {
    width: 120rpx;
    height: 120rpx;
    border-radius: 12rpx;

    .project-logo__letter {
      font-size: 48rpx;
    }
  }

  &.project-logo--round {
    border-radius: 50%;
  }
}

.project-logo__img {
  width: 100%;
  height: 100%;
}

.project-logo__letter {
  color: #fff;
  font-weight: 700;
  text-shadow: 0 1rpx 4rpx rgba(80, 40, 30, 0.2);
}
</style>
