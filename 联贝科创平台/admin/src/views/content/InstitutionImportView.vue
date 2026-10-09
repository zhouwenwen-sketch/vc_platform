<template>
  <div class="import-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-head">
          <h3>机构数据导入</h3>
          <el-text type="info">支持机构主表、基本信息、基金人员等多文件批量导入</el-text>
        </div>
      </template>

      <el-alert type="info" :closable="false" show-icon class="tip-alert">
        <template #title>导入说明</template>
        <ul class="tip-list">
          <li><strong>机构主表</strong>（如 机构_*.xlsx）→ <code>institution</code>：简称、全称、介绍、官网、行业、成立时间、管理规模等</li>
          <li><strong>基本信息</strong>（基本信息.xlsx）→ 补充 Logo、机构类型（与主表按「机构名称/简称」合并）</li>
          <li><strong>基金人员</strong>（基金人员.xlsx）→ <code>institution_team_member</code>：姓名、职位、介绍</li>
          <li><strong>头像</strong>：ZIP 必须是标准 ZIP（Mac 访达右键「压缩」）；<strong>不要用 RAR 改 .zip 后缀</strong>。也可直接多选 PNG 上传</li>
          <li>建议顺序：先传<strong>机构主表</strong>，再传基本信息 + 基金人员 + ZIP</li>
        </ul>
      </el-alert>

      <el-form label-width="120px" class="import-form">
        <el-form-item label="机构主表">
          <el-upload
            :auto-upload="false"
            :limit="1"
            accept=".xlsx,.xls"
            :on-change="(f) => onFileChange('institutions', f)"
            :on-remove="() => onFileRemove('institutions')"
          >
            <el-button>选择 机构_*.xlsx</el-button>
          </el-upload>
        </el-form-item>

        <el-form-item label="基本信息">
          <el-upload
            :auto-upload="false"
            :limit="1"
            accept=".xlsx,.xls"
            :on-change="(f) => onFileChange('basicInfo', f)"
            :on-remove="() => onFileRemove('basicInfo')"
          >
            <el-button>选择 基本信息.xlsx</el-button>
          </el-upload>
        </el-form-item>

        <el-form-item label="基金人员">
          <el-upload
            :auto-upload="false"
            :limit="1"
            accept=".xlsx,.xls"
            :on-change="(f) => onFileChange('members', f)"
            :on-remove="() => onFileRemove('members')"
          >
            <el-button>选择 基金人员.xlsx</el-button>
          </el-upload>
        </el-form-item>

        <el-form-item label="头像压缩包">
          <el-upload
            :auto-upload="false"
            :limit="1"
            accept=".zip"
            :on-change="(f) => onFileChange('images', f)"
            :on-remove="() => onFileRemove('images')"
          >
            <el-button>选择 ZIP（须为真 ZIP，不能是 RAR 改后缀）</el-button>
          </el-upload>
        </el-form-item>

        <el-form-item label="或批量选图">
          <el-upload
            :auto-upload="false"
            multiple
            accept=".png,.jpg,.jpeg,.webp"
            :on-change="onImagesChange"
            :on-remove="onImagesRemove"
            :file-list="imageFileList"
          >
            <el-button>选择多张机构头像 PNG</el-button>
          </el-upload>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="onImport">开始导入</el-button>
        </el-form-item>
      </el-form>

      <el-card v-if="result" shadow="never" class="result-card">
        <template #header>导入结果</template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="机构新增">{{ result.companiesInserted }}</el-descriptions-item>
          <el-descriptions-item label="机构更新">{{ result.companiesUpdated }}</el-descriptions-item>
          <el-descriptions-item label="成员新增">{{ result.membersInserted }}</el-descriptions-item>
          <el-descriptions-item label="成员更新">{{ result.membersUpdated }}</el-descriptions-item>
          <el-descriptions-item label="成员跳过">{{ result.membersSkipped }}</el-descriptions-item>
          <el-descriptions-item label="图片写入">{{ result.imagesSaved ?? 0 }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="result.warnings?.length" class="warnings">
          <h4>提示（{{ result.warnings.length }}）</h4>
          <ul>
            <li v-for="(w, i) in result.warnings" :key="i">{{ w }}</li>
          </ul>
        </div>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { importInstitutionExcel } from '@/api/institutionImport'

const files = reactive({
  institutions: null,
  basicInfo: null,
  members: null,
  images: null,
  imageList: []
})
const imageFileList = ref([])
const loading = ref(false)
const result = ref(null)

function onFileChange(key, uploadFile) {
  files[key] = uploadFile?.raw || null
}

function onFileRemove(key) {
  files[key] = null
}

function onImagesChange(_file, fileList) {
  imageFileList.value = fileList
  files.imageList = fileList.map((item) => item.raw).filter(Boolean)
}

function onImagesRemove(_file, fileList) {
  imageFileList.value = fileList
  files.imageList = fileList.map((item) => item.raw).filter(Boolean)
}

async function onImport() {
  if (!files.institutions && !files.basicInfo && !files.members) {
    ElMessage.warning('请至少上传机构主表、基本信息或基金人员 Excel')
    return
  }
  const formData = new FormData()
  if (files.institutions) formData.append('institutionsFile', files.institutions)
  if (files.basicInfo) formData.append('basicInfoFile', files.basicInfo)
  if (files.members) formData.append('membersFile', files.members)
  if (files.images) formData.append('imagesZip', files.images)
  for (const file of files.imageList) {
    formData.append('imageFiles', file)
  }

  loading.value = true
  try {
    result.value = await importInstitutionExcel(formData)
    ElMessage.success('导入完成')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.import-page {
  max-width: 900px;
}

.card-head h3 {
  margin: 0 0 4px;
}

.tip-alert {
  margin-bottom: 24px;
}

.tip-list {
  margin: 8px 0 0;
  padding-left: 18px;
  line-height: 1.8;
}

.import-form {
  margin-top: 8px;
}

.result-card {
  margin-top: 24px;
}

.warnings {
  margin-top: 16px;
}

.warnings h4 {
  margin: 0 0 8px;
  color: #e6a23c;
}

.warnings ul {
  margin: 0;
  padding-left: 18px;
  max-height: 240px;
  overflow: auto;
  color: #666;
  font-size: 13px;
  line-height: 1.6;
}
</style>
