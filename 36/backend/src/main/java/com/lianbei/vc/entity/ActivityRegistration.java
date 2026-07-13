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
@TableName("activity_registration")
public class ActivityRegistration {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long userId;
  private Long activityId;
  private String name;
  private String phone;
  private String orgName;
  private String position;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
