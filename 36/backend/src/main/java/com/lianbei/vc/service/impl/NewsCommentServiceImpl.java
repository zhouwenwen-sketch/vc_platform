package com.lianbei.vc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lianbei.vc.dto.request.NewsCommentCreateRequest;
import com.lianbei.vc.dto.response.NewsCommentVO;
import com.lianbei.vc.entity.News;
import com.lianbei.vc.entity.NewsComment;
import com.lianbei.vc.entity.User;
import com.lianbei.vc.exception.BusinessException;
import com.lianbei.vc.mapper.NewsCommentMapper;
import com.lianbei.vc.mapper.NewsMapper;
import com.lianbei.vc.mapper.UserMapper;
import com.lianbei.vc.service.NewsCommentService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class NewsCommentServiceImpl extends ServiceImpl<NewsCommentMapper, NewsComment>
    implements NewsCommentService {

  private static final int MAX_CONTENT_LEN = 500;
  private static final int DEFAULT_LIST_LIMIT = 100;

  private final NewsMapper newsMapper;
  private final UserMapper userMapper;

  public NewsCommentServiceImpl(NewsMapper newsMapper, UserMapper userMapper) {
    this.newsMapper = newsMapper;
    this.userMapper = userMapper;
  }

  @Override
  public List<NewsCommentVO> listByNewsId(Long newsId, int limit) {
    ensureNewsExists(newsId);
    int size = limit > 0 ? Math.min(limit, DEFAULT_LIST_LIMIT) : DEFAULT_LIST_LIMIT;
    List<NewsComment> rows =
        list(
            new LambdaQueryWrapper<NewsComment>()
                .eq(NewsComment::getNewsId, newsId)
                .orderByDesc(NewsComment::getCreateTime)
                .last("LIMIT " + size));
    return rows.stream().map(this::toVO).collect(Collectors.toList());
  }

  @Override
  public NewsCommentVO create(Long userId, Long newsId, NewsCommentCreateRequest request) {
    if (userId == null) {
      throw new BusinessException(401, "未登录，请先登录");
    }
    ensureNewsExists(newsId);
    String content = request == null ? "" : request.getContent();
    if (!StringUtils.hasText(content)) {
      throw new BusinessException("评论内容不能为空");
    }
    String trimmed = content.trim();
    if (trimmed.length() > MAX_CONTENT_LEN) {
      throw new BusinessException("评论内容不能超过" + MAX_CONTENT_LEN + "字");
    }

    User user = userMapper.selectById(userId);
    if (user == null) {
      throw new BusinessException(401, "用户不存在，请重新登录");
    }

    NewsComment comment =
        NewsComment.builder()
            .newsId(newsId)
            .userId(userId)
            .nickname(resolveNickname(user))
            .avatar(user.getAvatar())
            .content(trimmed)
            .build();
    save(comment);
    return toVO(comment);
  }

  private void ensureNewsExists(Long newsId) {
    if (newsId == null) {
      throw new BusinessException("资讯不存在");
    }
    News news = newsMapper.selectById(newsId);
    if (news == null) {
      throw new BusinessException("资讯不存在");
    }
  }

  private String resolveNickname(User user) {
    if (StringUtils.hasText(user.getNickname())) {
      return user.getNickname().trim();
    }
    return "氪友" + user.getId();
  }

  private NewsCommentVO toVO(NewsComment comment) {
    return NewsCommentVO.builder()
        .id(comment.getId())
        .newsId(comment.getNewsId())
        .userId(comment.getUserId())
        .nickname(comment.getNickname())
        .avatar(comment.getAvatar())
        .content(comment.getContent())
        .createTime(comment.getCreateTime())
        .build();
  }
}
