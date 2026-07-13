package com.lianbei.vc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianbei.vc.config.OnboardProperties;
import com.lianbei.vc.dto.request.OnboardCertifierRequest;
import com.lianbei.vc.dto.request.OnboardTeamMemberRequest;
import com.lianbei.vc.dto.request.ProjectOnboardSubmitRequest;
import com.lianbei.vc.dto.response.ProjectOnboardStatusVO;
import com.lianbei.vc.dto.response.ProjectOnboardSubmitResponse;
import com.lianbei.vc.entity.ProjectOnboardRecord;
import com.lianbei.vc.entity.User;
import com.lianbei.vc.exception.BusinessException;
import com.lianbei.vc.mapper.ProjectOnboardRecordMapper;
import com.lianbei.vc.mapper.UserMapper;
import com.lianbei.vc.service.CompanySearchService;
import com.lianbei.vc.service.FileStorageService;
import com.lianbei.vc.service.ProjectOnboardAuditService;
import com.lianbei.vc.service.ProjectOnboardService;
import com.lianbei.vc.service.ProjectService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ProjectOnboardServiceImpl implements ProjectOnboardService {

  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
  private static final Pattern WEBSITE_PATTERN = Pattern.compile("^https?://.+", Pattern.CASE_INSENSITIVE);

  private final ProjectOnboardRecordMapper onboardRecordMapper;
  private final UserMapper userMapper;
  private final ProjectService projectService;
  private final CompanySearchService companySearchService;
  private final FileStorageService fileStorageService;
  private final ObjectMapper objectMapper;
  private final ProjectOnboardAuditService auditService;
  private final OnboardProperties onboardProperties;

  public ProjectOnboardServiceImpl(
      ProjectOnboardRecordMapper onboardRecordMapper,
      UserMapper userMapper,
      ProjectService projectService,
      CompanySearchService companySearchService,
      FileStorageService fileStorageService,
      ObjectMapper objectMapper,
      ProjectOnboardAuditService auditService,
      OnboardProperties onboardProperties) {
    this.onboardRecordMapper = onboardRecordMapper;
    this.userMapper = userMapper;
    this.projectService = projectService;
    this.companySearchService = companySearchService;
    this.fileStorageService = fileStorageService;
    this.objectMapper = objectMapper;
    this.auditService = auditService;
    this.onboardProperties = onboardProperties;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public ProjectOnboardSubmitResponse submit(Long userId, ProjectOnboardSubmitRequest request) {
    if (userId == null) {
      throw new BusinessException(401, "未登录，请先登录");
    }
    if (request == null) {
      throw new BusinessException("申请数据不能为空");
    }

    User user = userMapper.selectById(userId);
    if (user == null) {
      throw new BusinessException("用户不存在");
    }

    ProjectOnboardRecord pending =
        onboardRecordMapper.selectOne(
            new LambdaQueryWrapper<ProjectOnboardRecord>()
                .eq(ProjectOnboardRecord::getUserId, userId)
                .eq(ProjectOnboardRecord::getStatus, "pending")
                .last("LIMIT 1"));
    if (pending != null) {
      throw new BusinessException("入驻审核中，请勿重复提交");
    }

    validateRequest(request, user.getPhone());

    String projectName = request.getProjectName().trim();
    if (projectService.existsByName(projectName)) {
      throw new BusinessException("该项目名称已被大学生创投收录，请更换名称");
    }

    String entityName = request.getEntityName().trim();
    if (!isValidEntityName(entityName)) {
      throw new BusinessException("请选择有效的企业主体");
    }

    try {
      Map<String, Object> payload = buildApplyPayload(request, user.getPhone());
      String applyJson = objectMapper.writeValueAsString(payload);

      ProjectOnboardRecord record =
          ProjectOnboardRecord.builder()
              .userId(userId)
              .projectName(projectName)
              .status("pending")
              .applyData(applyJson)
              .build();
      onboardRecordMapper.insert(record);

      user.setAuthStatus("pending");
      user.setRole("entrepreneur");
      userMapper.updateById(user);

      Long projectId = null;
      String message = "入驻申请已提交，请等待审核";
      if (onboardProperties.isAutoApprove()) {
        projectId = auditService.approve(record.getId(), null);
        message = "入驻申请已通过，项目已入库";
      }

      return ProjectOnboardSubmitResponse.builder()
          .applicationId(record.getId())
          .projectId(projectId)
          .message(message)
          .build();
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException("入驻申请提交失败");
    }
  }

  @Override
  public ProjectOnboardStatusVO getLatestStatus(Long userId) {
    if (userId == null) {
      throw new BusinessException(401, "未登录，请先登录");
    }
    ProjectOnboardRecord record =
        onboardRecordMapper.selectOne(
            new LambdaQueryWrapper<ProjectOnboardRecord>()
                .eq(ProjectOnboardRecord::getUserId, userId)
                .orderByDesc(ProjectOnboardRecord::getCreateTime)
                .last("LIMIT 1"));
    if (record == null) {
      return ProjectOnboardStatusVO.builder().status("none").build();
    }
    if (!userId.equals(record.getUserId())) {
      return ProjectOnboardStatusVO.builder().status("none").build();
    }
    return ProjectOnboardStatusVO.builder()
        .userId(record.getUserId())
        .applicationId(record.getId())
        .projectName(record.getProjectName())
        .status(record.getStatus())
        .auditRemark(record.getAuditRemark())
        .projectId(record.getProjectId())
        .build();
  }

  private void validateRequest(ProjectOnboardSubmitRequest request, String loginPhone) {
    requireText(request.getProjectName(), "请填写项目名称");
    requireText(request.getEntityName(), "请填写企业主体");
    requireText(request.getEstablishDate(), "请选择成立时间");
    requireManagedUrl(request.getLogoUrl(), "请上传项目LOGO");
    requireText(request.getOneLiner(), "请填写一句话介绍");
    requireText(request.getIntro(), "请填写项目简介");

    if (request.getIndustries() == null || request.getIndustries().isEmpty()) {
      throw new BusinessException("请选择所属行业");
    }
    if (request.getIndustries().size() > 2) {
      throw new BusinessException("所属行业最多选择两项");
    }

    String country = StringUtils.hasText(request.getCountry()) ? request.getCountry().trim() : "中国";
    if ("海外".equals(country)) {
      requireText(request.getOverseasLocation(), "请填写海外总部所在地");
    } else {
      requireText(request.getProvince(), "请选择总部所在地");
      requireText(request.getCity(), "请选择总部所在地");
    }

    requireText(request.getFinancingRound(), "请选择当前融资轮次");
    requireText(request.getNeedFinancing(), "请选择近期是否需要融资");

    if ("yes".equals(request.getNeedFinancing())) {
      requireText(request.getSeekingFinancingRound(), "请选择融资轮次");
    }

    if (StringUtils.hasText(request.getWebsite())
        && !WEBSITE_PATTERN.matcher(request.getWebsite().trim()).matches()) {
      throw new BusinessException("网址需以http或https开头");
    }

    if (StringUtils.hasText(request.getBpUrl())) {
      requireManagedUrl(request.getBpUrl(), "请重新上传项目BP");
    }

    List<OnboardTeamMemberRequest> members = request.getTeamMembers();
    if (members == null || members.isEmpty()) {
      throw new BusinessException("请至少添加一位核心成员");
    }
    for (OnboardTeamMemberRequest member : members) {
      requireText(member.getName(), "请填写成员姓名");
      requireText(member.getTitle(), "请填写成员职务");
      requireText(member.getBio(), "请填写个人经历");
      if (StringUtils.hasText(member.getAvatar())) {
        requireManagedUrl(member.getAvatar(), "请重新上传成员头像");
      }
    }

    OnboardCertifierRequest certifier = request.getCertifier();
    if (certifier == null) {
      throw new BusinessException("请填写认证信息");
    }
    requireText(certifier.getRealName(), "请填写真实姓名");
    requireText(certifier.getJobType(), "请选择职务类型");
    requireText(certifier.getJobTitle(), "请填写职位名称");
    requireText(certifier.getResponsibility(), "请选择负责方向");
    requireManagedUrl(certifier.getIdentityCertUrl(), "请上传身份认证材料");

    if (!StringUtils.hasText(loginPhone)) {
      throw new BusinessException("未获取到登录手机号");
    }

    if (StringUtils.hasText(certifier.getContactEmail())
        && !EMAIL_PATTERN.matcher(certifier.getContactEmail().trim()).matches()) {
      throw new BusinessException("请填写正确的邮箱格式");
    }
  }

  private Map<String, Object> buildApplyPayload(
      ProjectOnboardSubmitRequest request, String loginPhone) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("projectName", request.getProjectName().trim());
    payload.put("entityName", request.getEntityName().trim());
    payload.put("establishDate", request.getEstablishDate().trim());
    payload.put("logoUrl", request.getLogoUrl().trim());
    payload.put("country", request.getCountry());
    payload.put("province", request.getProvince());
    payload.put("city", request.getCity());
    payload.put("overseasLocation", request.getOverseasLocation());
    payload.put("oneLiner", request.getOneLiner().trim());
    payload.put("intro", request.getIntro().trim());
    payload.put("industries", request.getIndustries());
    payload.put("financingRound", request.getFinancingRound());
    payload.put("needFinancing", request.getNeedFinancing());
    payload.put("seekingFinancingRound", request.getSeekingFinancingRound());
    payload.put("financingAmount", request.getFinancingAmount());
    payload.put("financingCurrency", request.getFinancingCurrency());
    payload.put("equityPercent", request.getEquityPercent());
    payload.put("website", request.getWebsite());
    payload.put("bpUrl", request.getBpUrl());
    payload.put("bpFileName", request.getBpFileName());
    payload.put("teamMembers", request.getTeamMembers());

    OnboardCertifierRequest certifier = request.getCertifier();
    Map<String, Object> cert = new HashMap<>();
    cert.put("realName", certifier.getRealName().trim());
    cert.put("phone", loginPhone);
    cert.put("sameAsWechat", Boolean.TRUE.equals(certifier.getSameAsWechat()));
    cert.put("jobType", certifier.getJobType());
    cert.put("jobTitle", certifier.getJobTitle().trim());
    cert.put("responsibility", certifier.getResponsibility());
    cert.put("contactEmail", certifier.getContactEmail());
    cert.put("identityCertUrl", certifier.getIdentityCertUrl());
    payload.put("certifier", cert);
    return payload;
  }

  private boolean isValidEntityName(String entityName) {
    List<String> results = companySearchService.search(entityName, 20);
    return results.stream().anyMatch(name -> entityName.equals(name));
  }

  private void requireText(String value, String message) {
    if (!StringUtils.hasText(value) || !StringUtils.hasText(value.trim())) {
      throw new BusinessException(message);
    }
  }

  private void requireManagedUrl(String url, String message) {
    if (!fileStorageService.isManagedUrl(url)) {
      throw new BusinessException(message);
    }
  }
}
