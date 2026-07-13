<template>
  <text :class="['highlight-text', customClass]">
    <text
      v-for="(seg, index) in segments"
      :key="index"
      :class="{ 'is-match': seg.match }"
    >{{ seg.text }}</text>
  </text>
</template>

<script setup>
import { computed } from 'vue'
import { splitByKeyword } from '@/utils/highlight.js'

const props = defineProps({
  text: { type: String, default: '' },
  keyword: { type: String, default: '' },
  customClass: { type: String, default: '' }
})

const segments = computed(() => splitByKeyword(props.text, props.keyword))
</script>

<style scoped lang="scss">
.highlight-text {
  color: inherit;
}

.is-match {
  color: #78B9B1;
}
</style>
