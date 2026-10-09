package com.lianbei.vc.dto.request;

import lombok.Data;

@Data
public class OnboardTeamMemberRequest {
  private String name;
  private String title;
  private String bio;
  private String avatar;
}
