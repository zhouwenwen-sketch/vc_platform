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
@TableName("project_business")
public class ProjectBusiness {

  /** 与 project.id 保持一致 */
  @TableId(type = IdType.INPUT)
  private Long id;

  private Long projectId;
  private String fullName;
  private String englishName;
  private String legalPerson;
  private String registeredAddress;
  private LocalDate establishDate;
  private String unifiedSocialCreditCode;
  private String dataSource;
  private LocalDateTime verifiedAt;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
