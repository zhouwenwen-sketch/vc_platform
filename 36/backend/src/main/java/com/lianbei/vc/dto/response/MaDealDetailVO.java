package com.lianbei.vc.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MaDealDetailVO {

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
  private String projectName;
  private String mainBusiness;
  private String controllingStake;
  private String marketValue;
  private String revenueData;
  private String netProfitData;
  private String debtRatio;
  private String totalAssets;
  private String netAssets;
  private String bookFunds;
  private String cooperationIntent;
  private String contactPhone;
  private Integer viewCount;
  private Integer appointmentCount;
  private Integer favoriteCount;
  private Integer shareCount;
  private String publishTime;
  private Boolean appointed;
}
