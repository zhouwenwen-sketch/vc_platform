package com.lianbei.vc.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 用户信息 VO */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoVO {
  private Long id;
  private String phone;
  private String nickname;
  private String avatar;
  private String role;
  private String authStatus;
}
