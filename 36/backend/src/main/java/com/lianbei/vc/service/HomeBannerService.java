package com.lianbei.vc.service;

import java.util.List;
import java.util.Map;

public interface HomeBannerService {

  /** C 端首页轮播：仅返回启用项，按 sortOrder 升序 */
  List<Map<String, Object>> listActiveForHome();
}
