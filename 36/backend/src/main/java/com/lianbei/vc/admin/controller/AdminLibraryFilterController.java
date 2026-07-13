package com.lianbei.vc.admin.controller;

import com.lianbei.vc.admin.service.AdminPermissionService;
import com.lianbei.vc.common.Result;
import com.lianbei.vc.entity.LibraryFilterOption;
import com.lianbei.vc.service.LibraryFilterService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
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
@RequestMapping("/api/admin/library-filters")
public class AdminLibraryFilterController {

  private final LibraryFilterService libraryFilterService;
  private final AdminPermissionService adminPermissionService;

  public AdminLibraryFilterController(
      LibraryFilterService libraryFilterService, AdminPermissionService adminPermissionService) {
    this.libraryFilterService = libraryFilterService;
    this.adminPermissionService = adminPermissionService;
  }

  @GetMapping("/meta")
  public Result<Map<String, Object>> meta() {
    checkPermission();
    List<Map<String, String>> groups =
        libraryFilterService.listAdminGroups().stream()
            .map(
                group -> {
                  Map<String, String> item = new HashMap<>();
                  item.put("id", group.id());
                  item.put("scene", group.scene());
                  item.put("filterKey", group.filterKey());
                  item.put("label", group.label());
                  item.put("remark", group.remark());
                  return item;
                })
            .toList();
    return Result.ok(Map.of("groups", groups));
  }

  @GetMapping
  public Result<List<LibraryFilterOption>> list(
      @RequestParam String scene, @RequestParam String filterKey) {
    checkPermission();
    return Result.ok(libraryFilterService.listAdminOptions(scene, filterKey));
  }

  @PostMapping
  public Result<LibraryFilterOption> create(@RequestBody SaveRequest request) {
    checkPermission();
    LibraryFilterOption option =
        LibraryFilterOption.builder()
            .scene(request.getScene())
            .filterKey(request.getFilterKey())
            .label(request.getLabel())
            .value(request.getValue())
            .sortOrder(request.getSortOrder())
            .enabled(request.getEnabled())
            .build();
    return Result.ok(libraryFilterService.create(option));
  }

  @PutMapping("/{id}")
  public Result<LibraryFilterOption> update(@PathVariable Long id, @RequestBody SaveRequest request) {
    checkPermission();
    LibraryFilterOption patch =
        LibraryFilterOption.builder()
            .label(request.getLabel())
            .value(request.getValue())
            .sortOrder(request.getSortOrder())
            .enabled(request.getEnabled())
            .build();
    return Result.ok(libraryFilterService.update(id, patch));
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    checkPermission();
    libraryFilterService.delete(id);
    return Result.ok();
  }

  private void checkPermission() {
    adminPermissionService.checkSystemPermission("library_filter:manage");
  }

  @Data
  public static class SaveRequest {
    private String scene;
    private String filterKey;
    private String label;
    private String value;
    private Integer sortOrder;
    private Integer enabled;
  }
}
