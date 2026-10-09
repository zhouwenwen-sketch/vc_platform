package com.lianbei.vc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lianbei.vc.common.constants.MaDealConstants;
import com.lianbei.vc.dto.response.MaDealDetailVO;
import com.lianbei.vc.dto.response.MaDealListItemVO;
import com.lianbei.vc.entity.ConnectionRecord;
import com.lianbei.vc.entity.MaDeal;
import com.lianbei.vc.exception.BusinessException;
import com.lianbei.vc.mapper.ConnectionRecordMapper;
import com.lianbei.vc.mapper.MaDealMapper;
import com.lianbei.vc.service.MaDealService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class MaDealServiceImpl implements MaDealService {

  private final MaDealMapper maDealMapper;
  private final ConnectionRecordMapper connectionRecordMapper;

  @Override
  public IPage<MaDealListItemVO> pageDeals(int pageNum, int pageSize, String keyword, String category) {
    String normalizedCategory = normalizeCategory(category);
    String normalizedKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
    Page<MaDeal> page = new Page<>(pageNum, pageSize);
    IPage<MaDeal> result =
        maDealMapper.selectDealPage(page, normalizedKeyword, normalizedCategory);
    return result.convert(MaDealAssembler::toListItemVO);
  }

  @Override
  public MaDealDetailVO getDetail(Long id, Long userId) {
    MaDeal deal = requirePublishedDeal(id);
    maDealMapper.incrementViewCount(id);
    if (deal.getViewCount() == null) {
      deal.setViewCount(0);
    }
    deal.setViewCount(deal.getViewCount() + 1);
    boolean appointed = hasAppointed(userId, id);
    return MaDealAssembler.toDetailVO(deal, appointed);
  }

  @Override
  @Transactional
  public void appoint(Long userId, Long dealId) {
    if (userId == null) {
      throw new BusinessException(401, "未登录，请先登录");
    }
    MaDeal deal = requirePublishedDeal(dealId);
    if (hasAppointed(userId, dealId)) {
      throw new BusinessException("您已预约该项目");
    }
    ConnectionRecord record =
        ConnectionRecord.builder()
            .userId(userId)
            .targetType(MaDealConstants.TARGET_TYPE)
            .targetId(dealId)
            .targetName(buildTargetName(deal))
            .connectionTime(LocalDateTime.now())
            .status(MaDealConstants.CONNECTION_STATUS_PENDING)
            .build();
    connectionRecordMapper.insert(record);
    maDealMapper.incrementAppointmentCount(dealId);
  }

  @Override
  public void recordShare(Long id) {
    requirePublishedDeal(id);
    maDealMapper.incrementShareCount(id);
  }

  private MaDeal requirePublishedDeal(Long id) {
    MaDeal deal = maDealMapper.selectById(id);
    if (deal == null || deal.getStatus() == null || deal.getStatus() != 1) {
      throw new BusinessException("项目不存在");
    }
    return deal;
  }

  private boolean hasAppointed(Long userId, Long dealId) {
    if (userId == null) {
      return false;
    }
    Long count =
        connectionRecordMapper.selectCount(
            new LambdaQueryWrapper<ConnectionRecord>()
                .eq(ConnectionRecord::getUserId, userId)
                .eq(ConnectionRecord::getTargetType, MaDealConstants.TARGET_TYPE)
                .eq(ConnectionRecord::getTargetId, dealId));
    return count != null && count > 0;
  }

  private String buildTargetName(MaDeal deal) {
    if (StringUtils.hasText(deal.getSummary())) {
      return deal.getSummary();
    }
    if (StringUtils.hasText(deal.getTitle())) {
      return deal.getTitle();
    }
    return deal.getProjectNo();
  }

  private String normalizeCategory(String category) {
    if (!StringUtils.hasText(category) || "all".equalsIgnoreCase(category.trim())) {
      return null;
    }
    return category.trim();
  }
}
