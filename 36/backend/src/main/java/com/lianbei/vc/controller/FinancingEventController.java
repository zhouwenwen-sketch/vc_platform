package com.lianbei.vc.controller;

import com.lianbei.vc.common.PageResult;
import com.lianbei.vc.common.Result;
import com.lianbei.vc.dto.response.FinancingEventVO;
import com.lianbei.vc.service.FinancingEventService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/** 融资事件 */
@RestController
@RequestMapping("/api/financing-events")
public class FinancingEventController {

  private final FinancingEventService financingEventService;

  public FinancingEventController(FinancingEventService financingEventService) {
    this.financingEventService = financingEventService;
  }

  /** 首页融资事件预览 */
  @GetMapping("/featured")
  public Result<java.util.List<FinancingEventVO>> featured(
      @RequestParam(defaultValue = "6") int limit) {

    List<FinancingEventVO> result =financingEventService
            .pageEvents(1, limit, null, null, null, null, null, null)
            .getList();
    if(result.isEmpty()){
      return Result.ok(Collections.emptyList());
    }
    return Result.ok(result);
  }

  @GetMapping("/page")
  public Result<PageResult<FinancingEventVO>> page(
      @RequestParam(defaultValue = "1") int pageNum,
      @RequestParam(defaultValue = "10") int pageSize,
      @RequestParam(required = false) String industry,
      @RequestParam(required = false) String region,
      @RequestParam(required = false) String regionScope,
      @RequestParam(required = false) String round,
      @RequestParam(required = false) String financingYear,
      @RequestParam(required = false) String currency) {
    return Result.ok(
        financingEventService.pageEvents(
            pageNum, pageSize, industry, region, regionScope, round, financingYear, currency));
  }
}
