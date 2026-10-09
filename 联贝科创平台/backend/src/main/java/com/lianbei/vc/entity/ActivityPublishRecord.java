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
@TableName("activity_publish_request")
public class ActivityPublishRecord {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long userId;
  private String title;
  private String location;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private String organizerName;
  private String priceText;
  private String description;
  private String contactName;
  private String contactPhone;
  /** pending | approved | rejected */
  private String status;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
