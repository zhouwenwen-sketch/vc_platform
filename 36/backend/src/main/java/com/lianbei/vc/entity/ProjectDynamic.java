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
@TableName("project_dynamic")
public class ProjectDynamic {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long projectId;
  private LocalDate eventDate;
  private String content;
  private Integer sortOrder;
  private LocalDateTime createTime;
}
