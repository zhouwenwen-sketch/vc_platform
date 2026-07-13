package com.lianbei.vc.service;

import com.lianbei.vc.entity.News;
import com.lianbei.vc.entity.Project;

/** 快讯/文章与项目库的关联解析与字段同步 */
public interface NewsProjectLinkService {

  /** 按 project_id 优先、再按 project_name 精确匹配解析项目 */
  Project resolveProject(Long projectId, String projectName);

  /** 保存前同步 news 的 project_id 与 project_name */
  void syncNewsFields(News news);
}
