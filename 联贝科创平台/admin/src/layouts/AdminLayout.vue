<template>
  <el-container class="admin-layout">
    <el-aside width="240px" class="aside">
      <div class="brand">联贝科创后台</div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#001529"
        text-color="#bfcbd9"
        active-text-color="#fff"
      >
        <template v-for="menu in rootMenus" :key="menu.code || menu.id">
          <el-sub-menu v-if="menu.children?.length" :index="menu.code || String(menu.id)">
            <template #title>
              <el-icon><Menu /></el-icon>
              <span>{{ menu.name }}</span>
            </template>
            <MenuTree :menus="menu.children" :depth="1" />
          </el-sub-menu>
          <el-menu-item v-else-if="menu.path" :index="menu.path">
            <el-icon><Odometer /></el-icon>
            <span>{{ menu.name }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-title">{{ pageTitle }}</div>
        <div class="header-right">
          <span class="user-name">{{ userStore.profile?.nickname || userStore.profile?.username }}</span>
          <el-tag size="small">{{ userStore.profile?.roleName }}</el-tag>
          <el-button link type="primary" @click="onLogout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import MenuTree from '@/components/MenuTree.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const rootMenus = computed(() => userStore.menus || [])
const activeMenu = computed(() => route.path)

function flattenMenus(nodes, result = []) {
  for (const node of nodes) {
    result.push(node)
    if (node.children?.length) {
      flattenMenus(node.children, result)
    }
  }
  return result
}

const pageTitle = computed(() => {
  if (route.name === 'crud') {
    const current = flattenMenus(rootMenus.value).find((item) => item.path === route.path)
    return current?.name || '数据管理'
  }
  if (route.meta.title) {
    return route.meta.title
  }
  const current = flattenMenus(rootMenus.value).find((item) => item.path === route.path)
  return current?.name || '后台管理'
})

async function onLogout() {
  await userStore.logout()
  router.replace('/login')
}
</script>

<style scoped lang="scss">
.admin-layout {
  height: 100vh;
}

.aside {
  background: #001529;
  color: #fff;
  overflow-y: auto;
}

.brand {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-name {
  color: #606266;
}

.main {
  background: #f5f7fa;
}
</style>
