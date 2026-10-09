package com.lianbei.vc.dto.request;

import lombok.Data;
import org.springframework.util.StringUtils;

/** 手机号登录请求 */
@Data
public class LoginRequest {

  private String mobile;
  private String phone;
  private String captcha;
  private String code;

  public String resolveMobile() {
    if (StringUtils.hasText(mobile)) {
      return mobile.trim();
    }
    if (StringUtils.hasText(phone)) {
      return phone.trim();
    }
    return null;
  }

  public String resolveCaptcha() {
    if (StringUtils.hasText(captcha)) {
      return captcha.trim();
    }
    if (StringUtils.hasText(code)) {
      return code.trim();
    }
    return null;
  }
}
