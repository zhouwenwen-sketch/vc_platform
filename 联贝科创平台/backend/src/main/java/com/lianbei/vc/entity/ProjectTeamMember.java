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
@TableName("project_team_member")
public class ProjectTeamMember {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long projectId;
  private String memberName;
  private String title;
  private String avatarUrl;
  private String bio;
  private Integer sortOrder;
}
