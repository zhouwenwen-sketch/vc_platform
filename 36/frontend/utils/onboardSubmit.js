import { uploadFile } from '@/api/file.js'

function needsUpload(path) {
  if (!path) return false
  if (path.startsWith('/uploads/')) return false
  if ((path.startsWith('http://') || path.startsWith('https://')) && path.includes('/uploads/')) {
    return false
  }
  return true
}

async function uploadIfNeeded(path, category) {
  if (!needsUpload(path)) return path
  const res = await uploadFile(path, category)
  return res.data?.url || path
}

/**
 * 上传表单中的本地文件，返回可提交的 payload
 * @param {object} form 入驻表单
 */
export async function prepareOnboardPayload(form) {
  const logoUrl = await uploadIfNeeded(form.logoUrl, 'onboard-logo')
  const bpUrl = form.bpUrl ? await uploadIfNeeded(form.bpUrl, 'document') : ''
  const identityCertUrl = await uploadIfNeeded(form.identityCertUrl, 'onboard-cert')

  const teamMembers = []
  for (const member of form.teamMembers || []) {
    const avatar = member.avatar ? await uploadIfNeeded(member.avatar, 'onboard-avatar') : ''
    teamMembers.push({
      name: member.name,
      title: member.title,
      bio: member.bio,
      avatar
    })
  }

  return {
    projectName: form.projectName,
    entityName: form.entityName,
    establishDate: form.establishDate,
    logoUrl,
    country: form.country,
    province: form.province,
    city: form.city,
    overseasLocation: form.overseasLocation,
    oneLiner: form.oneLiner,
    intro: form.intro,
    industries: form.industries,
    financingRound: form.financingRound,
    needFinancing: form.needFinancing,
    seekingFinancingRound: form.seekingFinancingRound,
    financingAmount: form.financingAmount,
    financingCurrency: form.financingCurrency,
    equityPercent: form.equityPercent,
    website: form.website,
    bpUrl,
    bpFileName: form.bpFileName,
    teamMembers,
    certifier: {
      realName: form.realName,
      sameAsWechat: form.sameAsWechat,
      jobType: form.jobType,
      jobTitle: form.jobTitle,
      responsibility: form.responsibility,
      contactEmail: form.contactEmail,
      identityCertUrl
    }
  }
}
