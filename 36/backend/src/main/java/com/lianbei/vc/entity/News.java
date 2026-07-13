package com.lianbei.vc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("news")
public class News {

  @TableId(type = IdType.AUTO)
  private Long id;

  private String title;
  private String summary;
  private String source;
  private String tag;
  private String region;
  private String newsType;
  private String projectName;
  private Long projectId;
  private String content;
  private String sourceUrl;
  private String coverUrl;
  private String sourceAvatar;
  private String contentFormat;
  private String attribution;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
