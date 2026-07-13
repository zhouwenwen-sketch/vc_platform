package com.lianbei.vc.utils;

import java.util.regex.Pattern;
import org.springframework.util.StringUtils;

/** 手机号校验工具 */
public final class PhoneUtils {

  /** 中国大陆手机号：1 开头，第二位 3-9，共 11 位 */
  private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

  private PhoneUtils() {}

  public static boolean isValid(String phone) {
    return StringUtils.hasText(phone) && PHONE_PATTERN.matcher(phone.trim()).matches();
  }
}
