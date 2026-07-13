package com.lianbei.vc.service;

/** 入驻申请审核 */
public interface ProjectOnboardAuditService {

  /** 审核通过并落库，返回项目 ID */
  Long approve(Long recordId, Long auditorId);

  /** 驳回入驻申请 */
  void reject(Long recordId, Long auditorId, String auditRemark);
}
