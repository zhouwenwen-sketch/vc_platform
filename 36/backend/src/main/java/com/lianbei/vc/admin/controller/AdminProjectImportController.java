package com.lianbei.vc.admin.controller;

import com.lianbei.vc.admin.service.AdminPermissionService;
import com.lianbei.vc.admin.service.ProjectImportService;
import com.lianbei.vc.common.Result;
import com.lianbei.vc.dto.response.ProjectImportResultVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/project-import")
public class AdminProjectImportController {

  private final ProjectImportService projectImportService;
  private final AdminPermissionService adminPermissionService;

  public AdminProjectImportController(
      ProjectImportService projectImportService,
      AdminPermissionService adminPermissionService) {
    this.projectImportService = projectImportService;
    this.adminPermissionService = adminPermissionService;
  }

  @PostMapping
  public Result<ProjectImportResultVO> importExcel(
      @RequestParam(value = "projectsFile", required = false) MultipartFile projectsFile,
      @RequestParam(value = "portraitFile", required = false) MultipartFile portraitFile,
      @RequestParam(value = "membersFile", required = false) MultipartFile membersFile,
      @RequestParam(value = "dynamicsFile", required = false) MultipartFile dynamicsFile,
      @RequestParam(value = "financingFile", required = false) MultipartFile financingFile,
      @RequestParam(value = "imagesZip", required = false) MultipartFile imagesZip,
      @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles) {
    adminPermissionService.checkSystemPermission("project:import");
    return Result.ok(
        projectImportService.importExcel(
            projectsFile,
            portraitFile,
            membersFile,
            dynamicsFile,
            financingFile,
            imagesZip,
            imageFiles));
  }
}
