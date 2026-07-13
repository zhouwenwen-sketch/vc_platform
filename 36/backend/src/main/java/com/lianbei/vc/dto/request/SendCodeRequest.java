package com.lianbei.vc.dto.request;

import lombok.Data;
import org.springframework.util.StringUtils;

/** 发送验证码请求 */
@Data
public class SendCodeRequest {

  /** 手机号（与 CRM 字段一致） */
  private String mobile;

  /** 兼容旧字段 */
  private String phone;

  public String resolveMobile() {
    if (StringUtils.hasText(mobile)) {
      return mobile.trim();
    }
    if (StringUtils.hasText(phone)) {
      return phone.trim();
    }
    return null;
  }
}
