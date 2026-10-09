package com.lianbei.vc.admin.controller;

import com.lianbei.vc.admin.config.AdminTokenStore;
import com.lianbei.vc.admin.dto.AdminLoginRequest;
import com.lianbei.vc.admin.dto.AdminLoginVO;
import com.lianbei.vc.admin.service.AdminAuthService;
import com.lianbei.vc.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

  private final AdminAuthService adminAuthService;
  private final AdminTokenStore adminTokenStore;

  public AdminAuthController(AdminAuthService adminAuthService, AdminTokenStore adminTokenStore) {
    this.adminAuthService = adminAuthService;
    this.adminTokenStore = adminTokenStore;
  }

  @PostMapping("/login")
  public Result<AdminLoginVO> login(@RequestBody AdminLoginRequest request) {
    return Result.ok(adminAuthService.login(request));
  }

  @GetMapping("/me")
  public Result<AdminLoginVO> me() {
    return Result.ok(adminAuthService.current());
  }

  @PostMapping("/logout")
  public Result<Void> logout(HttpServletRequest request) {
    String token = request.getHeader("Authorization");
    if (token != null && token.startsWith("Bearer ")) {
      adminTokenStore.remove(token.substring(7));
    }
    return Result.ok();
  }
}
