package com.lianbei.vc.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NewsCommentVO {
  private Long id;
  private Long newsId;
  private Long userId;
  private String nickname;
  private String avatar;
  private String content;
  private LocalDateTime createTime;
}
