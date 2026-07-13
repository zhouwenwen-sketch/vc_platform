package com.lianbei.vc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lianbei.vc.common.PageResult;
import com.lianbei.vc.dto.response.NewsDetailVO;
import com.lianbei.vc.dto.response.NewsListItemVO;
import com.lianbei.vc.entity.News;

public interface NewsService extends IService<News> {

  PageResult<NewsListItemVO> pageNews(int pageNum, int pageSize);

  NewsDetailVO getNewsDetail(Long id);

  News getDetail(Long id);

  java.util.List<News> listByProject(Long projectId, String projectName, int limit);
}
