package com.lianbei.vc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 项目集（专题合集） */
@Data
@TableName("project_collection")
public class ProjectCollection {

  @TableId(type = IdType.AUTO)
  private Long id;

  /** 所属 Tab 分类，如「最受关注」「热门赛道」 */
  private String category;

  private String badge;
  private String title;
  private String cover;
  private String coverTitle;
  private LocalDate collectionDate;
  private String summary;
  private String description;
  /** 合集内项目 JSON 数组 */
  private String projectsData;
  private Integer sortOrder;
  /** 1=启用 0=禁用 */
  private Integer status;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
