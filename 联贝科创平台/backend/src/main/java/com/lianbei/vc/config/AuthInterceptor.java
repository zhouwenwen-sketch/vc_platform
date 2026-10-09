package com.lianbei.vc.config;

import com.lianbei.vc.utils.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 从请求头解析模拟 token，写入 UserContext
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

  private final TokenStore tokenStore;

  public AuthInterceptor(TokenStore tokenStore) {
    this.tokenStore = tokenStore;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    String token = request.getHeader("Authorization");
    if (token != null && token.startsWith("Bearer ")) {
      token = token.substring(7);
    }
    if (token == null || token.isBlank()) {
      token = request.getHeader("X-Token");
    }
    Long userId = tokenStore.getUserId(token);
    if (userId != null) {
      UserContext.setUserId(userId);
    }
    return true;
  }

  @Override
  public void afterCompletion(
      HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    UserContext.clear();
  }
}
