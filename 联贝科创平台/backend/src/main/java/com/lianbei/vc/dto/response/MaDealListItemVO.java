package com.lianbei.vc.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MaDealListItemVO {

  private Long id;
  private String projectNo;
  private String brandName;
  private String title;
  private String summary;
  private String category;
  private String categoryLabel;
  private List<String> tagList;
  private String dealAmountText;
  private String logoUrl;
  private String coverUrl;
  private String industry;
  private Integer viewCount;
  private Integer appointmentCount;
  private Integer favoriteCount;
  private Integer shareCount;
  private String publishTime;
  private String relativeTime;
}
