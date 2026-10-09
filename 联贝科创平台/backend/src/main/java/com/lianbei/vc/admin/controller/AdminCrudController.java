package com.lianbei.vc.admin.controller;

import com.lianbei.vc.admin.crud.AdminCrudService;
import com.lianbei.vc.admin.dto.CrudResourceVO;
import com.lianbei.vc.common.PageResult;
import com.lianbei.vc.common.Result;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/crud")
public class AdminCrudController {

  private final AdminCrudService adminCrudService;

  public AdminCrudController(AdminCrudService adminCrudService) {
    this.adminCrudService = adminCrudService;
  }

  @GetMapping("/resources")
  public Result<List<CrudResourceVO>> resources() {
    return Result.ok(adminCrudService.listResources());
  }

  /** 项目卡片编辑：标签可选值（来自筛选管理） */
  @GetMapping("/project/tag-options")
  public Result<List<Map<String, Object>>> projectTagOptions() {
    return Result.ok(adminCrudService.listProjectTagOptions());
  }

  /** 项目卡片编辑：工商信息 */
  @GetMapping("/project/{id}/business")
  public Result<Map<String, Object>> projectBusiness(@PathVariable Long id) {
    return Result.ok(adminCrudService.getProjectBusiness(id));
  }

  @GetMapping("/{resource}/meta")
  public Result<CrudResourceVO> meta(@PathVariable String resource) {
    return Result.ok(adminCrudService.getResourceMeta(resource));
  }

  @GetMapping("/{resource}")
  public Result<PageResult<Map<String, Object>>> page(
      @PathVariable String resource,
      @RequestParam(defaultValue = "1") int pageNum,
      @RequestParam(defaultValue = "10") int pageSize,
      @RequestParam(required = false) String keyword) {
    return Result.ok(adminCrudService.page(resource, pageNum, pageSize, keyword));
  }

  @GetMapping("/{resource}/{id}")
  public Result<Map<String, Object>> detail(@PathVariable String resource, @PathVariable Long id) {
    return Result.ok(adminCrudService.detail(resource, id));
  }

  @PostMapping("/{resource}")
  public Result<Map<String, Object>> create(
      @PathVariable String resource, @RequestBody Map<String, Object> body) {
    return Result.ok(adminCrudService.create(resource, body));
  }

  @PutMapping("/{resource}/{id}")
  public Result<Map<String, Object>> update(
      @PathVariable String resource, @PathVariable Long id, @RequestBody Map<String, Object> body) {
    return Result.ok(adminCrudService.update(resource, id, body));
  }

  @DeleteMapping("/{resource}/{id}")
  public Result<Void> delete(@PathVariable String resource, @PathVariable Long id) {
    adminCrudService.delete(resource, id);
    return Result.ok();
  }
}
