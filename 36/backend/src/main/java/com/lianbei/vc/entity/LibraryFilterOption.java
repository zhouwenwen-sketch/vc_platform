package com.lianbei.vc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 项目库等列表页筛选标签 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("library_filter_option")
public class LibraryFilterOption {

  @TableId(type = IdType.AUTO)
  private Long id;

  /** 场景：project_library */
  private String scene;

  /** industry / round / advantage / foundedYear */
  private String filterKey;

  /** 展示文案 */
  private String label;

  /** 提交给列表接口的值，默认同 label */
  private String value;

  private Integer sortOrder;

  /** 1 启用 0 停用 */
  private Integer enabled;

  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
