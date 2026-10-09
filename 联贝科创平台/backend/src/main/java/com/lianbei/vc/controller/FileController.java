package com.lianbei.vc.controller;

import com.lianbei.vc.common.Result;
import com.lianbei.vc.dto.response.FileUploadVO;
import com.lianbei.vc.exception.BusinessException;
import com.lianbei.vc.service.FileStorageService;
import com.lianbei.vc.utils.UserContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** 通用文件上传 */
@RestController
@RequestMapping("/api/files")
public class FileController {

  private final FileStorageService fileStorageService;

  public FileController(FileStorageService fileStorageService) {
    this.fileStorageService = fileStorageService;
  }

  /**
   * 上传文件
   *
   * @param file 文件
   * @param category image / document / onboard-logo / onboard-cert 等
   */
  @PostMapping("/upload")
  public Result<FileUploadVO> upload(
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "category", defaultValue = "image") String category) {
    if (UserContext.getUserId() == null) {
      throw new BusinessException(401, "未登录，请先登录");
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
