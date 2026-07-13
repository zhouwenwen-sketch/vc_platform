<template>
  <div v-if="data" class="coverage-apply-data">
    <div class="section-title">申请详情</div>

    <el-form label-width="160px" class="readonly-form">
      <el-form-item v-if="data.reportType" label="融资报道类型">
        <span>{{ formatFieldValue('reportType', data.reportType) }}</span>
      </el-form-item>

      <template v-if="data.financingList?.length">
        <div
          v-for="(row, idx) in data.financingList"
          :key="idx"
          class="financing-block"
        >
          <div v-if="data.financingList.length > 1" class="financing-title">
            融资信息 {{ idx + 1 }}
          </div>
          <el-form-item label="融资轮次">
            <span>{{ row.round || '-' }}</span>
          </el-form-item>
          <el-form-item label="融资时间">
            <span>{{ row.financingDate || '-' }}</span>
          </el-form-item>
          <el-form-item label="融资金额">
            <span>{{ row.amount || '-' }}</span>
          </el-form-item>
          <el-form-item label="投资方">
            <span>{{ row.investors || '-' }}</span>
          </el-form-item>
        </div>
      </template>

      <el-form-item v-if="data.reportContent" label="希望报道的内容">
        <span class="multiline">{{ data.reportContent }}</span>
      </el-form-item>
      <el-form-item v-if="data.competitiveness" label="核心竞争力">
        <span class="multiline">{{ data.competitiveness }}</span>
      </el-form-item>
      <el-form-item v-if="data.evaluation" label="创始人或投资人评价">
        <span class="multiline">{{ data.evaluation }}</span>
      </el-form-item>
      <el-form-item v-if="data.relatedReports" label="项目相关报道">
        <span class="multiline">{{ data.relatedReports }}</span>
      </el-form-item>
      <el-form-item v-if="data.debutMedia" label="将大学生创投作为首发媒体">
        <span>{{ formatFieldValue('debutMedia', data.debutMedia) }}</span>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { formatFieldValue } from '@/utils/fieldValueLabel'

defineProps({
  data: {
    type: Object,
    default: null
  }
})
</script>

<style scoped lang="scss">
.coverage-apply-data {
  margin: 8px 0 16px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.section-title {
  margin-bottom: 16px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.financing-block {
  margin-bottom: 4px;
  padding: 12px 0 4px;
  border-top: 1px solid #e4e7ed;
}

.financing-title {
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #606266;
}

.readonly-form {
  :deep(.el-form-item) {
    margin-bottom: 12px;
  }

  :deep(.el-form-item__label) {
    color: #909399;
  }
}

.multiline {
  white-space: pre-wrap;
  line-height: 1.6;
  color: #303133;
}
</style>
