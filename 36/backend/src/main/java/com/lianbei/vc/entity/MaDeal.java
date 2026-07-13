package com.lianbei.vc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 融资并购项目 */
@Data
@TableName("ma_deal")
public class MaDeal {

  @TableId(type = IdType.AUTO)
  private Long id;

  private String projectNo;
  private String brandName;
  private String title;
  private String summary;
  private String category;
  private String tags;
  private String dealAmountText;
  private String logoUrl;
  private String coverUrl;
  private String industry;
  private String projectName;
  private String mainBusiness;
  private String controllingStake;
  private String marketValue;
  private String revenueData;
  private String netProfitData;
  private String debtRatio;
  private String totalAssets;
  private String netAssets;
  private String bookFunds;
  private String cooperationIntent;
  private String contactPhone;
  private Integer viewCount;
  private Integer appointmentCount;
  private Integer favoriteCount;
  private Integer shareCount;
  private LocalDateTime publishTime;
  private Integer sortOrder;
  private Integer status;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
