package com.lianbei.vc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lianbei.vc.entity.Project;
import com.lianbei.vc.entity.ProjectTag;
import com.lianbei.vc.entity.ProjectTagRel;
import com.lianbei.vc.entity.ProjectTeamMember;
import com.lianbei.vc.entity.UserProject;
import com.lianbei.vc.exception.BusinessException;
import com.lianbei.vc.mapper.ProjectMapper;
import com.lianbei.vc.mapper.ProjectTagMapper;
import com.lianbei.vc.mapper.ProjectTagRelMapper;
import com.lianbei.vc.mapper.ProjectTeamMemberMapper;
import com.lianbei.vc.mapper.UserProjectMapper;
import com.lianbei.vc.service.ProjectBusinessSyncService;
import com.lianbei.vc.service.ProjectOnboardPublishService;
import com.lianbei.vc.service.ProjectService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ProjectOnboardPublishServiceImpl implements ProjectOnboardPublishService {

  private final ProjectMapper projectMapper;
  private final ProjectBusinessSyncService projectBusinessSyncService;
  private final ProjectTeamMemberMapper teamMemberMapper;
  private final ProjectTagMapper tagMapper;
  private final ProjectTagRelMapper tagRelMapper;
  private final UserProjectMapper userProjectMapper;
  private final ProjectService projectService;

  public ProjectOnboardPublishServiceImpl(
      ProjectMapper projectMapper,
      ProjectBusinessSyncService projectBusinessSyncService,
      ProjectTeamMemberMapper teamMemberMapper,
      ProjectTagMapper tagMapper,
      ProjectTagRelMapper tagRelMapper,
      UserProjectMapper userProjectMapper,
      ProjectService projectService) {
    this.projectMapper = projectMapper;
    this.projectBusinessSyncService = projectBusinessSyncService;
    this.teamMemberMapper = teamMemberMapper;
    this.tagMapper = tagMapper;
    this.tagRelMapper = tagRelMapper;
    this.userProjectMapper = userProjectMapper;
    this.projectService = projectService;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Long publish(Long userId, Map<String, Object> applyData) {
    if (userId == null || applyData == null || applyData.isEmpty()) {
      throw new BusinessException("落库数据无效");
    }

    String projectName = text(applyData.get("projectName"));
    if (!StringUtils.hasText(projectName)) {
      throw new BusinessException("项目名称不能为空");
    }
    if (projectService.existsByName(projectName)) {
      throw new BusinessException("项目名称已被收录，无法落库");
    }

    List<String> industries = readStringList(applyData.get("industries"));
    String location = buildLocation(applyData);
    String region = buildRegion(applyData);
    String financingRound = text(applyData.get("financingRound"));
    boolean needFinancing = "yes".equals(text(applyData.get("needFinancing")));

    Project project =
        Project.builder()
            .name(projectName)
            .companyDesc(text(applyData.get("oneLiner")))
            .intro(text(applyData.get("intro")))
            .logoUrl(text(applyData.get("logoUrl")))
            .website(text(applyData.get("website")))
            .round(financingRound)
            .latestRound(financingRound)
            .location(location)
            .region(region)
            .category(industries.isEmpty() ? null : industries.get(0))
            .tags(industries.isEmpty() ? null : String.join(",", industries))
            .foundingYear(extractYear(text(applyData.get("establishDate"))))
            .isFinancing(needFinancing ? 1 : 0)
            .isCertified(1)
            .status(1)
            .build();
    projectMapper.insert(project);

    project.setSlug("p-" + project.getId());
    projectMapper.updateById(project);

    String entityName = text(applyData.get("entityName"));
    Map<String, Object> business = new LinkedHashMap<>();
    business.put("fullName", StringUtils.hasText(entityName) ? entityName : "");
    business.put("registeredAddress", location);
    String establishDate = text(applyData.get("establishDate"));
    if (StringUtils.hasText(establishDate)) {
      business.put("establishDate", establishDate);
    }
    projectBusinessSyncService.upsertFromAdmin(project.getId(), business);

    List<Map<String, Object>> teamMembers =
        (List<Map<String, Object>>) applyData.getOrDefault("teamMembers", List.of());
    int sort = 0;
    for (Map<String, Object> member : teamMembers) {
      sort++;
      teamMemberMapper.insert(
          ProjectTeamMember.builder()
              .projectId(project.getId())
              .memberName(text(member.get("name")))
              .title(text(member.get("title")))
              .avatarUrl(text(member.get("avatar")))
              .bio(text(member.get("bio")))
              .sortOrder(sort)
              .build());
    }

    for (String industry : industries) {
      bindTag(project.getId(), industry);
    }

    Map<String, Object> certifier = (Map<String, Object>) applyData.get("certifier");
    String jobType = certifier != null ? text(certifier.get("jobType")) : null;
    userProjectMapper.insert(
        UserProject.builder()
            .userId(userId)
            .projectId(project.getId())
            .role("owner")
            .jobType(jobType)
            .isCertifier(1)
            .build());

    return project.getId();
  }

  private void bindTag(Long projectId, String tagName) {
    if (!StringUtils.hasText(tagName)) {
      return;
    }
    String name = tagName.trim();
    ProjectTag existing =
        tagMapper.selectOne(
            new LambdaQueryWrapper<ProjectTag>().eq(ProjectTag::getName, name).last("LIMIT 1"));
    Long tagId;
    if (existing != null) {
      tagId = existing.getId();
    } else {
      ProjectTag tag = ProjectTag.builder().name(name).build();
      tagMapper.insert(tag);
      tagId = tag.getId();
    }
    Long count =
        tagRelMapper.selectCount(
            new LambdaQueryWrapper<ProjectTagRel>()
                .eq(ProjectTagRel::getProjectId, projectId)
                .eq(ProjectTagRel::getTagId, tagId));
    if (count != null && count > 0) {
      return;
    }
    tagRelMapper.insert(
        ProjectTagRel.builder().projectId(projectId).tagId(tagId).build());
  }

  private String buildLocation(Map<String, Object> data) {
    if ("海外".equals(text(data.get("country")))) {
      return text(data.get("overseasLocation"));
    }
    String province = text(data.get("province"));
    String city = text(data.get("city"));
    if (StringUtils.hasText(province) && StringUtils.hasText(city)) {
      return province + " " + city;
    }
    return StringUtils.hasText(province) ? province : city;
  }

  private String buildRegion(Map<String, Object> data) {
    if ("海外".equals(text(data.get("country")))) {
      return "海外";
    }
    return text(data.get("province"));
  }

  private String extractYear(String establishDate) {
    if (!StringUtils.hasText(establishDate)) {
      return null;
    }
    String trimmed = establishDate.trim();
    if (trimmed.length() >= 4) {
      return trimmed.substring(0, 4);
    }
    return trimmed;
  }

  private LocalDate parseEstablishDate(String establishDate) {
    if (!StringUtils.hasText(establishDate)) {
      return null;
    }
    String trimmed = establishDate.trim();
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

  private List<String> readStringList(Object raw) {
    List<String> result = new ArrayList<>();
    if (!(raw instanceof List<?> list)) {
      return result;
    }
    for (Object item : list) {
      if (item != null && StringUtils.hasText(String.valueOf(item))) {
        result.add(String.valueOf(item).trim());
      }
    }
    return result;
  }

  private String text(Object value) {
    if (value == null) {
      return null;
    }
    String str = String.valueOf(value).trim();
    return str.isEmpty() ? null : str;
  }
}
