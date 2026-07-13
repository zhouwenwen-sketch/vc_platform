package com.lianbei.vc.admin.controller;

import com.lianbei.vc.admin.entity.AdminUser;
import com.lianbei.vc.admin.service.AdminUserService;
import com.lianbei.vc.common.PageResult;
import com.lianbei.vc.common.Result;
import java.util.Map;
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
@RequestMapping("/api/admin/users")
public class AdminUserController {

  private final AdminUserService adminUserService;

  public AdminUserController(AdminUserService adminUserService) {
    this.adminUserService = adminUserService;
  }

  @GetMapping
  public Result<PageResult<Map<String, Object>>> page(
      @RequestParam(defaultValue = "1") int pageNum,
      @RequestParam(defaultValue = "10") int pageSize,
      @RequestParam(required = false) String keyword) {
    return Result.ok(adminUserService.page(pageNum, pageSize, keyword));
  }

  @PostMapping
  public Result<Map<String, Object>> create(@RequestBody AdminUser user) {
    return Result.ok(adminUserService.create(user));
  }

  @PutMapping("/{id}")
  public Result<Map<String, Object>> update(@PathVariable Long id, @RequestBody AdminUser user) {
    return Result.ok(adminUserService.update(id, user));
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    adminUserService.remove(id);
    return Result.ok();
  }
}
