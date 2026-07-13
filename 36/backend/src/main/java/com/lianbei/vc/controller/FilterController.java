package com.lianbei.vc.controller;

import com.lianbei.vc.common.Result;
import com.lianbei.vc.service.LibraryFilterService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/filters")
public class FilterController {

  private final LibraryFilterService libraryFilterService;

  public FilterController(LibraryFilterService libraryFilterService) {
    this.libraryFilterService = libraryFilterService;
  }

  /** 按页面 bundle 返回筛选项，如 project-library / financing-events */
  @GetMapping("/bundle/{bundleName}")
  public Result<Map<String, List<String>>> bundle(@PathVariable String bundleName) {
    return Result.ok(libraryFilterService.listBundle(bundleName));
  }
}
