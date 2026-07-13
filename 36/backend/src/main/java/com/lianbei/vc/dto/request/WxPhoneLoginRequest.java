package com.lianbei.vc.dto.request;

import lombok.Data;
import org.springframework.util.StringUtils;

/** 微信小程序手机号一键登录 */
@Data
public class WxPhoneLoginRequest {

  /** getPhoneNumber 回调中的 code */
  private String phoneCode;

  public String resolvePhoneCode() {
    return StringUtils.hasText(phoneCode) ? phoneCode.trim() : null;
  }
}
