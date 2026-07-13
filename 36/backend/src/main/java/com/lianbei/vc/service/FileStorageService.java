package com.lianbei.vc.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

  /** 保存上传文件，返回可访问 URL 路径（如 /uploads/...） */
  String store(MultipartFile file, String category);

  /** 按原始文件名保存导入资源（如机构/人员头像），返回 /uploads/... 路径 */
  String storeImportedAsset(String originalFilename, byte[] content);

  /** 按分类目录保存导入资源 */
  String storeImportedAsset(String category, String originalFilename, byte[] content);

  /** 校验 URL 是否为本服务上传的文件 */
  boolean isManagedUrl(String url);
}
