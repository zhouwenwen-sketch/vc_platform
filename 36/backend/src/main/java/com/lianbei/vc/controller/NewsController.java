package com.lianbei.vc.controller;

import com.lianbei.vc.common.PageResult;
import com.lianbei.vc.common.Result;
import com.lianbei.vc.dto.request.NewsCommentCreateRequest;
import com.lianbei.vc.dto.response.NewsCommentVO;
import com.lianbei.vc.dto.response.NewsDetailVO;
import com.lianbei.vc.dto.response.NewsListItemVO;
import com.lianbei.vc.exception.BusinessException;
import com.lianbei.vc.service.NewsCommentService;
import com.lianbei.vc.service.NewsService;
import com.lianbei.vc.utils.UserContext;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 融资快讯接口
 */
@RestController
@RequestMapping("/api/news")
public class NewsController {

  private final NewsService newsService;
  private final NewsCommentService newsCommentService;

  public NewsController(NewsService newsService, NewsCommentService newsCommentService) {
    this.newsService = newsService;
    this.newsCommentService = newsCommentService;
  }

  @GetMapping("/page")
  public Result<PageResult<NewsListItemVO>> page(
      @RequestParam(defaultValue = "1") int pageNum,
      @RequestParam(defaultValue = "10") int pageSize) {
    return Result.ok(newsService.pageNews(pageNum, pageSize));
  }

  @GetMapping("/{id}")
  public Result<NewsDetailVO> detail(@PathVariable Long id) {
    return Result.ok(newsService.getNewsDetail(id));
  }

  /** 资讯评论列表 */
  @GetMapping("/{id}/comments")
  public Result<List<NewsCommentVO>> comments(
      @PathVariable Long id,
      @RequestParam(defaultValue = "100") int limit) {
    return Result.ok(newsCommentService.listByNewsId(id, limit));
  }

  /** 发表评论（需登录） */
  @PostMapping("/{id}/comments")
  public Result<NewsCommentVO> postComment(
      @PathVariable Long id, @RequestBody NewsCommentCreateRequest request) {
    Long userId = UserContext.getUserId();
    if (userId == null) {
      throw new BusinessException(401, "未登录，请先登录");
    }
    return Result.ok(newsCommentService.create(userId, id, request));
  }
}
