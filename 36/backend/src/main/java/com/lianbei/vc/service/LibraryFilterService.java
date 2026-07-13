package com.lianbei.vc.service;

import com.lianbei.vc.common.constants.LibraryFilterKeys.FilterGroupDef;
import com.lianbei.vc.entity.LibraryFilterOption;
import java.util.List;
import java.util.Map;

public interface LibraryFilterService {

  Map<String, List<String>> listBundle(String bundleName);

  /** @deprecated 使用 listBundle("project-library") */
  Map<String, List<String>> listProjectLibraryOptions();

  List<FilterGroupDef> listAdminGroups();

  List<LibraryFilterOption> listAdminOptions(String scene, String filterKey);

  LibraryFilterOption create(LibraryFilterOption option);

  LibraryFilterOption update(Long id, LibraryFilterOption option);

  void delete(Long id);

  /** 项目卡片「标签」字段可选值（按筛选列分组） */
  List<Map<String, Object>> listProjectTagOptionGroups();
}
