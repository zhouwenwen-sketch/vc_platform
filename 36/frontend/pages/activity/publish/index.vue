<template>

  <view class="activity-form-page">

    <scroll-view scroll-y class="activity-form-scroll" :show-scrollbar="false">

      <view class="activity-form-head">

        <view class="activity-form-back" @click="onBack">

          <u-icon name="arrow-left" color="#fff" size="40rpx" />

        </view>

        <text class="activity-form-title">发布活动</text>

        <text class="activity-form-sub">提交后将由平台审核，审核通过后展示在活动列表</text>

      </view>



      <view class="activity-form-card">

        <text class="activity-form-section-title">填写活动信息</text>



        <ApplyFormInput

          v-model="form.title"

          label="活动标题"

          placeholder="请填写活动标题"

          required

        />



        <ApplyFormInput

          v-model="form.location"

          label="活动地点"

          placeholder="如：上海 · 浦东 / 线上直播"

          required

        />



        <ApplyFormDateTimeField

          v-model="form.startTime"

          label="开始时间"

          placeholder="请选择开始时间"

          required

        />



        <ApplyFormDateTimeField

          v-model="form.endTime"

          label="结束时间"

          placeholder="请选择结束时间"

          required

        />



        <ApplyFormInput

          v-model="form.organizerName"

          label="主办方"

          placeholder="请填写主办方名称"

          required

        />



        <ApplyFormInput

          v-model="form.priceText"

          label="报名费用"

          placeholder="如：200元，留空则为免费"

        />



        <ApplyFormTextarea

          v-model="form.description"

          label="活动简介"

          placeholder="请简要介绍活动内容，500字以内"

          :maxlength="500"

          required

        />



        <ApplyFormInput

          v-model="form.contactName"

          label="联系人"

          placeholder="请填写联系人姓名"

          required

        />



        <ApplyFormInput

          v-model="form.contactPhone"

          label="联系电话"

          placeholder="请填写联系电话"

          type="number"

          maxlength="11"

          required

        />



        <view class="activity-submit-wrap">

          <u-button

            type="primary"

            text="提交发布"

            :loading="submitting"

            custom-style="background:#78B9B1;border-color:#78B9B1;border-radius:44rpx;height:88rpx"

            @click="onSubmit"

          />

        </view>

      </view>

    </scroll-view>

  </view>

</template>



<script setup>

import { reactive, ref } from 'vue'

import { onLoad } from '@dcloudio/uni-app'

import { submitActivityPublish } from '@/api/activity.js'

import { ensureLoggedIn } from '@/utils/authGuard.js'

import { SUCCESS_CODE } from '@/utils/request.js'

import ApplyFormInput from '@/components/ApplyFormInput/ApplyFormInput.vue'

import ApplyFormTextarea from '@/components/ApplyFormTextarea/ApplyFormTextarea.vue'

import ApplyFormDateTimeField from '@/components/ApplyFormDateTimeField/ApplyFormDateTimeField.vue'



const PUBLISH_PATH = '/pages/activity/publish/index'



const submitting = ref(false)



const form = reactive({

  title: '',

  location: '',

  startTime: '',

  endTime: '',

  organizerName: '',

  priceText: '',

  description: '',

  contactName: '',

  contactPhone: ''

})



onLoad(() => {

  if (!ensureLoggedIn(PUBLISH_PATH)) return

})



function onBack() {

  uni.navigateBack()

}



async function onSubmit() {

  if (submitting.value) return

  if (!form.title.trim()) {

    uni.showToast({ title: '请填写活动标题', icon: 'none' })

    return

  }

  if (!form.location.trim()) {

    uni.showToast({ title: '请填写活动地点', icon: 'none' })

    return

  }

  if (!form.startTime) {

    uni.showToast({ title: '请选择开始时间', icon: 'none' })

    return

  }

  if (!form.endTime) {

    uni.showToast({ title: '请选择结束时间', icon: 'none' })

    return

  }

  if (!form.organizerName.trim()) {

    uni.showToast({ title: '请填写主办方', icon: 'none' })

    return

  }

  if (!form.description.trim()) {

    uni.showToast({ title: '请填写活动简介', icon: 'none' })

    return

  }

  if (!form.contactName.trim()) {

    uni.showToast({ title: '请填写联系人', icon: 'none' })

    return

  }

  if (!form.contactPhone.trim()) {

    uni.showToast({ title: '请填写联系电话', icon: 'none' })

    return

  }



  submitting.value = true

  try {

    const res = await submitActivityPublish({

      title: form.title.trim(),

      location: form.location.trim(),

      startTime: form.startTime,

      endTime: form.endTime,

      organizerName: form.organizerName.trim(),

      priceText: form.priceText.trim(),

      description: form.description.trim(),

      contactName: form.contactName.trim(),

      contactPhone: form.contactPhone.trim()

    })

    if (res.code === SUCCESS_CODE) {

      uni.showToast({ title: '提交成功，等待审核', icon: 'success' })

      setTimeout(() => uni.navigateBack(), 800)

    }

  } finally {

    submitting.value = false

  }

}

</script>



<style scoped lang="scss">

@import '@/styles/apply-form.scss';

@import '@/styles/activity-form.scss';

</style>


