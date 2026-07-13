package com.lianbei.vc.admin.entity;

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
@TableName("admin_permission")
public class AdminPermission {

  @TableId(type = IdType.AUTO)
  private Long id;

  private String code;
  private String name;
  /** menu | button */
  private String type;
  private Long parentId;
  private String path;
  /** 关联 CRUD 资源 key */
  private String resource;
  private Integer sortOrder;
  private LocalDateTime createTime;
}
