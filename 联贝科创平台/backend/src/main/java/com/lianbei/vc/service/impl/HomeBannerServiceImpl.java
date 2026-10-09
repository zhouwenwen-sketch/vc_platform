package com.lianbei.vc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lianbei.vc.entity.HomeBanner;
import com.lianbei.vc.mapper.HomeBannerMapper;
import com.lianbei.vc.service.HomeBannerService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class HomeBannerServiceImpl implements HomeBannerService {

  private final HomeBannerMapper homeBannerMapper;

  public HomeBannerServiceImpl(HomeBannerMapper homeBannerMapper) {
    this.homeBannerMapper = homeBannerMapper;
  }

  @Override
  public List<Map<String, Object>> listActiveForHome() {
    LambdaQueryWrapper<HomeBanner> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(HomeBanner::getStatus, 1).orderByAsc(HomeBanner::getSortOrder).orderByAsc(HomeBanner::getId);
    return homeBannerMapper.selectList(wrapper).stream()
        .map(this::toHomeItem)
        .collect(Collectors.toList());
  }

  private Map<String, Object> toHomeItem(HomeBanner banner) {
    Map<String, Object> item = new LinkedHashMap<>();
    item.put("id", banner.getId());
    item.put("imageUrl", banner.getImageUrl());
    item.put("title", banner.getTitle());
    item.put("subtitle", banner.getSubtitle());
    if (banner.getLinkUrl() != null && !banner.getLinkUrl().isBlank()) {
      item.put("linkUrl", banner.getLinkUrl());
    }
    return item;
  }
}
