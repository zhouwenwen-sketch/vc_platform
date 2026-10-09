package com.lianbei.vc.admin.service;

import com.lianbei.vc.dto.response.ProjectImportResultVO;
import org.springframework.web.multipart.MultipartFile;

public interface ProjectImportService {

  ProjectImportResultVO importExcel(
      MultipartFile projectsFile,
      MultipartFile portraitFile,
      MultipartFile membersFile,
      MultipartFile dynamicsFile,
      MultipartFile financingFile,
      MultipartFile imagesZip,
      MultipartFile[] imageFiles);
}
