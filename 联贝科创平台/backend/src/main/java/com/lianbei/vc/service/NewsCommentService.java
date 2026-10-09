package com.lianbei.vc.service;

import com.lianbei.vc.dto.request.NewsCommentCreateRequest;
import com.lianbei.vc.dto.response.NewsCommentVO;
import java.util.List;

public interface NewsCommentService {

  List<NewsCommentVO> listByNewsId(Long newsId, int limit);

  NewsCommentVO create(Long userId, Long newsId, NewsCommentCreateRequest request);
}
