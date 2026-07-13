package com.lianbei.vc.service.impl;

import com.lianbei.vc.entity.User;
import com.lianbei.vc.entity.UserAuthRecord;
import com.lianbei.vc.exception.BusinessException;
import com.lianbei.vc.mapper.UserAuthRecordMapper;
import com.lianbei.vc.mapper.UserMapper;
import com.lianbei.vc.service.UserAuthAuditService;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserAuthAuditServiceImpl implements UserAuthAuditService {

  private final UserAuthRecordMapper userAuthRecordMapper;
  private final UserMapper userMapper;

  public UserAuthAuditServiceImpl(
      UserAuthRecordMapper userAuthRecordMapper, UserMapper userMapper) {
    this.userAuthRecordMapper = userAuthRecordMapper;
    this.userMapper = userMapper;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void approve(Long recordId, Long auditorId) {
    UserAuthRecord record = requirePending(recordId);
    record.setStatus("approved");
    record.setAuditRemark(null);
    record.setUpdateTime(LocalDateTime.now());
    userAuthRecordMapper.updateById(record);

    syncUserAuthStatus(record.getUserId(), "approved", record.getAuthType());
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void reject(Long recordId, Long auditorId, String auditRemark) {
    UserAuthRecord record = requirePending(recordId);
    record.setStatus("rejected");
    record.setAuditRemark(StringUtils.hasText(auditRemark) ? auditRemark.trim() : "审核未通过");
    record.setUpdateTime(LocalDateTime.now());
    userAuthRecordMapper.updateById(record);

    syncUserAuthStatus(record.getUserId(), "rejected", null);
  }

  private UserAuthRecord requirePending(Long recordId) {
    if (recordId == null) {
      throw new BusinessException("申请记录不存在");
    }
    UserAuthRecord record = userAuthRecordMapper.selectById(recordId);
    if (record == null) {
      throw new BusinessException("申请记录不存在");
    }
    if (!"pending".equals(record.getStatus())) {
      throw new BusinessException("当前申请状态不可审核");
    }
    return record;
  }

  private void syncUserAuthStatus(Long userId, String authStatus, String role) {
    if (userId == null) {
      return;
    }
    User user = userMapper.selectById(userId);
    if (user == null) {
      return;
    }
    user.setAuthStatus(authStatus);
    if (StringUtils.hasText(role)) {
      user.setRole(role);
    }
    userMapper.updateById(user);
  }
}
