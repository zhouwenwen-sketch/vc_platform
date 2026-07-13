package com.lianbei.vc.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lianbei.vc.dto.response.MaDealDetailVO;
import com.lianbei.vc.dto.response.MaDealListItemVO;

public interface MaDealService {

  IPage<MaDealListItemVO> pageDeals(int pageNum, int pageSize, String keyword, String category);

  MaDealDetailVO getDetail(Long id, Long userId);

  void appoint(Long userId, Long dealId);

  void recordShare(Long id);
}
