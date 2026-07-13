package com.lianbei.vc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianbei.vc.entity.ProjectBusiness;
import com.lianbei.vc.mapper.ProjectBusinessMapper;
import com.lianbei.vc.service.CompanySearchService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CompanySearchServiceImpl implements CompanySearchService {

  private final ProjectBusinessMapper businessMapper;
  private final ObjectMapper objectMapper;
  private List<String> mockCompanies;

  public CompanySearchServiceImpl(ProjectBusinessMapper businessMapper, ObjectMapper objectMapper) {
    this.businessMapper = businessMapper;
    this.objectMapper = objectMapper;
  }

  @Override
  public List<String> search(String keyword, int limit) {
    if (!StringUtils.hasText(keyword)) {
      return List.of();
    }
    int size = Math.min(Math.max(limit, 1), 20);
    String kw = keyword.trim();
    Set<String> names = new LinkedHashSet<>();

    List<ProjectBusiness> rows =
        businessMapper.selectList(
            new LambdaQueryWrapper<ProjectBusiness>()
                .like(ProjectBusiness::getFullName, kw)
                .last("LIMIT " + size));
    for (ProjectBusiness row : rows) {
      if (StringUtils.hasText(row.getFullName())) {
        names.add(row.getFullName().trim());
      }
      if (names.size() >= size) {
        return new ArrayList<>(names);
      }
    }

    for (String name : loadMockCompanies()) {
      if (name.contains(kw)) {
        names.add(name);
      }
      if (names.size() >= size) {
        break;
      }
    }
    return new ArrayList<>(names);
  }

  private List<String> loadMockCompanies() {
    if (mockCompanies != null) {
      return mockCompanies;
    }
    try {
      ClassPathResource resource = new ClassPathResource("config/companies-mock.json");
      mockCompanies =
          objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
    } catch (IOException e) {
      mockCompanies = List.of();
    }
    return mockCompanies;
  }
}
