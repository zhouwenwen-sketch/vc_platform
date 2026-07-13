<template>
  <view class="step3-fields">
    <ApplySectionBar title="认证信息" />

    <view class="tips-box">
      <text class="tips-line">
        认证人是申请入驻项目的企业成员之一，其联系方式将用于提升平台对接效率、跟进后续合作等场景，平台会保障您的隐私安全。
      </text>
    </view>

    <view class="field">
      <view class="label-row">
        <text class="required">*</text>
        <text class="label">真实姓名</text>
      </view>
      <input
        v-model="form.realName"
        class="input"
        :class="{ 'has-error': errors.realName }"
        placeholder="请输入真实姓名"
        placeholder-class="placeholder"
        @input="clearError('realName')"
      />
      <text v-if="errors.realName" class="field-error">{{ errors.realName }}</text>
    </view>

    <view class="field">
      <view class="label-row">
        <text class="required">*</text>
        <text class="label">手机号码</text>
      </view>
      <view class="input input-readonly">
        <text class="readonly-value">{{ maskedPhone }}</text>
      </view>
    </view>

    <view class="checkbox-row" @click="form.sameAsWechat = !form.sameAsWechat">
      <view class="checkbox" :class="{ checked: form.sameAsWechat }">
        <u-icon v-if="form.sameAsWechat" name="checkmark" color="#fff" size="24rpx" />
      </view>
      <text class="checkbox-text">同微信号</text>
    </view>

    <view class="field">
      <view class="label-row">
        <text class="required">*</text>
        <text class="label">职务类型</text>
      </view>
      <view
        class="picker-input"
        :class="{ 'has-error': errors.jobType }"
        @click="jobTypePickerShow = true"
      >
        <text class="picker-value" :class="{ empty: !form.jobType }">
          {{ form.jobType || '请选择职务类型' }}
        </text>
        <u-icon name="arrow-right" color="#ccc" size="28rpx" />
      </view>
      <text v-if="errors.jobType" class="field-error">{{ errors.jobType }}</text>
    </view>

    <view class="field">
      <view class="label-row">
        <text class="required">*</text>
        <text class="label">职位名称</text>
      </view>
      <input
        v-model="form.jobTitle"
        class="input"
        :class="{ 'has-error': errors.jobTitle }"
        placeholder="请输入职位名称"
        placeholder-class="placeholder"
        @input="clearError('jobTitle')"
      />
      <text v-if="errors.jobTitle" class="field-error">{{ errors.jobTitle }}</text>
    </view>

    <view class="field">
      <view class="label-row">
        <text class="required">*</text>
        <text class="label">负责方向</text>
      </view>
      <view
        class="picker-input"
        :class="{ 'has-error': errors.responsibility }"
        @click="responsibilityPickerShow = true"
      >
        <text class="picker-value" :class="{ empty: !form.responsibility }">
          {{ form.responsibility || '请选择负责方向' }}
        </text>
        <u-icon name="arrow-right" color="#ccc" size="28rpx" />
      </view>
      <text v-if="errors.responsibility" class="field-error">{{ errors.responsibility }}</text>
    </view>

    <view class="field">
      <view class="label-row">
        <text class="label">联系邮箱</text>
      </view>
      <input
        v-model="form.contactEmail"
        class="input"
        placeholder="请输入您的工作邮箱"
        placeholder-class="placeholder"
      />
    </view>

    <view class="field">
      <view class="label-row">
        <text class="required">*</text>
        <text class="label">身份认证</text>
      </view>
      <view class="upload-row">
        <view
          class="upload-box"
          :class="{ 'has-error': errors.identityCertUrl }"
          @click="chooseIdentityImage"
        >
          <image
            v-if="form.identityCertUrl"
            :src="form.identityCertUrl"
            class="upload-preview"
            mode="aspectFill"
          />
          <u-icon v-else name="plus" color="#ccc" size="48rpx" />
        </view>
        <text class="upload-tip">
          请上传名片/工牌等能证明您在项目中任职的材料，支持jpg/png，不超过5MB
        </text>
      </view>
      <text v-if="errors.identityCertUrl" class="field-error">{{ errors.identityCertUrl }}</text>
    </view>

    <u-picker
      :show="jobTypePickerShow"
      :columns="[ONBOARD_JOB_TYPE_OPTIONS]"
      title="职务类型"
      @confirm="onJobTypeConfirm"
      @cancel="jobTypePickerShow = false"
      @close="jobTypePickerShow = false"
    />

    <u-picker
      :show="responsibilityPickerShow"
      :columns="[ONBOARD_RESPONSIBILITY_OPTIONS]"
      title="负责方向"
      @confirm="onResponsibilityConfirm"
      @cancel="responsibilityPickerShow = false"
      @close="responsibilityPickerShow = false"
    />
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import ApplySectionBar from '@/components/ApplySectionBar/ApplySectionBar.vue'
import { useUserStore } from '@/store/user.js'
import {
  ONBOARD_JOB_TYPE_OPTIONS,
  ONBOARD_RESPONSIBILITY_OPTIONS
} from '@/utils/onboardCertData.js'

const form = defineModel({ type: Object, required: true })

const userStore = useUserStore()
const jobTypePickerShow = ref(false)
const responsibilityPickerShow = ref(false)
const errors = reactive({
  realName: '',
  jobType: '',
  jobTitle: '',
  responsibility: '',
  identityCertUrl: ''
})

const maskedPhone = computed(() => {
  const phone = form.value.phone || ''
  if (phone.length === 11) {
    return `${phone.slice(0, 3)}****${phone.slice(7)}`
  }
  return phone || '—'
})

onMounted(() => {
  if (!form.value.phone && userStore.userInfo?.phone) {
    form.value.phone = userStore.userInfo.phone
  }
})

function clearError(key) {
  errors[key] = ''
}

function onJobTypeConfirm(e) {
  const val = e.value?.[0]
  if (val != null) {
    form.value.jobType = val
    clearError('jobType')
  }
  jobTypePickerShow.value = false
}

function onResponsibilityConfirm(e) {
  const val = e.value?.[0]
  if (val != null) {
    form.value.responsibility = val
    clearError('responsibility')
  }
  responsibilityPickerShow.value = false
}

function chooseIdentityImage() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success(res) {
      const file = res.tempFiles?.[0] || { path: res.tempFilePaths?.[0], size: 0 }
      const path = file.path || res.tempFilePaths?.[0]
      if (!path) return
      if (file.size && file.size > 5 * 1024 * 1024) {
        uni.showToast({ title: '图片大小不能超过5MB', icon: 'none' })
        return
      }
      form.value.identityCertUrl = path
      clearError('identityCertUrl')
    }
  })
}

function validate() {
  let ok = true
  errors.realName = ''
  errors.jobType = ''
  errors.jobTitle = ''
  errors.responsibility = ''
  errors.identityCertUrl = ''

  if (!form.value.realName?.trim()) {
    errors.realName = '未填写真实姓名'
    ok = false
  }
  if (!form.value.phone?.trim()) {
    uni.showToast({ title: '未获取到登录手机号', icon: 'none' })
    ok = false
  }
  if (!form.value.jobType) {
    errors.jobType = '未选择职务类型'
    ok = false
  }
  if (!form.value.jobTitle?.trim()) {
    errors.jobTitle = '未填写职位名称'
    ok = false
  }
  if (!form.value.responsibility) {
    errors.responsibility = '未选择负责方向'
    ok = false
  }
  if (!form.value.identityCertUrl) {
    errors.identityCertUrl = '请上传身份认证材料'
    ok = false
  }

  const email = form.value.contactEmail?.trim() || ''
  if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    uni.showToast({ title: '请填写正确的邮箱格式', icon: 'none' })
    ok = false
  }

  return ok
}

defineExpose({ validate })
</script>

<style scoped lang="scss">
@import '@/styles/apply-form.scss';

.step3-fields {
  margin: 0 -32rpx;
}

.tips-box {
  margin: 0 32rpx 32rpx;
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

.input-readonly {
  display: flex;
  align-items: center;
}

.readonly-value {
  font-size: 28rpx;
  color: #333;
}

.has-error {
  border: 2rpx solid #e53935;
  box-sizing: border-box;
}

.field-error {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #e53935;
  line-height: 1.5;
}

.checkbox-row {
  display: flex;
  align-items: center;
  margin: -16rpx 32rpx 32rpx;
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

.field {
  padding: 0 32rpx;
}

.upload-row {
  display: flex;
  align-items: flex-start;
  gap: 24rpx;
}

.upload-box {
  width: 160rpx;
  height: 160rpx;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fb;
  border-radius: 12rpx;
  overflow: hidden;
}

.upload-preview {
  width: 100%;
  height: 100%;
}

.upload-tip {
  flex: 1;
  font-size: 24rpx;
  color: #999;
  line-height: 1.6;
  padding-top: 8rpx;
}
</style>
