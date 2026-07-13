package com.lianbei.vc.admin.service;

import com.lianbei.vc.common.PageResult;
import com.lianbei.vc.dto.response.SearchProjectVO;
import com.lianbei.vc.service.ProjectService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AdminLookupService {

  private final AdminPermissionService permissionService;
  private final ProjectService projectService;

  public AdminLookupService(
      AdminPermissionService permissionService, ProjectService projectService) {
    this.permissionService = permissionService;
    this.projectService = projectService;
  }

  public List<SearchProjectVO> lookupProjects(String keyword, int limit) {
    permissionService.checkResource("news", "list");
    if (!StringUtils.hasText(keyword)) {
      return List.of();
    }
    int size = Math.min(Math.max(limit, 1), 20);
    PageResult<SearchProjectVO> page =
        projectService.searchProjects(keyword.trim(), 1, size);
    return page.getList() == null ? List.of() : page.getList();
  }
}
