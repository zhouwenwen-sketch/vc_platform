package com.lianbei.vc.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDetailVO {

  private Long id;
  private String name;
  private String logoUrl;
  private String slogan;
  private String round;
  private String location;
  private String region;
  private String establishDate;
  private String website;
  private Boolean isCertified;
  private Boolean isFinancing;
  private List<String> tags;
  private String intro;
  private List<FinancingHistoryVO> financingHistory;
  private BusinessInfoVO businessInfo;
  private List<ShareholderVO> shareholders;
  private List<TeamMemberVO> teamMembers;
  private List<IndustryNewsVO> industryNews;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class FinancingHistoryVO {
    private String date;
    private String round;
    private String amount;
    private List<String> investors;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class BusinessInfoVO {
    private String fullName;
    private String englishName;
    private String legalPerson;
    private String address;
    private String establishDate;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ShareholderVO {
    private String name;
    private String ratio;
    private String capital;
    private String capitalDate;
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
  public static class IndustryNewsVO {
    private Long id;
    private String title;
    private String summary;
    private String date;
    private String newsType;
    /** news=资讯表；dynamic=项目动态表 */
    private String source;
  }
}
