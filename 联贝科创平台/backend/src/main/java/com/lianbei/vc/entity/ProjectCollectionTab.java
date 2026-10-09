package com.lianbei.vc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 项目集 Tab 分类 */
@Data
@TableName("project_collection_tab")
public class ProjectCollectionTab {

  @TableId(type = IdType.AUTO)
  private Long id;

  private String name;
  private Integer sortOrder;
}
