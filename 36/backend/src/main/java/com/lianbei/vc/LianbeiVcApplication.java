package com.lianbei.vc;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan({"com.lianbei.vc.mapper", "com.lianbei.vc.admin.mapper"})
public class LianbeiVcApplication {

  public static void main(String[] args) {
    SpringApplication.run(LianbeiVcApplication.class, args);
  }
}
