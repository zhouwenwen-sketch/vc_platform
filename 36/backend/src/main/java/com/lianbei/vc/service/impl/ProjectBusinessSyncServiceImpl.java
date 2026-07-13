package com.lianbei.vc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lianbei.vc.entity.Project;
import com.lianbei.vc.entity.ProjectBusiness;
import com.lianbei.vc.mapper.ProjectBusinessMapper;
import com.lianbei.vc.service.ProjectBusinessSyncService;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ProjectBusinessSyncServiceImpl implements ProjectBusinessSyncService {

  private final ProjectBusinessMapper businessMapper;

  public ProjectBusinessSyncServiceImpl(ProjectBusinessMapper businessMapper) {
    this.businessMapper = businessMapper;
  }

  @Override
  public Map<String, Object> getForAdmin(Long projectId) {
    ProjectBusiness business = findByProjectId(projectId);
    if (business == null) {
      return emptyBusinessMap();
    }
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("fullName", business.getFullName());
    map.put("englishName", business.getEnglishName());
    map.put("legalPerson", business.getLegalPerson());
    map.put("registeredAddress", business.getRegisteredAddress());
    map.put(
        "establishDate",
        business.getEstablishDate() != null ? business.getEstablishDate().toString() : null);
    map.put("unifiedSocialCreditCode", business.getUnifiedSocialCreditCode());
    return map;
  }

  @Override
  public void ensureForProject(Project project) {
    if (project == null || project.getId() == null) {
      return;
    }
    if (findByProjectId(project.getId()) != null) {
      return;
    }
    businessMapper.insert(buildStub(project));
  }

  @Override
  public void upsertFromAdmin(Long projectId, Map<String, Object> business) {
    if (projectId == null) {
      return;
    }
    ProjectBusiness existing = findByProjectId(projectId);
    ProjectBusiness payload = mapToEntity(projectId, business);
    if (existing == null) {
      businessMapper.insert(payload);
      return;
    }
    payload.setId(projectId);
    payload.setProjectId(projectId);
    businessMapper.updateById(payload);
  }

  @Override
  public void deleteByProjectId(Long projectId) {
    if (projectId == null) {
      return;
    }
    businessMapper.deleteById(projectId);
  }

  public ProjectBusiness buildStub(Project project) {
    return ProjectBusiness.builder()
        .id(project.getId())
        .projectId(project.getId())
        .fullName("")
        .registeredAddress(project.getLocation())
        .establishDate(parseFoundingYear(project.getFoundingYear()))
        .dataSource("auto")
        .build();
  }

  private ProjectBusiness mapToEntity(Long projectId, Map<String, Object> business) {
    if (business == null) {
      business = Map.of();
    }
    return ProjectBusiness.builder()
        .id(projectId)
        .projectId(projectId)
        .fullName(stringValue(business.get("fullName")))
        .englishName(nullIfBlank(stringValue(business.get("englishName"))))
        .legalPerson(nullIfBlank(stringValue(business.get("legalPerson"))))
        .registeredAddress(nullIfBlank(stringValue(business.get("registeredAddress"))))
        .establishDate(parseEstablishDate(stringValue(business.get("establishDate"))))
        .unifiedSocialCreditCode(nullIfBlank(stringValue(business.get("unifiedSocialCreditCode"))))
        .dataSource("manual")
        .build();
  }

  private ProjectBusiness findByProjectId(Long projectId) {
    return businessMapper.selectOne(
        new LambdaQueryWrapper<ProjectBusiness>().eq(ProjectBusiness::getProjectId, projectId));
  }

  private Map<String, Object> emptyBusinessMap() {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("fullName", "");
    map.put("englishName", "");
    map.put("legalPerson", "");
    map.put("registeredAddress", "");
    map.put("establishDate", null);
    map.put("unifiedSocialCreditCode", "");
    return map;
  }

  private String stringValue(Object value) {
    return value == null ? "" : String.valueOf(value).trim();
  }

  private String nullIfBlank(String value) {
    return StringUtils.hasText(value) ? value.trim() : null;
  }

  private LocalDate parseEstablishDate(String raw) {
    if (!StringUtils.hasText(raw)) {
      return null;
    }
    String trimmed = raw.trim();
    if (trimmed.matches("\\d{4}-\\d{2}")) {
      return LocalDate.parse(trimmed + "-01");
    }
    if (trimmed.matches("\\d{4}-\\d{2}-\\d{2}")) {
      return LocalDate.parse(trimmed);
    }
    if (trimmed.matches("\\d{4}")) {
      return LocalDate.parse(trimmed + "-01-01");
    }
    return null;
  }

  private LocalDate parseFoundingYear(String foundingYear) {
    if (!StringUtils.hasText(foundingYear)) {
      return null;
    }
    String year = foundingYear.replace("年", "").trim();
    if (year.matches("\\d{4}")) {
      return LocalDate.parse(year + "-01-01");
    }
    return null;
  }
}
