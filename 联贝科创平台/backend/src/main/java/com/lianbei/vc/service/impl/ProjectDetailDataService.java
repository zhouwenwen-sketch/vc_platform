package com.lianbei.vc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lianbei.vc.dto.response.ProjectDetailVO;
import com.lianbei.vc.entity.Project;
import com.lianbei.vc.entity.ProjectBusiness;
import com.lianbei.vc.entity.ProjectFinancing;
import com.lianbei.vc.entity.ProjectFinancingInvestor;
import com.lianbei.vc.entity.ProjectDynamic;
import com.lianbei.vc.entity.ProjectShareholder;
import com.lianbei.vc.entity.ProjectTeamMember;
import com.lianbei.vc.entity.ProjectTag;
import com.lianbei.vc.entity.ProjectTagRel;
import com.lianbei.vc.mapper.ProjectBusinessMapper;
import com.lianbei.vc.mapper.ProjectDynamicMapper;
import com.lianbei.vc.mapper.ProjectFinancingInvestorMapper;
import com.lianbei.vc.mapper.ProjectFinancingMapper;
import com.lianbei.vc.mapper.ProjectShareholderMapper;
import com.lianbei.vc.mapper.ProjectTagMapper;
import com.lianbei.vc.mapper.ProjectTagRelMapper;
import com.lianbei.vc.mapper.ProjectTeamMemberMapper;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** 项目详情子表数据访问（工商/融资/股东/团队/标签） */
@Service
public class ProjectDetailDataService {

  private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

  private final ProjectBusinessMapper businessMapper;
  private final ProjectFinancingMapper financingMapper;
  private final ProjectFinancingInvestorMapper financingInvestorMapper;
  private final ProjectShareholderMapper shareholderMapper;
  private final ProjectTeamMemberMapper teamMemberMapper;
  private final ProjectDynamicMapper dynamicMapper;
  private final ProjectTagMapper tagMapper;
  private final ProjectTagRelMapper tagRelMapper;

  public ProjectDetailDataService(
      ProjectBusinessMapper businessMapper,
      ProjectFinancingMapper financingMapper,
      ProjectFinancingInvestorMapper financingInvestorMapper,
      ProjectShareholderMapper shareholderMapper,
      ProjectTeamMemberMapper teamMemberMapper,
      ProjectDynamicMapper dynamicMapper,
      ProjectTagMapper tagMapper,
      ProjectTagRelMapper tagRelMapper) {
    this.businessMapper = businessMapper;
    this.financingMapper = financingMapper;
    this.financingInvestorMapper = financingInvestorMapper;
    this.shareholderMapper = shareholderMapper;
    this.teamMemberMapper = teamMemberMapper;
    this.dynamicMapper = dynamicMapper;
    this.tagMapper = tagMapper;
    this.tagRelMapper = tagRelMapper;
  }

  public ProjectBusiness getBusiness(Long projectId) {
    if (projectId == null) {
      return null;
    }
    return businessMapper.selectOne(
        new LambdaQueryWrapper<ProjectBusiness>().eq(ProjectBusiness::getProjectId, projectId));
  }

  public String getBusinessFullName(Long projectId) {
    ProjectBusiness business = getBusiness(projectId);
    if (business != null && StringUtils.hasText(business.getFullName())) {
      return business.getFullName().trim();
    }
    return "";
  }

  public List<ProjectDynamic> listDynamics(Long projectId) {
    if (projectId == null) {
      return List.of();
    }
    return dynamicMapper.selectList(
        new LambdaQueryWrapper<ProjectDynamic>()
            .eq(ProjectDynamic::getProjectId, projectId)
            .orderByDesc(ProjectDynamic::getEventDate)
            .orderByDesc(ProjectDynamic::getCreateTime)
            .orderByAsc(ProjectDynamic::getSortOrder));
  }

  public List<String> listTagNames(Long projectId) {
    if (projectId == null) {
      return List.of();
    }
    List<ProjectTagRel> rels =
        tagRelMapper.selectList(
            new LambdaQueryWrapper<ProjectTagRel>().eq(ProjectTagRel::getProjectId, projectId));
    if (rels.isEmpty()) {
      return List.of();
    }
    List<Long> tagIds = rels.stream().map(ProjectTagRel::getTagId).collect(Collectors.toList());
    return tagMapper.selectBatchIds(tagIds).stream()
        .map(ProjectTag::getName)
        .filter(StringUtils::hasText)
        .collect(Collectors.toList());
  }

  public List<String> parseLegacyTags(String tags) {
    if (!StringUtils.hasText(tags)) {
      return new ArrayList<>();
    }
    return Arrays.stream(tags.split("[,，]"))
        .map(String::trim)
        .filter(StringUtils::hasText)
        .collect(Collectors.toList());
  }

  public List<ProjectDetailVO.FinancingHistoryVO> listFinancingHistory(Project project) {
    if (project == null || project.getId() == null) {
      return Collections.emptyList();
    }
    List<ProjectFinancing> rows =
        financingMapper.selectList(
            new LambdaQueryWrapper<ProjectFinancing>()
                .eq(ProjectFinancing::getProjectId, project.getId())
                .orderByAsc(ProjectFinancing::getSortOrder)
                .orderByDesc(ProjectFinancing::getFinancingDate));
    if (rows.isEmpty()) {
      return fallbackFinancingFromProject(project);
    }
    List<ProjectDetailVO.FinancingHistoryVO> list = new ArrayList<>();
    for (ProjectFinancing row : rows) {
      list.add(
          ProjectDetailVO.FinancingHistoryVO.builder()
              .date(formatFinancingDate(row.getFinancingDate()))
              .round(StringUtils.hasText(row.getRound()) ? row.getRound() : project.getRound())
              .amount(StringUtils.hasText(row.getAmount()) ? row.getAmount() : "未透露")
              .investors(listInvestorNames(row.getId()))
              .build());
    }
    return list;
  }

  public ProjectDetailVO.BusinessInfoVO buildBusinessInfo(Project project) {
    ProjectBusiness business = getBusiness(project.getId());
    if (business != null) {
      return ProjectDetailVO.BusinessInfoVO.builder()
          .fullName(StringUtils.hasText(business.getFullName()) ? business.getFullName() : "-")
          .englishName(
              StringUtils.hasText(business.getEnglishName()) ? business.getEnglishName() : "-")
          .legalPerson(
              StringUtils.hasText(business.getLegalPerson()) ? business.getLegalPerson() : "-")
          .address(
              StringUtils.hasText(business.getRegisteredAddress())
                  ? business.getRegisteredAddress()
                  : project.getLocation())
          .establishDate(
              business.getEstablishDate() != null
                  ? formatMonthDate(business.getEstablishDate())
                  : fallbackEstablish(project))
          .build();
    }
    return ProjectDetailVO.BusinessInfoVO.builder()
        .fullName("-")
        .englishName("-")
        .legalPerson("-")
        .address(project.getLocation())
        .establishDate(fallbackEstablish(project))
        .build();
  }

  public List<ProjectDetailVO.ShareholderVO> listShareholders(Long projectId) {
    if (projectId == null) {
      return Collections.emptyList();
    }
    return shareholderMapper
        .selectList(
            new LambdaQueryWrapper<ProjectShareholder>()
                .eq(ProjectShareholder::getProjectId, projectId)
                .orderByAsc(ProjectShareholder::getSortOrder))
        .stream()
        .map(
            s ->
                ProjectDetailVO.ShareholderVO.builder()
                    .name(s.getShareholderName())
                    .ratio(s.getRatio())
                    .capital(s.getCapital())
                    .capitalDate(
                        StringUtils.hasText(s.getCapitalDate())
                            ? formatMonthDate(s.getCapitalDate())
                            : "-")
                    .build())
        .collect(Collectors.toList());
  }

  public List<ProjectDetailVO.TeamMemberVO> listTeamMembers(Long projectId) {
    if (projectId == null) {
      return Collections.emptyList();
    }
    return teamMemberMapper
        .selectList(
            new LambdaQueryWrapper<ProjectTeamMember>()
                .eq(ProjectTeamMember::getProjectId, projectId)
                .orderByAsc(ProjectTeamMember::getSortOrder))
        .stream()
        .map(
            m ->
                ProjectDetailVO.TeamMemberVO.builder()
                    .name(m.getMemberName())
                    .title(m.getTitle())
                    .avatar(m.getAvatarUrl())
                    .bio(m.getBio())
                    .build())
        .collect(Collectors.toList());
  }

  public String formatInvestorsForList(Project project) {
    ProjectFinancing latest = getLatestFinancing(project.getId());
    if (latest == null) {
      return "-";
    }
    List<String> investors = listInvestorNames(latest.getId());
    return investors.isEmpty() ? "-" : String.join("、", investors);
  }

  public String formatEventDate(Project project) {
    ProjectFinancing latest = getLatestFinancing(project.getId());
    if (latest != null && latest.getFinancingDate() != null) {
      return formatMonthDate(latest.getFinancingDate());
    }
    if (project.getLatestFinancingDate() != null) {
      return formatMonthDate(project.getLatestFinancingDate());
    }
    if (project.getCreateTime() != null) {
      return MONTH_FMT.format(project.getCreateTime());
    }
    return "";
  }

  public String formatEventAmount(Project project) {
    if (StringUtils.hasText(project.getLatestAmount())) {
      return project.getLatestAmount();
    }
    if (StringUtils.hasText(project.getInvestmentAmount())) {
      return project.getInvestmentAmount();
    }
    ProjectFinancing latest = getLatestFinancing(project.getId());
    if (latest != null && StringUtils.hasText(latest.getAmount())) {
      return latest.getAmount();
    }
    return "未透露";
  }

  private ProjectFinancing getLatestFinancing(Long projectId) {
    if (projectId == null) {
      return null;
    }
    ProjectFinancing marked =
        financingMapper.selectOne(
            new LambdaQueryWrapper<ProjectFinancing>()
                .eq(ProjectFinancing::getProjectId, projectId)
                .eq(ProjectFinancing::getIsLatest, 1)
                .last("LIMIT 1"));
    if (marked != null) {
      return marked;
    }
    List<ProjectFinancing> rows =
        financingMapper.selectList(
            new LambdaQueryWrapper<ProjectFinancing>()
                .eq(ProjectFinancing::getProjectId, projectId)
                .orderByDesc(ProjectFinancing::getFinancingDate)
                .orderByAsc(ProjectFinancing::getSortOrder)
                .last("LIMIT 1"));
    return rows.isEmpty() ? null : rows.get(0);
  }

  private List<String> listInvestorNames(Long financingId) {
    return financingInvestorMapper
        .selectList(
            new LambdaQueryWrapper<ProjectFinancingInvestor>()
                .eq(ProjectFinancingInvestor::getFinancingId, financingId))
        .stream()
        .map(ProjectFinancingInvestor::getInvestorName)
        .filter(StringUtils::hasText)
        .collect(Collectors.toList());
  }

  private List<ProjectDetailVO.FinancingHistoryVO> fallbackFinancingFromProject(Project project) {
    if (!StringUtils.hasText(project.getRound())) {
      return Collections.emptyList();
    }
    return List.of(
        ProjectDetailVO.FinancingHistoryVO.builder()
            .date("")
            .round(project.getRound())
            .amount(
                StringUtils.hasText(project.getInvestmentAmount())
                    ? project.getInvestmentAmount()
                    : "未透露")
            .investors(Collections.emptyList())
            .build());
  }

  private String formatFinancingDate(java.time.LocalDate date) {
    return formatMonthDate(date);
  }

  private String formatMonthDate(java.time.LocalDate date) {
    if (date == null) {
      return "";
    }
    return MONTH_FMT.format(date);
  }

  private String formatMonthDate(String dateStr) {
    if (!StringUtils.hasText(dateStr)) {
      return dateStr;
    }
    String trimmed = dateStr.trim();
    if (trimmed.length() >= 7 && trimmed.charAt(4) == '-') {
      return trimmed.substring(0, 7);
    }
    return trimmed;
  }

  private String fallbackEstablish(Project project) {
    if (StringUtils.hasText(project.getFoundingYear())) {
      return project.getFoundingYear().replace("年", "");
    }
    return "";
  }
}
