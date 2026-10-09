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
@TableName("activity")
public class Activity {

  @TableId(type = IdType.AUTO)
  private Long id;

  private String title;
  private String coverUrl;
  private String location;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private String status;
  private Integer participantCount;
  private String description;
  private String activityType;
  /** JSON 数组字符串，轮播图 URL 列表 */
  private String bannerUrls;
  /** JSON 数组字符串，详情长图 URL 列表 */
  private String detailImages;
  private java.math.BigDecimal price;
  private String priceText;
  private Long organizerId;
  private String organizerName;
  private Integer likeCount;
  /** 首页推荐：1=推荐活动 */
  private Integer isRecommended;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
