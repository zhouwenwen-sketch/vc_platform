<template>
  <div v-if="data" class="onboard-apply-data">
    <div class="section-title">入驻申请详情</div>

    <el-form label-width="160px" class="readonly-form">
      <div class="subsection-title">公司/项目基础信息</div>
      <el-form-item v-if="data.projectName" label="项目名称">
        <span>{{ data.projectName }}</span>
      </el-form-item>
      <el-form-item v-if="data.entityName" label="企业主体">
        <span>{{ data.entityName }}</span>
      </el-form-item>
      <el-form-item v-if="data.establishDate" label="成立时间">
        <span>{{ data.establishDate }}</span>
      </el-form-item>
      <el-form-item v-if="data.logoUrl" label="项目LOGO">
        <el-image :src="data.logoUrl" fit="contain" class="logo-image" />
      </el-form-item>
      <el-form-item v-if="locationText" label="总部所在地">
        <span>{{ locationText }}</span>
      </el-form-item>
      <el-form-item v-if="industriesText" label="所属行业">
        <span>{{ industriesText }}</span>
      </el-form-item>
      <el-form-item v-if="data.oneLiner" label="一句话介绍">
        <span class="multiline">{{ data.oneLiner }}</span>
      </el-form-item>
      <el-form-item v-if="data.intro" label="项目简介">
        <span class="multiline">{{ data.intro }}</span>
      </el-form-item>

      <div class="subsection-title">融资信息</div>
      <el-form-item v-if="data.financingRound" label="当前融资轮次">
        <span>{{ data.financingRound }}</span>
      </el-form-item>
      <el-form-item v-if="data.needFinancing" label="近期需要融资">
        <span>{{ formatFieldValue('needFinancing', data.needFinancing) }}</span>
      </el-form-item>
      <template v-if="data.needFinancing === 'yes'">
        <el-form-item v-if="data.seekingFinancingRound" label="融资轮次">
          <span>{{ data.seekingFinancingRound }}</span>
        </el-form-item>
        <el-form-item v-if="financingAmountText" label="融资金额">
          <span>{{ financingAmountText }}</span>
        </el-form-item>
        <el-form-item v-if="data.equityPercent" label="出让股权">
          <span>{{ data.equityPercent }}%</span>
        </el-form-item>
      </template>
      <el-form-item v-if="data.website" label="官方网址">
        <el-link :href="data.website" target="_blank" type="primary">{{ data.website }}</el-link>
      </el-form-item>
      <el-form-item v-if="data.bpUrl" label="项目BP">
        <el-link :href="data.bpUrl" target="_blank" type="primary">
          {{ data.bpFileName || '查看文件' }}
        </el-link>
      </el-form-item>

      <template v-if="data.teamMembers?.length">
        <div class="subsection-title">核心团队成员</div>
        <div
          v-for="(member, idx) in data.teamMembers"
          :key="idx"
          class="member-block"
        >
          <div v-if="data.teamMembers.length > 1" class="member-title">
            成员 {{ idx + 1 }}
          </div>
          <el-form-item label="姓名">
            <span>{{ member.name || '-' }}</span>
          </el-form-item>
          <el-form-item label="职务">
            <span>{{ member.title || '-' }}</span>
          </el-form-item>
          <el-form-item v-if="member.bio" label="个人经历">
            <span class="multiline">{{ member.bio }}</span>
          </el-form-item>
          <el-form-item v-if="member.avatar" label="头像">
            <el-image :src="member.avatar" fit="cover" class="avatar-image" />
          </el-form-item>
        </div>
      </template>

      <template v-if="certifier">
        <div class="subsection-title">认证信息</div>
        <el-form-item v-if="certifier.realName" label="真实姓名">
          <span>{{ certifier.realName }}</span>
        </el-form-item>
        <el-form-item v-if="certifier.phone" label="手机号码">
          <span>{{ certifier.phone }}</span>
        </el-form-item>
        <el-form-item label="同微信号">
          <span>{{ certifier.sameAsWechat ? '是' : '否' }}</span>
        </el-form-item>
        <el-form-item v-if="certifier.jobType" label="职务类型">
          <span>{{ certifier.jobType }}</span>
        </el-form-item>
        <el-form-item v-if="certifier.jobTitle" label="职位名称">
          <span>{{ certifier.jobTitle }}</span>
        </el-form-item>
        <el-form-item v-if="certifier.responsibility" label="负责方向">
          <span>{{ certifier.responsibility }}</span>
        </el-form-item>
        <el-form-item v-if="certifier.contactEmail" label="联系邮箱">
          <span>{{ certifier.contactEmail }}</span>
        </el-form-item>
        <el-form-item v-if="certifier.identityCertUrl" label="身份认证材料">
          <el-link :href="certifier.identityCertUrl" target="_blank" type="primary">
            查看材料
          </el-link>
        </el-form-item>
      </template>
    </el-form>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { formatFieldValue } from '@/utils/fieldValueLabel'

const props = defineProps({
  data: {
    type: Object,
    default: null
  }
})

const certifier = computed(() => props.data?.certifier || null)

const locationText = computed(() => {
  const data = props.data
  if (!data) return ''
  if (data.country === '海外') {
    return data.overseasLocation || '海外'
  }
  return [data.province, data.city].filter(Boolean).join(' ')
})

const industriesText = computed(() => {
  const list = props.data?.industries
  return Array.isArray(list) && list.length ? list.join('、') : ''
})

const financingAmountText = computed(() => {
  const data = props.data
  if (!data?.financingAmount) return ''
  const currency = data.financingCurrency || '人民币'
  return `${data.financingAmount}万 ${currency}`
})
</script>

<style scoped lang="scss">
.onboard-apply-data {
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

.subsection-title {
  margin: 8px 0 12px;
  padding-top: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  border-top: 1px solid #e4e7ed;

  &:first-of-type {
    margin-top: 0;
    padding-top: 0;
    border-top: none;
  }
}

.member-block {
  margin-bottom: 4px;
  padding: 12px 0 4px;
  border-top: 1px solid #e4e7ed;
}

.member-title {
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

.logo-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.avatar-image {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: 1px solid #ebeef5;
}
</style>
