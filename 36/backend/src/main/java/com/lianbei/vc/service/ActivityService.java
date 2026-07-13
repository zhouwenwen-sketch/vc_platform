package com.lianbei.vc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lianbei.vc.common.PageResult;
import com.lianbei.vc.dto.request.ActivityRegisterRequest;
import com.lianbei.vc.dto.response.ActivityDetailVO;
import com.lianbei.vc.entity.Activity;
import java.util.List;

public interface ActivityService extends IService<Activity> {

  PageResult<Activity> pageActivities(int pageNum, int pageSize, String status);

  List<Activity> listFeaturedRoadshow(int limit);

  /** 首页活动推荐：活动库 event 类型，推荐优先，再按开始时间倒序 */
  List<Activity> listHomeRecommendedActivities(int limit);

  ActivityDetailVO getDetail(Long id, Long userId);

  void register(Long userId, Long activityId, ActivityRegisterRequest request);

  PageResult<Activity> pageMyActivities(
      Long userId, String tab, String keyword, int pageNum, int pageSize);
}
