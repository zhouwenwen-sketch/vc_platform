package com.lianbei.vc.dto.request;

import lombok.Data;

@Data
public class ActivityPublishRequest {

  private String title;
  private String location;
  private String startTime;
  private String endTime;
  private String organizerName;
  private String priceText;
  private String description;
  private String contactName;
  private String contactPhone;
}
