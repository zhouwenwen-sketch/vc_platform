package com.lianbei.vc.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页响应数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

  private Long total;
  private List<T> list;

  public static <T> PageResult<T> of(IPage<T> page) {
    return PageResult.<T>builder().total(page.getTotal()).list(page.getRecords()).build();
  }

  public static <T> PageResult<T> of(long total, List<T> list) {
    return PageResult.<T>builder().total(total).list(list).build();
  }
}
