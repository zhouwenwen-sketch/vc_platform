package com.lianbei.vc.admin.util;

import java.util.Collections;
import java.util.Set;

public final class AdminContext {

  private static final ThreadLocal<AdminSession> HOLDER = new ThreadLocal<>();

  private AdminContext() {}

  public static void set(AdminSession session) {
    HOLDER.set(session);
  }

  public static AdminSession get() {
    return HOLDER.get();
  }

  public static Long getAdminId() {
    AdminSession session = HOLDER.get();
    return session == null ? null : session.getAdminId();
  }

  public static String getRoleCode() {
    AdminSession session = HOLDER.get();
    return session == null ? null : session.getRoleCode();
  }

  public static Set<String> getPermissions() {
    AdminSession session = HOLDER.get();
    return session == null ? Collections.emptySet() : session.getPermissions();
  }

  public static boolean isSuperAdmin() {
    return "super_admin".equals(getRoleCode());
  }

  public static void clear() {
    HOLDER.remove();
  }
}
