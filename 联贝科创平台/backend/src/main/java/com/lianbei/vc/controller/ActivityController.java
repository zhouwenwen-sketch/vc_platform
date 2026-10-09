package com.lianbei.vc.controller;



import com.lianbei.vc.common.PageResult;

import com.lianbei.vc.common.Result;

import com.lianbei.vc.dto.request.ActivityRegisterRequest;

import com.lianbei.vc.dto.response.ActivityDetailVO;

import com.lianbei.vc.entity.Activity;

import com.lianbei.vc.exception.BusinessException;

import com.lianbei.vc.service.ActivityService;

import com.lianbei.vc.utils.UserContext;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.RestController;



/**

 * 创投活动 / 每日路演接口

 */

@RestController

@RequestMapping("/api/activities")

public class ActivityController {



  private final ActivityService activityService;



  public ActivityController(ActivityService activityService) {

    this.activityService = activityService;

  }



  @GetMapping("/page")

  public Result<PageResult<Activity>> page(

      @RequestParam(defaultValue = "1") int pageNum,

      @RequestParam(defaultValue = "10") int pageSize,

      @RequestParam(required = false) String status) {

    return Result.ok(activityService.pageActivities(pageNum, pageSize, status));

  }



  /** 首页每日路演，默认前 6 条 */
  @GetMapping("/featured")
  public Result<List<Activity>> featured(@RequestParam(defaultValue = "6") int limit) {
    return Result.ok(activityService.listFeaturedRoadshow(limit));
  }

  /** 首页活动推荐：活动库 event，推荐优先，再按最新事件排序 */
  @GetMapping("/recommended")
  public Result<List<Activity>> recommended(@RequestParam(defaultValue = "6") int limit) {
    return Result.ok(activityService.listHomeRecommendedActivities(limit));
  }



  /** 我参与的活动：tab=reserved|signed_up|attended */

  @GetMapping("/my/page")

  public Result<PageResult<Activity>> myPage(

      @RequestParam(defaultValue = "1") int pageNum,

      @RequestParam(defaultValue = "10") int pageSize,

      @RequestParam(defaultValue = "reserved") String tab,

      @RequestParam(required = false) String keyword) {

    return Result.ok(

        activityService.pageMyActivities(

            UserContext.getUserId(), tab, keyword, pageNum, pageSize));

  }



  @GetMapping("/{id}")

  public Result<ActivityDetailVO> detail(@PathVariable Long id) {

    return Result.ok(activityService.getDetail(id, UserContext.getUserId()));

  }



  /** 提交活动报名 */

  @PostMapping("/{id}/register")

  public Result<Void> register(

      @PathVariable Long id, @RequestBody ActivityRegisterRequest request) {

    Long userId = UserContext.getUserId();

    if (userId == null) {

      throw new BusinessException(401, "未登录，请先登录");

    }

    activityService.register(userId, id, request);

    return Result.ok(null);

  }

}

