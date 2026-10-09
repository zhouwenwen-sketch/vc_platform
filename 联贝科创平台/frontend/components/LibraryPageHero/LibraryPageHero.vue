<template>

  <view class="library-hero">

    <view class="hero-body">

      <text class="hero-title">{{ title }}</text>

    </view>

  </view>

</template>



<script setup>

defineProps({

  title: {

    type: String,

    required: true

  }

})

</script>



<style lang="scss">

@import '@/styles/library-hero.scss';

</style>

