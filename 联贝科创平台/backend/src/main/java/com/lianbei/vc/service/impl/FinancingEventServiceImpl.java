package com.lianbei.vc.service.impl;

import com.lianbei.vc.common.PageResult;
import com.lianbei.vc.dto.response.FinancingEventVO;
import com.lianbei.vc.service.FinancingEventService;
import com.lianbei.vc.service.ProjectService;
import org.springframework.stereotype.Service;

@Service
public class FinancingEventServiceImpl implements FinancingEventService {

  private final ProjectService projectService;

  public FinancingEventServiceImpl(ProjectService projectService) {
    this.projectService = projectService;
  }

  @Override
  public PageResult<FinancingEventVO> pageEvents(
      int pageNum,
      int pageSize,
      String industry,
      String region,
      String regionScope,
      String round,
      String financingYear,
      String currency) {
    return projectService.pageFinancingEvents(
        pageNum, pageSize, industry, region, regionScope, round, financingYear, currency);
  }
}
