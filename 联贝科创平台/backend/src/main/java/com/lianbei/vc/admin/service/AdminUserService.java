package com.lianbei.vc.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lianbei.vc.admin.entity.AdminRole;
import com.lianbei.vc.admin.entity.AdminUser;
import com.lianbei.vc.admin.mapper.AdminRoleMapper;
import com.lianbei.vc.admin.mapper.AdminUserMapper;
import com.lianbei.vc.common.PageResult;
import com.lianbei.vc.exception.BusinessException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AdminUserService extends ServiceImpl<AdminUserMapper, AdminUser> {

  private final AdminRoleMapper adminRoleMapper;
  private final AdminAuthService adminAuthService;
  private final AdminPermissionService permissionService;

  public AdminUserService(
      AdminRoleMapper adminRoleMapper,
      AdminAuthService adminAuthService,
      AdminPermissionService permissionService) {
    this.adminRoleMapper = adminRoleMapper;
    this.adminAuthService = adminAuthService;
    this.permissionService = permissionService;
  }

  public PageResult<Map<String, Object>> page(int pageNum, int pageSize, String keyword) {
    permissionService.checkSystemPermission("system:admin_user");
    Page<AdminUser> page = new Page<>(pageNum, pageSize);
    LambdaQueryWrapper<AdminUser> wrapper = new LambdaQueryWrapper<>();
    if (StringUtils.hasText(keyword)) {
      wrapper.and(
          w ->
              w.like(AdminUser::getUsername, keyword)
                  .or()
                  .like(AdminUser::getNickname, keyword));
    }
    wrapper.orderByDesc(AdminUser::getId);
    Page<AdminUser> result = page(page, wrapper);
    Map<Long, String> roleNames =
        adminRoleMapper.selectList(null).stream()
            .collect(Collectors.toMap(AdminRole::getId, AdminRole::getName, (a, b) -> a));
    List<Map<String, Object>> list =
        result.getRecords().stream()
            .map(user -> toView(user, roleNames.get(user.getRoleId())))
            .collect(Collectors.toList());
    return PageResult.of(result.getTotal(), list);
  }

  public Map<String, Object> create(AdminUser user) {
    permissionService.checkSystemPermission("system:admin_user");
    validateUser(user, true);
    user.setPassword(adminAuthService.passwordEncoder().encode(user.getPassword()));
    user.setCreateTime(LocalDateTime.now());
    user.setUpdateTime(LocalDateTime.now());
    if (user.getStatus() == null) {
      user.setStatus(1);
    }
    save(user);
    return toView(user, roleName(user.getRoleId()));
  }

  public Map<String, Object> update(Long id, AdminUser user) {
    permissionService.checkSystemPermission("system:admin_user");
    AdminUser existing = getById(id);
    if (existing == null) {
      throw new BusinessException(404, "管理员不存在");
    }
    if (StringUtils.hasText(user.getUsername())) {
      existing.setUsername(user.getUsername());
    }
    if (StringUtils.hasText(user.getNickname())) {
      existing.setNickname(user.getNickname());
    }
    if (user.getRoleId() != null) {
      existing.setRoleId(user.getRoleId());
    }
    if (user.getStatus() != null) {
      existing.setStatus(user.getStatus());
    }
    if (StringUtils.hasText(user.getPassword())) {
      existing.setPassword(adminAuthService.passwordEncoder().encode(user.getPassword()));
    }
    existing.setUpdateTime(LocalDateTime.now());
    updateById(existing);
    return toView(existing, roleName(existing.getRoleId()));
  }

  public void remove(Long id) {
    permissionService.checkSystemPermission("system:admin_user");
    if (!removeById(id)) {
      throw new BusinessException(404, "管理员不存在");
    }
  }

  private void validateUser(AdminUser user, boolean creating) {
    if (!StringUtils.hasText(user.getUsername())) {
      throw new BusinessException("用户名不能为空");
    }
    if (creating && !StringUtils.hasText(user.getPassword())) {
      throw new BusinessException("密码不能为空");
    }
    if (user.getRoleId() == null) {
      throw new BusinessException("角色不能为空");
    }
    AdminRole role = adminRoleMapper.selectById(user.getRoleId());
    if (role == null) {
      throw new BusinessException("角色不存在");
    }
    long count =
        count(
            new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUsername, user.getUsername()));
    if (count > 0) {
      throw new BusinessException("用户名已存在");
    }
  }

  private String roleName(Long roleId) {
    AdminRole role = adminRoleMapper.selectById(roleId);
    return role == null ? "" : role.getName();
  }

  private Map<String, Object> toView(AdminUser user, String roleName) {
    Map<String, Object> map = new HashMap<>();
    map.put("id", user.getId());
    map.put("username", user.getUsername());
    map.put("nickname", user.getNickname());
    map.put("roleId", user.getRoleId());
    map.put("roleName", roleName);
    map.put("status", user.getStatus());
    map.put("createTime", user.getCreateTime());
    map.put("updateTime", user.getUpdateTime());
    return map;
  }
}
