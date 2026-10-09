package com.lianbei.vc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianbei.vc.entity.ProjectOnboardRecord;
import com.lianbei.vc.entity.User;
import com.lianbei.vc.exception.BusinessException;
import com.lianbei.vc.mapper.ProjectOnboardRecordMapper;
import com.lianbei.vc.mapper.UserMapper;
import com.lianbei.vc.service.ProjectOnboardAuditService;
import com.lianbei.vc.service.ProjectOnboardPublishService;
import com.lianbei.vc.service.ProjectService;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ProjectOnboardAuditServiceImpl implements ProjectOnboardAuditService {

  private final ProjectOnboardRecordMapper onboardRecordMapper;
  private final UserMapper userMapper;
  private final ProjectOnboardPublishService publishService;
  private final ProjectService projectService;
  private final ObjectMapper objectMapper;

  public ProjectOnboardAuditServiceImpl(
      ProjectOnboardRecordMapper onboardRecordMapper,
      UserMapper userMapper,
      ProjectOnboardPublishService publishService,
      ProjectService projectService,
      ObjectMapper objectMapper) {
    this.onboardRecordMapper = onboardRecordMapper;
    this.userMapper = userMapper;
    this.publishService = publishService;
    this.projectService = projectService;
    this.objectMapper = objectMapper;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Long approve(Long recordId, Long auditorId) {
    if (recordId == null) {
      throw new BusinessException("申请记录不存在");
    }

    ProjectOnboardRecord record = onboardRecordMapper.selectById(recordId);
    if (record == null) {
      throw new BusinessException("申请记录不存在");
    }

    if ("approved".equals(record.getStatus()) && record.getProjectId() != null) {
      return record.getProjectId();
    }

    if (!"pending".equals(record.getStatus())) {
      throw new BusinessException("当前申请状态不可审核通过");
    }

    if (projectService.existsByName(record.getProjectName())) {
      throw new BusinessException("项目名称已被收录，无法审核通过");
    }

    Map<String, Object> applyData = parseApplyData(record.getApplyData());
    Long projectId = publishService.publish(record.getUserId(), applyData);

    record.setStatus("approved");
    record.setProjectId(projectId);
    record.setAuditorId(auditorId);
    record.setAuditRemark(null);
    record.setUpdateTime(LocalDateTime.now());
    onboardRecordMapper.updateById(record);

    User user = userMapper.selectById(record.getUserId());
    if (user != null) {
      user.setAuthStatus("approved");
      user.setRole("entrepreneur");
      userMapper.updateById(user);
    }

    return projectId;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void reject(Long recordId, Long auditorId, String auditRemark) {
    ProjectOnboardRecord record = requirePending(recordId);
    record.setStatus("rejected");
    record.setAuditorId(auditorId);
    record.setAuditRemark(StringUtils.hasText(auditRemark) ? auditRemark.trim() : "审核未通过");
    record.setUpdateTime(LocalDateTime.now());
    onboardRecordMapper.updateById(record);

    User user = userMapper.selectById(record.getUserId());
    if (user != null) {
      user.setAuthStatus("rejected");
      userMapper.updateById(user);
    }
  }

  private ProjectOnboardRecord requirePending(Long recordId) {
    if (recordId == null) {
      throw new BusinessException("申请记录不存在");
    }
    ProjectOnboardRecord record = onboardRecordMapper.selectById(recordId);
    if (record == null) {
      throw new BusinessException("申请记录不存在");
    }
    if (!"pending".equals(record.getStatus())) {
      throw new BusinessException("当前申请状态不可审核");
    }
    return record;
  }

  private Map<String, Object> parseApplyData(String applyJson) {
    if (!StringUtils.hasText(applyJson)) {
      throw new BusinessException("申请数据为空");
    }
    try {
      return objectMapper.readValue(applyJson, new TypeReference<>() {});
    } catch (Exception e) {
      throw new BusinessException("申请数据解析失败");
    }
  }
}
