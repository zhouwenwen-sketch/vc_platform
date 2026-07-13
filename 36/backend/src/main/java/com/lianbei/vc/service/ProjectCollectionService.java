package com.lianbei.vc.service;

import com.lianbei.vc.common.PageResult;
import java.util.List;
import java.util.Map;

/** 项目集（专题合集） */
public interface ProjectCollectionService {

  List<String> listTabs();

  PageResult<Map<String, Object>> pageCollections(int pageNum, int pageSize, String tab);

  Map<String, Object> getCollectionDetail(
      Long id,
      int pageNum,
      int pageSize,
      String keyword,
      String round,
      String industry);

  /** 项目集内实际项目条数（以 projects 列表为准） */
  int getProjectCount(Long id);
}
