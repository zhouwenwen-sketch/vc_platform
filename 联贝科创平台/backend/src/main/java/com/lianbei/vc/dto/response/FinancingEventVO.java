package com.lianbei.vc.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 融资事件列表项（事件视角 VO）。
 * <p>一期：一行对应一个 project 的最新融资快照；{@code projectId} 与 {@code id} 相同，详情页共用项目详情。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancingEventVO {

  /** 列表主键，一期等同 projectId */
  private Long id;
  /** 关联项目 ID，跳转详情时使用 */
  private Long projectId;
  private String companyName;
  private String logoUrl;
  private String description;
  private String amount;
  private String round;
  private String investors;
  private String date;
}
