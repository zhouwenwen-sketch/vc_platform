package com.lianbei.vc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lianbei.vc.common.PageResult;
import com.lianbei.vc.dto.response.InstitutionDetailVO;
import com.lianbei.vc.entity.Institution;

public interface InstitutionService extends IService<Institution> {

  InstitutionDetailVO getInstitutionDetail(Long id);

  PageResult<Institution> pageLibrary(
      int pageNum,
      int pageSize,
      String keyword,
      String investmentField,
      String instType,
      String foundedYear);
}
