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
@TableName("project_onboard_record")
public class ProjectOnboardRecord {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long userId;
  private String projectName;
  /** pending / approved / rejected */
  private String status;
  /** 完整申请表 JSON */
  private String applyData;
  /** 审核通过后关联的项目 ID */
  private Long projectId;
  private String auditRemark;
  private Long auditorId;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
