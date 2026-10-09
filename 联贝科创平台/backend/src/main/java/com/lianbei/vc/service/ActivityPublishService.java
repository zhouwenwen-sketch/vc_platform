package com.lianbei.vc.service;

import com.lianbei.vc.dto.request.ActivityPublishRequest;

public interface ActivityPublishService {

  void submit(Long userId, ActivityPublishRequest request);
}
