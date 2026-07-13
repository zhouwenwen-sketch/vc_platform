package com.lianbei.vc.admin.entity;

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
@TableName("admin_role_permission")
public class AdminRolePermission {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long roleId;
  private Long permissionId;
}
