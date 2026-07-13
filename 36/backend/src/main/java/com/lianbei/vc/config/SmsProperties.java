package com.lianbei.vc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "vc.sms")
public class SmsProperties {

  /** false 时走 mock，不调用 CRM、登录可用固定验证码 */
  private boolean enabled = false;

  private String mockCode = "123456";
  private int sendIntervalSeconds = 60;
  private int dailyLimit = 10;
  private String scene = "mobilelogin";
  private Crm crm = new Crm();

  @Data
  public static class Crm {
    private String baseUrl = "https://crm.lianbei88.com";
    private String sendPath = "/api/sms/send";
    private String checkPath = "/api/sms/check";
    private String event = "mobilelogin";
    private int successCode = 1;
  }
}
