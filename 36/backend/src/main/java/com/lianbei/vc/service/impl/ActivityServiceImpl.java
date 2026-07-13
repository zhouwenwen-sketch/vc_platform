package com.lianbei.vc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianbei.vc.common.PageResult;
import com.lianbei.vc.common.constants.Constants;
import com.lianbei.vc.dto.request.ActivityRegisterRequest;
import com.lianbei.vc.dto.response.ActivityDetailVO;
import com.lianbei.vc.entity.Activity;
import com.lianbei.vc.entity.ActivityRegistration;
import com.lianbei.vc.entity.Institution;
import com.lianbei.vc.entity.UserActivityRecord;
import com.lianbei.vc.exception.BusinessException;
import com.lianbei.vc.mapper.ActivityMapper;
import com.lianbei.vc.mapper.ActivityRegistrationMapper;
import com.lianbei.vc.mapper.InstitutionMapper;
import com.lianbei.vc.mapper.UserActivityRecordMapper;
import com.lianbei.vc.service.ActivityService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity>
    implements ActivityService {

  private final UserActivityRecordMapper userActivityRecordMapper;
  private final ActivityRegistrationMapper activityRegistrationMapper;
  private final InstitutionMapper institutionMapper;
  private final ObjectMapper objectMapper;

  public ActivityServiceImpl(
      UserActivityRecordMapper userActivityRecordMapper,
      ActivityRegistrationMapper activityRegistrationMapper,
      InstitutionMapper institutionMapper,
      ObjectMapper objectMapper) {
    this.userActivityRecordMapper = userActivityRecordMapper;
    this.activityRegistrationMapper = activityRegistrationMapper;
    this.institutionMapper = institutionMapper;
    this.objectMapper = objectMapper;
  }

  @Override
  public PageResult<Activity> pageActivities(int pageNum, int pageSize, String status) {
    LambdaQueryWrapper<Activity> wrapper =
        new LambdaQueryWrapper<Activity>()
            .eq(Activity::getActivityType, Constants.ACTIVITY_TYPE_EVENT)
            .orderByDesc(Activity::getIsRecommended)
            .orderByDesc(Activity::getStartTime)
            .orderByDesc(Activity::getId);
    if (Constants.STATUS_ONGOING.equals(status)) {
      wrapper.in(
          Activity::getStatus, Constants.STATUS_ONGOING, Constants.STATUS_REGISTERING);
    } else if (Constants.STATUS_ENDED.equals(status)) {
      wrapper.eq(Activity::getStatus, Constants.STATUS_ENDED);
    } else if (StringUtils.hasText(status) && !"all".equalsIgnoreCase(status.trim())) {
      wrapper.eq(Activity::getStatus, status);
    }
    Page<Activity> page = page(new Page<>(pageNum, pageSize), wrapper);
    return PageResult.of(page);
  }

  @Override
  public List<Activity> listFeaturedRoadshow(int limit) {
    return list(
        new LambdaQueryWrapper<Activity>()
            .eq(Activity::getActivityType, Constants.ACTIVITY_TYPE_ROADSHOW)
            .orderByAsc(Activity::getStartTime)
            .last("LIMIT " + limit));
  }

  @Override
  public List<Activity> listHomeRecommendedActivities(int limit) {
    return list(
        new LambdaQueryWrapper<Activity>()
            .eq(Activity::getActivityType, Constants.ACTIVITY_TYPE_EVENT)
            .orderByDesc(Activity::getIsRecommended)
            .orderByDesc(Activity::getStartTime)
            .orderByDesc(Activity::getId)
            .last("LIMIT " + limit));
  }

  @Override
  public ActivityDetailVO getDetail(Long id, Long userId) {
    Activity activity = getById(id);
    if (activity == null) {
      throw new BusinessException("活动不存在");
    }
    boolean signedUp = userId != null && hasSignedUp(userId, id);
    return ActivityDetailVO.builder()
        .id(activity.getId())
        .title(activity.getTitle())
        .coverUrl(activity.getCoverUrl())
        .bannerUrls(resolveBannerUrls(activity))
        .detailImages(parseJsonList(activity.getDetailImages()))
        .location(activity.getLocation())
        .startTime(activity.getStartTime())
        .endTime(activity.getEndTime())
        .status(activity.getStatus())
        .statusText(statusText(activity.getStatus()))
        .priceText(resolvePriceText(activity))
        .participantCount(safeCount(activity.getParticipantCount()))
        .likeCount(safeCount(activity.getLikeCount()))
        .organizerName(activity.getOrganizerName())
        .organizer(buildOrganizer(activity))
        .signedUp(signedUp)
        .liked(false)
        .build();
  }

  @Override
  @Transactional
  public void register(Long userId, Long activityId, ActivityRegisterRequest request) {
    if (userId == null) {
      throw new BusinessException(401, "未登录，请先登录");
    }
    validateRegisterRequest(request);

    Activity activity = getById(activityId);
    if (activity == null) {
      throw new BusinessException("活动不存在");
    }
    if (Constants.STATUS_ENDED.equals(activity.getStatus())) {
      throw new BusinessException("活动已结束，无法报名");
    }
    if (!Constants.STATUS_REGISTERING.equals(activity.getStatus())) {
      throw new BusinessException("当前活动未开放报名");
    }

    ActivityRegistration existing =
        activityRegistrationMapper.selectOne(
            new LambdaQueryWrapper<ActivityRegistration>()
                .eq(ActivityRegistration::getUserId, userId)
                .eq(ActivityRegistration::getActivityId, activityId));
    if (existing != null) {
      throw new BusinessException("您已报名该活动");
    }

    ActivityRegistration registration =
        ActivityRegistration.builder()
            .userId(userId)
            .activityId(activityId)
            .name(request.getName().trim())
            .phone(request.getPhone().trim())
            .orgName(request.getOrgName().trim())
            .position(request.getPosition().trim())
            .build();
    activityRegistrationMapper.insert(registration);

    UserActivityRecord record =
        userActivityRecordMapper.selectOne(
            new LambdaQueryWrapper<UserActivityRecord>()
                .eq(UserActivityRecord::getUserId, userId)
                .eq(UserActivityRecord::getActivityId, activityId));
    if (record == null) {
      userActivityRecordMapper.insert(
          UserActivityRecord.builder()
              .userId(userId)
              .activityId(activityId)
              .joinStatus(Constants.ACTIVITY_JOIN_SIGNED_UP)
              .build());
    } else if (!Constants.ACTIVITY_JOIN_SIGNED_UP.equals(record.getJoinStatus())
        && !Constants.ACTIVITY_JOIN_ATTENDED.equals(record.getJoinStatus())) {
      record.setJoinStatus(Constants.ACTIVITY_JOIN_SIGNED_UP);
      userActivityRecordMapper.updateById(record);
    }

    activity.setParticipantCount(safeCount(activity.getParticipantCount()) + 1);
    updateById(activity);
  }

  @Override
  public PageResult<Activity> pageMyActivities(
      Long userId, String tab, String keyword, int pageNum, int pageSize) {
    if (userId == null) {
      throw new BusinessException(401, "未登录，请先登录");
    }
    String joinStatus = normalizeJoinStatus(tab);

    List<UserActivityRecord> records =
        userActivityRecordMapper.selectList(
            new LambdaQueryWrapper<UserActivityRecord>()
                .eq(UserActivityRecord::getUserId, userId)
                .eq(UserActivityRecord::getJoinStatus, joinStatus));
    List<Long> activityIds =
        records.stream().map(UserActivityRecord::getActivityId).filter(Objects::nonNull).toList();
    if (activityIds.isEmpty()) {
      return PageResult.of(0L, Collections.emptyList());
    }

    LambdaQueryWrapper<Activity> wrapper =
        new LambdaQueryWrapper<Activity>()
            .in(Activity::getId, activityIds)
            .like(StringUtils.hasText(keyword), Activity::getTitle, keyword.trim())
            .orderByDesc(Activity::getStartTime);
    Page<Activity> page = page(new Page<>(pageNum, pageSize), wrapper);
    return PageResult.of(page);
  }

  private boolean hasSignedUp(Long userId, Long activityId) {
    Long count =
        activityRegistrationMapper.selectCount(
            new LambdaQueryWrapper<ActivityRegistration>()
                .eq(ActivityRegistration::getUserId, userId)
                .eq(ActivityRegistration::getActivityId, activityId));
    return count != null && count > 0;
  }

  /** 详情顶栏轮播：封面图排第一，其余来自 banner_urls（去重） */
  private List<String> resolveBannerUrls(Activity activity) {
    List<String> result = new ArrayList<>();
    if (StringUtils.hasText(activity.getCoverUrl())) {
      result.add(activity.getCoverUrl().trim());
    }
    for (String url : parseJsonList(activity.getBannerUrls())) {
      if (!StringUtils.hasText(url)) {
        continue;
      }
      String trimmed = url.trim();
      if (!result.contains(trimmed)) {
        result.add(trimmed);
      }
    }
    return result;
  }

  private List<String> parseJsonList(String raw) {
    if (!StringUtils.hasText(raw)) {
      return Collections.emptyList();
    }
    try {
      return objectMapper.readValue(raw, new TypeReference<List<String>>() {});
    } catch (Exception ex) {
      return Collections.emptyList();
    }
  }

  private ActivityDetailVO.OrganizerVO buildOrganizer(Activity activity) {
    if (activity.getOrganizerId() == null) {
      return null;
    }
    Institution institution = institutionMapper.selectById(activity.getOrganizerId());
    if (institution == null) {
      return null;
    }
    List<String> tags = new ArrayList<>();
    if (StringUtils.hasText(institution.getInvestmentField())) {
      tags.add(institution.getInvestmentField());
    }
    if (StringUtils.hasText(institution.getInstType())) {
      tags.add(institution.getInstType());
    }
    String metaText =
        String.join(
            " · ",
            List.of(
                    institution.getInstType() != null ? institution.getInstType() : "企业服务",
                    "未知规模",
                    institution.getEventCount() != null
                        ? institution.getEventCount() + "个事件"
                        : "500万")
                .stream()
                .filter(StringUtils::hasText)
                .toList());
    return ActivityDetailVO.OrganizerVO.builder()
        .id(institution.getId())
        .name(institution.getEntityName() != null ? institution.getEntityName() : institution.getName())
        .entityName(institution.getEntityName())
        .logoUrl(institution.getLogoUrl())
        .metaText(metaText)
        .tags(tags)
        .build();
  }

  private static String statusText(String status) {
    if (Constants.STATUS_REGISTERING.equals(status)) {
      return "正在报名";
    }
    if (Constants.STATUS_ENDED.equals(status)) {
      return "已结束";
    }
    return "进行中";
  }

  private static String resolvePriceText(Activity activity) {
    if (StringUtils.hasText(activity.getPriceText())) {
      return activity.getPriceText();
    }
    if (activity.getPrice() != null
        && activity.getPrice().compareTo(java.math.BigDecimal.ZERO) > 0) {
      return activity.getPrice().stripTrailingZeros().toPlainString() + "元";
    }
    return "免费";
  }

  private static int safeCount(Integer value) {
    return value != null ? value : 0;
  }

  private static void validateRegisterRequest(ActivityRegisterRequest request) {
    if (request == null) {
      throw new BusinessException("请填写报名信息");
    }
    if (!StringUtils.hasText(request.getName())) {
      throw new BusinessException("请填写姓名");
    }
    if (!StringUtils.hasText(request.getPhone())) {
      throw new BusinessException("请填写联系电话");
    }
    if (!StringUtils.hasText(request.getOrgName())) {
      throw new BusinessException("请填写单位名称");
    }
    if (!StringUtils.hasText(request.getPosition())) {
      throw new BusinessException("请填写职位");
    }
  }

  private static String normalizeJoinStatus(String tab) {
    if (!StringUtils.hasText(tab)) {
      return Constants.ACTIVITY_JOIN_RESERVED;
    }
    return switch (tab) {
      case Constants.ACTIVITY_JOIN_RESERVED,
          Constants.ACTIVITY_JOIN_SIGNED_UP,
          Constants.ACTIVITY_JOIN_ATTENDED -> tab;
      default -> throw new BusinessException("无效的活动状态");
    };
  }
}
