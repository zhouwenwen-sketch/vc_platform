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
@TableName("`user`")
public class User {

  @TableId(type = IdType.AUTO)
  private Long id;

  private String phone;
  private String nickname;
  private String avatar;
  private String role;
  private String authStatus;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
