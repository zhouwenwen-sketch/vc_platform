package com.lianbei.vc.admin.config;

import com.lianbei.vc.admin.util.AdminSession;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class AdminTokenStore {

  private final Map<String, AdminSession> tokenSessionMap = new ConcurrentHashMap<>();

  public String createToken(AdminSession session) {
    String token = "admin-" + UUID.randomUUID();
    tokenSessionMap.put(token, session);
    return token;
  }

  public AdminSession getSession(String token) {
    if (token == null || token.isBlank()) {
      return null;
    }
    return tokenSessionMap.get(token);
  }

  public void remove(String token) {
    tokenSessionMap.remove(token);
  }
}
