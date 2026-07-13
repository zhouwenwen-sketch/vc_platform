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
@TableName("user_auth_record")
public class UserAuthRecord {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long userId;
  private String authType;
  private String status;
  private String applyData;
  private String auditRemark;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
