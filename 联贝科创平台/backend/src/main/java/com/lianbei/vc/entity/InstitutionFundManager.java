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
@TableName("institution_fund_manager")
public class InstitutionFundManager {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long institutionId;
  /** 基金管理人全称 */
  private String fullName;
  private String legalPerson;
  private String instType;
  private String officeAddress;
  private String registeredCapital;
  private String paidInCapital;
  private String paidInRatio;
  private String registrationNo;
  private String establishDate;
  private String registerDate;
  private Integer sortOrder;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
