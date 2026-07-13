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
@TableName("institution_team_member")
public class InstitutionTeamMember {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long institutionId;
  private String memberName;
  private String title;
  private String avatar;
  private String bio;
  private Integer sortOrder;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
