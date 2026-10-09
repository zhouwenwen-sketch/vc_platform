package com.lianbei.vc.admin.controller;

import com.lianbei.vc.admin.service.AdminPermissionService;
import com.lianbei.vc.admin.util.AdminContext;
import com.lianbei.vc.common.Result;
import com.lianbei.vc.service.ProjectOnboardAuditService;
import java.util.Map;
import lombok.Data;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/onboard")
public class AdminOnboardController {

  private final ProjectOnboardAuditService projectOnboardAuditService;
  private final AdminPermissionService adminPermissionService;

  public AdminOnboardController(
      ProjectOnboardAuditService projectOnboardAuditService,
      AdminPermissionService adminPermissionService) {
    this.projectOnboardAuditService = projectOnboardAuditService;
    this.adminPermissionService = adminPermissionService;
  }

  @PostMapping("/{id}/approve")
  public Result<Map<String, Object>> approve(@PathVariable Long id) {
    adminPermissionService.checkResource("project_onboard_record", "update");
    Long projectId =
        projectOnboardAuditService.approve(id, AdminContext.getAdminId());
    return Result.ok(Map.of("projectId", projectId, "message", "审核通过，项目已入库"));
  }

  @PostMapping("/{id}/reject")
  public Result<Void> reject(@PathVariable Long id, @RequestBody RejectRequest request) {
    adminPermissionService.checkResource("project_onboard_record", "update");
    projectOnboardAuditService.reject(id, AdminContext.getAdminId(), request.getAuditRemark());
    return Result.ok();
  }

  @Data
  public static class RejectRequest {
    private String auditRemark;
  }
}
