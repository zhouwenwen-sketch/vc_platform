package com.lianbei.vc.admin.controller;

import com.lianbei.vc.admin.service.AdminPermissionService;
import com.lianbei.vc.admin.service.InstitutionImportService;
import com.lianbei.vc.common.Result;
import com.lianbei.vc.dto.response.InstitutionImportResultVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/institution-import")
public class AdminInstitutionImportController {

  private final InstitutionImportService institutionImportService;
  private final AdminPermissionService adminPermissionService;

  public AdminInstitutionImportController(
      InstitutionImportService institutionImportService,
      AdminPermissionService adminPermissionService) {
    this.institutionImportService = institutionImportService;
    this.adminPermissionService = adminPermissionService;
  }

  @PostMapping
  public Result<InstitutionImportResultVO> importExcel(
      @RequestParam(value = "institutionsFile", required = false) MultipartFile institutionsFile,
      @RequestParam(value = "basicInfoFile", required = false) MultipartFile basicInfoFile,
      @RequestParam(value = "membersFile", required = false) MultipartFile membersFile,
      @RequestParam(value = "companiesFile", required = false) MultipartFile companiesFile,
      @RequestParam(value = "imagesZip", required = false) MultipartFile imagesZip,
      @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles) {
    adminPermissionService.checkSystemPermission("institution:import");
    return Result.ok(
        institutionImportService.importExcel(
            institutionsFile, basicInfoFile, membersFile, companiesFile, imagesZip, imageFiles));
  }
}
