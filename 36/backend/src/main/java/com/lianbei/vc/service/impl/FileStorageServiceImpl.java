package com.lianbei.vc.service.impl;

import com.lianbei.vc.common.MediaUrlResolver;
import com.lianbei.vc.exception.BusinessException;
import com.lianbei.vc.service.FileStorageService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageServiceImpl implements FileStorageService {

  private static final Set<String> IMAGE_EXT = Set.of("jpg", "jpeg", "png", "webp");
  private static final Set<String> DOC_EXT = Set.of("pdf");

  private final Path uploadRoot;
  private final MediaUrlResolver mediaUrlResolver;

  public FileStorageServiceImpl(
      @Value("${vc.upload-dir:uploads}") String uploadDir, MediaUrlResolver mediaUrlResolver) {
    this.mediaUrlResolver = mediaUrlResolver;
    this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    try {
      Files.createDirectories(uploadRoot);
    } catch (IOException e) {
      throw new IllegalStateException("无法创建上传目录: " + uploadRoot, e);
    }
  }

  @Override
  public String store(MultipartFile file, String category) {
    if (file == null || file.isEmpty()) {
      throw new BusinessException("上传文件不能为空");
    }
    String originalName = file.getOriginalFilename();
    String ext = extractExt(originalName);
    validateExt(ext, category);
    validateSize(file.getSize(), category);

    String datePath = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    String safeCategory = sanitizeCategory(category);
    Path dir = uploadRoot.resolve(safeCategory).resolve(datePath);
    try {
      Files.createDirectories(dir);
      String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
      Path target = dir.resolve(filename);
      file.transferTo(target);
      String path = "/uploads/" + safeCategory + "/" + datePath + "/" + filename;
      return mediaUrlResolver.resolve(path);
    } catch (IOException e) {
      throw new BusinessException("文件保存失败");
    }
  }

  @Override
  public String storeImportedAsset(String originalFilename, byte[] content) {
    return storeImportedAsset("institution-import", originalFilename, content);
  }

  @Override
  public String storeImportedAsset(String category, String originalFilename, byte[] content) {
    if (!StringUtils.hasText(originalFilename) || content == null || content.length == 0) {
      throw new BusinessException("导入资源无效");
    }
    String safeName = sanitizeImportedFilename(originalFilename.trim());
    String ext = extractExt(safeName);
    if (!StringUtils.hasText(ext) || !IMAGE_EXT.contains(ext)) {
      throw new BusinessException("不支持的图片类型: " + originalFilename);
    }
    if (content.length > 5L * 1024 * 1024) {
      throw new BusinessException("图片大小超出限制: " + originalFilename);
    }
    String safeCategory = sanitizeCategory(category);
    Path dir = uploadRoot.resolve(safeCategory);
    try {
      Files.createDirectories(dir);
      Path target = dir.resolve(safeName);
      Files.write(target, content);
      return "/uploads/" + safeCategory + "/" + safeName;
    } catch (IOException e) {
      throw new BusinessException("导入图片保存失败: " + originalFilename);
    }
  }

  @Override
  public boolean isManagedUrl(String url) {
    if (!StringUtils.hasText(url)) {
      return false;
    }
    String trimmed = url.trim();
    if (trimmed.startsWith("/uploads/")) {
      return true;
    }
    int idx = trimmed.indexOf("/uploads/");
    return idx >= 0;
  }

  private void validateExt(String ext, String category) {
    if (!StringUtils.hasText(ext)) {
      throw new BusinessException("不支持的文件类型");
    }
    String cat = sanitizeCategory(category);
    if ("document".equals(cat)) {
      if (!DOC_EXT.contains(ext)) {
        throw new BusinessException("仅支持 PDF 文件");
      }
      return;
    }
    if (!IMAGE_EXT.contains(ext)) {
      throw new BusinessException("仅支持 jpg/png 图片");
    }
  }

  private void validateSize(long size, String category) {
    long max = "document".equals(sanitizeCategory(category)) ? 20L * 1024 * 1024 : 5L * 1024 * 1024;
    if (size > max) {
      throw new BusinessException("文件大小超出限制");
    }
  }

  private String extractExt(String filename) {
    if (!StringUtils.hasText(filename) || !filename.contains(".")) {
      return "";
    }
    return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
  }

  private String sanitizeCategory(String category) {
    if (!StringUtils.hasText(category)) {
      return "common";
    }
    String cat = category.trim().toLowerCase(Locale.ROOT);
    if (cat.matches("[a-z0-9_-]{1,32}")) {
      return cat;
    }
    return "common";
  }

  private String sanitizeImportedFilename(String filename) {
    String base = filename.replace("\\", "/");
    int slash = base.lastIndexOf('/');
    if (slash >= 0) {
      base = base.substring(slash + 1);
    }
    if (!base.matches("[A-Za-z0-9._-]{1,128}")) {
      throw new BusinessException("非法文件名: " + filename);
    }
    return base;
  }
}
