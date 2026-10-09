package com.lianbei.vc.admin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianbei.vc.admin.util.AdminContext;
import com.lianbei.vc.admin.util.AdminSession;
import com.lianbei.vc.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

  private final AdminTokenStore adminTokenStore;
  private final ObjectMapper objectMapper;

  public AdminAuthInterceptor(AdminTokenStore adminTokenStore, ObjectMapper objectMapper) {
    this.adminTokenStore = adminTokenStore;
    this.objectMapper = objectMapper;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    String token = resolveToken(request);
    AdminSession session = adminTokenStore.getSession(token);
    if (session == null) {
      writeUnauthorized(response);
      return false;
    }
    AdminContext.set(session);
    return true;
  }

  @Override
  public void afterCompletion(
      HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    AdminContext.clear();
  }

  private String resolveToken(HttpServletRequest request) {
    String token = request.getHeader("Authorization");
    if (token != null && token.startsWith("Bearer ")) {
      return token.substring(7);
    }
    token = request.getHeader("X-Admin-Token");
    if (token != null && !token.isBlank()) {
      return token;
    }
    return null;
  }

  private void writeUnauthorized(HttpServletResponse response) throws Exception {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    objectMapper.writeValue(response.getWriter(), Result.fail(401, "未登录或登录已过期"));
  }
}
