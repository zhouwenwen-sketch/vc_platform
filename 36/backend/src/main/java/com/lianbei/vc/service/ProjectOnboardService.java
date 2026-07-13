package com.lianbei.vc.service;

import com.lianbei.vc.dto.request.ProjectOnboardSubmitRequest;
import com.lianbei.vc.dto.response.ProjectOnboardStatusVO;
import com.lianbei.vc.dto.response.ProjectOnboardSubmitResponse;

public interface ProjectOnboardService {

  ProjectOnboardSubmitResponse submit(Long userId, ProjectOnboardSubmitRequest request);

  ProjectOnboardStatusVO getLatestStatus(Long userId);
}
