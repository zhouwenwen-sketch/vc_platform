package com.lianbei.vc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "vc.onboard")
public class OnboardProperties {

  /** 提交后是否自动审核通过并落库（生产环境应为 false） */
  private boolean autoApprove = false;
}
