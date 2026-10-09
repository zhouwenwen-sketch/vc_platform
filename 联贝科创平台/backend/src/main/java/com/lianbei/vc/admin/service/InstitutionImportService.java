package com.lianbei.vc.admin.service;

import com.lianbei.vc.dto.response.InstitutionImportResultVO;
import org.springframework.web.multipart.MultipartFile;

public interface InstitutionImportService {

  InstitutionImportResultVO importExcel(
      MultipartFile institutionsFile,
      MultipartFile basicInfoFile,
      MultipartFile membersFile,
      MultipartFile companiesFile,
      MultipartFile imagesZip,
      MultipartFile[] imageFiles);
}
