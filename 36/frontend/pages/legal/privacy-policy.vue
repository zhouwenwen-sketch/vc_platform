<template>
  <LegalDocPage :doc="PRIVACY_POLICY" />
</template>

<script setup>
import LegalDocPage from '@/components/LegalDocPage/LegalDocPage.vue'
import { PRIVACY_POLICY } from '@/content/privacy-policy.js'
</script>
