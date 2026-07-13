<template>

  <view class="activity-form-page">

    <scroll-view scroll-y class="activity-form-scroll" :show-scrollbar="false">

      <view class="activity-form-head">

        <view class="activity-form-back" @click="onBack">

          <u-icon name="arrow-left" color="#fff" size="40rpx" />

        </view>

        <text class="activity-form-title">活动报名</text>

        <text v-if="activityTitle" class="activity-form-sub">{{ activityTitle }}</text>

      </view>



      <view class="activity-form-card">

        <text class="activity-form-section-title">填写报名信息</text>



        <ApplyFormInput

          v-model="form.name"

          label="姓名"

          placeholder="请填写真实姓名"

          required

        />



        <ApplyFormInput

          v-model="form.phone"

          label="联系电话"

          placeholder="请填写联系电话"

          type="number"

          maxlength="11"

          required

        />



        <ApplyFormInput

          v-model="form.orgName"

          label="单位名称"

          placeholder="请填写单位名称"

          required

        />



        <ApplyFormInput

          v-model="form.position"

          label="职位"

          placeholder="请填写职位名称"

          required

        />



        <view class="activity-submit-wrap">

          <u-button

            type="primary"

            text="立即报名"

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

import { fetchActivityDetail, submitActivityRegistration } from '@/api/activity.js'

import { ensureLoggedIn } from '@/utils/authGuard.js'

import { SUCCESS_CODE } from '@/utils/request.js'

import ApplyFormInput from '@/components/ApplyFormInput/ApplyFormInput.vue'



const REGISTER_PATH = '/pages/activity/register/index'



const activityId = ref('')

const activityTitle = ref('')

const submitting = ref(false)

const form = reactive({

  name: '',

  phone: '',

  orgName: '',

  position: ''

})



onLoad((query) => {

  activityId.value = query?.id || ''

  const path = `${REGISTER_PATH}?id=${activityId.value}`

  if (!ensureLoggedIn(path)) return

  loadActivityTitle()

})



function onBack() {

  uni.navigateBack()

}



async function loadActivityTitle() {

  if (!activityId.value) return

  const res = await fetchActivityDetail(activityId.value)

  if (res.code === SUCCESS_CODE) {

    activityTitle.value = res.data?.title || ''

  }

}



async function onSubmit() {

  if (submitting.value) return

  if (!form.name.trim()) {

    uni.showToast({ title: '请填写姓名', icon: 'none' })

    return

  }

  if (!form.phone.trim()) {

    uni.showToast({ title: '请填写联系电话', icon: 'none' })

    return

  }

  if (!form.orgName.trim()) {

    uni.showToast({ title: '请填写单位名称', icon: 'none' })

    return

  }

  if (!form.position.trim()) {

    uni.showToast({ title: '请填写职位', icon: 'none' })

    return

  }



  submitting.value = true

  try {

    const res = await submitActivityRegistration(activityId.value, {

      name: form.name.trim(),

      phone: form.phone.trim(),

      orgName: form.orgName.trim(),

      position: form.position.trim()

    })

    if (res.code === SUCCESS_CODE) {

      uni.showToast({ title: '报名成功', icon: 'success' })

      setTimeout(() => {

        uni.navigateBack()

      }, 800)

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


