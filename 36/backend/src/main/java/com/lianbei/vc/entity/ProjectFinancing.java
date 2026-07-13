package com.lianbei.vc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("project_financing")
public class ProjectFinancing {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long projectId;
  private LocalDate financingDate;
  private String round;
  private String amount;
  private String amountCurrency;
  private Integer isLatest;
  private Integer sortOrder;
  private String source;
  private LocalDateTime createTime;
}
