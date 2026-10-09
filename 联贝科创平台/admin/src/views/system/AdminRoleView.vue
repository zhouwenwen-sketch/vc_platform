<template>
  <div class="system-page">
    <el-card shadow="never">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索角色编码/名称" clearable style="width: 220px" />
        <el-button @click="loadData">查询</el-button>
        <el-button type="primary" @click="openCreate">新增角色</el-button>
      </div>

      <el-table :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="code" label="编码" min-width="140" />
        <el-table-column prop="name" label="名称" min-width="140" />
        <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button
              v-if="row.code !== 'super_admin'"
              link
              type="danger"
              @click="onDelete(row)"
            >
              删除
            </el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="760px" destroy-on-close>
      <el-form label-width="90px">
        <el-form-item label="编码">
          <el-input v-model="form.code" :disabled="dialogMode === 'edit'" />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="权限">
          <el-tree
            ref="treeRef"
            :data="permissionTree"
            node-key="id"
            show-checkbox
            default-expand-all
            :props="{ label: 'name', children: 'children' }"
          />
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
  createRole,
  deleteRole,
  fetchAllPermissions,
  fetchRoleDetail,
  fetchRoles,
  updateRole
} from '@/api/system'

const tableData = ref([])
const permissionTree = ref([])
const permissionMap = ref({})
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const keyword = ref('')
const dialogVisible = ref(false)
const dialogMode = ref('create')
const dialogTitle = ref('新增角色')
const saving = ref(false)
const editingId = ref(null)
const treeRef = ref(null)
const form = reactive({
  code: '',
  name: '',
  description: '',
  status: 1
})

onMounted(async () => {
  await loadPermissions()
  await loadData()
})

async function loadPermissions() {
  const list = await fetchAllPermissions()
  permissionMap.value = Object.fromEntries(list.map((item) => [item.id, item]))
  const roots = []
  const nodeMap = {}
  for (const item of list) {
    nodeMap[item.id] = { ...item, children: [] }
  }
  for (const item of list) {
    const node = nodeMap[item.id]
    const parentId = item.parentId || 0
    if (parentId === 0) {
      roots.push(node)
    } else if (nodeMap[parentId]) {
      nodeMap[parentId].children.push(node)
    }
  }
  permissionTree.value = roots
}

async function loadData() {
  const data = await fetchRoles({
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    keyword: keyword.value || undefined
  })
  tableData.value = data.list || []
  total.value = data.total || 0
}

function resetForm(row = {}) {
  form.code = row.code || ''
  form.name = row.name || ''
  form.description = row.description || ''
  form.status = row.status ?? 1
}

function openCreate() {
  dialogMode.value = 'create'
  dialogTitle.value = '新增角色'
  editingId.value = null
  resetForm()
  dialogVisible.value = true
  setTimeout(() => treeRef.value?.setCheckedKeys([]), 0)
}

async function openEdit(row) {
  dialogMode.value = 'edit'
  dialogTitle.value = '编辑角色'
  editingId.value = row.id
  const detail = await fetchRoleDetail(row.id)
  resetForm(detail)
  dialogVisible.value = true
  setTimeout(() => treeRef.value?.setCheckedKeys(detail.permissionIds || []), 0)
}

async function onSave() {
  saving.value = true
  try {
    const checked = treeRef.value?.getCheckedKeys(false) || []
    const halfChecked = treeRef.value?.getHalfCheckedKeys() || []
    const permissionIds = [...checked, ...halfChecked]
    const payload = {
      role: { ...form },
      permissionIds
    }
    if (dialogMode.value === 'create') {
      await createRole(payload)
      ElMessage.success('创建成功')
    } else {
      await updateRole(editingId.value, payload)
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    await loadData()
  } finally {
    saving.value = false
  }
}

async function onDelete(row) {
  await ElMessageBox.confirm(`确认删除角色 ${row.name}？`, '提示', { type: 'warning' })
  await deleteRole(row.id)
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
