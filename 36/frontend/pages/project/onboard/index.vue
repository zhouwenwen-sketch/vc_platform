<template>
  <view class="onboard-page">
    <scroll-view
      scroll-y
      class="onboard-scroll"
      :style="{ height: scrollHeight + 'px' }"
      :show-scrollbar="false"
    >
      <OnboardFormHeader />
      <ApplyFormStepper :current="step" />

      <view v-if="step === 1" class="step-body">
        <text class="section-title">公司/项目基础信息</text>
        <view class="tips-box">
          <text class="tips-line">
            请根据企业官网、BP等官方介绍材料进行客观、完整、准确且具体的项目信息描述，以避免投资人和潜在合作方无法了解您的项目；

            若填写的信息过于简短、项目介绍不够具体、项目信息过于夸大或失实，将导致无法入驻成功，请务必认真填写；
          </text>
        </view>

        <OnboardProjectNameField
          ref="nameFieldRef"
          v-model="form.projectName"
          @name-valid="onNameValid"
          @name-duplicate="onNameDuplicate"
          @name-reset="onNameReset"
          @blur-empty="onBlurEmpty"
        />

        <view class="field">
          <view class="label-row">
            <text class="required">*</text>
            <text class="label">所属行业</text>
          </view>
          <view class="picker-input" @click="openIndustryPicker">
            <text class="picker-value" :class="{ empty: !industryDisplay }">
              {{ industryDisplay || '最多选择两项' }}
            </text>
            <u-icon name="arrow-right" color="#ccc" size="28rpx" />
          </view>
        </view>

        <OnboardStep1Fields v-if="formExpanded" v-model="form" />
      </view>

      <view v-else-if="step === 2" class="step-body">
        <OnboardStep2Fields v-model="form.teamMembers" />
      </view>

      <view v-else class="step-body">
        <OnboardStep3Fields ref="step3Ref" v-model="form" />
      </view>
    </scroll-view>

    <view class="onboard-footer">
      <view class="footer-actions">
        <u-button
          v-if="step > 1"
          text="上一步"
          custom-style="flex:1;height:88rpx;border-radius:48rpx"
          @click="step -= 1"
        />
        <u-button
          type="primary"
          :text="step < 3 ? '下一步' : '提交'"
          :loading="submitting"
          custom-style="flex:1;background:#78B9B1;border-color:#78B9B1;height:88rpx;border-radius:48rpx"
          @click="onNext"
        />
      </view>
    </view>

    <ApplyFormPickerSheet
      :show="industryPickerShow"
      title="所属行业（最多2项）"
      :options="INDUSTRY_OPTIONS"
      :selected="tempIndustries"
      @close="industryPickerShow = false"
      @confirm="confirmIndustry"
      @toggle="toggleIndustry"
    />
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import OnboardFormHeader from '@/components/OnboardFormHeader/OnboardFormHeader.vue'
import ApplyFormStepper from '@/components/ApplyFormStepper/ApplyFormStepper.vue'
import OnboardProjectNameField from '@/components/OnboardProjectNameField/OnboardProjectNameField.vue'
import OnboardStep1Fields from '@/components/OnboardStep1Fields/OnboardStep1Fields.vue'
import OnboardStep2Fields from '@/components/OnboardStep2Fields/OnboardStep2Fields.vue'
import OnboardStep3Fields from '@/components/OnboardStep3Fields/OnboardStep3Fields.vue'
import ApplyFormPickerSheet from '@/components/ApplyFormPickerSheet/ApplyFormPickerSheet.vue'
import { INDUSTRY_OPTIONS } from '@/utils/projectFilterData.js'
import { useUserStore } from '@/store/user.js'
import { submitProjectOnboard } from '@/api/onboard.js'
import { prepareOnboardPayload } from '@/utils/onboardSubmit.js'
import { goOnboardResult, guardOnboardPage } from '@/utils/projectOnboardGuard.js'
import { validateEntityName } from '@/api/company.js'
import { SUCCESS_CODE } from '@/utils/request.js'
import { setUserInfo } from '@/utils/storage.js'

const userStore = useUserStore()

const step = ref(1)
const formExpanded = ref(false)
const scrollHeight = ref(uni.getSystemInfoSync().windowHeight - 100)
const industryPickerShow = ref(false)
const tempIndustries = ref([])
const nameFieldRef = ref(null)
const step3Ref = ref(null)
const submitting = ref(false)

function createEmptyForm() {
  return {
    projectName: '',
    entityName: '',
    establishDate: '',
    logoUrl: '',
    country: '中国',
    province: '',
    city: '',
    overseasLocation: '',
    oneLiner: '',
    intro: '',
    industries: [],
    financingRound: '',
    needFinancing: 'no',
    seekingFinancingRound: '',
    financingAmount: '',
    financingCurrency: '人民币',
    equityPercent: '',
    website: '',
    bpUrl: '',
    bpFileName: '',
    teamMembers: [],
    realName: '',
    phone: '',
    sameAsWechat: false,
    jobType: '',
    jobTitle: '',
    responsibility: '',
    contactEmail: '',
    identityCertUrl: ''
  }
}

const form = reactive(createEmptyForm())

const industryDisplay = computed(() => form.industries.join('、'))

function updateScrollHeight() {
  nextTick(() => {
    const query = uni.createSelectorQuery()
    query.select('.onboard-footer').boundingClientRect()
    query.exec((res) => {
      const footer = res?.[0]
      const { windowHeight } = uni.getSystemInfoSync()
      const footerH = footer?.height || 0
      scrollHeight.value = Math.max(windowHeight - footerH, 200)
    })
  })
}

function resetExtendedFields(keepIndustries = false) {
  const name = form.projectName
  const industries = keepIndustries ? [...form.industries] : []
  Object.assign(form, createEmptyForm(), { projectName: name, industries })
}

function openIndustryPicker() {
  tempIndustries.value = [...form.industries]
  industryPickerShow.value = true
}

function toggleIndustry(item) {
  const idx = tempIndustries.value.indexOf(item)
  if (idx >= 0) {
    tempIndustries.value.splice(idx, 1)
    return
  }
  if (tempIndustries.value.length >= 2) {
    uni.showToast({ title: '最多选择两项', icon: 'none' })
    return
  }
  tempIndustries.value.push(item)
}

function confirmIndustry() {
  form.industries = [...tempIndustries.value]
  industryPickerShow.value = false
}

function onNameValid(name) {
  form.projectName = name
  formExpanded.value = true
  updateScrollHeight()
}

function onNameDuplicate() {
  formExpanded.value = false
  resetExtendedFields(true)
}

function onNameReset() {
  formExpanded.value = false
}

function onBlurEmpty() {
  formExpanded.value = false
  resetExtendedFields(false)
}

async function validateStep1() {
  if (!form.projectName.trim()) {
    uni.showToast({ title: '请填写项目名称', icon: 'none' })
    return false
  }
  const nameOk = await nameFieldRef.value?.validateName?.()
  if (!nameOk) return false
  if (!form.entityName.trim()) {
    uni.showToast({ title: '请填写企业主体', icon: 'none' })
    return false
  }
  const entityOk = await validateEntityName(form.entityName)
  if (!entityOk) {
    uni.showToast({ title: '请选择有效的企业主体', icon: 'none' })
    return false
  }
  if (!form.establishDate) {
    uni.showToast({ title: '请选择成立时间', icon: 'none' })
    return false
  }
  if (!form.logoUrl) {
    uni.showToast({ title: '请上传项目LOGO', icon: 'none' })
    return false
  }
  if (form.country === '海外') {
    if (!form.overseasLocation.trim()) {
      uni.showToast({ title: '请填写海外总部所在地', icon: 'none' })
      return false
    }
  } else if (!form.province || !form.city) {
    uni.showToast({ title: '请选择总部所在地', icon: 'none' })
    return false
  }
  if (!form.oneLiner.trim()) {
    uni.showToast({ title: '请填写一句话介绍', icon: 'none' })
    return false
  }
  if (!form.intro.trim()) {
    uni.showToast({ title: '请填写项目简介', icon: 'none' })
    return false
  }
  if (!form.industries.length) {
    uni.showToast({ title: '请选择所属行业', icon: 'none' })
    return false
  }
  if (!form.financingRound) {
    uni.showToast({ title: '请选择当前融资轮次', icon: 'none' })
    return false
  }
  if (!form.needFinancing) {
    uni.showToast({ title: '请选择近期是否需要融资', icon: 'none' })
    return false
  }
  if (form.needFinancing === 'yes') {
    if (!form.seekingFinancingRound) {
      uni.showToast({ title: '请选择融资轮次', icon: 'none' })
      return false
    }
    const amount = String(form.financingAmount || '').trim()
    if (amount && (!/^\d+(\.\d+)?$/.test(amount) || Number(amount) <= 0)) {
      uni.showToast({ title: '融资金额需为正数', icon: 'none' })
      return false
    }
    const equity = String(form.equityPercent || '').trim()
    if (equity && (!/^\d+(\.\d+)?$/.test(equity) || Number(equity) <= 0 || Number(equity) > 100)) {
      uni.showToast({ title: '出让股权需在 0-100 之间', icon: 'none' })
      return false
    }
  }
  const website = form.website.trim()
  if (website && !/^https?:\/\/.+/i.test(website)) {
    uni.showToast({ title: '网址需以http或https开头', icon: 'none' })
    return false
  }
  return true
}

function validateStep2() {
  if (!form.teamMembers.length) {
    uni.showToast({ title: '请至少添加一位核心成员', icon: 'none' })
    return false
  }
  return true
}

function validateStep3() {
  return step3Ref.value?.validate?.() ?? false
}

async function onSubmit() {
  if (!validateStep3()) return
  submitting.value = true
  uni.showLoading({ title: '正在上传文件...', mask: true })
  try {
    const payload = await prepareOnboardPayload(form)
    uni.hideLoading()
    uni.showLoading({ title: '正在提交...', mask: true })
    const res = await submitProjectOnboard(payload)
    uni.hideLoading()
    if (res.code === SUCCESS_CODE) {
      const projectId = res.data?.projectId || ''
      const isApproved = !!projectId
      if (userStore.userInfo) {
        const next = {
          ...userStore.userInfo,
          authStatus: isApproved ? 'approved' : 'pending',
          role: 'entrepreneur'
        }
        userStore.userInfo = next
        setUserInfo(next)
      }
      goOnboardResult()
      return
    }
    uni.showToast({ title: res.message || '提交失败', icon: 'none' })
  } catch (e) {
    uni.hideLoading()
    const msg = e?.message || '提交失败，请稍后重试'
    uni.showToast({ title: msg, icon: 'none' })
  } finally {
    submitting.value = false
  }
}

function onNext() {
  if (step.value === 1) {
    validateStep1().then((ok) => {
      if (!ok) return
      if (step.value < 3) step.value += 1
    })
    return
  }
  if (step.value === 2 && !validateStep2()) return
  if (step.value < 3) {
    if (step.value === 2 && !form.phone && userStore.userInfo?.phone) {
      form.phone = userStore.userInfo.phone
    }
    step.value += 1
    return
  }
  onSubmit()
}

watch(formExpanded, updateScrollHeight)
watch(step, updateScrollHeight)

onLoad(async () => {
  const ok = await guardOnboardPage()
  if (!ok) return
  if (userStore.userInfo?.phone) {
    form.phone = userStore.userInfo.phone
  }
})

onMounted(updateScrollHeight)
</script>

<style scoped lang="scss">
@import '@/styles/apply-form.scss';

.onboard-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background: #fff;
  box-sizing: border-box;
}

.onboard-scroll {
  flex-shrink: 0;
  padding: 24rpx 32rpx;
  box-sizing: border-box;
}

.onboard-footer {
  flex-shrink: 0;
  background: #fff;
  border-top: 1rpx solid #f0f0f0;
  padding: 16rpx 32rpx;
  box-sizing: border-box;

  /* #ifndef H5 */
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  /* #endif */
}

.footer-actions {
  display: flex;
  gap: 16rpx;
}

.step-body {
  padding-bottom: 8rpx;
}

.tips-box {
  margin-bottom: 32rpx;
  padding: 24rpx;
  background: #eef5ff;
  border-radius: 8rpx;
}

.tips-line {
  display: block;
  font-size: 26rpx;
  color: #666;
  line-height: 1.65;
}
</style>
