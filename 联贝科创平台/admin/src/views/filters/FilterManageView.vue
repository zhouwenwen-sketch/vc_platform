<template>
  <div class="filter-page">
    <el-card shadow="never">
      <el-table :data="groups" border stripe>
        <el-table-column prop="label" label="筛选项" min-width="140" />
        <el-table-column prop="remark" label="适用范围" min-width="280" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEditor(row)">编辑标签</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="editorVisible"
      :title="editorTitle"
      width="760px"
      destroy-on-close
      @closed="resetEditor"
    >
      <div class="toolbar">
        <el-button type="primary" @click="openCreate">新增标签</el-button>
        <el-button @click="loadOptions">刷新</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe max-height="420">
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="label" label="展示名称" min-width="160" />
        <el-table-column prop="value" label="筛选值" min-width="160" />
        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled === 1 ? 'success' : 'info'">
              {{ row.enabled === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="formVisible" :title="formTitle" width="520px" append-to-body destroy-on-close>
      <el-form label-width="90px">
        <el-form-item label="展示名称">
          <el-input v-model="form.label" placeholder="如：文化娱乐" />
        </el-form-item>
        <el-form-item label="筛选值">
          <el-input
            v-model="form.value"
            placeholder="留空则与展示名称相同，需与业务字段值一致"
          />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="1" :max="9999" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createLibraryFilterOption,
  deleteLibraryFilterOption,
  fetchLibraryFilterMeta,
  fetchLibraryFilterOptions,
  updateLibraryFilterOption
} from '@/api/libraryFilter'

const groups = ref([])
const activeGroup = ref(null)
const tableData = ref([])
const loading = ref(false)
const editorVisible = ref(false)
const editorTitle = ref('')
const formVisible = ref(false)
const formTitle = ref('新增标签')
const formMode = ref('create')
const saving = ref(false)
const editingId = ref(null)
const form = reactive({
  label: '',
  value: '',
  sortOrder: 1,
  enabled: 1
})

onMounted(async () => {
  const meta = await fetchLibraryFilterMeta()
  groups.value = meta.groups || []
})

function resetEditor() {
  activeGroup.value = null
  tableData.value = []
}

async function openEditor(row) {
  activeGroup.value = row
  editorTitle.value = `编辑「${row.label}」标签`
  editorVisible.value = true
  await loadOptions()
}

async function loadOptions() {
  if (!activeGroup.value) return
  loading.value = true
  try {
    tableData.value = await fetchLibraryFilterOptions(
      activeGroup.value.scene,
      activeGroup.value.filterKey
    )
  } finally {
    loading.value = false
  }
}

function nextSortOrder() {
  if (!tableData.value.length) return 1
  return Math.max(...tableData.value.map((item) => item.sortOrder || 0)) + 1
}

function resetForm(row = {}) {
  form.label = row.label || ''
  form.value = row.value || ''
  form.sortOrder = row.sortOrder || nextSortOrder()
  form.enabled = row.enabled ?? 1
}

function openCreate() {
  formMode.value = 'create'
  formTitle.value = '新增标签'
  editingId.value = null
  resetForm()
  formVisible.value = true
}

function openEdit(row) {
  formMode.value = 'edit'
  formTitle.value = '编辑标签'
  editingId.value = row.id
  resetForm(row)
  formVisible.value = true
}

async function onSave() {
  if (!form.label.trim()) {
    ElMessage.warning('请填写展示名称')
    return
  }
  saving.value = true
  try {
    const payload = {
      scene: activeGroup.value.scene,
      filterKey: activeGroup.value.filterKey,
      label: form.label.trim(),
      value: form.value.trim() || form.label.trim(),
      sortOrder: form.sortOrder,
      enabled: form.enabled
    }
    if (formMode.value === 'create') {
      await createLibraryFilterOption(payload)
      ElMessage.success('已新增')
    } else {
      await updateLibraryFilterOption(editingId.value, payload)
      ElMessage.success('已保存')
    }
    formVisible.value = false
    await loadOptions()
  } finally {
    saving.value = false
  }
}

async function onDelete(row) {
  await ElMessageBox.confirm(`确定删除「${row.label}」？`, '提示', { type: 'warning' })
  await deleteLibraryFilterOption(row.id)
  ElMessage.success('已删除')
  await loadOptions()
}
</script>

<style scoped>
.filter-page {
  padding: 0;
}
.toolbar {
  margin-bottom: 16px;
  display: flex;
  gap: 8px;
}
</style>
