package com.lianbei.vc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lianbei.vc.common.constants.LibraryFilterKeys;
import com.lianbei.vc.common.constants.LibraryFilterKeys.FilterGroupDef;
import com.lianbei.vc.entity.LibraryFilterOption;
import com.lianbei.vc.exception.BusinessException;
import com.lianbei.vc.mapper.LibraryFilterOptionMapper;
import com.lianbei.vc.service.LibraryFilterService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class LibraryFilterServiceImpl implements LibraryFilterService {

  private final LibraryFilterOptionMapper libraryFilterOptionMapper;

  public LibraryFilterServiceImpl(LibraryFilterOptionMapper libraryFilterOptionMapper) {
    this.libraryFilterOptionMapper = libraryFilterOptionMapper;
  }

  @Override
  public Map<String, List<String>> listBundle(String bundleName) {
    LinkedHashMap<String, String> sources = LibraryFilterKeys.bundleSource(bundleName);
    if (sources == null) {
      throw new BusinessException("不支持的筛选项包: " + bundleName);
    }
    Map<String, List<String>> result = new LinkedHashMap<>();
    sources.forEach(
        (outputKey, source) -> {
          String[] parts = source.split(":", 2);
          result.put(outputKey, listEnabledValues(parts[0], parts[1]));
        });
    return result;
  }

  @Override
  public Map<String, List<String>> listProjectLibraryOptions() {
    return listBundle("project-library");
  }

  @Override
  public List<FilterGroupDef> listAdminGroups() {
    return LibraryFilterKeys.ADMIN_GROUPS;
  }

  @Override
  public List<LibraryFilterOption> listAdminOptions(String scene, String filterKey) {
    validateGroup(scene, filterKey);
    return libraryFilterOptionMapper.selectList(
        new LambdaQueryWrapper<LibraryFilterOption>()
            .eq(LibraryFilterOption::getScene, scene)
            .eq(LibraryFilterOption::getFilterKey, filterKey)
            .orderByAsc(LibraryFilterOption::getSortOrder)
            .orderByAsc(LibraryFilterOption::getId));
  }

  @Override
  public LibraryFilterOption create(LibraryFilterOption option) {
    validateGroup(option.getScene(), option.getFilterKey());
    if (!StringUtils.hasText(option.getLabel())) {
      throw new BusinessException("标签名称不能为空");
    }
    String label = option.getLabel().trim();
    String value = StringUtils.hasText(option.getValue()) ? option.getValue().trim() : label;
    LibraryFilterOption entity =
        LibraryFilterOption.builder()
            .scene(option.getScene())
            .filterKey(option.getFilterKey())
            .label(label)
            .value(value)
            .sortOrder(
                option.getSortOrder() != null
                    ? option.getSortOrder()
                    : nextSortOrder(option.getScene(), option.getFilterKey()))
            .enabled(option.getEnabled() != null ? option.getEnabled() : 1)
            .createTime(LocalDateTime.now())
            .updateTime(LocalDateTime.now())
            .build();
    libraryFilterOptionMapper.insert(entity);
    return entity;
  }

  @Override
  public LibraryFilterOption update(Long id, LibraryFilterOption option) {
    LibraryFilterOption existing = requireOption(id);
    if (StringUtils.hasText(option.getLabel())) {
      existing.setLabel(option.getLabel().trim());
    }
    if (option.getValue() != null) {
      existing.setValue(
          StringUtils.hasText(option.getValue()) ? option.getValue().trim() : existing.getLabel());
    }
    if (option.getSortOrder() != null) {
      existing.setSortOrder(option.getSortOrder());
    }
    if (option.getEnabled() != null) {
      existing.setEnabled(option.getEnabled());
    }
    existing.setUpdateTime(LocalDateTime.now());
    libraryFilterOptionMapper.updateById(existing);
    return existing;
  }

  @Override
  public void delete(Long id) {
    requireOption(id);
    libraryFilterOptionMapper.deleteById(id);
  }

  @Override
  public List<Map<String, Object>> listProjectTagOptionGroups() {
    List<Map<String, Object>> groups = new ArrayList<>();
    groups.add(
        optionGroup(
            "所属行业",
            listEnabledValues(LibraryFilterKeys.SCENE_SHARED, LibraryFilterKeys.INDUSTRY)));
    groups.add(
        optionGroup(
            "项目优势",
            listEnabledValues(
                LibraryFilterKeys.SCENE_PROJECT_LIBRARY, LibraryFilterKeys.ADVANTAGE)));
    return groups;
  }

  private Map<String, Object> optionGroup(String label, List<String> values) {
    Map<String, Object> group = new LinkedHashMap<>();
    group.put("label", label);
    group.put("values", values);
    return group;
  }

  private List<String> listEnabledValues(String scene, String filterKey) {
    if (LibraryFilterKeys.SCENE_SHARED.equals(scene)
        && LibraryFilterKeys.INDUSTRY.equals(filterKey)) {
      List<String> shared = queryEnabledValues(scene, filterKey);
      if (!shared.isEmpty()) {
        return shared;
      }
      return queryEnabledValues(LibraryFilterKeys.SCENE_PROJECT_LIBRARY, filterKey);
    }
    return queryEnabledValues(scene, filterKey);
  }

  private List<String> queryEnabledValues(String scene, String filterKey) {
    return libraryFilterOptionMapper
        .selectList(
            new LambdaQueryWrapper<LibraryFilterOption>()
                .eq(LibraryFilterOption::getScene, scene)
                .eq(LibraryFilterOption::getFilterKey, filterKey)
                .eq(LibraryFilterOption::getEnabled, 1)
                .orderByAsc(LibraryFilterOption::getSortOrder)
                .orderByAsc(LibraryFilterOption::getId))
        .stream()
        .map(LibraryFilterOption::getValue)
        .collect(Collectors.toList());
  }

  private int nextSortOrder(String scene, String filterKey) {
    LibraryFilterOption last =
        libraryFilterOptionMapper.selectOne(
            new LambdaQueryWrapper<LibraryFilterOption>()
                .eq(LibraryFilterOption::getScene, scene)
                .eq(LibraryFilterOption::getFilterKey, filterKey)
                .orderByDesc(LibraryFilterOption::getSortOrder)
                .last("LIMIT 1"));
    return last == null || last.getSortOrder() == null ? 1 : last.getSortOrder() + 1;
  }

  private LibraryFilterOption requireOption(Long id) {
    if (id == null) {
      throw new BusinessException("标签不存在");
    }
    LibraryFilterOption existing = libraryFilterOptionMapper.selectById(id);
    if (existing == null) {
      throw new BusinessException("标签不存在");
    }
    return existing;
  }

  private void validateGroup(String scene, String filterKey) {
    if (LibraryFilterKeys.findGroup(scene, filterKey) == null) {
      throw new BusinessException("不支持的筛选项: " + scene + "/" + filterKey);
    }
  }
}
