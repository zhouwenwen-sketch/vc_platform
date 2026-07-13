package com.lianbei.vc.admin.controller;

import com.lianbei.vc.admin.service.AdminLookupService;
import com.lianbei.vc.common.Result;
import com.lianbei.vc.dto.response.SearchProjectVO;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/lookup")
public class AdminLookupController {

  private final AdminLookupService adminLookupService;

  public AdminLookupController(AdminLookupService adminLookupService) {
    this.adminLookupService = adminLookupService;
  }

  /** 后台关联项目：按关键字模糊搜索项目库 */
  @GetMapping("/projects")
  public Result<List<SearchProjectVO>> lookupProjects(
      @RequestParam String keyword,
      @RequestParam(defaultValue = "10") int limit) {
    return Result.ok(adminLookupService.lookupProjects(keyword, limit));
  }
}
