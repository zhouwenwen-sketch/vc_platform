package com.lianbei.vc.service.impl;

import com.lianbei.vc.dto.response.ProjectDetailVO;
import com.lianbei.vc.entity.News;
import com.lianbei.vc.entity.Project;
import com.lianbei.vc.entity.ProjectBusiness;
import com.lianbei.vc.entity.ProjectDynamic;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ProjectDetailAssembler {

  private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

  private final ProjectDetailDataService detailDataService;

  public ProjectDetailAssembler(ProjectDetailDataService detailDataService) {
    this.detailDataService = detailDataService;
  }

  /** 卡片 tags 字段优先（后台编辑入口），无值时再回退关联表 */
  private List<String> resolveTags(Project project) {
    if (project.getTags() != null) {
      return detailDataService.parseLegacyTags(project.getTags());
    }
    return detailDataService.listTagNames(project.getId());
  }

  public ProjectDetailVO assemble(Project project, List<News> relatedNews) {
    List<String> tags = resolveTags(project);
    ProjectBusiness business = detailDataService.getBusiness(project.getId());
    String establishDate =
        business != null && business.getEstablishDate() != null
            ? MONTH_FMT.format(business.getEstablishDate())
            : fallbackEstablish(project);

    return ProjectDetailVO.builder()
        .id(project.getId())
        .name(project.getName())
        .logoUrl(project.getLogoUrl())
        .slogan(project.getCompanyDesc())
        .round(
            StringUtils.hasText(project.getLatestRound())
                ? project.getLatestRound()
                : project.getRound())
        .location(
            StringUtils.hasText(project.getLocation()) ? project.getLocation() : project.getRegion())
        .region(project.getRegion())
        .establishDate(establishDate)
        .website(project.getWebsite())
        .isCertified(project.getIsCertified() != null && project.getIsCertified() == 1)
        .isFinancing(project.getIsFinancing() != null && project.getIsFinancing() == 1)
        .tags(tags)
        .intro(
            StringUtils.hasText(project.getIntro())
                ? project.getIntro()
                : project.getCompanyDesc())
        .financingHistory(detailDataService.listFinancingHistory(project))
        .businessInfo(detailDataService.buildBusinessInfo(project))
        .shareholders(detailDataService.listShareholders(project.getId()))
        .teamMembers(detailDataService.listTeamMembers(project.getId()))
        .industryNews(mergeIndustryNews(relatedNews, project.getId()))
        .build();
  }

  public String formatInvestorsForList(Project project) {
    return detailDataService.formatInvestorsForList(project);
  }

  public String formatEventDate(Project project) {
    return detailDataService.formatEventDate(project);
  }

  public String formatEventAmount(Project project) {
    return detailDataService.formatEventAmount(project);
  }

  public String resolveBusinessFullName(Project project) {
    if (project == null || project.getId() == null) {
      return "";
    }
    return detailDataService.getBusinessFullName(project.getId());
  }

  private String fallbackEstablish(Project project) {
    if (StringUtils.hasText(project.getFoundingYear())) {
      return project.getFoundingYear().replace("年", "");
    }
    return "";
  }

  private List<ProjectDetailVO.IndustryNewsVO> mergeIndustryNews(
      List<News> relatedNews, Long projectId) {
    List<ProjectDetailVO.IndustryNewsVO> merged = new ArrayList<>();
    List<ProjectDetailVO.IndustryNewsVO> dynamics =
        mapDynamics(detailDataService.listDynamics(projectId));
    merged.addAll(dynamics);
    if (dynamics.isEmpty()) {
      merged.addAll(mapLinkedFlashAsDynamics(relatedNews));
    } else {
      merged.addAll(mapNewsArticles(relatedNews));
    }
    merged.sort(Comparator.comparing(ProjectDetailVO.IndustryNewsVO::getDate, this::compareDateDesc));
    return merged;
  }

  private int compareDateDesc(String left, String right) {
    if (!StringUtils.hasText(left) && !StringUtils.hasText(right)) {
      return 0;
    }
    if (!StringUtils.hasText(left)) {
      return 1;
    }
    if (!StringUtils.hasText(right)) {
      return -1;
    }
    return right.compareTo(left);
  }

  private List<ProjectDetailVO.IndustryNewsVO> mapDynamics(List<ProjectDynamic> dynamics) {
    if (dynamics == null || dynamics.isEmpty()) {
      return List.of();
    }
    return dynamics.stream()
        .filter(d -> StringUtils.hasText(d.getContent()))
        .map(
            d ->
                buildDynamicItem(
                    d.getId(), d.getContent().trim(), formatDynamicDate(d)))
        .toList();
  }

  private List<ProjectDetailVO.IndustryNewsVO> mapLinkedFlashAsDynamics(List<News> newsList) {
    if (newsList == null || newsList.isEmpty()) {
      return List.of();
    }
    return newsList.stream()
        .filter(n -> n.getProjectId() != null)
        .filter(n -> "快讯".equals(n.getNewsType()))
        .map(
            n ->
                buildDynamicItem(
                    n.getId(),
                    StringUtils.hasText(n.getContent()) ? n.getContent().trim() : n.getTitle(),
                    n.getCreateTime() != null ? MONTH_FMT.format(n.getCreateTime()) : ""))
        .toList();
  }

  private List<ProjectDetailVO.IndustryNewsVO> mapNewsArticles(List<News> newsList) {
    if (newsList == null || newsList.isEmpty()) {
      return List.of();
    }
    return newsList.stream()
        .filter(n -> !"快讯".equals(n.getNewsType()))
        .map(
            n ->
                ProjectDetailVO.IndustryNewsVO.builder()
                    .id(n.getId())
                    .title(n.getTitle())
                    .summary(StringUtils.hasText(n.getSummary()) ? n.getSummary().trim() : null)
                    .date(
                        n.getCreateTime() != null ? MONTH_FMT.format(n.getCreateTime()) : "")
                    .newsType(StringUtils.hasText(n.getNewsType()) ? n.getNewsType() : "文章")
                    .source("news")
                    .build())
        .toList();
  }

  private ProjectDetailVO.IndustryNewsVO buildDynamicItem(
      Long id, String content, String date) {
    TitleSummary parts = splitTitleSummary(content);
    return ProjectDetailVO.IndustryNewsVO.builder()
        .id(id)
        .title(parts.title())
        .summary(parts.summary())
        .date(date)
        .newsType("动态")
        .source("dynamic")
        .build();
  }

  private TitleSummary splitTitleSummary(String content) {
    if (!StringUtils.hasText(content)) {
      return new TitleSummary("", null);
    }
    String normalized = content.trim();
    int newline = normalized.indexOf('\n');
    if (newline < 0) {
      return new TitleSummary(normalized, null);
    }
    String title = normalized.substring(0, newline).trim();
    String summary = normalized.substring(newline + 1).trim();
    if (!StringUtils.hasText(title)) {
      return new TitleSummary(normalized, null);
    }
    if (!StringUtils.hasText(summary)) {
      return new TitleSummary(title, null);
    }
    return new TitleSummary(title, summary);
  }

  private record TitleSummary(String title, String summary) {}

  private String formatDynamicDate(ProjectDynamic dynamic) {
    if (dynamic.getEventDate() != null) {
      return MONTH_FMT.format(dynamic.getEventDate());
    }
    if (dynamic.getCreateTime() != null) {
      return MONTH_FMT.format(dynamic.getCreateTime());
    }
    return "";
  }

}
