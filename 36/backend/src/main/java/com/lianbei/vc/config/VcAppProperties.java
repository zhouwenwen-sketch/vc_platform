package com.lianbei.vc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "vc")
public class VcAppProperties {

  /** 对外访问根地址，用于拼接 /uploads 等相对资源路径 */
  private String publicBaseUrl = "";
}
