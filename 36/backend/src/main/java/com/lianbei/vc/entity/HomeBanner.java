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
@TableName("home_banner")
public class HomeBanner {

  @TableId(type = IdType.AUTO)
  private Long id;

  private String imageUrl;
  private String title;
  private String subtitle;
  private String linkUrl;
  private Integer sortOrder;
  /** 1=启用 0=禁用 */
  private Integer status;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
