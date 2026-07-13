<template>
  <div class="dashboard">
    <el-row :gutter="16">
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-title">当前账号</div>
          <div class="stat-value">{{ userStore.profile?.username }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-title">角色</div>
          <div class="stat-value">{{ userStore.profile?.roleName }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-title">可管理资源</div>
          <div class="stat-value">{{ resourceCount }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="panel" shadow="never">
      <template #header>快捷入口</template>
      <div class="quick-links">
        <el-button
          v-for="item in quickLinks"
          :key="item.path"
          @click="$router.push(item.path)"
        >
          {{ item.name }}
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { fetchResources } from '@/api/crud'

const userStore = useUserStore()
const resourceCount = ref(0)

const quickLinks = computed(() => {
  const links = []
  for (const menu of userStore.menus || []) {
    if (menu.path) {
      links.push({ name: menu.name, path: menu.path })
    }
    for (const child of menu.children || []) {
      if (child.path) {
        links.push({ name: child.name, path: child.path })
      }
    }
  }
  return links.slice(0, 8)
})

onMounted(async () => {
  const resources = await fetchResources()
  resourceCount.value = resources.length
})
</script>

<style scoped lang="scss">
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stat-title {
  color: #909399;
  font-size: 14px;
}

.stat-value {
  margin-top: 8px;
  font-size: 24px;
  font-weight: 600;
}

.quick-links {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
</style>
