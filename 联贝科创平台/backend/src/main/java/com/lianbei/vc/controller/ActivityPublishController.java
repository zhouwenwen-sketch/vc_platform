package com.lianbei.vc.controller;

import com.lianbei.vc.common.Result;
import com.lianbei.vc.dto.request.ActivityPublishRequest;
import com.lianbei.vc.exception.BusinessException;
import com.lianbei.vc.service.ActivityPublishService;
import com.lianbei.vc.utils.UserContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/activities/publish")
public class ActivityPublishController {

  private final ActivityPublishService activityPublishService;

  public ActivityPublishController(ActivityPublishService activityPublishService) {
    this.activityPublishService = activityPublishService;
  }

  @PostMapping
  public Result<Void> submit(@RequestBody ActivityPublishRequest request) {
    Long userId = UserContext.getUserId();
    if (userId == null) {
      throw new BusinessException(401, "未登录，请先登录");
    }
    activityPublishService.submit(userId, request);
    return Result.ok(null);
  }
}
