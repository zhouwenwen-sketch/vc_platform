package com.lianbei.vc.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDetailVO {

  private Long id;
  private String title;
  private String coverUrl;
  private List<String> bannerUrls;
  private List<String> detailImages;
  private String location;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  /** registering | ongoing | ended */
  private String status;
  private String statusText;
  private String priceText;
  private Integer participantCount;
  private Integer likeCount;
  private String organizerName;
  private OrganizerVO organizer;
  private Boolean signedUp;
  private Boolean liked;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class OrganizerVO {
    private Long id;
    private String name;
    private String entityName;
    private String logoUrl;
    private String metaText;
    private List<String> tags;
  }
}
