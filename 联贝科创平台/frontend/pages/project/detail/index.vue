<template>
  <view />
</template>

<script setup>
import { onLoad } from '@dcloudio/uni-app'

/** 旧路径兼容：跳转至项目集列表 */
onLoad((query) => {
  const id = query?.id
  if (id) {
    uni.redirectTo({ url: `/pages/project/collection/detail?id=${id}` })
  } else {
    uni.redirectTo({ url: '/pages/project/collection/index' })
  }
})
</script>
