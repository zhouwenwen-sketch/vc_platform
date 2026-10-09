package com.lianbei.vc.admin.dto;

import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminLoginVO {

  private String token;
  private Long adminId;
  private String username;
  private String nickname;
  private String roleCode;
  private String roleName;
  private Set<String> permissions;
  private List<AdminMenuVO> menus;
}
