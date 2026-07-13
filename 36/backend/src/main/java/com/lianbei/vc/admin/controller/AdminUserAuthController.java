package com.lianbei.vc.admin.controller;

import com.lianbei.vc.admin.service.AdminPermissionService;
import com.lianbei.vc.admin.util.AdminContext;
import com.lianbei.vc.common.Result;
import com.lianbei.vc.service.UserAuthAuditService;
import java.util.Map;
import lombok.Data;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/user-auth")
public class AdminUserAuthController {

  private final UserAuthAuditService userAuthAuditService;
  private final AdminPermissionService adminPermissionService;

  public AdminUserAuthController(
      UserAuthAuditService userAuthAuditService,
      AdminPermissionService adminPermissionService) {
    this.userAuthAuditService = userAuthAuditService;
    this.adminPermissionService = adminPermissionService;
  }

  @PostMapping("/{id}/approve")
  public Result<Map<String, Object>> approve(@PathVariable Long id) {
    adminPermissionService.checkResource("user_auth_record", "update");
    userAuthAuditService.approve(id, AdminContext.getAdminId());
    return Result.ok(Map.of("message", "认证审核已通过"));
  }

  @PostMapping("/{id}/reject")
  public Result<Void> reject(@PathVariable Long id, @RequestBody RejectRequest request) {
    adminPermissionService.checkResource("user_auth_record", "update");
    userAuthAuditService.reject(id, AdminContext.getAdminId(), request.getAuditRemark());
    return Result.ok();
  }

  @Data
  public static class RejectRequest {
    private String auditRemark;
  }
}
