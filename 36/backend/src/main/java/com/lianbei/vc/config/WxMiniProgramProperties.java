package com.lianbei.vc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "vc.wx-mini-program")
public class WxMiniProgramProperties {

  /** 小程序 AppID，需与 manifest.json mp-weixin.appid 一致 */
  private String appId = "";

  /** 小程序 AppSecret，生产环境通过环境变量注入 */
  private String appSecret = "";

  /**
   * 未配置 appSecret 时是否允许 mock 登录（仅 dev 建议开启）
   */
  private boolean mockEnabled = false;

  /** mock 模式下使用的固定手机号 */
  private String mockPhone = "13800138000";
}
