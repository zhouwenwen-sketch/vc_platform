package com.lianbei.vc.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectOnboardStatusVO {
  private Long userId;
  private Long applicationId;
  private String projectName;
  /** pending / approved / rejected / none */
  private String status;
  private String auditRemark;
  private Long projectId;
}
