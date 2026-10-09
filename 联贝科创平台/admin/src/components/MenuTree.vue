<template>
  <template v-for="menu in menus" :key="menu.code || menu.id">
    <el-sub-menu v-if="menu.children?.length" :index="menu.code || String(menu.id)">
      <template #title>
        <el-icon v-if="depth === 0"><Menu /></el-icon>
        <span>{{ menu.name }}</span>
      </template>
      <MenuTree :menus="menu.children" :depth="depth + 1" />
    </el-sub-menu>
    <el-menu-item v-else-if="menu.path" :index="menu.path">
      <el-icon v-if="depth === 0"><Document /></el-icon>
      <span>{{ menu.name }}</span>
    </el-menu-item>
  </template>
</template>

<script setup>
defineProps({
  menus: { type: Array, default: () => [] },
  depth: { type: Number, default: 0 }
})
</script>
