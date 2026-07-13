<template>
  <div class="import-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-head">
          <h3>项目数据导入</h3>
          <el-text type="info">支持项目信息、画像、成员、动态、融资五表批量导入</el-text>
        </div>
      </template>

      <el-alert type="info" :closable="false" show-icon class="tip-alert">
        <template #title>导入说明</template>
        <ul class="tip-list">
          <li><strong>项目信息</strong> → 主表：「被投公司简称」= 项目简称；「机构名称」= 投资机构（如深创投）</li>
          <li><strong>项目画像</strong> → 按「项目简称」关联项目；<code>项目头像</code> 列填 Logo 文件名（需同时上传 ZIP/图片）</li>
          <li><strong>融资 / 成员 / 动态</strong> → 按「项目简称」关联项目；投资方从融资表「投资方」列解析</li>
          <li>机构关联仅在 <strong>项目信息</strong> 表：机构名称 → <code>institution.name</code>，请先完成机构导入</li>
          <li>建议顺序：<strong>项目信息</strong> → 画像 → 融资 → 成员 → 动态（可分批上传，避免一次传太多表超时）</li>
          <li>若出现 <strong>504</strong>：多为 Nginx 默认 60 秒超时，需在服务器调大 <code>proxy_read_timeout</code>（见 DEPLOY.md），或先只传项目信息表</li>
          <li>
            <strong>Logo</strong>：上传 ZIP 或多张图片；在<strong>项目画像</strong>的 <code>项目头像</code> 列填文件名，或文件名与「项目简称」一致（如 <code>VAST.png</code>）
          </li>
        </ul>
      </el-alert>

      <el-form label-width="120px" class="import-form">
        <el-form-item label="项目信息" required>
          <el-upload
            :auto-upload="false"
            :limit="1"
            accept=".xlsx,.xls"
            :on-change="(f) => onFileChange('projects', f)"
            :on-remove="() => onFileRemove('projects')"
          >
            <el-button>选择 项目信息.xlsx</el-button>
          </el-upload>
        </el-form-item>

        <el-form-item label="项目画像">
          <el-upload
            :auto-upload="false"
            :limit="1"
            accept=".xlsx,.xls"
            :on-change="(f) => onFileChange('portrait', f)"
            :on-remove="() => onFileRemove('portrait')"
          >
            <el-button>选择 项目画像.xlsx</el-button>
          </el-upload>
        </el-form-item>

        <el-form-item label="融资信息">
          <el-upload
            :auto-upload="false"
            :limit="1"
            accept=".xlsx,.xls"
            :on-change="(f) => onFileChange('financing', f)"
            :on-remove="() => onFileRemove('financing')"
          >
            <el-button>选择 融资信息.xlsx</el-button>
          </el-upload>
        </el-form-item>

        <el-form-item label="成员信息">
          <el-upload
            :auto-upload="false"
            :limit="1"
            accept=".xlsx,.xls"
            :on-change="(f) => onFileChange('members', f)"
            :on-remove="() => onFileRemove('members')"
          >
            <el-button>选择 成员信息.xlsx</el-button>
          </el-upload>
        </el-form-item>

        <el-form-item label="动态信息">
          <el-upload
            :auto-upload="false"
            :limit="1"
            accept=".xlsx,.xls"
            :on-change="(f) => onFileChange('dynamics', f)"
            :on-remove="() => onFileRemove('dynamics')"
          >
            <el-button>选择 动态信息.xlsx</el-button>
          </el-upload>
        </el-form-item>

        <el-form-item label="Logo 压缩包">
          <el-upload
            :auto-upload="false"
            :limit="1"
            accept=".zip"
            :on-change="(f) => onFileChange('images', f)"
            :on-remove="() => onFileRemove('images')"
          >
            <el-button>选择 ZIP（可选）</el-button>
          </el-upload>
        </el-form-item>

        <el-form-item label="Logo 图片">
          <el-upload
            :auto-upload="false"
            multiple
            accept="image/*"
            :file-list="imageFileList"
            :on-change="onImagesChange"
            :on-remove="onImagesRemove"
          >
            <el-button>选择图片（可选，可多选）</el-button>
          </el-upload>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="onImport">开始导入</el-button>
        </el-form-item>
      </el-form>

      <el-card v-if="result" shadow="never" class="result-card">
        <template #header>导入结果</template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="项目新增">{{ result.projectsInserted }}</el-descriptions-item>
          <el-descriptions-item label="项目更新">{{ result.projectsUpdated }}</el-descriptions-item>
          <el-descriptions-item label="画像更新">{{ result.portraitsUpdated }}</el-descriptions-item>
          <el-descriptions-item label="融资新增">{{ result.financingInserted }}</el-descriptions-item>
          <el-descriptions-item label="融资更新">{{ result.financingUpdated }}</el-descriptions-item>
          <el-descriptions-item label="成员新增">{{ result.membersInserted }}</el-descriptions-item>
          <el-descriptions-item label="成员更新">{{ result.membersUpdated }}</el-descriptions-item>
          <el-descriptions-item label="成员跳过">{{ result.membersSkipped }}</el-descriptions-item>
          <el-descriptions-item label="动态写入">{{ result.dynamicsInserted }}</el-descriptions-item>
          <el-descriptions-item label="动态跳过">{{ result.dynamicsSkipped }}</el-descriptions-item>
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
import { importProjectExcel } from '@/api/projectImport'

const files = reactive({
  projects: null,
  portrait: null,
  financing: null,
  members: null,
  dynamics: null,
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
  if (!files.projects && !files.portrait && !files.financing && !files.members && !files.dynamics) {
    ElMessage.warning('请至少上传一个 Excel 文件')
    return
  }
  const formData = new FormData()
  if (files.projects) formData.append('projectsFile', files.projects)
  if (files.portrait) formData.append('portraitFile', files.portrait)
  if (files.financing) formData.append('financingFile', files.financing)
  if (files.members) formData.append('membersFile', files.members)
  if (files.dynamics) formData.append('dynamicsFile', files.dynamics)
  if (files.images) formData.append('imagesZip', files.images)
  for (const file of files.imageList) {
    formData.append('imageFiles', file)
  }

  loading.value = true
  try {
    result.value = await importProjectExcel(formData)
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
  max-height: 320px;
  overflow: auto;
  color: #666;
  font-size: 13px;
  line-height: 1.6;
}
</style>
