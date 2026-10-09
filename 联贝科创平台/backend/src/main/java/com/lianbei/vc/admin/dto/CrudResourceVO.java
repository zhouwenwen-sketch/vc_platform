package com.lianbei.vc.admin.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CrudResourceVO {

  private String key;
  private String label;
  private String tableName;
  private boolean readOnly;
  private List<CrudFieldVO> fields;
}
