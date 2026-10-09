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
@TableName("project")
public class Project {

  @TableId(type = IdType.AUTO)
  private Long id;

  private String name;
  private String slug;
  private String round;
  private String region;
  private String category;
  private String tags;
  private String companyDesc;
  private String intro;
  private String investmentAmount;
  private String statusLabel;
  private String location;
  private String foundingYear;
  private String logoUrl;
  /** 公司官网 */
  private String website;
  private Integer isFinancing;
  private Integer isHot;
  private Integer isCertified;
  private Integer status;
  private String latestRound;
  private String latestAmount;
  private java.time.LocalDate latestFinancingDate;
  private String nationalEconomyIndustry;
  private String strategicEmergingIndustry;
  private String highPrecisionIndustry;
  private String listingBoard;
  private java.time.LocalDate listingDate;
  private String employeeCount;
  private String managerCount;
  private String importSource;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
