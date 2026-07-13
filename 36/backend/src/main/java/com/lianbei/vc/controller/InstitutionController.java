package com.lianbei.vc.controller;

import com.lianbei.vc.common.PageResult;
import com.lianbei.vc.common.Result;
import com.lianbei.vc.dto.response.InstitutionDetailVO;
import com.lianbei.vc.entity.Institution;
import com.lianbei.vc.service.InstitutionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 投资机构库 */
@RestController
@RequestMapping("/api/institutions")
public class InstitutionController {

  private final InstitutionService institutionService;

  public InstitutionController(InstitutionService institutionService) {
    this.institutionService = institutionService;
  }

  /** 机构详情 */
  @GetMapping("/detail/{id}")
  public Result<InstitutionDetailVO> detail(@PathVariable Long id) {
    return Result.ok(institutionService.getInstitutionDetail(id));
  }

  /** 机构库列表（筛选 + 分页） */
  @GetMapping("/library")
  public Result<PageResult<Institution>> library(
      @RequestParam(defaultValue = "1") int pageNum,
      @RequestParam(defaultValue = "10") int pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String investmentField,
      @RequestParam(required = false) String instType,
      @RequestParam(required = false) String foundedYear) {
    return Result.ok(
        institutionService.pageLibrary(
            pageNum, pageSize, keyword, investmentField, instType, foundedYear));
  }
}
