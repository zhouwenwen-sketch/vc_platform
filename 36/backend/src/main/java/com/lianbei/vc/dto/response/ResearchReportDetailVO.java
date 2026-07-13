package com.lianbei.vc.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 研究院报告详情 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResearchReportDetailVO {

  private Long id;
  private String title;
  private String summary;
  private String content;
  private String contentFormat;
  private String publishTime;
  private String reportType;
  private String industry;
  private List<String> tagList;
  private Integer viewCount;
  /** Phase 1 占位 */
  private Integer likeCount;
  private Integer commentCount;
  private Integer favoriteCount;
  private Publisher publisher;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Publisher {
    private String name;
    private String avatarUrl;
  }
}
