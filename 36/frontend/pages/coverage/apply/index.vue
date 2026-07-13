<template>
  <view class="apply-page coverage-page">
    <scroll-view scroll-y class="form-scroll coverage-scroll" :show-scrollbar="false">
      <CoverageApplyHero />

      <view class="coverage-main">
        <CoverageProjectPicker
          ref="pickerRef"
          v-model="selectedProject"
          @create-project="onCreateProject"
        />

        <ApplySectionBar title="报道需求" />

        <view class="coverage-section">
          <view class="field">
            <view class="label-row">
              <text class="required">*</text>
              <text class="label">我希望进行的融资报道类型为</text>
            </view>
            <view class="radio-group-col">
              <u-radio-group v-model="form.reportType" placement="column">
                <u-radio
                  name="latest_financing"
                  label="我想发布最新融资消息"
                  active-color="#78B9B1"
                />
                <u-radio
                  name="no_financing"
                  label="没有新融资，但希望我们报道您的项目"
                  active-color="#78B9B1"
                />
              </u-radio-group>
            </view>
          </view>

          <view v-if="form.reportType === 'latest_financing'" class="field">
            <view class="label-row">
              <text class="required">*</text>
              <text class="label">请选择您此次需要报道的融资轮次</text>
            </view>
            <view v-for="(row, idx) in form.financingList" :key="idx" class="fin-row">
              <view class="fin-grid">
                <view class="fin-cell" @click="openRoundPicker(idx)">
                  <text class="fin-label">融资轮次</text>
                  <text class="fin-value" :class="{ empty: !row.round }">{{ row.round || '请选择' }}</text>
                </view>
                <view class="fin-cell">
                  <text class="fin-label">融资时间</text>
                  <picker mode="date" :value="row.financingDate" @change="(e) => onFinDate(idx, e)">
                    <text class="fin-value" :class="{ empty: !row.financingDate }">
                      {{ row.financingDate || '请选择' }}
                    </text>
                  </picker>
                </view>
                <view class="fin-cell">
                  <text class="fin-label">融资金额</text>
                  <input
                    v-model="row.amount"
                    class="fin-input"
                    placeholder="如：数千万人民币"
                    placeholder-class="placeholder"
                  />
                </view>
                <view class="fin-cell">
                  <text class="fin-label">投资方</text>
                  <input
                    v-model="row.investors"
                    class="fin-input"
                    placeholder="如：红杉中国、高瓴创投"
                    placeholder-class="placeholder"
                  />
                </view>
              </view>
              <view v-if="form.financingList.length > 1" class="fin-remove" @click="removeFinancing(idx)">
                删除本条
              </view>
            </view>
            <view class="fin-add" @click="addFinancing">
              <text>+ 新增融资信息</text>
            </view>
          </view>

          <ApplyFormTextarea
            v-model="form.reportContent"
            required
            label="希望报道的内容"
            placeholder="请填写您希望报道的事项，请简要描述，200字以内"
            :maxlength="200"
          />

          <ApplyFormTextarea
            v-model="form.competitiveness"
            required
            label="项目在行业内的核心竞争力"
            placeholder="请填写项目在行业内的核心竞争力，请简要描述，200字以内"
            :maxlength="200"
          />

          <ApplyFormTextarea
            v-model="form.evaluation"
            label="项目创始人或投资人评价"
            placeholder="非必填，如有评价性文字请填入，以扩展报道丰富度，200字以内"
            :maxlength="200"
          />

          <ApplyFormTextarea
            v-model="form.relatedReports"
            label="项目相关报道"
            placeholder="非必填，如有项目相关报道请填入，以扩展报道丰富度，200字以内"
            :maxlength="200"
          />
        </view>

        <ApplySectionBar title="报道内容/计划" />

        <view class="coverage-section">
          <view class="field">
            <view class="label-row">
              <text class="required">*</text>
              <text class="label">将联贝科创作为首发媒体</text>
            </view>
            <view class="radio-group-row">
              <u-radio-group v-model="form.debutMedia" placement="row">
                <u-radio name="yes" label="是" active-color="#78B9B1" />
                <u-radio name="no" label="否" active-color="#78B9B1" />
              </u-radio-group>
            </view>
          </view>
        </view>

        <view class="coverage-submit-notes">
          <text class="notes-title">提交说明</text>
          <text class="notes-item">1. 每用户每个项目仅可提交一次报道申请；</text>
          <text class="notes-item">2. 提交后工作人员会尽快与您联系；</text>
          <text class="notes-item">3. 未收录项目请先通过「项目入驻」创建项目后再申请。</text>
        </view>

        <view class="coverage-submit-wrap">
          <u-button
            type="primary"
            text="提交申请"
            :loading="submitting"
            custom-style="background:#78B9B1;border-color:#78B9B1;border-radius:48rpx;height:88rpx"
            @click="onSubmit"
          />
        </view>
      </view>
    </scroll-view>

    <u-picker
      :show="roundPickerShow"
      :columns="[roundOptions]"
      @confirm="onRoundConfirm"
      @cancel="roundPickerShow = false"
      @close="roundPickerShow = false"
    />
  </view>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import CoverageApplyHero from '@/components/CoverageApplyHero/CoverageApplyHero.vue'
import ApplySectionBar from '@/components/ApplySectionBar/ApplySectionBar.vue'
import CoverageProjectPicker from '@/components/CoverageProjectPicker/CoverageProjectPicker.vue'
import ApplyFormTextarea from '@/components/ApplyFormTextarea/ApplyFormTextarea.vue'
import { fetchProjectDetail } from '@/api/project.js'
import { submitCoverageApply, checkCoverageApplied } from '@/api/coverage.js'
import { SUCCESS_CODE } from '@/utils/request.js'
import { EVENT_ROUND_OPTIONS } from '@/utils/financingFilterData.js'
import { openProjectOnboard } from '@/utils/projectOnboardGuard.js'
import { guardCoveragePage } from '@/utils/coverageGuard.js'
import { buildProjectCardMetaLine } from '@/utils/projectCardMeta.js'

const pickerRef = ref(null)
const selectedProject = ref(null)
const submitting = ref(false)
const alreadyApplied = ref(false)
const roundPickerShow = ref(false)
const roundPickerIndex = ref(0)
const roundOptions = EVENT_ROUND_OPTIONS

const form = reactive({
  reportType: 'latest_financing',
  financingList: [emptyFinancingRow()],
  reportContent: '',
  competitiveness: '',
  evaluation: '',
  relatedReports: '',
  debutMedia: 'yes'
})

function emptyFinancingRow() {
  return { round: '', financingDate: '', amount: '', investors: '' }
}

function addFinancing() {
  form.financingList.push(emptyFinancingRow())
}

function removeFinancing(idx) {
  form.financingList.splice(idx, 1)
}

function openRoundPicker(idx) {
  roundPickerIndex.value = idx
  roundPickerShow.value = true
}

function onRoundConfirm(e) {
  const val = e.value?.[0]
  if (val != null) {
    form.financingList[roundPickerIndex.value].round = val
  }
  roundPickerShow.value = false
}

function onFinDate(idx, e) {
  form.financingList[idx].financingDate = e.detail.value
}

function onCreateProject() {
  openProjectOnboard()
}

async function loadPrefillProject(projectId) {
  const res = await fetchProjectDetail(projectId)
  if (res.code !== SUCCESS_CODE || !res.data) return
  const d = res.data
  const project = {
    id: d.id,
    name: d.name,
    round: d.round,
    companyDesc: d.slogan || d.intro,
    logoUrl: d.logoUrl,
    metaLine: buildProjectCardMetaLine({
      ...d,
      companyDesc: d.slogan || d.intro,
      foundingYear: d.establishDate
    })
  }
  pickerRef.value?.setProject(project)
  selectedProject.value = project
  await checkAppliedStatus(project.id)
}

async function checkAppliedStatus(projectId) {
  alreadyApplied.value = false
  if (!projectId) return
  const res = await checkCoverageApplied(projectId)
  if (res.code === SUCCESS_CODE && res.data?.applied) {
    alreadyApplied.value = true
    uni.showToast({ title: '您已提交过报道申请', icon: 'none' })
  }
}

function validate() {
  if (!selectedProject.value?.id) {
    uni.showToast({ title: '请先选择要报道的项目', icon: 'none' })
    return false
  }
  if (alreadyApplied.value) {
    uni.showToast({ title: '您已对该项目提交过报道申请', icon: 'none' })
    return false
  }
  if (form.reportType === 'latest_financing') {
    const hasRound = form.financingList.some((r) => r.round)
    if (!hasRound) {
      uni.showToast({ title: '请填写融资轮次', icon: 'none' })
      return false
    }
  }
  if (!form.reportContent.trim()) {
    uni.showToast({ title: '请填写希望报道的内容', icon: 'none' })
    return false
  }
  if (!form.competitiveness.trim()) {
    uni.showToast({ title: '请填写核心竞争力', icon: 'none' })
    return false
  }
  if (!form.debutMedia) {
    uni.showToast({ title: '请选择是否首发媒体', icon: 'none' })
    return false
  }
  return true
}

async function onSubmit() {
  if (submitting.value || !validate()) return
  submitting.value = true
  try {
    const payload = {
      projectId: selectedProject.value.id,
      projectName: selectedProject.value.name,
      reportType: form.reportType,
      financingList: form.reportType === 'latest_financing' ? form.financingList : [],
      reportContent: form.reportContent.trim(),
      competitiveness: form.competitiveness.trim(),
      evaluation: form.evaluation.trim(),
      relatedReports: form.relatedReports.trim(),
      debutMedia: form.debutMedia
    }
    const res = await submitCoverageApply(payload)
    if (res.code === SUCCESS_CODE) {
      uni.showToast({ title: res.data?.message || '提交成功', icon: 'success' })
      setTimeout(() => uni.navigateBack(), 600)
    } else {
      uni.showToast({ title: res.message || '提交失败', icon: 'none' })
    }
  } finally {
    submitting.value = false
  }
}

watch(
  () => selectedProject.value?.id,
  (projectId) => {
    if (projectId) checkAppliedStatus(projectId)
  }
)

onLoad((query) => {
  if (!guardCoveragePage()) return
  const pid = query?.projectId
  if (pid) {
    loadPrefillProject(pid)
  }
})
</script>

<style scoped lang="scss">
@import '@/styles/apply-form.scss';
@import '@/styles/coverage-form.scss';

.field {
  margin-bottom: 32rpx;
}

.fin-row {
  margin-bottom: 20rpx;
}

.fin-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16rpx;
  padding: 20rpx;
  background: #f8f9fb;
  border-radius: 12rpx;
}

.fin-cell {
  background: #fff;
  border-radius: 8rpx;
  padding: 16rpx;
  min-height: 88rpx;
}

.fin-label {
  display: block;
  font-size: 22rpx;
  color: #999;
  margin-bottom: 8rpx;
}

.fin-value {
  font-size: 26rpx;
  color: #333;

  &.empty {
    color: #bbb;
  }
}

.fin-input {
  width: 100%;
  font-size: 26rpx;
  color: #333;
}

.fin-remove {
  margin-top: 8rpx;
  text-align: right;
  font-size: 24rpx;
  color: #e53935;
}

.fin-add {
  margin-top: 8rpx;
  text-align: center;
  padding: 20rpx;
  border: 2rpx dashed #78B9B1;
  border-radius: 12rpx;

  text {
    font-size: 28rpx;
    color: #78B9B1;
  }
}
</style>
