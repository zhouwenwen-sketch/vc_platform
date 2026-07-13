package com.lianbei.vc.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CrudFieldVO {

  private String name;
  private String label;
  private String type;
  private boolean required;
  private boolean readOnly;
  private boolean hidden;
  /** 下拉/多选配置（来自筛选标签或固定字典） */
  private CrudFieldOptionsVO fieldOptions;
}
