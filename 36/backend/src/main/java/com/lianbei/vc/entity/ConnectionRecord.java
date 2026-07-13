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
@TableName("connection_record")
public class ConnectionRecord {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long userId;
  private String targetType;
  private Long targetId;
  private String targetName;
  private LocalDateTime connectionTime;
  private String status;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
