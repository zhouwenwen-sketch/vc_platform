package com.lianbei.vc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lianbei.vc.entity.ProjectTag;
import com.lianbei.vc.entity.ProjectTagRel;
import com.lianbei.vc.mapper.ProjectTagMapper;
import com.lianbei.vc.mapper.ProjectTagRelMapper;
import com.lianbei.vc.service.ProjectTagSyncService;
import java.util.Arrays;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ProjectTagSyncServiceImpl implements ProjectTagSyncService {

  private final ProjectTagMapper tagMapper;
  private final ProjectTagRelMapper tagRelMapper;

  public ProjectTagSyncServiceImpl(ProjectTagMapper tagMapper, ProjectTagRelMapper tagRelMapper) {
    this.tagMapper = tagMapper;
    this.tagRelMapper = tagRelMapper;
  }

  @Override
  public void syncFromTagsField(Long projectId, String tags) {
    if (projectId == null) {
      return;
    }
    tagRelMapper.delete(
        new LambdaQueryWrapper<ProjectTagRel>().eq(ProjectTagRel::getProjectId, projectId));
    if (!StringUtils.hasText(tags)) {
      return;
    }
    Arrays.stream(tags.split("[,，]"))
        .map(String::trim)
        .filter(StringUtils::hasText)
        .forEach(name -> bindTag(projectId, name));
  }

  private void bindTag(Long projectId, String tagName) {
    ProjectTag existing =
        tagMapper.selectOne(
            new LambdaQueryWrapper<ProjectTag>().eq(ProjectTag::getName, tagName).last("LIMIT 1"));
    Long tagId;
    if (existing != null) {
      tagId = existing.getId();
    } else {
      ProjectTag tag = ProjectTag.builder().name(tagName).build();
      tagMapper.insert(tag);
      tagId = tag.getId();
    }
    tagRelMapper.insert(ProjectTagRel.builder().projectId(projectId).tagId(tagId).build());
  }
}
