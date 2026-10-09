import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '工作台' }
      },
      {
        path: 'crud/:resource',
        name: 'crud',
        component: () => import('@/views/crud/CrudPage.vue'),
        meta: { title: '数据管理' }
      },
      {
        path: 'filters/manage',
        name: 'filter-manage',
        component: () => import('@/views/filters/FilterManageView.vue'),
        meta: { title: '筛选管理', permission: 'library_filter:manage' }
      },
      {
        path: 'content/institution-import',
        name: 'institution-import',
        component: () => import('@/views/content/InstitutionImportView.vue'),
        meta: { title: '机构导入', permission: 'institution:import' }
      },
      {
        path: 'content/project-import',
        name: 'project-import',
        component: () => import('@/views/content/ProjectImportView.vue'),
        meta: { title: '项目导入', permission: 'project:import' }
      },
      {
        path: 'system/admin-user',
        name: 'admin-user',
        component: () => import('@/views/system/AdminUserView.vue'),
        meta: { title: '管理员账号', permission: 'system:admin_user' }
      },
      {
        path: 'system/admin-role',
        name: 'admin-role',
        component: () => import('@/views/system/AdminRoleView.vue'),
        meta: { title: '角色权限', permission: 'system:admin_role' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  if (to.meta.public) return true
  const userStore = useUserStore()
  if (!userStore.token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (!userStore.profile) {
    try {
      await userStore.loadProfile()
    } catch {
      await userStore.logout()
      return { path: '/login' }
    }
  }
  const permission = to.meta.permission
  if (permission && !userStore.hasPermission(permission)) {
    return { path: '/dashboard' }
  }
  return true
})

export default router
