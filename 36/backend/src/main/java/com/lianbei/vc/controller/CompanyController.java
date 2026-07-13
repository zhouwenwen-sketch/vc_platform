package com.lianbei.vc.controller;

import com.lianbei.vc.common.Result;
import com.lianbei.vc.service.CompanySearchService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 企业主体搜索（项目入驻） */
@RestController
@RequestMapping("/api/companies")
public class CompanyController {

  private final CompanySearchService companySearchService;

  public CompanyController(CompanySearchService companySearchService) {
    this.companySearchService = companySearchService;
  }

  @GetMapping("/search")
  public Result<List<String>> search(
      @RequestParam String keyword,
      @RequestParam(defaultValue = "10") int limit) {
    return Result.ok(companySearchService.search(keyword, limit));
  }
}
