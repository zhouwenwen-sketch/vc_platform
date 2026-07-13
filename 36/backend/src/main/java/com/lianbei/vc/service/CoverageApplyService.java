package com.lianbei.vc.service;

import com.lianbei.vc.dto.request.CoverageApplySubmitRequest;
import com.lianbei.vc.dto.response.CoverageApplyCheckVO;
import com.lianbei.vc.dto.response.CoverageApplySubmitResponse;

public interface CoverageApplyService {

  CoverageApplySubmitResponse submit(Long userId, CoverageApplySubmitRequest request);

  CoverageApplyCheckVO checkApplied(Long userId, Long projectId);
}
