package com.lianbei.vc.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianbei.vc.common.Result;
import com.lianbei.vc.dto.request.AuthApplyRequest;
import com.lianbei.vc.dto.request.LoginRequest;
import com.lianbei.vc.dto.request.SendCodeRequest;
import com.lianbei.vc.dto.request.WxPhoneLoginRequest;
import com.lianbei.vc.dto.response.LoginResponse;
import com.lianbei.vc.dto.response.UserInfoVO;
import com.lianbei.vc.utils.UserContext;
import com.lianbei.vc.entity.ConnectionRecord;
import com.lianbei.vc.service.SmsCodeService;
import com.lianbei.vc.service.UserService;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户登录、认证、对接记录
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

  private final UserService userService;
  private final SmsCodeService smsCodeService;
  private final ObjectMapper objectMapper;

  public UserController(
      UserService userService, SmsCodeService smsCodeService, ObjectMapper objectMapper) {
    this.userService = userService;
    this.smsCodeService = smsCodeService;
    this.objectMapper = objectMapper;
  }

  /** 我的页菜单与认证入口配置 */
  @GetMapping("/mine/init")
  public Result<Map<String, Object>> mineInit() throws IOException {
    ClassPathResource resource = new ClassPathResource("config/mine-init.json");
    Map<String, Object> data =
        objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
    return Result.ok(data);
  }

  @PostMapping("/sendCode")
  public Result<Map<String, String>> sendCode(
      @RequestBody SendCodeRequest request, HttpServletRequest servletRequest) {
    userService.sendCode(request.resolveMobile(), resolveClientIp(servletRequest));
    Map<String, String> data = new HashMap<>();
    data.put("message", smsCodeService.getSendSuccessMessage());
    return Result.ok(data);
  }

  /** 短信验证码模式（前端展示 mock 提示） */
  @GetMapping("/sms/config")
  public Result<Map<String, Object>> smsConfig() {
    Map<String, Object> data = new HashMap<>();
    data.put("mock", smsCodeService.isMockMode());
    if (smsCodeService.isMockMode()) {
      data.put("mockHint", smsCodeService.getSendSuccessMessage());
    }
    return Result.ok(data);
  }

  @PostMapping("/login")
  public Result<LoginResponse> login(@RequestBody LoginRequest request) {
    return Result.ok(userService.login(request.resolveMobile(), request.resolveCaptcha()));
  }

  /** 微信小程序手机号一键登录 */
  @PostMapping("/wxLogin")
  public Result<LoginResponse> wxLogin(@RequestBody WxPhoneLoginRequest request) {
    return Result.ok(userService.loginByWxPhone(request.resolvePhoneCode()));
  }

  @GetMapping("/info")
  public Result<UserInfoVO> info() {
    return Result.ok(userService.getCurrentUserInfo(UserContext.getUserId()));
  }

  /** 退出登录，注销 token */
  @PostMapping("/logout")
  public Result<Map<String, String>> logout(
      @RequestHeader(value = "Authorization", required = false) String authorization,
      @RequestHeader(value = "X-Token", required = false) String xToken) {
    userService.logout(extractToken(authorization, xToken));
    Map<String, String> data = new HashMap<>();
    data.put("message", "已退出登录");
    return Result.ok(data);
  }

  @PostMapping("/auth")
  public Result<Map<String, String>> auth(@RequestBody AuthApplyRequest request) {
    userService.submitAuth(UserContext.getUserId(), request);
    Map<String, String> data = new HashMap<>();
    data.put("message", "认证申请已提交，请等待审核");
    return Result.ok(data);
  }

  /** 对接记录查询 */
  @GetMapping("/connections")
  public Result<List<ConnectionRecord>> connections() {
    return Result.ok(userService.listConnections(UserContext.getUserId()));
  }

  private static String extractToken(String authorization, String xToken) {
    if (authorization != null && authorization.startsWith("Bearer ")) {
      return authorization.substring(7);
    }
    if (xToken != null && !xToken.isBlank()) {
      return xToken;
    }
    return null;
  }

  private static String resolveClientIp(HttpServletRequest request) {
    if (request == null) {
      return null;
    }
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
