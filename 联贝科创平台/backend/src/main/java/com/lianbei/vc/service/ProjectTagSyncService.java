package com.lianbei.vc.service;

/** 同步 project.tags 与 project_tag / project_tag_rel */
public interface ProjectTagSyncService {

  void syncFromTagsField(Long projectId, String tags);
}
