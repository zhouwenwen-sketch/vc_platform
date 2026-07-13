package com.lianbei.vc.service;

import java.util.Map;

/** 入驻申请审核通过后的项目落库 */
public interface ProjectOnboardPublishService {

  /**
   * 将 apply_data 写入 project 及相关表
   *
   * @return 新建项目 ID
   */
  Long publish(Long userId, Map<String, Object> applyData);
}
