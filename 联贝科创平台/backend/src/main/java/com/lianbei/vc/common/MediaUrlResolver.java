package com.lianbei.vc.common;

import com.lianbei.vc.config.VcAppProperties;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** 将 /uploads/ 相对路径补全为对外可访问的绝对地址 */
@Component
public class MediaUrlResolver {

  private final VcAppProperties vcAppProperties;

  public MediaUrlResolver(VcAppProperties vcAppProperties) {
    this.vcAppProperties = vcAppProperties;
  }

  public String resolve(String url) {
    if (!StringUtils.hasText(url)) {
      return url;
    }
    String trimmed = url.trim();
    String base = normalizeBase(vcAppProperties.getPublicBaseUrl());
    if (!StringUtils.hasText(base)) {
      return trimmed;
    }
    if (trimmed.startsWith("/uploads/")) {
      return base + trimmed;
    }
    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
      return trimmed;
    }
    int idx = trimmed.indexOf("/uploads/");
    if (idx >= 0) {
      return base + trimmed.substring(idx);
    }
    return trimmed;
  }

  public void resolveDeep(Object root) {
    if (root == null) {
      return;
    }
    IdentityHashMap<Object, Boolean> visited = new IdentityHashMap<>();
    resolveDeep(root, visited);
  }

  private void resolveDeep(Object value, IdentityHashMap<Object, Boolean> visited) {
    if (value == null || visited.containsKey(value)) {
      return;
    }
    if (value instanceof String str) {
      return;
    }
    if (value instanceof Collection<?> collection) {
      visited.put(value, Boolean.TRUE);
      for (Object item : collection) {
        resolveDeep(item, visited);
      }
      return;
    }
    if (value instanceof Map<?, ?> map) {
      visited.put(value, Boolean.TRUE);
      for (Object item : map.values()) {
        resolveDeep(item, visited);
      }
      return;
    }
    if (value.getClass().isArray()) {
      visited.put(value, Boolean.TRUE);
      int len = Array.getLength(value);
      for (int i = 0; i < len; i++) {
        resolveDeep(Array.get(value, i), visited);
      }
      return;
    }
    if (isJdkType(value.getClass())) {
      return;
    }

    visited.put(value, Boolean.TRUE);
    Class<?> type = value.getClass();
    while (type != null && type != Object.class) {
      for (Field field : type.getDeclaredFields()) {
        if (Modifier.isStatic(field.getModifiers())) {
          continue;
        }
        field.setAccessible(true);
        try {
          Object fieldValue = field.get(value);
          if (fieldValue instanceof String str) {
            String resolved = resolve(str);
            if (!str.equals(resolved)) {
              field.set(value, resolved);
            }
          } else {
            resolveDeep(fieldValue, visited);
          }
        } catch (IllegalAccessException ignored) {
          // skip inaccessible fields
        }
      }
      type = type.getSuperclass();
    }
  }

  private static String normalizeBase(String baseUrl) {
    if (!StringUtils.hasText(baseUrl)) {
      return "";
    }
    String trimmed = baseUrl.trim();
    return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
  }

  private static boolean isJdkType(Class<?> type) {
    return type.isPrimitive()
        || type.getName().startsWith("java.")
        || type.getName().startsWith("javax.")
        || type.getName().startsWith("jakarta.");
  }
}
