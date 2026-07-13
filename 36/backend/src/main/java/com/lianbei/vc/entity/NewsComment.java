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
@TableName("news_comment")
public class NewsComment {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long newsId;
  private Long userId;
  private String nickname;
  private String avatar;
  private String content;
  private LocalDateTime createTime;
}
