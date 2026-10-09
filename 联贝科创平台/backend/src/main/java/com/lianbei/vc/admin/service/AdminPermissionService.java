package com.lianbei.vc.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lianbei.vc.admin.dto.AdminMenuVO;
import com.lianbei.vc.admin.entity.AdminPermission;
import com.lianbei.vc.admin.entity.AdminRolePermission;
import com.lianbei.vc.admin.mapper.AdminPermissionMapper;
import com.lianbei.vc.admin.mapper.AdminRolePermissionMapper;
import com.lianbei.vc.admin.util.AdminContext;
import com.lianbei.vc.exception.BusinessException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class AdminPermissionService {

  private final AdminPermissionMapper permissionMapper;
  private final AdminRolePermissionMapper rolePermissionMapper;

  public AdminPermissionService(
      AdminPermissionMapper permissionMapper, AdminRolePermissionMapper rolePermissionMapper) {
    this.permissionMapper = permissionMapper;
    this.rolePermissionMapper = rolePermissionMapper;
  }

  public Set<String> loadPermissionCodes(Long roleId) {
    List<AdminRolePermission> rels =
        rolePermissionMapper.selectList(
            new LambdaQueryWrapper<AdminRolePermission>().eq(AdminRolePermission::getRoleId, roleId));
    if (rels.isEmpty()) {
      return Set.of();
    }
    Set<Long> permissionIds =
        rels.stream().map(AdminRolePermission::getPermissionId).collect(Collectors.toSet());
    List<AdminPermission> permissions = permissionMapper.selectBatchIds(permissionIds);
    return permissions.stream().map(AdminPermission::getCode).collect(Collectors.toSet());
  }

  public List<AdminMenuVO> buildMenus(Set<String> permissionCodes) {
    List<AdminPermission> all =
        permissionMapper.selectList(
            new LambdaQueryWrapper<AdminPermission>()
                .eq(AdminPermission::getType, "menu")
                .orderByAsc(AdminPermission::getSortOrder));
    List<AdminPermission> allowed =
        all.stream()
            .filter(
                item ->
                    AdminContext.isSuperAdmin()
                        || permissionCodes.contains(item.getCode())
                        || hasChildPermission(item.getId(), all, permissionCodes))
            .collect(Collectors.toList());
    Map<Long, AdminMenuVO> nodeMap = new HashMap<>();
    List<AdminMenuVO> roots = new ArrayList<>();
    for (AdminPermission item : allowed) {
      AdminMenuVO node =
          AdminMenuVO.builder()
              .id(item.getId())
              .code(item.getCode())
              .name(item.getName())
              .path(item.getPath())
              .resource(item.getResource())
              .sortOrder(item.getSortOrder())
              .build();
      nodeMap.put(item.getId(), node);
    }
    for (AdminPermission item : allowed) {
      AdminMenuVO node = nodeMap.get(item.getId());
      Long parentId = item.getParentId() == null ? 0L : item.getParentId();
      if (parentId == 0L) {
        roots.add(node);
      } else if (nodeMap.containsKey(parentId)) {
        nodeMap.get(parentId).getChildren().add(node);
      }
    }
    sortMenuTree(roots);
    return roots;
  }

  private void sortMenuTree(List<AdminMenuVO> nodes) {
    nodes.sort(
        Comparator.comparing(
            (AdminMenuVO node) -> node.getSortOrder() == null ? 0 : node.getSortOrder()));
    for (AdminMenuVO node : nodes) {
      if (!node.getChildren().isEmpty()) {
        sortMenuTree(node.getChildren());
      }
    }
  }

  public boolean canAccessResource(String resource) {
    if (AdminContext.isSuperAdmin()) {
      return true;
    }
    return AdminContext.getPermissions().contains("crud:" + resource)
        || AdminContext.getPermissions().contains("menu:" + resource);
  }

  public void checkResource(String resource, String action) {
    if (AdminContext.isSuperAdmin()) {
      return;
    }
    Set<String> permissions = AdminContext.getPermissions();
    if (permissions.contains("crud:" + resource)
        || permissions.contains("crud:" + resource + ":" + action)
        || permissions.contains("menu:" + resource)) {
      return;
    }
    throw new BusinessException(403, "无权限操作: " + resource);
  }

  public void checkSystemPermission(String code) {
    if (AdminContext.isSuperAdmin()) {
      return;
    }
    if (!AdminContext.getPermissions().contains(code)) {
      throw new BusinessException(403, "无权限: " + code);
    }
  }

  private boolean hasChildPermission(
      Long parentId, List<AdminPermission> all, Set<String> permissionCodes) {
    for (AdminPermission item : all) {
      if (parentId.equals(item.getParentId())) {
        if (permissionCodes.contains(item.getCode())
            || hasChildPermission(item.getId(), all, permissionCodes)) {
          return true;
        }
      }
    }
    return false;
  }

  public List<AdminPermission> listAllPermissions() {
    return permissionMapper.selectList(
        new LambdaQueryWrapper<AdminPermission>().orderByAsc(AdminPermission::getSortOrder));
  }

  public void saveRolePermissions(Long roleId, List<Long> permissionIds) {
    rolePermissionMapper.delete(
        new LambdaQueryWrapper<AdminRolePermission>().eq(AdminRolePermission::getRoleId, roleId));
    if (permissionIds == null || permissionIds.isEmpty()) {
      return;
    }
    Set<Long> unique = new HashSet<>(permissionIds);
    for (Long permissionId : unique) {
      rolePermissionMapper.insert(
          AdminRolePermission.builder().roleId(roleId).permissionId(permissionId).build());
    }
  }

  public List<Long> listRolePermissionIds(Long roleId) {
    return rolePermissionMapper
        .selectList(
            new LambdaQueryWrapper<AdminRolePermission>().eq(AdminRolePermission::getRoleId, roleId))
        .stream()
        .map(AdminRolePermission::getPermissionId)
        .collect(Collectors.toList());
  }
}
