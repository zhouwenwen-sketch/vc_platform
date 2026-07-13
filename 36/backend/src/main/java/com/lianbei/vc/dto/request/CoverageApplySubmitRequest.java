package com.lianbei.vc.dto.request;

import java.util.List;
import lombok.Data;

@Data
public class CoverageApplySubmitRequest {

  private Long projectId;
  private String projectName;
  private String reportType;
  private List<FinancingItem> financingList;
  private String reportContent;
  private String competitiveness;
  private String evaluation;
  private String relatedReports;
  private String debutMedia;

  @Data
  public static class FinancingItem {
    private String round;
    private String financingDate;
    private String amount;
    private String investors;
  }
}
