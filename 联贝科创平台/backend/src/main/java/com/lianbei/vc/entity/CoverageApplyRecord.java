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
@TableName("coverage_apply_record")
public class CoverageApplyRecord {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long userId;
  private Long projectId;
  private String projectName;
  /** latest_financing | no_financing */
  private String reportType;
  /** submitted | rejected（历史数据可能含 pending/approved） */
  private String status;
  /** 完整申请表 JSON */
  private String applyData;
  /** 历史审核流程遗留字段，新申请不再写入 */
  private Long newsId;
  private String auditRemark;
  private Long auditorId;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
