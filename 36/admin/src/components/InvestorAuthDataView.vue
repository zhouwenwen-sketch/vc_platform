<template>
  <div v-if="data" class="investor-auth-data">
    <div class="section-title">认证申请详情</div>

    <el-form label-width="160px" class="readonly-form">
      <el-form-item v-if="data.name" label="姓名">
        <span>{{ data.name }}</span>
      </el-form-item>
      <el-form-item v-if="data.company" label="机构简称">
        <span>{{ data.company }}</span>
      </el-form-item>
      <el-form-item v-if="data.companyEntity" label="公司主体">
        <span>{{ data.companyEntity }}</span>
      </el-form-item>
      <el-form-item v-if="data.position" label="职位">
        <span>{{ data.position }}</span>
      </el-form-item>
      <el-form-item v-if="data.idCard" label="身份证号">
        <span>{{ data.idCard }}</span>
      </el-form-item>
      <el-form-item v-if="data.phone" label="联系手机">
        <span>{{ data.phone }}</span>
      </el-form-item>
      <el-form-item v-if="data.email" label="企业邮箱">
        <span>{{ data.email }}</span>
      </el-form-item>
      <el-form-item label="邮件订阅">
        <span>{{ data.emailSubscribe ? '是' : '否' }}</span>
      </el-form-item>
      <el-form-item v-if="focusAreasText" label="关注领域">
        <span>{{ focusAreasText }}</span>
      </el-form-item>
      <el-form-item v-if="focusRoundsText" label="关注轮次">
        <span>{{ focusRoundsText }}</span>
      </el-form-item>
      <el-form-item v-if="data.remark" label="其他补充">
        <span class="multiline">{{ data.remark }}</span>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  data: {
    type: Object,
    default: null
  }
})

const focusAreasText = computed(() => {
  const list = props.data?.focusAreas
  return Array.isArray(list) && list.length ? list.join('、') : ''
})

const focusRoundsText = computed(() => {
  const list = props.data?.focusRounds
  return Array.isArray(list) && list.length ? list.join('、') : ''
})
</script>

<style scoped lang="scss">
.investor-auth-data {
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
