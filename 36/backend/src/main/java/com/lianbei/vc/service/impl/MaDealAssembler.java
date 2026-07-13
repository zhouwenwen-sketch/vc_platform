package com.lianbei.vc.service.impl;

import com.lianbei.vc.common.constants.MaDealConstants;
import com.lianbei.vc.dto.response.MaDealDetailVO;
import com.lianbei.vc.dto.response.MaDealListItemVO;
import com.lianbei.vc.entity.MaDeal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;

final class MaDealAssembler {

  private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  private MaDealAssembler() {}

  static MaDealListItemVO toListItemVO(MaDeal deal) {
    if (deal == null) {
      return null;
    }
    return MaDealListItemVO.builder()
        .id(deal.getId())
        .projectNo(deal.getProjectNo())
        .brandName(deal.getBrandName())
        .title(deal.getTitle())
        .summary(deal.getSummary())
        .category(deal.getCategory())
        .categoryLabel(MaDealConstants.categoryLabel(deal.getCategory()))
        .tagList(parseTags(deal.getTags()))
        .dealAmountText(deal.getDealAmountText())
        .logoUrl(deal.getLogoUrl())
        .coverUrl(deal.getCoverUrl())
        .industry(deal.getIndustry())
        .viewCount(safeCount(deal.getViewCount()))
        .appointmentCount(safeCount(deal.getAppointmentCount()))
        .favoriteCount(safeCount(deal.getFavoriteCount()))
        .shareCount(safeCount(deal.getShareCount()))
        .publishTime(formatTime(deal.getPublishTime()))
        .relativeTime(relativeTime(deal.getPublishTime()))
        .build();
  }

  static MaDealDetailVO toDetailVO(MaDeal deal, boolean appointed) {
    if (deal == null) {
      return null;
    }
    return MaDealDetailVO.builder()
        .id(deal.getId())
        .projectNo(deal.getProjectNo())
        .brandName(deal.getBrandName())
        .title(deal.getTitle())
        .summary(deal.getSummary())
        .category(deal.getCategory())
        .categoryLabel(MaDealConstants.categoryLabel(deal.getCategory()))
        .tagList(parseTags(deal.getTags()))
        .dealAmountText(deal.getDealAmountText())
        .logoUrl(deal.getLogoUrl())
        .coverUrl(deal.getCoverUrl())
        .industry(deal.getIndustry())
        .projectName(deal.getProjectName())
        .mainBusiness(deal.getMainBusiness())
        .controllingStake(deal.getControllingStake())
        .marketValue(deal.getMarketValue())
        .revenueData(deal.getRevenueData())
        .netProfitData(deal.getNetProfitData())
        .debtRatio(deal.getDebtRatio())
        .totalAssets(deal.getTotalAssets())
        .netAssets(deal.getNetAssets())
        .bookFunds(deal.getBookFunds())
        .cooperationIntent(deal.getCooperationIntent())
        .contactPhone(deal.getContactPhone())
        .viewCount(safeCount(deal.getViewCount()))
        .appointmentCount(safeCount(deal.getAppointmentCount()))
        .favoriteCount(safeCount(deal.getFavoriteCount()))
        .shareCount(safeCount(deal.getShareCount()))
        .publishTime(formatTime(deal.getPublishTime()))
        .appointed(appointed)
        .build();
  }

  private static int safeCount(Integer value) {
    return value != null ? value : 0;
  }

  private static String formatTime(LocalDateTime time) {
    return time != null ? time.format(TIME_FMT) : "";
  }

  private static String relativeTime(LocalDateTime time) {
    if (time == null) {
      return "";
    }
    Duration diff = Duration.between(time, LocalDateTime.now());
    long minutes = diff.toMinutes();
    if (minutes < 1) {
      return "刚刚";
    }
    if (minutes < 60) {
      return minutes + "分钟前";
    }
    long hours = diff.toHours();
    if (hours < 24) {
      return hours + "小时前";
    }
    long days = diff.toDays();
    if (days < 7) {
      return days + "天前";
    }
    return formatTime(time).substring(0, 10);
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
