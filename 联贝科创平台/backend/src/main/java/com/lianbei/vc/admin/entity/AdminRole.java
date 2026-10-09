package com.lianbei.vc.admin.entity;

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
@TableName("admin_role")
public class AdminRole {

  @TableId(type = IdType.AUTO)
  private Long id;

  private String code;
  private String name;
  private String description;
  /** 1启用 0禁用 */
  private Integer status;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
