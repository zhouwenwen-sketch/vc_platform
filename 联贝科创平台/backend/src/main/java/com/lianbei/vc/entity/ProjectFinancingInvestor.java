package com.lianbei.vc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("project_financing_investor")
public class ProjectFinancingInvestor {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long financingId;
  private Long institutionId;
  private String investorName;
  /** lead=领投 follow=跟投 */
  private String investorRole;
}
