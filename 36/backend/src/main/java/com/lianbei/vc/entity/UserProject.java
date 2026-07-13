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
@TableName("user_project")
public class UserProject {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long userId;
  private Long projectId;
  private String role;
  private String jobType;
  private Integer isCertifier;
  private LocalDateTime createTime;
}
