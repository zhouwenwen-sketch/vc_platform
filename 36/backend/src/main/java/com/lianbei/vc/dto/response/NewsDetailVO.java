package com.lianbei.vc.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 融资快报详情 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsDetailVO {

  private Long id;
  private String title;
  private String newsType;
  private String source;
  private String summary;
  private String content;
  private String contentFormat;
  private String sourceUrl;
  private String coverUrl;
  private String sourceAvatar;
  private String attribution;
  private Integer likeCount;
  private String publishTime;
  private String projectName;
  private Long projectId;
  private String logoUrl;
  private String round;
  private String tag;
  private String region;
  private NextNewsPreview nextNews;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class NextNewsPreview {
    private Long id;
    private String title;
    private String summary;
    private LocalDateTime createTime;
  }
}
