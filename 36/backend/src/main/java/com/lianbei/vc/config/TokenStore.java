package com.lianbei.vc.config;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * 模拟 Token 存储（暂不接入 JWT）
 */
@Component
public class TokenStore {

  private final Map<String, Long> tokenUserMap = new ConcurrentHashMap<>();

  public String createToken(Long userId) {
    String token = "mock-" + UUID.randomUUID();
    tokenUserMap.put(token, userId);
    return token;
  }

  public Long getUserId(String token) {
    if (token == null || token.isBlank()) {
      return null;
    }
    return tokenUserMap.get(token);
  }

  public void remove(String token) {
    tokenUserMap.remove(token);
  }
}
