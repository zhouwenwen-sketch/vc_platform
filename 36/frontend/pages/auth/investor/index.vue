<template>
  <view class="apply-page">
    <scroll-view scroll-y class="form-scroll" :show-scrollbar="false">
      <view class="section-head">
        <text class="section-title">认证资料</text>
        <text class="section-tip">
          仅接受投资人申请，个人投资人/FA/孵化器/产业园请勿申请，
          <text class="link" @click="onRulesTap">查看详细审核规则 {{ LINK_ARROW }}</text>
        </text>
      </view>

      <view class="field">
        <view class="label-row">
          <text class="required">*</text>
          <text class="label">机构简称</text>
        </view>
        <input
          v-model="form.company"
          class="input"
          placeholder="您所在机构的简称/机构主体/基金管理人"
          placeholder-class="placeholder"
        />
      </view>

      <view class="field">
        <view class="label-row">
          <text class="label">公司主体</text>
        </view>
        <input
          v-model="form.companyEntity"
          class="input"
          placeholder="您所在公司的工商主体"
          placeholder-class="placeholder"
        />
      </view>

      <view class="field">
        <view class="label-row">
          <text class="required">*</text>
          <text class="label">职位</text>
        </view>
        <input
          v-model="form.position"
          class="input"
          placeholder="您的职位"
          placeholder-class="placeholder"
        />
      </view>

      <view class="field">
        <view class="label-row">
          <text class="required">*</text>
          <text class="label">姓名</text>
        </view>
        <input
          v-model="form.name"
          class="input"
          placeholder="您的真实姓名"
          placeholder-class="placeholder"
        />
      </view>

      <view class="field">
        <view class="label-row">
          <text class="label">联系手机</text>
        </view>
        <input
          v-model="form.phone"
          class="input"
          type="number"
          maxlength="11"
          placeholder="您的联系手机"
          placeholder-class="placeholder"
        />
      </view>

      <view class="field">
        <view class="label-row">
          <text class="required">*</text>
          <text class="label">企业邮箱</text>
        </view>
        <input
          v-model="form.email"
          class="input"
          placeholder="您的企业邮箱，用于接收项目方资料"
          placeholder-class="placeholder"
        />
        <view class="checkbox-row" @click="form.emailSubscribe = !form.emailSubscribe">
          <view class="checkbox" :class="{ checked: form.emailSubscribe }">
            <u-icon v-if="form.emailSubscribe" name="checkmark" color="#fff" size="24rpx" />
          </view>
          <text class="checkbox-text">邮件订阅优质项目、创投资讯</text>
        </view>
      </view>

      <view class="field" @click="openPicker('areas')">
        <view class="label-row">
          <text class="required">*</text>
          <text class="label">关注领域</text>
        </view>
        <view class="picker-input">
          <text :class="form.focusAreas.length ? 'picker-value' : 'placeholder'">
            {{ focusAreasText }}
          </text>
          <u-icon name="arrow-right" color="#ccc" size="28rpx" />
        </view>
      </view>

      <view class="field" @click="openPicker('rounds')">
        <view class="label-row">
          <text class="required">*</text>
          <text class="label">关注轮次</text>
        </view>
        <view class="picker-input">
          <text :class="form.focusRounds.length ? 'picker-value' : 'placeholder'">
            {{ focusRoundsText }}
          </text>
          <u-icon name="arrow-right" color="#ccc" size="28rpx" />
        </view>
      </view>

      <view class="field">
        <view class="label-row">
          <text class="label">其他补充</text>
        </view>
        <view class="textarea-wrap">
          <textarea
            v-model="form.remark"
            class="textarea"
            maxlength="50"
            placeholder="可补充您关注的细分赛道，便于发现更精准的项目"
            placeholder-class="placeholder"
          />
          <text class="char-count">{{ form.remark.length }}/50</text>
        </view>
      </view>

      <view class="submit-notes">
        <text class="notes-title">提交说明</text>
        <text class="notes-item">1. 邮箱须为企业邮箱，用于接收项目方资料；</text>
        <text class="notes-item">2. 申请结果会在1个工作日内通过短信通知您；</text>
        <text class="notes-item">3. 如果您是FA、孵化器或企业服务机构，请联系平台运营提交需求。</text>
      </view>

      <view class="submit-wrap">
        <u-button
          type="primary"
          text="提交申请"
          :loading="submitting"
          custom-style="background:#5A9A92;border-color:#5A9A92;border-radius:48rpx;height:88rpx"
          @click="onSubmit"
        />
      </view>
    </scroll-view>

    <ApplyFormPickerSheet
      :show="pickerShow"
      :title="pickerTitle"
      :options="pickerOptions"
      :selected="tempSelected"
      @close="pickerShow = false"
      @confirm="confirmPicker"
      @toggle="togglePickerItem"
    />
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { submitAuth } from '@/api/user.js'
import { useUserStore } from '@/store/user.js'
import { SUCCESS_CODE } from '@/utils/request.js'
import { LINK_ARROW } from '@/utils/linkText.js'
import { guardInvestorAuthPage } from '@/utils/authGuard.js'
import { isValidPhone } from '@/utils/phone.js'
import ApplyFormPickerSheet from '@/components/ApplyFormPickerSheet/ApplyFormPickerSheet.vue'
import { INDUSTRY_OPTIONS, ROUND_OPTIONS } from '@/utils/projectFilterData.js'
import { setUserInfo } from '@/utils/storage.js'

const userStore = useUserStore()

const form = ref({
  company: '',
  companyEntity: '',
  position: '',
  name: '',
  phone: '',
  email: '',
  emailSubscribe: true,
  focusAreas: [],
  focusRounds: [],
  remark: ''
})

const submitting = ref(false)
const pickerShow = ref(false)
const pickerType = ref('areas')
const tempSelected = ref([])

const focusAreasText = computed(() =>
  form.value.focusAreas.length ? form.value.focusAreas.join('、') : '请选择领域'
)
const focusRoundsText = computed(() =>
  form.value.focusRounds.length ? form.value.focusRounds.join('、') : '请选择轮次'
)
const pickerTitle = computed(() =>
  pickerType.value === 'areas' ? '选择关注领域' : '选择关注轮次'
)
const pickerOptions = computed(() =>
  pickerType.value === 'areas' ? INDUSTRY_OPTIONS : ROUND_OPTIONS
)

onLoad(() => {
  guardInvestorAuthPage()
})

function onRulesTap() {
  uni.navigateTo({ url: '/pages/auth/investor/rules' })
}

function openPicker(type) {
  pickerType.value = type
  tempSelected.value = [
    ...(type === 'areas' ? form.value.focusAreas : form.value.focusRounds)
  ]
  pickerShow.value = true
}

function togglePickerItem(item) {
  const idx = tempSelected.value.indexOf(item)
  if (idx >= 0) {
    tempSelected.value.splice(idx, 1)
  } else {
    tempSelected.value.push(item)
  }
}

function confirmPicker() {
  if (pickerType.value === 'areas') {
    form.value.focusAreas = [...tempSelected.value]
  } else {
    form.value.focusRounds = [...tempSelected.value]
  }
  pickerShow.value = false
}

function validateEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

function onSubmit() {
  const f = form.value
  if (!f.company?.trim()) {
    uni.showToast({ title: '请填写机构简称', icon: 'none' })
    return
  }
  if (!f.position?.trim()) {
    uni.showToast({ title: '请填写职位', icon: 'none' })
    return
  }
  if (!f.name?.trim()) {
    uni.showToast({ title: '请填写姓名', icon: 'none' })
    return
  }
  if (!f.email?.trim()) {
    uni.showToast({ title: '请填写企业邮箱', icon: 'none' })
    return
  }
  if (!validateEmail(f.email.trim())) {
    uni.showToast({ title: '请填写正确的邮箱格式', icon: 'none' })
    return
  }
  const phone = f.phone?.trim() || ''
  if (phone && !isValidPhone(phone)) {
    uni.showToast({ title: '请填写正确的手机号', icon: 'none' })
    return
  }
  if (!f.focusAreas.length) {
    uni.showToast({ title: '请选择关注领域', icon: 'none' })
    return
  }
  if (!f.focusRounds.length) {
    uni.showToast({ title: '请选择关注轮次', icon: 'none' })
    return
  }

  submitting.value = true
  submitAuth({
    type: 'investor',
    name: f.name.trim(),
    company: f.company.trim(),
    companyEntity: f.companyEntity?.trim() || '',
    position: f.position.trim(),
    phone,
    email: f.email.trim(),
    emailSubscribe: f.emailSubscribe,
    focusAreas: f.focusAreas.join(','),
    focusRounds: f.focusRounds.join(','),
    remark: f.remark?.trim() || ''
  })
    .then((res) => {
      if (res.code === SUCCESS_CODE) {
        uni.showToast({ title: '提交成功，请等待审核', icon: 'success' })
        if (userStore.userInfo) {
          const next = { ...userStore.userInfo, authStatus: 'pending' }
          userStore.userInfo = next
          setUserInfo(next)
        }
        setTimeout(() => uni.navigateBack(), 600)
      }
    })
    .finally(() => {
      submitting.value = false
    })
}
</script>

<style scoped lang="scss">
@import '@/styles/apply-form.scss';

.checkbox-row {
  display: flex;
  align-items: center;
  margin-top: 20rpx;
  gap: 12rpx;
}

.checkbox {
  width: 36rpx;
  height: 36rpx;
  border: 2rpx solid #ccc;
  border-radius: 6rpx;
  display: flex;
  align-items: center;
  justify-content: center;

  &.checked {
    background: #78B9B1;
    border-color: #78B9B1;
  }
}

.checkbox-text {
  font-size: 26rpx;
  color: #666;
}
</style>
