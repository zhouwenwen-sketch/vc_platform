package com.lianbei.vc.admin.dto;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class CrudFieldOptionsVO {

  /** select | multiselect */
  private String inputType;

  private List<String> options;

  /** 静态枚举：value + label */
  private List<Map<String, String>> labeledOptions;

  /** 分组多选（如项目标签） */
  private List<CrudFieldOptionGroupVO> optionGroups;
}
