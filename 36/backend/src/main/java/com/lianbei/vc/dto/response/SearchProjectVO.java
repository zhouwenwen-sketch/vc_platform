package com.lianbei.vc.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 首页搜索 - 项目结果 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchProjectVO {

  private Long id;
  private String name;
  private String round;
  private String companyDesc;
  private String logoUrl;
  private String entityName;
}
