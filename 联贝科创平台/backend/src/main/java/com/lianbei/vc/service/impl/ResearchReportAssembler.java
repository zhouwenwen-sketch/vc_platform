package com.lianbei.vc.service.impl;

import com.lianbei.vc.common.constants.Constants;
import com.lianbei.vc.dto.response.ResearchReportDetailVO;
import com.lianbei.vc.entity.ResearchReport;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;

/** 研究院报告 VO 组装 */
final class ResearchReportAssembler {

  private static final DateTimeFormatter DETAIL_TIME =
      DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm");

  private ResearchReportAssembler() {}

  static ResearchReportDetailVO toDetailVO(ResearchReport report) {
    if (report == null) {
      return null;
    }
    return ResearchReportDetailVO.builder()
        .id(report.getId())
        .title(report.getTitle())
        .summary(report.getSummary())
        .content(resolveContent(report))
        .contentFormat("text")
        .publishTime(formatPublishTime(report))
        .reportType(report.getReportType())
        .industry(report.getIndustry())
        .tagList(parseTags(report.getTags()))
        .viewCount(report.getViewCount() != null ? report.getViewCount() : 0)
        .likeCount(0)
        .commentCount(0)
        .favoriteCount(0)
        .publisher(
            ResearchReportDetailVO.Publisher.builder()
                .name(Constants.RESEARCH_PUBLISHER_NAME)
                .avatarUrl(Constants.RESEARCH_PUBLISHER_AVATAR)
                .build())
        .build();
  }

  private static String resolveContent(ResearchReport report) {
    if (StringUtils.hasText(report.getContent())) {
      return report.getContent();
    }
    if (StringUtils.hasText(report.getSummary())) {
      return report.getSummary();
    }
    return report.getTitle();
  }

  private static String formatPublishTime(ResearchReport report) {
    LocalDate date = report.getPublishDate();
    if (date != null) {
      return date.atTime(7, 30).format(DETAIL_TIME);
    }
    LocalDateTime updated = report.getUpdateTime();
    if (updated != null) {
      return updated.format(DETAIL_TIME);
    }
    return "";
  }

  private static List<String> parseTags(String tags) {
    if (!StringUtils.hasText(tags)) {
      return Collections.emptyList();
    }
    return Arrays.stream(tags.split("[,，]"))
        .map(String::trim)
        .filter(StringUtils::hasText)
        .collect(Collectors.toList());
  }
}
