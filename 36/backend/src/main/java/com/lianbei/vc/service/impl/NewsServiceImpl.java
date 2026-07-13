package com.lianbei.vc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lianbei.vc.common.BrandTextSanitizer;
import com.lianbei.vc.exception.BusinessException;
import com.lianbei.vc.common.PageResult;
import com.lianbei.vc.dto.response.NewsDetailVO;
import com.lianbei.vc.dto.response.NewsListItemVO;
import com.lianbei.vc.entity.News;
import com.lianbei.vc.entity.Project;
import com.lianbei.vc.mapper.NewsMapper;
import com.lianbei.vc.service.NewsProjectLinkService;
import com.lianbei.vc.service.NewsService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class NewsServiceImpl extends ServiceImpl<NewsMapper, News> implements NewsService {

  private static final DateTimeFormatter PUBLISH_FMT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
  private static final DateTimeFormatter ARTICLE_PUBLISH_FMT =
      DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm");

  private final NewsProjectLinkService newsProjectLinkService;

  public NewsServiceImpl(NewsProjectLinkService newsProjectLinkService) {
    this.newsProjectLinkService = newsProjectLinkService;
  }

  @Override
  public PageResult<NewsListItemVO> pageNews(int pageNum, int pageSize) {
    Page<News> page =
        page(
            new Page<>(pageNum, pageSize),
            new LambdaQueryWrapper<News>().orderByDesc(News::getCreateTime));
    List<NewsListItemVO> list =
        page.getRecords().stream().map(this::toListItem).collect(Collectors.toList());
    return PageResult.of(page.getTotal(), list);
  }

  private NewsListItemVO toListItem(News news) {
    Project project = resolveProject(news);
    String round =
        project != null && StringUtils.hasText(project.getRound())
            ? project.getRound()
            : news.getTag();
    return NewsListItemVO.builder()
        .id(news.getId())
        .title(BrandTextSanitizer.sanitize(news.getTitle()))
        .newsType(StringUtils.hasText(news.getNewsType()) ? news.getNewsType() : "快讯")
        .source(BrandTextSanitizer.sanitize(news.getSource()))
        .tag(news.getTag())
        .region(news.getRegion())
        .projectName(project != null ? project.getName() : news.getProjectName())
        .createTime(news.getCreateTime())
        .projectId(project != null ? project.getId() : news.getProjectId())
        .logoUrl(project != null ? project.getLogoUrl() : null)
        .round(round)
        .build();
  }

  private Project resolveProject(News news) {
    if (news == null) {
      return null;
    }
    return newsProjectLinkService.resolveProject(news.getProjectId(), news.getProjectName());
  }

  @Override
  public List<News> listByProject(Long projectId, String projectName, int limit) {
    if (projectId == null && !StringUtils.hasText(projectName)) {
      return List.of();
    }
    LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();
    wrapper.and(
        w -> {
          if (projectId != null) {
            w.eq(News::getProjectId, projectId);
          }
          if (StringUtils.hasText(projectName)) {
            if (projectId != null) {
              w.or().eq(News::getProjectName, projectName.trim());
            } else {
              w.eq(News::getProjectName, projectName.trim());
            }
          }
        });
    return list(wrapper.orderByDesc(News::getCreateTime).last("LIMIT " + Math.max(limit, 1)));
  }

  @Override
  public NewsDetailVO getNewsDetail(Long id) {
    News news = getDetail(id);
    Project project = resolveProject(news);
    News next = findNextNews(news);
    String newsType = StringUtils.hasText(news.getNewsType()) ? news.getNewsType() : "快讯";
    String round =
        project != null && StringUtils.hasText(project.getRound())
            ? project.getRound()
            : news.getTag();
    return NewsDetailVO.builder()
        .id(news.getId())
        .title(BrandTextSanitizer.sanitize(news.getTitle()))
        .newsType(newsType)
        .source(BrandTextSanitizer.sanitize(news.getSource()))
        .summary(BrandTextSanitizer.sanitize(news.getSummary()))
        .content(resolveContent(news))
        .contentFormat(
            StringUtils.hasText(news.getContentFormat()) ? news.getContentFormat() : "text")
        .sourceUrl(news.getSourceUrl())
        .coverUrl(news.getCoverUrl())
        .sourceAvatar(news.getSourceAvatar())
        .attribution(resolveAttribution(news))
        .likeCount(0)
        .publishTime(formatPublishTime(news.getCreateTime(), newsType))
        .projectName(project != null ? project.getName() : news.getProjectName())
        .projectId(project != null ? project.getId() : news.getProjectId())
        .logoUrl(project != null ? project.getLogoUrl() : null)
        .round(round)
        .tag(news.getTag())
        .region(news.getRegion())
        .nextNews(next != null ? toNextPreview(next) : null)
        .build();
  }

  private String resolveAttribution(News news) {
    if (StringUtils.hasText(news.getAttribution())) {
      return BrandTextSanitizer.sanitize(news.getAttribution());
    }
    if (!"文章".equals(news.getNewsType())) {
      return null;
    }
    String source = StringUtils.hasText(news.getSource()) ? news.getSource() : "大学生创投";
    return "本文来自「" + source + "」，大学生创投经授权发布。\n该文观点仅代表作者本人，大学生创投平台仅提供信息存储空间服务。";
  }

  private News findNextNews(News current) {
    if (current.getCreateTime() == null) {
      return null;
    }
    return getOne(
        new LambdaQueryWrapper<News>()
            .lt(News::getCreateTime, current.getCreateTime())
            .orderByDesc(News::getCreateTime)
            .last("LIMIT 1"));
  }

  private NewsDetailVO.NextNewsPreview toNextPreview(News news) {
    return NewsDetailVO.NextNewsPreview.builder()
        .id(news.getId())
        .title(news.getTitle())
        .summary(
            StringUtils.hasText(news.getSummary())
                ? news.getSummary()
                : truncate(news.getTitle(), 80))
        .createTime(news.getCreateTime())
        .build();
  }

  private String resolveContent(News news) {
    if (StringUtils.hasText(news.getContent())) {
      return BrandTextSanitizer.sanitize(news.getContent());
    }
    if (StringUtils.hasText(news.getSummary())) {
      return BrandTextSanitizer.sanitize(news.getSummary());
    }
    return BrandTextSanitizer.sanitize(news.getTitle());
  }

  private String formatPublishTime(LocalDateTime time, String newsType) {
    if (time == null) {
      return "";
    }
    if ("文章".equals(newsType)) {
      return ARTICLE_PUBLISH_FMT.format(time);
    }
    return PUBLISH_FMT.format(time);
  }

  private String truncate(String text, int maxLen) {
    if (!StringUtils.hasText(text) || text.length() <= maxLen) {
      return text;
    }
    return text.substring(0, maxLen) + "...";
  }

  @Override
  public News getDetail(Long id) {
    News news = getById(id);
    if (news == null) {
      throw new BusinessException("快讯不存在");
    }
    return news;
  }
}
