package com.lianbei.vc.service.impl;

import com.lianbei.vc.dto.request.ActivityPublishRequest;
import com.lianbei.vc.entity.ActivityPublishRecord;
import com.lianbei.vc.exception.BusinessException;
import com.lianbei.vc.mapper.ActivityPublishRecordMapper;
import com.lianbei.vc.service.ActivityPublishService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ActivityPublishServiceImpl implements ActivityPublishService {

  private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final ActivityPublishRecordMapper activityPublishRecordMapper;

  public ActivityPublishServiceImpl(ActivityPublishRecordMapper activityPublishRecordMapper) {
    this.activityPublishRecordMapper = activityPublishRecordMapper;
  }

  @Override
  public void submit(Long userId, ActivityPublishRequest request) {
    if (userId == null) {
      throw new BusinessException(401, "未登录，请先登录");
    }
    validate(request);

    ActivityPublishRecord record =
        ActivityPublishRecord.builder()
            .userId(userId)
            .title(request.getTitle().trim())
            .location(request.getLocation().trim())
            .startTime(parseDateTime(request.getStartTime()))
            .endTime(parseDateTime(request.getEndTime()))
            .organizerName(request.getOrganizerName().trim())
            .priceText(
                StringUtils.hasText(request.getPriceText())
                    ? request.getPriceText().trim()
                    : "免费")
            .description(request.getDescription().trim())
            .contactName(request.getContactName().trim())
            .contactPhone(request.getContactPhone().trim())
            .status("pending")
            .build();
    activityPublishRecordMapper.insert(record);
  }

  private static void validate(ActivityPublishRequest request) {
    if (request == null) {
      throw new BusinessException("请填写活动信息");
    }
    if (!StringUtils.hasText(request.getTitle())) {
      throw new BusinessException("请填写活动标题");
    }
    if (!StringUtils.hasText(request.getLocation())) {
      throw new BusinessException("请填写活动地点");
    }
    if (!StringUtils.hasText(request.getStartTime())) {
      throw new BusinessException("请选择开始时间");
    }
    if (!StringUtils.hasText(request.getEndTime())) {
      throw new BusinessException("请选择结束时间");
    }
    if (!StringUtils.hasText(request.getOrganizerName())) {
      throw new BusinessException("请填写主办方");
    }
    if (!StringUtils.hasText(request.getDescription())) {
      throw new BusinessException("请填写活动简介");
    }
    if (!StringUtils.hasText(request.getContactName())) {
      throw new BusinessException("请填写联系人");
    }
    if (!StringUtils.hasText(request.getContactPhone())) {
      throw new BusinessException("请填写联系电话");
    }
  }

  private static LocalDateTime parseDateTime(String raw) {
    String value = raw.trim();
    if (value.length() == 16) {
      value += ":00";
    }
    return LocalDateTime.parse(value, DT);
  }
}
