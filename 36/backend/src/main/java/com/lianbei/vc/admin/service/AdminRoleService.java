package com.lianbei.vc.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lianbei.vc.admin.entity.AdminRole;
import com.lianbei.vc.admin.mapper.AdminRoleMapper;
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
public class AdminRoleService extends ServiceImpl<AdminRoleMapper, AdminRole> {

  private final AdminPermissionService permissionService;

  public AdminRoleService(AdminPermissionService permissionService) {
    this.permissionService = permissionService;
  }

  public PageResult<Map<String, Object>> page(int pageNum, int pageSize, String keyword) {
    permissionService.checkSystemPermission("system:admin_role");
    Page<AdminRole> page = new Page<>(pageNum, pageSize);
    LambdaQueryWrapper<AdminRole> wrapper = new LambdaQueryWrapper<>();
    if (StringUtils.hasText(keyword)) {
      wrapper.and(
          w -> w.like(AdminRole::getCode, keyword).or().like(AdminRole::getName, keyword));
    }
    wrapper.orderByDesc(AdminRole::getId);
    Page<AdminRole> result = page(page, wrapper);
    List<Map<String, Object>> list =
        result.getRecords().stream().map(this::toView).collect(Collectors.toList());
    return PageResult.of(result.getTotal(), list);
  }

  public Map<String, Object> detail(Long id) {
    permissionService.checkSystemPermission("system:admin_role");
    AdminRole role = getById(id);
    if (role == null) {
      throw new BusinessException(404, "角色不存在");
    }
    Map<String, Object> view = toView(role);
    view.put("permissionIds", permissionService.listRolePermissionIds(id));
    return view;
  }

  public Map<String, Object> create(AdminRole role, List<Long> permissionIds) {
    permissionService.checkSystemPermission("system:admin_role");
    validateRole(role, true);
    role.setCreateTime(LocalDateTime.now());
    role.setUpdateTime(LocalDateTime.now());
    if (role.getStatus() == null) {
      role.setStatus(1);
    }
    save(role);
    permissionService.saveRolePermissions(role.getId(), permissionIds);
    return detail(role.getId());
  }

  public Map<String, Object> update(Long id, AdminRole role, List<Long> permissionIds) {
    permissionService.checkSystemPermission("system:admin_role");
    AdminRole existing = getById(id);
    if (existing == null) {
      throw new BusinessException(404, "角色不存在");
    }
    if ("super_admin".equals(existing.getCode())) {
      throw new BusinessException("超级管理员角色不可修改");
    }
    if (StringUtils.hasText(role.getName())) {
      existing.setName(role.getName());
    }
    if (StringUtils.hasText(role.getDescription())) {
      existing.setDescription(role.getDescription());
    }
    if (role.getStatus() != null) {
      existing.setStatus(role.getStatus());
    }
    existing.setUpdateTime(LocalDateTime.now());
    updateById(existing);
    if (permissionIds != null) {
      permissionService.saveRolePermissions(id, permissionIds);
    }
    return detail(id);
  }

  public void remove(Long id) {
    permissionService.checkSystemPermission("system:admin_role");
    AdminRole role = getById(id);
    if (role == null) {
      throw new BusinessException(404, "角色不存在");
    }
    if ("super_admin".equals(role.getCode())) {
      throw new BusinessException("超级管理员角色不可删除");
    }
    removeById(id);
  }

  public List<Map<String, Object>> options() {
    return list(new LambdaQueryWrapper<AdminRole>().eq(AdminRole::getStatus, 1)).stream()
        .map(
            role -> {
              Map<String, Object> item = new HashMap<>();
              item.put("id", role.getId());
              item.put("code", role.getCode());
              item.put("name", role.getName());
              return item;
            })
        .collect(Collectors.toList());
  }

  private void validateRole(AdminRole role, boolean creating) {
    if (!StringUtils.hasText(role.getCode()) || !StringUtils.hasText(role.getName())) {
      throw new BusinessException("角色编码和名称不能为空");
    }
    if (creating) {
      long count = count(new LambdaQueryWrapper<AdminRole>().eq(AdminRole::getCode, role.getCode()));
      if (count > 0) {
        throw new BusinessException("角色编码已存在");
      }
    }
  }

  private Map<String, Object> toView(AdminRole role) {
    Map<String, Object> map = new HashMap<>();
    map.put("id", role.getId());
    map.put("code", role.getCode());
    map.put("name", role.getName());
    map.put("description", role.getDescription());
    map.put("status", role.getStatus());
    map.put("createTime", role.getCreateTime());
    map.put("updateTime", role.getUpdateTime());
    return map;
  }
}
