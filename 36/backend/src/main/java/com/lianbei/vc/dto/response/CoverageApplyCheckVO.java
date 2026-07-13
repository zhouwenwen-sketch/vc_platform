package com.lianbei.vc.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CoverageApplyCheckVO {

  private boolean applied;
  private String status;
  private Long recordId;
}
