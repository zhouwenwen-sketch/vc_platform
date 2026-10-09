package com.lianbei.vc.service;

import com.lianbei.vc.common.PageResult;
import com.lianbei.vc.dto.response.FinancingEventVO;

public interface FinancingEventService {

  PageResult<FinancingEventVO> pageEvents(
      int pageNum,
      int pageSize,
      String industry,
      String region,
      String regionScope,
      String round,
      String financingYear,
      String currency);
}
