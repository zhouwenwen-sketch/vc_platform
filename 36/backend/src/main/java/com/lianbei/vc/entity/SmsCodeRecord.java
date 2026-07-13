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
@TableName("sms_code_record")
public class SmsCodeRecord {

  @TableId(type = IdType.AUTO)
  private Long id;

  private String phone;
  private String code;
  private String scene;
  private String status;
  private Integer failCount;
  private LocalDateTime expireTime;
  private LocalDateTime usedTime;
  private String sendIp;
  private LocalDateTime createTime;
}
