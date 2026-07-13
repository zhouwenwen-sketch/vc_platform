package com.lianbei.vc.admin.controller;

import com.lianbei.vc.admin.entity.AdminRole;
import com.lianbei.vc.admin.service.AdminPermissionService;
import com.lianbei.vc.admin.service.AdminRoleService;
import com.lianbei.vc.common.PageResult;
import com.lianbei.vc.common.Result;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/roles")
public class AdminRoleController {

  private final AdminRoleService adminRoleService;
  private final AdminPermissionService adminPermissionService;

  public AdminRoleController(
      AdminRoleService adminRoleService, AdminPermissionService adminPermissionService) {
    this.adminRoleService = adminRoleService;
    this.adminPermissionService = adminPermissionService;
  }

  @GetMapping
  public Result<PageResult<Map<String, Object>>> page(
      @RequestParam(defaultValue = "1") int pageNum,
      @RequestParam(defaultValue = "10") int pageSize,
      @RequestParam(required = false) String keyword) {
    return Result.ok(adminRoleService.page(pageNum, pageSize, keyword));
  }

  @GetMapping("/options")
  public Result<List<Map<String, Object>>> options() {
    return Result.ok(adminRoleService.options());
  }

  @GetMapping("/{id}")
  public Result<Map<String, Object>> detail(@PathVariable Long id) {
    return Result.ok(adminRoleService.detail(id));
  }

  @GetMapping("/permissions/all")
  public Result<List<com.lianbei.vc.admin.entity.AdminPermission>> permissions() {
    adminPermissionService.checkSystemPermission("system:admin_role");
    return Result.ok(adminPermissionService.listAllPermissions());
  }

  @PostMapping
  public Result<Map<String, Object>> create(@RequestBody RoleSaveRequest request) {
    return Result.ok(adminRoleService.create(request.getRole(), request.getPermissionIds()));
  }

  @PutMapping("/{id}")
  public Result<Map<String, Object>> update(
      @PathVariable Long id, @RequestBody RoleSaveRequest request) {
    return Result.ok(adminRoleService.update(id, request.getRole(), request.getPermissionIds()));
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    adminRoleService.remove(id);
    return Result.ok();
  }

  @Data
  public static class RoleSaveRequest {
    private AdminRole role;
    private List<Long> permissionIds;
  }
}
