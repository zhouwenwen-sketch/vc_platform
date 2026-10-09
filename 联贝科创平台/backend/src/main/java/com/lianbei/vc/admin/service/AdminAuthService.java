package com.lianbei.vc.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lianbei.vc.admin.config.AdminTokenStore;
import com.lianbei.vc.admin.dto.AdminLoginRequest;
import com.lianbei.vc.admin.dto.AdminLoginVO;
import com.lianbei.vc.admin.entity.AdminRole;
import com.lianbei.vc.admin.entity.AdminUser;
import com.lianbei.vc.admin.mapper.AdminRoleMapper;
import com.lianbei.vc.admin.mapper.AdminUserMapper;
import com.lianbei.vc.admin.util.AdminContext;
import com.lianbei.vc.admin.util.AdminSession;
import com.lianbei.vc.exception.BusinessException;
import java.util.Set;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {

  private final AdminUserMapper adminUserMapper;
  private final AdminRoleMapper adminRoleMapper;
  private final AdminPermissionService permissionService;
  private final AdminTokenStore adminTokenStore;
  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  public AdminAuthService(
      AdminUserMapper adminUserMapper,
      AdminRoleMapper adminRoleMapper,
      AdminPermissionService permissionService,
      AdminTokenStore adminTokenStore) {
    this.adminUserMapper = adminUserMapper;
    this.adminRoleMapper = adminRoleMapper;
    this.permissionService = permissionService;
    this.adminTokenStore = adminTokenStore;
  }

  public AdminLoginVO login(AdminLoginRequest request) {
    if (request == null
        || !org.springframework.util.StringUtils.hasText(request.getUsername())
        || !org.springframework.util.StringUtils.hasText(request.getPassword())) {
      throw new BusinessException("用户名和密码不能为空");
    }
    AdminUser user =
        adminUserMapper.selectOne(
            new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUsername, request.getUsername()));
    if (user == null || user.getStatus() == null || user.getStatus() != 1) {
      throw new BusinessException("用户名或密码错误");
    }
    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new BusinessException("用户名或密码错误");
    }
    AdminRole role = adminRoleMapper.selectById(user.getRoleId());
    if (role == null || role.getStatus() == null || role.getStatus() != 1) {
      throw new BusinessException("账号角色不可用");
    }
    Set<String> permissions = permissionService.loadPermissionCodes(user.getRoleId());
    AdminSession session =
        AdminSession.builder()
            .adminId(user.getId())
            .username(user.getUsername())
            .nickname(user.getNickname())
            .roleId(role.getId())
            .roleCode(role.getCode())
            .roleName(role.getName())
            .permissions(permissions)
            .build();
    String token = adminTokenStore.createToken(session);
    return AdminLoginVO.builder()
        .token(token)
        .adminId(user.getId())
        .username(user.getUsername())
        .nickname(user.getNickname())
        .roleCode(role.getCode())
        .roleName(role.getName())
        .permissions(permissions)
        .menus(permissionService.buildMenus(permissions))
        .build();
  }

  public AdminLoginVO current() {
    AdminSession session = AdminContext.get();
    if (session == null) {
      throw new BusinessException(401, "未登录");
    }
    Set<String> permissions = permissionService.loadPermissionCodes(session.getRoleId());
    session.setPermissions(permissions);
    return AdminLoginVO.builder()
        .adminId(session.getAdminId())
        .username(session.getUsername())
        .nickname(session.getNickname())
        .roleCode(session.getRoleCode())
        .roleName(session.getRoleName())
        .permissions(permissions)
        .menus(permissionService.buildMenus(permissions))
        .build();
  }

  public void logout(String token) {
    adminTokenStore.remove(token);
  }

  public PasswordEncoder passwordEncoder() {
    return passwordEncoder;
  }
}
