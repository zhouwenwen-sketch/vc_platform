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
@TableName("admin_user")
public class AdminUser {

  @TableId(type = IdType.AUTO)
  private Long id;

  private String username;
  private String password;
  private String nickname;
  private Long roleId;
  /** 1启用 0禁用 */
  private Integer status;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
