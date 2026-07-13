package com.lianbei.vc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lianbei.vc.common.ProjectKeywordQuery;
import com.lianbei.vc.entity.News;
import com.lianbei.vc.entity.Project;
import com.lianbei.vc.exception.BusinessException;
import com.lianbei.vc.mapper.ProjectMapper;
import com.lianbei.vc.service.NewsProjectLinkService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class NewsProjectLinkServiceImpl implements NewsProjectLinkService {

  private static final int AUTO_MATCH_LIMIT = 5;

  private final ProjectMapper projectMapper;

  public NewsProjectLinkServiceImpl(ProjectMapper projectMapper) {
    this.projectMapper = projectMapper;
  }

  @Override
  public Project resolveProject(Long projectId, String projectName) {
    if (projectId != null) {
      Project byId = projectMapper.selectById(projectId);
      if (byId != null) {
        return byId;
      }
    }
    if (!StringUtils.hasText(projectName)) {
      return null;
    }
    return projectMapper.selectOne(
        new LambdaQueryWrapper<Project>()
            .eq(Project::getName, projectName.trim())
            .last("LIMIT 1"));
  }

  @Override
  public void syncNewsFields(News news) {
    if (news == null) {
      return;
    }
    if (news.getProjectId() != null) {
      Project project = projectMapper.selectById(news.getProjectId());
      if (project == null) {
        throw new BusinessException("关联项目不存在，请重新搜索选择");
      }
      news.setProjectId(project.getId());
      news.setProjectName(project.getName());
      return;
    }
    if (!StringUtils.hasText(news.getProjectName())) {
      news.setProjectId(null);
      news.setProjectName(null);
      return;
    }
    String name = news.getProjectName().trim();
    Project exact = findExactByName(name);
    if (exact != null) {
      news.setProjectId(exact.getId());
      news.setProjectName(exact.getName());
      return;
    }
    List<Project> candidates = searchCandidates(name, AUTO_MATCH_LIMIT);
    if (candidates.isEmpty()) {
      news.setProjectId(null);
      news.setProjectName(name);
      return;
    }
    if (candidates.size() == 1) {
      news.setProjectId(candidates.get(0).getId());
      news.setProjectName(candidates.get(0).getName());
      return;
    }
    List<Project> caseInsensitive =
        candidates.stream().filter(item -> name.equalsIgnoreCase(item.getName())).toList();
    if (caseInsensitive.size() == 1) {
      news.setProjectId(caseInsensitive.get(0).getId());
      news.setProjectName(caseInsensitive.get(0).getName());
      return;
    }
    throw new BusinessException(
        "项目名称「"
            + name
            + "」匹配到多个项目，请输入更完整的关键字并从列表中选择");
  }

  private Project findExactByName(String name) {
    return projectMapper.selectOne(
        new LambdaQueryWrapper<Project>().eq(Project::getName, name).last("LIMIT 1"));
  }

  private List<Project> searchCandidates(String keyword, int limit) {
    LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
    ProjectKeywordQuery.apply(wrapper, keyword);
    wrapper.orderByDesc(Project::getUpdateTime);
    return projectMapper.selectList(wrapper.last("LIMIT " + Math.max(limit, 1)));
  }
}
