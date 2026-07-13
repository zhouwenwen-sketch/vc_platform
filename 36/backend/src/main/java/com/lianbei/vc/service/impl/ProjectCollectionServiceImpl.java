package com.lianbei.vc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianbei.vc.common.FilterParamUtils;
import com.lianbei.vc.common.PageResult;
import com.lianbei.vc.entity.ProjectCollection;
import com.lianbei.vc.entity.ProjectCollectionTab;
import com.lianbei.vc.mapper.ProjectCollectionMapper;
import com.lianbei.vc.mapper.ProjectCollectionTabMapper;
import com.lianbei.vc.service.ProjectCollectionService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ProjectCollectionServiceImpl implements ProjectCollectionService {

  private final ProjectCollectionMapper projectCollectionMapper;
  private final ProjectCollectionTabMapper projectCollectionTabMapper;
  private final ObjectMapper objectMapper;

  public ProjectCollectionServiceImpl(
      ProjectCollectionMapper projectCollectionMapper,
      ProjectCollectionTabMapper projectCollectionTabMapper,
      ObjectMapper objectMapper) {
    this.projectCollectionMapper = projectCollectionMapper;
    this.projectCollectionTabMapper = projectCollectionTabMapper;
    this.objectMapper = objectMapper;
  }

  @Override
  public List<String> listTabs() {
    LambdaQueryWrapper<ProjectCollectionTab> wrapper = new LambdaQueryWrapper<>();
    wrapper.orderByAsc(ProjectCollectionTab::getSortOrder).orderByAsc(ProjectCollectionTab::getId);
    List<ProjectCollectionTab> tabs = projectCollectionTabMapper.selectList(wrapper);
    List<String> names = new ArrayList<>();
    names.add("全部");
    tabs.stream().map(ProjectCollectionTab::getName).forEach(names::add);
    return names;
  }

  @Override
  public PageResult<Map<String, Object>> pageCollections(int pageNum, int pageSize, String tab) {
    String normalizedTab = tab == null ? "" : tab.trim();
    LambdaQueryWrapper<ProjectCollection> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(ProjectCollection::getStatus, 1);
    if (!normalizedTab.isEmpty() && !"全部".equals(normalizedTab)) {
      wrapper.eq(ProjectCollection::getCategory, normalizedTab);
    }
    wrapper
        .orderByAsc(ProjectCollection::getSortOrder)
        .orderByAsc(ProjectCollection::getId);
    List<ProjectCollection> all = projectCollectionMapper.selectList(wrapper);
    List<Map<String, Object>> cards =
        all.stream().map(this::toListCard).collect(Collectors.toList());

    int from = Math.max(0, (pageNum - 1) * pageSize);
    int to = Math.min(cards.size(), from + pageSize);
    List<Map<String, Object>> page = from >= cards.size() ? List.of() : cards.subList(from, to);
    return PageResult.of(cards.size(), page);
  }

  @Override
  public Map<String, Object> getCollectionDetail(
      Long id,
      int pageNum,
      int pageSize,
      String keyword,
      String round,
      String industry) {
    ProjectCollection collection = projectCollectionMapper.selectById(id);
    if (collection == null || collection.getStatus() == null || collection.getStatus() != 1) {
      throw new IllegalArgumentException("项目集不存在");
    }

    List<Map<String, Object>> projects = parseProjects(collection.getProjectsData());
    String kw = keyword == null ? "" : keyword.trim().toLowerCase();
    List<String> roundFilters = FilterParamUtils.splitValues(round);
    List<String> industryFilters = FilterParamUtils.splitValues(industry);

    List<Map<String, Object>> filtered = new ArrayList<>();
    for (Map<String, Object> project : projects) {
      if (!roundFilters.isEmpty()
          && !roundFilters.contains(String.valueOf(project.get("round")))) {
        continue;
      }
      String tags = joinTags(project.get("tags"));
      if (!industryFilters.isEmpty() && !matchesAnyIndustry(tags, project, industryFilters)) {
        continue;
      }
      if (!kw.isEmpty()) {
        String blob =
            (String.valueOf(project.get("name"))
                    + String.valueOf(project.get("companyDesc"))
                    + tags
                    + String.valueOf(project.get("location")))
                .toLowerCase();
        if (!blob.contains(kw)) {
          continue;
        }
      }
      filtered.add(enrichProject(project));
    }

    int from = Math.max(0, (pageNum - 1) * pageSize);
    int to = Math.min(filtered.size(), from + pageSize);
    List<Map<String, Object>> page =
        from >= filtered.size() ? List.of() : filtered.subList(from, to);

    Map<String, Object> detail = new LinkedHashMap<>();
    detail.put("id", collection.getId());
    detail.put("title", collection.getTitle());
    detail.put("cover", collection.getCover());
    detail.put("coverTitle", collection.getCoverTitle());
    detail.put("badge", collection.getBadge());
    detail.put("projectCount", projects.size());
    detail.put("date", formatDate(collection));
    detail.put("description", collection.getDescription());
    detail.put("summary", collection.getSummary());
    detail.put("total", filtered.size());
    detail.put("list", page);
    detail.put("hasMore", to < filtered.size());
    return detail;
  }

  @Override
  public int getProjectCount(Long id) {
    ProjectCollection collection = projectCollectionMapper.selectById(id);
    if (collection == null) {
      return 0;
    }
    return parseProjects(collection.getProjectsData()).size();
  }

  private Map<String, Object> toListCard(ProjectCollection collection) {
    Map<String, Object> card = new LinkedHashMap<>();
    card.put("id", collection.getId());
    card.put("title", collection.getTitle());
    card.put("cover", collection.getCover());
    card.put("coverTitle", collection.getCoverTitle());
    card.put("projectCount", parseProjects(collection.getProjectsData()).size());
    card.put("date", formatDate(collection));
    card.put("summary", collection.getSummary());
    card.put("category", collection.getCategory());
    return card;
  }

  private List<Map<String, Object>> parseProjects(String projectsData) {
    if (projectsData == null || projectsData.isBlank()) {
      return List.of();
    }
    try {
      return objectMapper.readValue(projectsData, new TypeReference<>() {});
    } catch (Exception ex) {
      return List.of();
    }
  }

  private String formatDate(ProjectCollection collection) {
    return collection.getCollectionDate() == null
        ? null
        : collection.getCollectionDate().toString();
  }

  private Map<String, Object> enrichProject(Map<String, Object> project) {
    Map<String, Object> out = new HashMap<>(project);
    String tags = joinTags(project.get("tags"));
    List<String> parts = new ArrayList<>();
    if (!tags.isEmpty()) {
      parts.add(tags.replace(",", " | "));
    }
    if (project.get("location") != null && !String.valueOf(project.get("location")).isEmpty()) {
      parts.add(String.valueOf(project.get("location")));
    }
    if (project.get("foundingYear") != null
        && !String.valueOf(project.get("foundingYear")).isEmpty()) {
      parts.add(String.valueOf(project.get("foundingYear")));
    }
    out.put("metaLine", String.join(" | ", parts));
    return out;
  }

  private boolean matchesAnyIndustry(
      String tags, Map<String, Object> project, List<String> industryFilters) {
    String category = String.valueOf(project.get("category"));
    for (String industry : industryFilters) {
      if (industry.equals(category) || tags.contains(industry)) {
        return true;
      }
    }
    return false;
  }

  private String joinTags(Object tagsObj) {
    if (tagsObj instanceof List<?> list) {
      return list.stream().map(String::valueOf).collect(Collectors.joining(","));
    }
    return tagsObj == null ? "" : String.valueOf(tagsObj);
  }
}
