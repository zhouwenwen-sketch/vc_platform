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
@TableName("project_shareholder")
public class ProjectShareholder {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long projectId;
  private String shareholderName;
  private String ratio;
  private String capital;
  private String capitalDate;
  private Integer sortOrder;
}
