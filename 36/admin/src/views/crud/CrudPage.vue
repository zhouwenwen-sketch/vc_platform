<template>

  <div class="crud-page" v-loading="metaLoading">

    <el-card shadow="never">

      <div class="toolbar">

        <div class="toolbar-left">

          <h3>{{ meta?.label || resource }}</h3>

          <el-tag v-if="meta?.readOnly" type="info">只读</el-tag>

        </div>

        <div class="toolbar-right">

          <el-input

            v-model="keyword"

            placeholder="搜索关键词或ID"

            clearable

            style="width: 220px"

            @keyup.enter="loadData"

          />

          <el-button @click="loadData">查询</el-button>

          <el-button v-if="showCreate" type="primary" @click="openCreate">新增</el-button>

        </div>

      </div>



      <el-table :data="tableData" border stripe>

        <el-table-column

          v-for="field in displayFields"

          :key="field.name"

          :prop="field.name"

          :label="field.label"

          :min-width="columnWidth(field)"

          show-overflow-tooltip

        >

          <template #default="{ row }">

            {{ formatFieldValue(field.name, row[field.name], resource) }}

          </template>

        </el-table-column>

        <el-table-column label="操作" :width="actionWidth" fixed="right">

          <template #default="{ row }">

            <template v-if="hasPendingAudit(row)">

              <el-button link type="success" @click="onApprove(row)">通过</el-button>

              <el-button link type="warning" @click="onReject(row)">驳回</el-button>

            </template>

            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>

            <el-button v-if="showDelete" link type="danger" @click="onDelete(row)">删除</el-button>

          </template>

        </el-table-column>

      </el-table>



      <div class="pager">

        <el-pagination

          v-model:current-page="pageNum"

          v-model:page-size="pageSize"

          layout="total, prev, pager, next, sizes"

          :total="total"

          :page-sizes="[10, 20, 50]"

          @current-change="loadData"

          @size-change="loadData"

        />

      </div>

    </el-card>



    <el-dialog

      v-model="dialogVisible"

      :title="dialogTitle"

      :width="hasApplyDataView || isProjectResource ? '760px' : '720px'"

      destroy-on-close

    >

      <el-form label-width="120px">

        <template v-for="field in formFields" :key="field.name">

          <el-form-item :label="field.label">

            <div v-if="isCoverImageField(field)" class="project-cover-upload">

              <el-input

                v-model="form[field.name]"

                placeholder="图片地址，或点击下方上传"

                class="cover-url-input"

              />

              <div class="image-upload-row">

                <el-upload

                  :show-file-list="false"

                  accept="image/*"

                  :http-request="(options) => onImageUpload(options, field.name)"

                >

                  <el-button type="primary">上传封面图</el-button>

                </el-upload>

                <el-image

                  v-if="form[field.name]"

                  :src="resolvePreviewUrl(form[field.name])"

                  fit="cover"

                  class="cover-preview-thumb"

                />

              </div>

            </div>

            <el-select

              v-else-if="hasFormSelectOptions(field.name, resource)"

              v-model="form[field.name]"

              style="width: 100%"

            >

              <el-option

                v-for="opt in getFormSelectOptions(field.name, resource)"

                :key="opt.value"

                :label="opt.label"

                :value="opt.value"

              />

            </el-select>

            <el-select

              v-else-if="isMultiselectField(field)"

              v-model="multiselectValues[field.name]"

              multiple

              collapse-tags

              collapse-tags-tooltip

              filterable

              clearable

              :placeholder="`请选择${field.label}`"

              style="width: 100%"

            >

              <template v-if="getFieldOptionGroups(field).length">

                <el-option-group

                  v-for="group in getFieldOptionGroups(field)"

                  :key="group.label"

                  :label="group.label"

                >

                  <el-option

                    v-for="opt in group.values"

                    :key="`${group.label}-${opt}`"

                    :label="opt"

                    :value="opt"

                  />

                </el-option-group>

              </template>

              <template v-else>

                <el-option

                  v-for="opt in getFieldSelectOptions(field)"

                  :key="opt"

                  :label="opt"

                  :value="opt"

                />

              </template>

            </el-select>

            <el-select

              v-else-if="isSelectField(field)"

              v-model="form[field.name]"

              filterable

              clearable

              :placeholder="`请选择${field.label}`"

              style="width: 100%"

            >

              <el-option

                v-for="opt in getFieldLabeledOptions(field)"

                :key="opt.value"

                :label="opt.label"

                :value="opt.value"

              />

            </el-select>

            <el-select
              v-else-if="isProjectLookupField(field)"
              v-model="form.projectId"
              filterable
              remote
              clearable
              reserve-keyword
              :remote-method="searchNewsProjects"
              :loading="newsProjectLoading"
              placeholder="输入项目名称关键字搜索并选择"
              style="width: 100%"
              @change="onNewsProjectChange"
              @clear="onNewsProjectClear"
            >
              <el-option
                v-for="item in newsProjectOptions"
                :key="item.id"
                :label="formatProjectOption(item)"
                :value="item.id"
              />
            </el-select>

            <div v-else-if="isImageUrlField(field.name)" class="image-field" :class="{ 'image-field--cover': field.name === 'cover' }">

              <el-input v-model="form[field.name]" placeholder="图片地址，或点击下方上传" />

              <div class="image-upload-row">

                <el-upload

                  :show-file-list="false"

                  accept="image/*"

                  :http-request="(options) => onImageUpload(options, field.name)"

                >

                  <el-button type="primary" plain>上传图片</el-button>

                </el-upload>

                <el-image

                  v-if="form[field.name]"

                  :src="resolvePreviewUrl(form[field.name])"

                  fit="cover"

                  class="image-preview"

                />

              </div>

            </div>

            <el-input

              v-else-if="field.type === 'text' && !isCoverImageField(field)"

              v-model="form[field.name]"

              :type="isLongText(field.name) ? 'textarea' : 'text'"

              :rows="isLongText(field.name) ? 4 : 1"

            />

            <el-input-number

              v-else-if="field.type === 'number'"

              v-model="form[field.name]"

              style="width: 100%"

            />

            <el-switch

              v-else-if="field.type === 'boolean'"

              v-model="form[field.name]"

            />

            <el-date-picker

              v-else-if="isDateOnlyField(field.name)"

              v-model="form[field.name]"

              type="date"

              value-format="YYYY-MM-DD"

              style="width: 100%"

            />

            <el-date-picker

              v-else-if="field.type === 'datetime'"

              v-model="form[field.name]"

              type="datetime"

              value-format="YYYY-MM-DDTHH:mm:ss"

              style="width: 100%"

            />

            <el-input v-else v-model="form[field.name]" />

          </el-form-item>



          <CoverageApplyDataView

            v-if="isCoverageResource && field.name === 'reportType' && parsedApplyData"

            :data="parsedApplyData"

          />

          <ProjectOnboardDataView

            v-if="isOnboardResource && field.name === 'status' && parsedApplyData"

            :data="parsedApplyData"

          />

          <InvestorAuthDataView

            v-if="isUserAuthResource && field.name === 'authType' && parsedApplyData"

            :data="parsedApplyData"

          />

        </template>

        <template v-if="isProjectResource">
          <el-divider content-position="left">工商信息</el-divider>
          <el-form-item label="工商全称">
            <el-input v-model="businessForm.fullName" />
          </el-form-item>
          <el-form-item label="英文全称">
            <el-input v-model="businessForm.englishName" />
          </el-form-item>
          <el-form-item label="法定代表人">
            <el-input v-model="businessForm.legalPerson" />
          </el-form-item>
          <el-form-item label="注册地址">
            <el-input v-model="businessForm.registeredAddress" />
          </el-form-item>
          <el-form-item label="成立时间">
            <el-date-picker
              v-model="businessForm.establishDate"
              type="date"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="统一社会信用代码">
            <el-input v-model="businessForm.unifiedSocialCreditCode" />
          </el-form-item>
        </template>

      </el-form>

      <template #footer>

        <el-button @click="dialogVisible = false">取消</el-button>

        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>

      </template>

    </el-dialog>

  </div>

</template>



<script setup>

import { computed, onMounted, reactive, ref, watch } from 'vue'

import { useRoute } from 'vue-router'

import { ElMessage, ElMessageBox } from 'element-plus'

import {

  createRecord,

  deleteRecord,

  fetchPage,

  fetchResourceMeta,

  fetchProjectBusiness,

  updateRecord

} from '@/api/crud'

import { approveOnboard, rejectOnboard } from '@/api/onboard'

import { approveUserAuth, rejectUserAuth } from '@/api/userAuth'

import { uploadFile } from '@/api/file'

import { lookupProjects } from '@/api/lookup'

import { MEDIA_BASE } from '@/api/request'

import CoverageApplyDataView from '@/components/CoverageApplyDataView.vue'

import InvestorAuthDataView from '@/components/InvestorAuthDataView.vue'

import ProjectOnboardDataView from '@/components/ProjectOnboardDataView.vue'

import { formatFieldValue, getFormSelectOptions, hasFormSelectOptions } from '@/utils/fieldValueLabel'



const route = useRoute()

const resource = computed(() => route.params.resource)

const meta = ref(null)

const metaLoading = ref(false)

const tableData = ref([])

const pageNum = ref(1)

const pageSize = ref(10)

const total = ref(0)

const keyword = ref('')

const dialogVisible = ref(false)

const dialogMode = ref('create')

const saving = ref(false)

const form = reactive({})

const editingId = ref(null)

const multiselectValues = reactive({})

const businessForm = reactive({
  fullName: '',
  englishName: '',
  legalPerson: '',
  registeredAddress: '',
  establishDate: '',
  unifiedSocialCreditCode: ''
})

const newsProjectOptions = ref([])
const newsProjectLoading = ref(false)

const coverageHiddenFields = ['status', 'newsId', 'auditRemark', 'auditorId']

const userAuthHiddenFields = ['status', 'auditRemark']

const isNewsResource = computed(() => resource.value === 'news')

const isProjectResource = computed(() => resource.value === 'project')

const isCoverageResource = computed(() => resource.value === 'coverage_apply_record')

const isOnboardResource = computed(() => resource.value === 'project_onboard_record')

const isUserAuthResource = computed(() => resource.value === 'user_auth_record')

const hasApplyDataView = computed(
  () => isCoverageResource.value || isOnboardResource.value || isUserAuthResource.value
)



const displayFields = computed(() =>

  (meta.value?.fields || [])

    .filter((f) => !f.hidden && f.name !== 'applyData')

    .filter((f) => !(isCoverageResource.value && coverageHiddenFields.includes(f.name)))

    .slice(0, 8)

)



const formFields = computed(() =>

  (meta.value?.fields || [])

    .filter((f) => !f.readOnly && !f.hidden)

    .filter((f) => !(hasApplyDataView.value && f.name === 'applyData'))

    .filter((f) => !(isCoverageResource.value && coverageHiddenFields.includes(f.name)))

    .filter((f) => !(isUserAuthResource.value && userAuthHiddenFields.includes(f.name)))

)



const parsedApplyData = computed(() => parseApplyData(form.applyData))



const dialogTitle = computed(() =>

  dialogMode.value === 'create' ? `新增${meta.value?.label || ''}` : `编辑${meta.value?.label || ''}`

)



const showCreate = computed(() => !meta.value?.readOnly && !hasApplyDataView.value)

const showDelete = computed(() => !meta.value?.readOnly)

const actionWidth = computed(() =>
  isOnboardResource.value || isUserAuthResource.value ? 260 : 180
)



watch(resource, () => {

  pageNum.value = 1

  initPage()

})



onMounted(() => {

  initPage()

})



async function initPage() {

  await loadMeta()

  await loadData()

}



async function loadMeta() {

  metaLoading.value = true

  try {

    meta.value = await fetchResourceMeta(resource.value)

  } finally {

    metaLoading.value = false

  }

}



function parseMultiValue(raw) {

  if (!raw) return []

  return String(raw)

    .split(/[,，]/)

    .map((item) => item.trim())

    .filter(Boolean)

}



function joinMultiValue(values) {

  return (values || []).filter(Boolean).join(',')

}



function isSelectField(field) {

  return field.fieldOptions?.inputType === 'select'

}



function isMultiselectField(field) {

  return field.fieldOptions?.inputType === 'multiselect'

}

function isProjectLookupField(field) {
  return field.fieldOptions?.inputType === 'project_lookup'
}

function formatProjectOption(item) {
  if (!item) return ''
  const parts = [item.name]
  if (item.round) parts.push(item.round)
  if (item.entityName && item.entityName !== item.name) parts.push(item.entityName)
  return parts.join(' · ')
}

async function searchNewsProjects(query) {
  const keyword = String(query || '').trim()
  if (!keyword) {
    newsProjectOptions.value = buildNewsProjectSeedOptions()
    return
  }
  newsProjectLoading.value = true
  try {
    const list = await lookupProjects(keyword, 10)
    newsProjectOptions.value = list || []
  } catch {
    newsProjectOptions.value = buildNewsProjectSeedOptions()
  } finally {
    newsProjectLoading.value = false
  }
}

function buildNewsProjectSeedOptions() {
  if (form.projectId && form.projectName) {
    return [{ id: form.projectId, name: form.projectName }]
  }
  return []
}

function onNewsProjectChange(projectId) {
  const selected = newsProjectOptions.value.find((item) => item.id === projectId)
  form.projectName = selected?.name || ''
}

function onNewsProjectClear() {
  form.projectId = null
  form.projectName = ''
  newsProjectOptions.value = []
}

function syncNewsProjectOptionsFromForm() {
  if (!isNewsResource.value) {
    newsProjectOptions.value = []
    return
  }
  if (form.projectId) {
    newsProjectOptions.value = [
      {
        id: form.projectId,
        name: form.projectName || `项目 #${form.projectId}`
      }
    ]
    return
  }
  newsProjectOptions.value = []
}

async function hydrateNewsProjectFromRow(row = {}) {
  if (!isNewsResource.value) return
  if (row.projectId) {
    syncNewsProjectOptionsFromForm()
    return
  }
  const name = String(row.projectName || '').trim()
  if (!name) {
    newsProjectOptions.value = []
    return
  }
  await searchNewsProjects(name)
  const exact = newsProjectOptions.value.find((item) => item.name === name)
  if (exact) {
    form.projectId = exact.id
    form.projectName = exact.name
  }
}


function getKnownOptionValues(field) {

  const known = new Set()

  for (const value of field.fieldOptions?.options || []) {

    known.add(value)

  }

  for (const group of field.fieldOptions?.optionGroups || []) {

    for (const value of group.values || []) {

      known.add(value)

    }

  }

  return known

}



function getFieldSelectOptions(field) {

  const options = [...(field.fieldOptions?.options || [])]

  if (field.fieldOptions?.optionGroups?.length) {

    return options

  }

  const selected = parseMultiValue(form[field.name])

  const known = new Set(options)

  for (const value of selected) {

    if (!known.has(value)) {

      options.push(value)

    }

  }

  return options

}



function getFieldOptionGroups(field) {

  const groups = (field.fieldOptions?.optionGroups || []).map((group) => ({

    label: group.label,

    values: [...(group.values || [])]

  }))

  if (!groups.length) {

    return []

  }

  const selected = multiselectValues[field.name] || []

  const known = getKnownOptionValues(field)

  const legacy = selected.filter((value) => !known.has(value))

  if (legacy.length) {

    groups.push({ label: '历史值（请改选上方选项）', values: legacy })

  }

  return groups

}



function getFieldLabeledOptions(field) {

  const labeled = field.fieldOptions?.labeledOptions

  if (labeled?.length) {

    const current = form[field.name]

    if (current && !labeled.some((item) => item.value === current)) {

      return [...labeled, { value: current, label: String(current) }]

    }

    return labeled

  }

  return getFieldSelectOptions(field).map((value) => ({ value, label: value }))

}



function syncMultiselectValues() {

  Object.keys(multiselectValues).forEach((key) => delete multiselectValues[key])

  for (const field of formFields.value) {

    if (isMultiselectField(field)) {

      multiselectValues[field.name] = parseMultiValue(form[field.name])

    }

  }

}



function applyMultiselectToPayload(payload) {

  for (const field of formFields.value) {

    if (isMultiselectField(field)) {

      payload[field.name] = joinMultiValue(multiselectValues[field.name])

    }

  }

}



async function loadData() {

  const data = await fetchPage(resource.value, {

    pageNum: pageNum.value,

    pageSize: pageSize.value,

    keyword: keyword.value || undefined

  })

  tableData.value = data.list || []

  total.value = data.total || 0

}



function parseApplyData(raw) {

  if (!raw) return null

  try {

    return typeof raw === 'string' ? JSON.parse(raw) : raw

  } catch {

    return null

  }

}



function resetBusinessForm(data = {}) {
  businessForm.fullName = data.fullName ?? ''
  businessForm.englishName = data.englishName ?? ''
  businessForm.legalPerson = data.legalPerson ?? ''
  businessForm.registeredAddress = data.registeredAddress ?? ''
  businessForm.establishDate = data.establishDate ?? ''
  businessForm.unifiedSocialCreditCode = data.unifiedSocialCreditCode ?? ''
}

async function loadProjectBusiness(projectId) {
  if (!projectId) {
    resetBusinessForm()
    return
  }
  try {
    const data = await fetchProjectBusiness(projectId)
    resetBusinessForm(data || {})
  } catch {
    resetBusinessForm()
  }
}

function resetForm(row = {}) {

  Object.keys(form).forEach((key) => delete form[key])

  for (const field of formFields.value) {

    form[field.name] = row[field.name] ?? defaultValue(field)

  }

  if (isNewsResource.value) {

    form.projectId = row.projectId ?? null

    form.projectName = row.projectName ?? ''

  }

  if (hasApplyDataView.value) {

    form.applyData = row.applyData ?? ''

  }

  syncMultiselectValues()

  syncNewsProjectOptionsFromForm()

}



function defaultValue(field) {

  if (field.type === 'number') return null

  if (field.type === 'boolean') return false

  return ''

}



function openCreate() {

  dialogMode.value = 'create'

  editingId.value = null

  resetForm()

  resetBusinessForm()

  onNewsProjectClear()

  if (resource.value === 'home_banner') {

    form.status = 1

    form.sortOrder = 0

  }

  if (resource.value === 'project_collection') {

    form.status = 1

    form.sortOrder = 0

  }

  dialogVisible.value = true

}



function openEdit(row) {

  dialogMode.value = 'edit'

  editingId.value = row.id

  resetForm(row)

  hydrateNewsProjectFromRow(row)

  loadProjectBusiness(row.id)

  dialogVisible.value = true

}



async function onSave() {

  saving.value = true

  try {

    const payload = { ...form }

    applyMultiselectToPayload(payload)

    if (isProjectResource.value) {

      payload.business = { ...businessForm }

    }

    if (dialogMode.value === 'create') {

      await createRecord(resource.value, payload)

      ElMessage.success('创建成功')

    } else {

      await updateRecord(resource.value, editingId.value, payload)

      ElMessage.success('更新成功')

    }

    dialogVisible.value = false

    await loadData()

  } finally {

    saving.value = false

  }

}



async function onDelete(row) {

  await ElMessageBox.confirm(`确认删除 ID=${row.id} 的记录？`, '提示', { type: 'warning' })

  await deleteRecord(resource.value, row.id)

  ElMessage.success('删除成功')

  await loadData()

}



function hasPendingAudit(row) {

  return (isOnboardResource.value || isUserAuthResource.value) && row.status === 'pending'

}



async function onApprove(row) {

  if (isUserAuthResource.value) {

    const authTypeLabel = formatFieldValue('authType', row.authType)

    await ElMessageBox.confirm(

      `确认通过该${authTypeLabel || '用户'}认证申请？`,

      '审核通过',

      { type: 'warning' }

    )

    const data = await approveUserAuth(row.id)

    ElMessage.success(data?.message || '认证审核已通过')

    await loadData()

    return

  }

  await ElMessageBox.confirm(

    `确认通过「${row.projectName || row.id}」的入驻申请？通过后将自动创建项目并入库。`,

    '审核通过',

    { type: 'warning' }

  )

  const data = await approveOnboard(row.id)

  ElMessage.success(data?.message || '审核通过，项目已入库')

  await loadData()

}



async function onReject(row) {

  if (isUserAuthResource.value) {

    const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回认证申请', {

      confirmButtonText: '确认驳回',

      cancelButtonText: '取消',

      inputPlaceholder: '如：请使用企业邮箱重新提交'

    })

    await rejectUserAuth(row.id, value)

    ElMessage.success('已驳回')

    await loadData()

    return

  }

  const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回入驻申请', {

    confirmButtonText: '确认驳回',

    cancelButtonText: '取消',

    inputPlaceholder: '如：项目信息不完整，请补充后重新提交'

  })

  await rejectOnboard(row.id, value)

  ElMessage.success('已驳回')

  await loadData()

}



function columnWidth(field) {

  if (field.name === 'id') return 80

  if (field.type === 'datetime') return 170

  if (isLongText(field.name)) return 220

  return 120

}



function isLongText(name) {

  return ['content', 'summary', 'description', 'applyData', 'attribution', 'intro', 'projectsData', 'coverTitle'].includes(name)

}



function isDateOnlyField(name) {

  return ['collectionDate', 'establishDate', 'publishDate'].includes(name)

}



function isCoverImageField(field) {

  return field?.name === 'cover' || field?.label === '封面图'

}



function isImageUrlField(name) {

  if (name === 'cover') {

    return false

  }

  return ['imageUrl', 'coverUrl', 'logoUrl', 'avatarUrl', 'sourceAvatar'].includes(name)

}



function resolveUploadCategory(fieldName) {

  if (fieldName === 'cover') {

    return 'banner'

  }

  return 'banner'

}



function resolvePreviewUrl(url) {

  if (!url) return ''

  if (/^https?:\/\//i.test(url)) return url

  if (url.startsWith('/uploads/')) {

    return `${MEDIA_BASE}${url}`

  }

  return url

}



async function onImageUpload(options, fieldName) {

  try {

    const data = await uploadFile(options.file, resolveUploadCategory(fieldName))

    form[fieldName] = data.url || ''

    ElMessage.success('上传成功')

    options.onSuccess?.(data)

  } catch (err) {

    options.onError?.(err)

  }

}

</script>



<style scoped lang="scss">

.crud-page {

  min-height: 100%;

}



.toolbar {

  display: flex;

  justify-content: space-between;

  align-items: center;

  margin-bottom: 16px;

  gap: 12px;

}



.toolbar-left {

  display: flex;

  align-items: center;

  gap: 8px;



  h3 {

    margin: 0;

  }

}



.toolbar-right {

  display: flex;

  align-items: center;

  gap: 8px;

}



.pager {

  display: flex;

  justify-content: flex-end;

  margin-top: 16px;

}



.image-field {

  width: 100%;

}



.image-upload-row {

  display: flex;

  align-items: center;

  gap: 12px;

  margin-top: 8px;

}



.image-preview {

  width: 120px;

  height: 60px;

  border-radius: 4px;

  border: 1px solid #ebeef5;

}



.image-field--cover .image-preview {

  width: 240px;

  height: 120px;

}



.project-cover-upload {

  width: 100%;

}



.cover-uploader {

  width: 100%;

}



.cover-uploader :deep(.el-upload-dragger) {

  width: 100%;

  min-height: 160px;

  padding: 12px;

}



.cover-uploader-preview {

  width: 100%;

  height: 160px;

  border-radius: 8px;

}



.cover-uploader-placeholder {

  display: flex;

  flex-direction: column;

  align-items: center;

  justify-content: center;

  min-height: 136px;

  color: #606266;

}



.cover-uploader-title {

  margin: 0 0 8px;

  font-size: 15px;

  font-weight: 600;

  color: #78B9B1;

}



.cover-uploader-tip {

  margin: 0;

  font-size: 12px;

  color: #909399;

}



.cover-url-input {

  margin-bottom: 12px;

}



.cover-preview-thumb {

  width: 120px;

  height: 72px;

  border-radius: 8px;

  flex-shrink: 0;

}

</style>


