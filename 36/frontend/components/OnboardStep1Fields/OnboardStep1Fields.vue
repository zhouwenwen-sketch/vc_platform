<template>
  <view class="step1-fields">
    <OnboardEntityField
      v-model="form.entityName"
      required
      label="企业主体"
      placeholder="请输入您的企业名称"
    />

    <ApplyFormDateField
      v-model="form.establishDate"
      required
      year-month-only
      label="成立时间"
      placeholder="请选择公司成立时间"
      :max-date="today"
    />

    <ApplyFormUploadImage v-model="form.logoUrl" required label="项目LOGO" />

    <ApplyFormLocationField
      v-model:country="form.country"
      v-model:province="form.province"
      v-model:city="form.city"
      v-model:overseas-location="form.overseasLocation"
      required
      label="总部所在地"
    />

    <ApplyFormTextarea
      v-model="form.oneLiner"
      required
      label="一句话介绍"
      placeholder="请对公司行业定位+产品技术与服务进行一句话客观提炼总结，如：联贝科创传媒的一句话简介为新经济资讯及服务提供商，请勿进行夸张描述"
      :maxlength="120"
    />

    <view class="field">
      <view class="label-row">
        <text class="required">*</text>
        <text class="label">项目简介</text>
      </view>
      <view class="textarea-wrap textarea-wrap-lg">
        <textarea
          v-model="form.intro"
          class="textarea textarea-lg"
          maxlength="800"
          placeholder="请对公司所属行业及市场定位、主营业务、产品技术与服务、目标客户群体、运营数据等信息进行客观提炼总结描述，请勿进行夸张描述"
          placeholder-class="placeholder"
        />
        <text class="char-count">{{ (form.intro || '').length }}/800</text>
      </view>
    </view>

    <view class="field">
      <view class="label-row">
        <text class="required">*</text>
        <text class="label">当前融资轮次</text>
      </view>
      <view class="picker-input" @click="currentRoundPickerShow = true">
        <text class="picker-value" :class="{ empty: !form.financingRound }">
          {{ form.financingRound || '选择融资轮次' }}
        </text>
        <u-icon name="arrow-right" color="#ccc" size="28rpx" />
      </view>
    </view>

    <view class="field">
      <view class="label-row">
        <text class="required">*</text>
        <text class="label">近期需要融资</text>
      </view>
      <view class="radio-group-row">
        <u-radio-group v-model="form.needFinancing" placement="row">
          <u-radio name="no" label="否" active-color="#78B9B1" />
          <u-radio name="yes" label="是" active-color="#78B9B1" />
        </u-radio-group>
      </view>
    </view>

    <template v-if="form.needFinancing === 'yes'">
      <view class="field">
        <view class="label-row">
          <text class="required">*</text>
          <text class="label">融资轮次</text>
        </view>
        <view class="picker-input" @click="seekingRoundPickerShow = true">
          <text class="picker-value" :class="{ empty: !form.seekingFinancingRound }">
            {{ form.seekingFinancingRound || '选择融资轮次' }}
          </text>
          <u-icon name="arrow-right" color="#ccc" size="28rpx" />
        </view>
      </view>

      <view class="field">
        <view class="label-row">
          <text class="label">融资金额</text>
        </view>
        <view class="amount-row">
          <view class="amount-input-wrap">
            <input
              v-model="form.financingAmount"
              class="input amount-input"
              type="digit"
              placeholder="请填写金额数"
              placeholder-class="placeholder"
            />
            <text class="amount-suffix">万</text>
          </view>
          <view class="currency-cell" @click="currencyPickerShow = true">
            <view class="picker-input currency-picker">
              <text class="picker-value">{{ form.financingCurrency || '人民币' }}</text>
              <u-icon name="arrow-right" color="#ccc" size="28rpx" />
            </view>
          </view>
        </view>
      </view>

      <view class="field">
        <view class="label-row">
          <text class="label">出让股权</text>
        </view>
        <view class="equity-input-wrap">
          <input
            v-model="form.equityPercent"
            class="input equity-input"
            type="digit"
            placeholder="请填写股权比"
            placeholder-class="placeholder"
          />
          <text class="equity-suffix">%</text>
        </view>
      </view>

      <text class="financing-tip">
        *平台将对融资项目提供额外服务，或推荐给相关领域认证投资人，上传BP、准确填写联系人能有效提升对接率
      </text>
    </template>

    <ApplyFormInput
      v-model="form.website"
      label="官方网址（非必填）"
      placeholder="请输入网址，以http或https开头"
    />

    <ApplyFormUploadFile
      v-model="form.bpUrl"
      v-model:file-name="form.bpFileName"
      label="项目BP（非必传）"
    />

    <OnboardProjectPreview :form="form" />

    <u-picker
      :show="currentRoundPickerShow"
      :columns="[EVENT_ROUND_OPTIONS]"
      title="当前融资轮次"
      @confirm="onCurrentRoundConfirm"
      @cancel="currentRoundPickerShow = false"
      @close="currentRoundPickerShow = false"
    />

    <u-picker
      :show="seekingRoundPickerShow"
      :columns="[ONBOARD_SEEKING_ROUND_OPTIONS]"
      title="融资轮次"
      @confirm="onSeekingRoundConfirm"
      @cancel="seekingRoundPickerShow = false"
      @close="seekingRoundPickerShow = false"
    />

    <u-picker
      :show="currencyPickerShow"
      :columns="[ONBOARD_CURRENCY_OPTIONS]"
      title="融资金额单位"
      @confirm="onCurrencyConfirm"
      @cancel="currencyPickerShow = false"
      @close="currencyPickerShow = false"
    />
  </view>
</template>

<script setup>
import { ref, watch } from 'vue'
import ApplyFormInput from '@/components/ApplyFormInput/ApplyFormInput.vue'
import OnboardEntityField from '@/components/OnboardEntityField/OnboardEntityField.vue'
import ApplyFormDateField from '@/components/ApplyFormDateField/ApplyFormDateField.vue'
import ApplyFormUploadImage from '@/components/ApplyFormUploadImage/ApplyFormUploadImage.vue'
import ApplyFormLocationField from '@/components/ApplyFormLocationField/ApplyFormLocationField.vue'
import ApplyFormTextarea from '@/components/ApplyFormTextarea/ApplyFormTextarea.vue'
import ApplyFormUploadFile from '@/components/ApplyFormUploadFile/ApplyFormUploadFile.vue'
import OnboardProjectPreview from '@/components/OnboardProjectPreview/OnboardProjectPreview.vue'
import {
  EVENT_ROUND_OPTIONS,
  ONBOARD_SEEKING_ROUND_OPTIONS,
  ONBOARD_CURRENCY_OPTIONS
} from '@/utils/financingFilterData.js'

const form = defineModel({ type: Object, required: true })

const today = new Date().toISOString().slice(0, 10)
const currentRoundPickerShow = ref(false)
const seekingRoundPickerShow = ref(false)
const currencyPickerShow = ref(false)

function clearSeekingFinancingFields() {
  form.value.seekingFinancingRound = ''
  form.value.financingAmount = ''
  form.value.financingCurrency = '人民币'
  form.value.equityPercent = ''
}

watch(
  () => form.value.needFinancing,
  (val) => {
    if (val !== 'yes') {
      clearSeekingFinancingFields()
    }
  }
)

function onCurrentRoundConfirm(e) {
  const val = e.value?.[0]
  if (val != null) {
    form.value.financingRound = val
  }
  currentRoundPickerShow.value = false
}

function onSeekingRoundConfirm(e) {
  const val = e.value?.[0]
  if (val != null) {
    form.value.seekingFinancingRound = val
  }
  seekingRoundPickerShow.value = false
}

function onCurrencyConfirm(e) {
  const val = e.value?.[0]
  if (val != null) {
    form.value.financingCurrency = val
  }
  currencyPickerShow.value = false
}
</script>

<style scoped lang="scss">
@import '@/styles/apply-form.scss';

.textarea-wrap-lg {
  padding-bottom: 48rpx;
}

.textarea-lg {
  height: 240rpx;
  min-height: 240rpx;
}

.amount-row {
  display: flex;
  gap: 16rpx;
}

.amount-input-wrap {
  position: relative;
  flex: 1;
  min-width: 0;
}

.amount-input {
  width: 100%;
  padding-right: 64rpx;
}

.amount-suffix {
  position: absolute;
  right: 24rpx;
  top: 50%;
  transform: translateY(-50%);
  font-size: 28rpx;
  color: #999;
}

.currency-cell {
  flex: 0 0 200rpx;
}

.currency-picker {
  padding: 0 16rpx;
}

.equity-input-wrap {
  position: relative;
}

.equity-input {
  width: 100%;
  padding-right: 64rpx;
}

.equity-suffix {
  position: absolute;
  right: 24rpx;
  top: 50%;
  transform: translateY(-50%);
  font-size: 28rpx;
  color: #999;
}

.financing-tip {
  display: block;
  margin: -8rpx 0 32rpx;
  font-size: 24rpx;
  color: #78B9B1;
  line-height: 1.65;
}
</style>
