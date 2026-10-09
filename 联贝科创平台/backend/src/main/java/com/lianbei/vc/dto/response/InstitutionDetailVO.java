package com.lianbei.vc.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 机构详情 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionDetailVO {

  private Long id;
  private String name;
  private String entityName;
  private String logoUrl;
  private List<String> instTypes;
  private String foundedYear;
  private String region;
  private Integer eventCount;
  private String manageScale;
  private String teamCount;
  private String intro;
  private List<String> investmentFields;
  private List<InvestmentEventVO> investmentEvents;
  private BusinessInfoVO businessInfo;
  private List<FundManagerVO> fundManagers;
  private List<TeamMemberVO> teamMembers;
  private ContactVO contact;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class InvestmentEventVO {
    private Long projectId;
    private String companyName;
    private String logoUrl;
    private String round;
    private String industry;
    private String description;
    private String date;
    private String amount;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class BusinessInfoVO {
    private String fullName;
    private String legalPerson;
    private String establishDate;
    private String address;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class FundManagerVO {
    /** 兼容旧字段：简称，默认同 fullName */
    private String name;
    /** 兼容旧字段：主体名称，默认同 fullName */
    private String entityName;
    private String fullName;
    private String legalPerson;
    private String instType;
    private String officeAddress;
    private String registeredCapital;
    private String paidInCapital;
    private String paidInRatio;
    private String registrationNo;
    private String establishDate;
    private String registerDate;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TeamMemberVO {
    private String name;
    private String title;
    private String avatar;
    private String bio;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ContactVO {
    private String website;
    private String phone;
    private String email;
    private String address;
  }
}
