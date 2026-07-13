<template>
  <LegalDocPage :doc="USER_AGREEMENT" />
</template>

<script setup>
import LegalDocPage from '@/components/LegalDocPage/LegalDocPage.vue'
import { USER_AGREEMENT } from '@/content/user-agreement.js'
</script>
