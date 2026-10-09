package com.lianbei.vc.controller;

import com.lianbei.vc.common.Result;
import com.lianbei.vc.dto.request.ProjectOnboardSubmitRequest;
import com.lianbei.vc.dto.response.ProjectOnboardStatusVO;
import com.lianbei.vc.dto.response.ProjectOnboardSubmitResponse;
import com.lianbei.vc.exception.BusinessException;
import com.lianbei.vc.service.ProjectOnboardService;
import com.lianbei.vc.utils.UserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 项目入驻申请 */
@RestController
@RequestMapping("/api/projects/onboard")
public class ProjectOnboardController {

  private final ProjectOnboardService projectOnboardService;

  public ProjectOnboardController(ProjectOnboardService projectOnboardService) {
    this.projectOnboardService = projectOnboardService;
  }

  /** 提交入驻申请（三步表单） */
  @PostMapping
  public Result<ProjectOnboardSubmitResponse> submit(@RequestBody ProjectOnboardSubmitRequest request) {
    Long userId = UserContext.getUserId();
    if (userId == null) {
      throw new BusinessException(401, "未登录，请先登录");
    }
    return Result.ok(projectOnboardService.submit(userId, request));
  }

  /** 当前用户最新入驻申请状态 */
  @GetMapping("/status")
  public Result<ProjectOnboardStatusVO> status() {
    Long userId = UserContext.getUserId();
    if (userId == null) {
      throw new BusinessException(401, "未登录，请先登录");
    }
    return Result.ok(projectOnboardService.getLatestStatus(userId));
  }
}
