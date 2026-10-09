package com.lianbei.vc.admin.controller;

import com.lianbei.vc.admin.util.AdminContext;
import com.lianbei.vc.common.Result;
import com.lianbei.vc.dto.response.FileUploadVO;
import com.lianbei.vc.exception.BusinessException;
import com.lianbei.vc.service.FileStorageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** 管理端文件上传 */
@RestController
@RequestMapping("/api/admin/files")
public class AdminFileController {

  private final FileStorageService fileStorageService;

  public AdminFileController(FileStorageService fileStorageService) {
    this.fileStorageService = fileStorageService;
  }

  @PostMapping("/upload")
  public Result<FileUploadVO> upload(
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "category", defaultValue = "banner") String category) {
    if (AdminContext.getAdminId() == null) {
      throw new BusinessException(401, "未登录或登录已过期");
    }
    String url = fileStorageService.store(file, category);
    String originalName = file.getOriginalFilename();
    return Result.ok(
        FileUploadVO.builder()
            .url(url)
            .fileName(originalName != null ? originalName : "")
            .build());
  }
}
