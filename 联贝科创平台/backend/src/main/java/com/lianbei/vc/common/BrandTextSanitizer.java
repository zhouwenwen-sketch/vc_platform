package com.lianbei.vc.common;

import org.springframework.util.StringUtils;

/** 将历史品牌文案统一替换为联贝科创。 */
public final class BrandTextSanitizer {

  private static final String[][] REPLACEMENTS = {
    {"36氪硬氪", "联贝科创"},
    {"36氪创投派", "联贝科创派"},
    {"36氪", "联贝科创"},
    {"硬氪", "联贝科创"},
    {"联贝创投平台", "联贝科创平台"},
    {"联贝创投派", "联贝科创派"},
    {"联贝研究院", "联贝科创研究院"},
    {"联贝并购", "联贝科创并购"},
    {"联贝", "联贝科创"},
    {"大学生创投研究院", "联贝科创研究院"},
    {"大学生创投平台", "联贝科创平台"},
    {"大学生创投派", "联贝科创派"},
    {"大学生创投并购", "联贝科创并购"},
    {"大学生创投", "联贝科创"},
    {"36kr.com", "lianbei.com"},
    {"36Kr.com", "lianbei.com"},
    {"36KR.com", "lianbei.com"},
    {"img.36krcdn.com", "img.lianbeicdn.com"},
    {"36krcdn.com", "lianbeicdn.com"},
    {"36kr", "lianbei"},
    {"36Kr", "lianbei"},
    {"36KR", "lianbei"},
  };

  private BrandTextSanitizer() {}

  public static String sanitize(String text) {
    if (!StringUtils.hasText(text)) {
      return text;
    }
    String result = text;
    for (String[] pair : REPLACEMENTS) {
      result = result.replace(pair[0], pair[1]);
    }
    return result;
  }
}
