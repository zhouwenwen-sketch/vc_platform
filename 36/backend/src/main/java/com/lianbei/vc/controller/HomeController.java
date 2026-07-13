package com.lianbei.vc.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianbei.vc.common.Result;
import com.lianbei.vc.service.HomeBannerService;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页控制器
 */
@RestController
@RequestMapping("/api/home")
public class HomeController {

  private final ObjectMapper objectMapper;
  private final HomeBannerService homeBannerService;

  public HomeController(ObjectMapper objectMapper, HomeBannerService homeBannerService) {
    this.objectMapper = objectMapper;
    this.homeBannerService = homeBannerService;
  }

  /**
   * 首页初始化数据（字段与前端 mock/home.js 对齐）
   */
  @GetMapping("/init")
  public Result<Map<String, Object>> init() throws IOException {
    ClassPathResource resource = new ClassPathResource("mock/home-init.json");
    Map<String, Object> data =
        objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
    List<Map<String, Object>> banners = homeBannerService.listActiveForHome();
    if (!banners.isEmpty()) {
      data.put("bannerList", banners);
    }
    return Result.ok(data);
  }
}
