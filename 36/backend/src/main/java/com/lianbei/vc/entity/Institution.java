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
@TableName("institution")
public class Institution {

  @TableId(type = IdType.AUTO)
  private Long id;

  private String name;
  private String entityName;
  private String instType;
  private String investmentField;
  private String recentInvestment;
  private Integer eventCount;
  private String foundedYear;
  private String logoUrl;
  /** 机构介绍（导入/后台维护） */
  private String intro;
  /** 管理规模展示，如 4900亿人民币 */
  private String manageScale;
  /** 官网 */
  private String website;
  /** 投资领域完整列表，逗号分隔 */
  private String investmentFields;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
