package com.lianbei.vc.common;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;

/** 列表筛选项多选参数解析（逗号分隔） */
public final class FilterParamUtils {

  private FilterParamUtils() {}

  public static List<String> splitValues(String raw) {
    if (!StringUtils.hasText(raw)) {
      return List.of();
    }
    return Arrays.stream(raw.split(","))
        .map(String::trim)
        .filter(StringUtils::hasText)
        .collect(Collectors.toList());
  }

  public static boolean hasValues(String raw) {
    return !splitValues(raw).isEmpty();
  }

  public static List<Integer> splitYears(String raw) {
    return splitValues(raw).stream()
        .map(v -> v.replace("年", "").trim())
        .filter(v -> v.matches("\\d{4}"))
        .map(Integer::parseInt)
        .collect(Collectors.toList());
  }
}
