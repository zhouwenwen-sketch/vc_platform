package com.lianbei.vc.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lianbei.vc.common.PageResult;
import com.lianbei.vc.common.Result;
import com.lianbei.vc.dto.response.MaDealDetailVO;
import com.lianbei.vc.dto.response.MaDealListItemVO;
import com.lianbei.vc.exception.BusinessException;
import com.lianbei.vc.service.MaDealService;
import com.lianbei.vc.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 融资并购 C 端接口 */
@RestController
@RequestMapping("/api/ma-deals")
@RequiredArgsConstructor
public class MaDealController {

  private final MaDealService maDealService;

  @GetMapping("/page")
  public Result<PageResult<MaDealListItemVO>> page(
      @RequestParam(defaultValue = "1") int pageNum,
      @RequestParam(defaultValue = "10") int pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String category) {
    IPage<MaDealListItemVO> page = maDealService.pageDeals(pageNum, pageSize, keyword, category);
    return Result.ok(PageResult.of(page));
  }

  @GetMapping("/{id}")
  public Result<MaDealDetailVO> detail(@PathVariable Long id) {
    return Result.ok(maDealService.getDetail(id, UserContext.getUserId()));
  }

  @PostMapping("/{id}/appoint")
  public Result<Void> appoint(@PathVariable Long id) {
    Long userId = UserContext.getUserId();
    if (userId == null) {
      throw new BusinessException(401, "未登录，请先登录");
    }
    maDealService.appoint(userId, id);
    return Result.ok(null);
  }

  @PostMapping("/{id}/share")
  public Result<Void> share(@PathVariable Long id) {
    maDealService.recordShare(id);
    return Result.ok(null);
  }
}
