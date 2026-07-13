package com.lianbei.vc.service;

import com.lianbei.vc.entity.Project;
import java.util.Map;

/** 项目与工商信息 1:1 同步（id 与 project.id 一致） */
public interface ProjectBusinessSyncService {

  Map<String, Object> getForAdmin(Long projectId);

  void ensureForProject(Project project);

  void upsertFromAdmin(Long projectId, Map<String, Object> business);

  void deleteByProjectId(Long projectId);
}
