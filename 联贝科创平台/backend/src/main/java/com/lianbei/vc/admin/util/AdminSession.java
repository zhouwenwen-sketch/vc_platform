package com.lianbei.vc.admin.util;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminSession {

  private Long adminId;
  private String username;
  private String nickname;
  private Long roleId;
  private String roleCode;
  private String roleName;
  private Set<String> permissions;
}
