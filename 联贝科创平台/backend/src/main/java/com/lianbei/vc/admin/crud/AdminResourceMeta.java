package com.lianbei.vc.admin.crud;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminResourceMeta {

  private final String key;
  private final String label;
  private final String tableName;
  private final Class<?> entityClass;
  private final BaseMapper<?> mapper;
  private final boolean readOnly;
}
