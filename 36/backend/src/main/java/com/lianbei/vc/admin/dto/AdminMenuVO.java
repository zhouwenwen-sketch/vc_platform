package com.lianbei.vc.admin.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminMenuVO {

  private Long id;
  private String code;
  private String name;
  private String path;
  private String resource;
  private Integer sortOrder;
  private List<AdminMenuVO> children;

  public List<AdminMenuVO> getChildren() {
    if (children == null) {
      children = new ArrayList<>();
    }
    return children;
  }
}
