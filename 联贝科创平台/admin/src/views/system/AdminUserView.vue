<template>
  <div class="system-page">
    <el-card shadow="never">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索用户名/昵称" clearable style="width: 220px" />
        <el-button @click="loadData">查询</el-button>
        <el-button type="primary" @click="openCreate">新增管理员</el-button>
      </div>

      <el-table :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="roleName" label="角色" min-width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          layout="total, prev, pager, next"
          :total="total"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" destroy-on-close>
      <el-form label-width="90px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" :disabled="dialogMode === 'edit'" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            :placeholder="dialogMode === 'edit' ? '留空则不修改' : '请输入密码'"
          />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleId" style="width: 100%">
            <el-option
              v-for="item in roleOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createAdminUser,
  deleteAdminUser,
  fetchAdminUsers,
  fetchRoleOptions,
  updateAdminUser
} from '@/api/system'

const tableData = ref([])
const roleOptions = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const keyword = ref('')
const dialogVisible = ref(false)
const dialogMode = ref('create')
const saving = ref(false)
const editingId = ref(null)
const form = reactive({
  username: '',
  nickname: '',
  password: '',
  roleId: null,
  status: 1
})

const dialogTitle = ref('新增管理员')

onMounted(async () => {
  roleOptions.value = await fetchRoleOptions()
  await loadData()
})

async function loadData() {
  const data = await fetchAdminUsers({
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    keyword: keyword.value || undefined
  })
  tableData.value = data.list || []
  total.value = data.total || 0
}

function resetForm(row = {}) {
  form.username = row.username || ''
  form.nickname = row.nickname || ''
  form.password = ''
  form.roleId = row.roleId || roleOptions.value[0]?.id || null
  form.status = row.status ?? 1
}

function openCreate() {
  dialogMode.value = 'create'
  dialogTitle.value = '新增管理员'
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  dialogMode.value = 'edit'
  dialogTitle.value = '编辑管理员'
  editingId.value = row.id
  resetForm(row)
  dialogVisible.value = true
}

async function onSave() {
  saving.value = true
  try {
    const payload = { ...form }
    if (dialogMode.value === 'create') {
      await createAdminUser(payload)
      ElMessage.success('创建成功')
    } else {
      if (!payload.password) delete payload.password
      await updateAdminUser(editingId.value, payload)
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    await loadData()
  } finally {
    saving.value = false
  }
}

async function onDelete(row) {
  await ElMessageBox.confirm(`确认删除管理员 ${row.username}？`, '提示', { type: 'warning' })
  await deleteAdminUser(row.id)
  ElMessage.success('删除成功')
  await loadData()
}
</script>

<style scoped lang="scss">
.toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
