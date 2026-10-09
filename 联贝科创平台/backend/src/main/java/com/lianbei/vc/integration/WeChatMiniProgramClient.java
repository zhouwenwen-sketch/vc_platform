package com.lianbei.vc.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianbei.vc.config.WxMiniProgramProperties;
import com.lianbei.vc.exception.BusinessException;
import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/** 调用微信小程序服务端接口：access_token、手机号解密 */
@Component
public class WeChatMiniProgramClient {

  private static final Logger log = LoggerFactory.getLogger(WeChatMiniProgramClient.class);

  private final RestTemplate restTemplate;
  private final WxMiniProgramProperties wxProperties;
  private final ObjectMapper objectMapper;

  private String cachedAccessToken;
  private Instant accessTokenExpireAt = Instant.EPOCH;

  public WeChatMiniProgramClient(
      RestTemplate restTemplate,
      WxMiniProgramProperties wxProperties,
      ObjectMapper objectMapper) {
    this.restTemplate = restTemplate;
    this.wxProperties = wxProperties;
    this.objectMapper = objectMapper;
  }

  /** 通过 getPhoneNumber 返回的 code 换取手机号 */
  public String getPhoneNumber(String phoneCode) {
    if (!StringUtils.hasText(phoneCode)) {
      throw new BusinessException("手机号授权码无效");
    }
    if (canUseMock()) {
      log.warn("wx mini program mock login enabled, phone={}", wxProperties.getMockPhone());
      return wxProperties.getMockPhone();
    }

    String accessToken = getAccessToken();
    String url =
        "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token="
            + accessToken;
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<Map<String, String>> entity =
          new HttpEntity<>(Map.of("code", phoneCode.trim()), headers);
      String body = restTemplate.postForObject(url, entity, String.class);
      WxPhoneResponse response = objectMapper.readValue(body, WxPhoneResponse.class);
      if (response.errcode != null && response.errcode != 0) {
        log.warn("wx getuserphonenumber failed: errcode={}, errmsg={}", response.errcode, response.errmsg);
        throw new BusinessException("手机号授权失败，请使用验证码登录");
      }
      if (response.phoneInfo == null || !StringUtils.hasText(response.phoneInfo.purePhoneNumber)) {
        throw new BusinessException("未能获取手机号，请使用验证码登录");
      }
      return response.phoneInfo.purePhoneNumber.trim();
    } catch (BusinessException e) {
      throw e;
    } catch (RestClientException e) {
      log.error("wx getuserphonenumber request failed", e);
      throw new BusinessException("微信服务暂不可用，请使用验证码登录");
    } catch (Exception e) {
      log.error("wx getuserphonenumber parse failed", e);
      throw new BusinessException("手机号解析失败，请使用验证码登录");
    }
  }

  private boolean canUseMock() {
    return wxProperties.isMockEnabled()
        && (!StringUtils.hasText(wxProperties.getAppSecret())
            || !StringUtils.hasText(wxProperties.getAppId()));
  }

  private synchronized String getAccessToken() {
    if (cachedAccessToken != null && Instant.now().isBefore(accessTokenExpireAt)) {
      return cachedAccessToken;
    }
    if (!StringUtils.hasText(wxProperties.getAppId())
        || !StringUtils.hasText(wxProperties.getAppSecret())) {
      throw new BusinessException("微信小程序未配置，请使用验证码登录");
    }

    String url =
        "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
            + wxProperties.getAppId()
            + "&secret="
            + wxProperties.getAppSecret();
    try {
      String body = restTemplate.getForObject(url, String.class);
      AccessTokenResponse response = objectMapper.readValue(body, AccessTokenResponse.class);
      if (!StringUtils.hasText(response.access_token)) {
        log.warn("wx access_token failed: errcode={}, errmsg={}", response.errcode, response.errmsg);
        throw new BusinessException("微信登录服务暂不可用，请使用验证码登录");
      }
      cachedAccessToken = response.access_token;
      int expiresIn = response.expires_in == null ? 7200 : response.expires_in;
      accessTokenExpireAt = Instant.now().plusSeconds(Math.max(expiresIn - 300, 60));
      return cachedAccessToken;
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("wx access_token request failed", e);
      throw new BusinessException("微信登录服务暂不可用，请使用验证码登录");
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private static class AccessTokenResponse {
    public String access_token;
    public Integer expires_in;
    public Integer errcode;
    public String errmsg;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private static class WxPhoneResponse {
    public Integer errcode;
    public String errmsg;
    public PhoneInfo phoneInfo;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private static class PhoneInfo {
    public String purePhoneNumber;
    public String phoneNumber;
    public String countryCode;
  }
}
