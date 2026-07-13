package com.lianbei.vc.controller;

import com.lianbei.vc.common.Result;
import com.lianbei.vc.dto.request.CoverageApplySubmitRequest;
import com.lianbei.vc.dto.response.CoverageApplyCheckVO;
import com.lianbei.vc.dto.response.CoverageApplySubmitResponse;
import com.lianbei.vc.exception.BusinessException;
import com.lianbei.vc.service.CoverageApplyService;
import com.lianbei.vc.utils.UserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 寻求报道 */
@RestController
@RequestMapping("/api/coverage")
public class CoverageController {

  private final CoverageApplyService coverageApplyService;

  public CoverageController(CoverageApplyService coverageApplyService) {
    this.coverageApplyService = coverageApplyService;
  }

  @PostMapping("/apply")
  public Result<CoverageApplySubmitResponse> apply(@RequestBody CoverageApplySubmitRequest request) {
    Long userId = UserContext.getUserId();
    if (userId == null) {
      throw new BusinessException(401, "未登录，请先登录");
    }
    return Result.ok(coverageApplyService.submit(userId, request));
  }

  @GetMapping("/check")
  public Result<CoverageApplyCheckVO> check(@RequestParam Long projectId) {
    Long userId = UserContext.getUserId();
    if (userId == null) {
      throw new BusinessException(401, "未登录，请先登录");
    }
    return Result.ok(coverageApplyService.checkApplied(userId, projectId));
  }
}
