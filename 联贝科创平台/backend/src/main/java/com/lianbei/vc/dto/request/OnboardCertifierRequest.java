package com.lianbei.vc.dto.request;

import lombok.Data;

@Data
public class OnboardCertifierRequest {
  private String realName;
  private Boolean sameAsWechat;
  private String jobType;
  private String jobTitle;
  private String responsibility;
  private String contactEmail;
  private String identityCertUrl;
}
