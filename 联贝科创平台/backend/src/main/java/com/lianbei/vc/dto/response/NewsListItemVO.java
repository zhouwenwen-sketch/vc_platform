package com.lianbei.vc.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 融资快报列表项 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsListItemVO {

  private Long id;
  private String title;
  private String newsType;
  private String source;
  private String tag;
  private String region;
  private String projectName;
  private LocalDateTime createTime;

  /** 关联项目（按 project_name 匹配） */
  private Long projectId;
  private String logoUrl;
  private String round;
}
