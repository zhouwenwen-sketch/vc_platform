package com.lianbei.vc.dto.request;

import java.util.List;
import lombok.Data;

/** 项目入驻三步表单提交 */
@Data
public class ProjectOnboardSubmitRequest {
  private String projectName;
  private String entityName;
  private String establishDate;
  private String logoUrl;
  private String country;
  private String province;
  private String city;
  private String overseasLocation;
  private String oneLiner;
  private String intro;
  private List<String> industries;
  private String financingRound;
  private String needFinancing;
  private String seekingFinancingRound;
  private String financingAmount;
  private String financingCurrency;
  private String equityPercent;
  private String website;
  private String bpUrl;
  private String bpFileName;
  private List<OnboardTeamMemberRequest> teamMembers;
  private OnboardCertifierRequest certifier;
}
