package com.lianbei.vc.service;

/** 用户认证申请审核 */
public interface UserAuthAuditService {

  /** 审核通过 */
  void approve(Long recordId, Long auditorId);

  /** 驳回认证申请 */
  void reject(Long recordId, Long auditorId, String auditRemark);
}
