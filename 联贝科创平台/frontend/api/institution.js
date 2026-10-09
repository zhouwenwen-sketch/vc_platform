import { request, SUCCESS_CODE } from '@/utils/request.js'
import { resolveMediaUrl } from '@/utils/mediaUrl.js'
import { serializeFilterValues } from '@/utils/filterMulti.js'
import { mapInstitutionItem, normalizePage } from '@/utils/transform.js'

/** 机构库列表（筛选 + 分页） */
export function fetchInstitutionLibrary(pageNum = 1, pageSize = 10, filters = {}) {
  const { keyword, investmentField, instType, foundedYear } = filters
  const params = { pageNum, pageSize }
  if (keyword) params.keyword = keyword
  const investmentFieldValue = serializeFilterValues(investmentField)
  const instTypeValue = serializeFilterValues(instType)
  const foundedYearValue = serializeFilterValues(foundedYear)
  if (investmentFieldValue) params.investmentField = investmentFieldValue
  if (instTypeValue) params.instType = instTypeValue
  if (foundedYearValue) params.foundedYear = foundedYearValue

  return request({
    url: '/api/institutions/library',
    data: params,
    showError: false
  }).then((res) => {
    if (res.code !== SUCCESS_CODE) return res
    const page = normalizePage(res.data, pageNum, pageSize, mapInstitutionItem)
    return { code: SUCCESS_CODE, data: { ...page, total: res.data.total ?? page.total } }
  })
}

function mapInstitutionDetail(row) {
  return {
    id: row.id,
    name: row.name || '',
    entityName: row.entityName || row.entity_name || '',
    logoUrl: resolveMediaUrl(row.logoUrl || row.logo_url || ''),
    instTypes: row.instTypes || row.inst_types || [],
    foundedYear: row.foundedYear || row.founded_year || '',
    region: row.region || '',
    eventCount: row.eventCount ?? row.event_count ?? 0,
    manageScale: row.manageScale || row.manage_scale || '-',
    teamCount: row.teamCount || row.team_count || '-',
    intro: row.intro || '',
    investmentFields: row.investmentFields || row.investment_fields || [],
    investmentEvents: (row.investmentEvents || row.investment_events || []).map(mapInvestmentEvent),
    businessInfo: row.businessInfo || row.business_info || null,
    fundManagers: (row.fundManagers || row.fund_managers || []).map(mapFundManager),
    teamMembers: (row.teamMembers || row.team_members || []).map(mapTeamMember),
    contact: row.contact || { website: '-', phone: '-', email: '-', address: '-' }
  }
}

function mapFundManager(fm) {
  const fullName = fm.fullName || fm.full_name || fm.name || fm.entityName || fm.entity_name || ''
  return {
    name: fm.name || fullName,
    entityName: fm.entityName || fm.entity_name || fullName,
    fullName,
    legalPerson: fm.legalPerson || fm.legal_person || '',
    instType: fm.instType || fm.inst_type || '',
    officeAddress: fm.officeAddress || fm.office_address || '',
    registeredCapital: fm.registeredCapital || fm.registered_capital || '',
    paidInCapital: fm.paidInCapital || fm.paid_in_capital || '',
    paidInRatio: fm.paidInRatio || fm.paid_in_ratio || '',
    registrationNo: fm.registrationNo || fm.registration_no || '',
    establishDate: fm.establishDate || fm.establish_date || '',
    registerDate: fm.registerDate || fm.register_date || ''
  }
}

function mapTeamMember(member) {
  return {
    name: member.name || '',
    title: member.title || '',
    avatar: resolveMediaUrl(member.avatar || ''),
    bio: member.bio || ''
  }
}

function mapInvestmentEvent(ev) {
  return {
    projectId: ev.projectId ?? ev.project_id ?? null,
    companyName: ev.companyName || ev.company_name || '',
    logoUrl: resolveMediaUrl(ev.logoUrl || ev.logo_url || ''),
    round: ev.round || '',
    industry: ev.industry || '',
    description: ev.description || '',
    date: ev.date || '',
    amount: ev.amount || '未透露'
  }
}

/** 机构详情 */
export function fetchInstitutionDetail(id) {
  return request({
    url: `/api/institutions/detail/${id}`,
    showError: true
  }).then((res) => {
    if (res.code !== SUCCESS_CODE) return res
    return { code: SUCCESS_CODE, data: mapInstitutionDetail(res.data || {}) }
  })
}
