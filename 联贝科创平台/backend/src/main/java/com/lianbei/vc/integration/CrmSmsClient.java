package com.lianbei.vc.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianbei.vc.config.SmsProperties;
import com.lianbei.vc.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/** 调用 CRM 短信接口：发送与校验均由 CRM 负责 */
@Component
public class CrmSmsClient {

  private static final Logger log = LoggerFactory.getLogger(CrmSmsClient.class);

  private final RestTemplate restTemplate;
  private final SmsProperties smsProperties;
  private final ObjectMapper objectMapper;

  public CrmSmsClient(
      RestTemplate restTemplate, SmsProperties smsProperties, ObjectMapper objectMapper) {
    this.restTemplate = restTemplate;
    this.smsProperties = smsProperties;
    this.objectMapper = objectMapper;
  }

  /** POST /api/sms/send  参数：mobile、event=mobilelogin */
  public void sendLoginCode(String mobile) {
    SmsProperties.Crm crm = smsProperties.getCrm();
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("mobile", mobile);
    body.add("event", crm.getEvent());
    postForm(crm.getSendPath(), body, crm.getSuccessCode(), "短信发送失败，请稍后重试", mobile);
  }

  /** POST /api/sms/check  参数：mobile、captcha、event=mobilelogin */
  public void verifyLoginCode(String mobile, String captcha) {
    SmsProperties.Crm crm = smsProperties.getCrm();
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("mobile", mobile);
    body.add("captcha", captcha);
    body.add("event", crm.getEvent());
    postForm(crm.getCheckPath(), body, crm.getSuccessCode(), "验证码错误", mobile);
  }

  private void postForm(
      String path,
      MultiValueMap<String, String> body,
      int successCode,
      String defaultError,
      String mobile) {
    SmsProperties.Crm crm = smsProperties.getCrm();
    String url = trimTrailingSlash(crm.getBaseUrl()) + path;

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

    try {
      ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
      parseAndValidate(response.getBody(), successCode, defaultError);
    } catch (BusinessException ex) {
      throw ex;
    } catch (RestClientException ex) {
      log.error("[sms] CRM request failed, mobile={}", maskMobile(mobile), ex);
      throw new BusinessException(defaultError);
    }
  }

  private void parseAndValidate(String body, int successCode, String defaultError) {
    if (body == null || body.isBlank()) {
      throw new BusinessException(defaultError);
    }
    try {
      CrmSmsResponse parsed = objectMapper.readValue(body, CrmSmsResponse.class);
      if (parsed.getCode() == null || parsed.getCode() != successCode) {
        String msg =
            parsed.getMsg() != null && !parsed.getMsg().isBlank() ? parsed.getMsg() : defaultError;
        throw new BusinessException(msg);
      }
    } catch (BusinessException ex) {
      throw ex;
    } catch (Exception ex) {
      log.error("[sms] CRM response parse failed: {}", body, ex);
      throw new BusinessException(defaultError);
    }
  }

  private static String trimTrailingSlash(String baseUrl) {
    if (baseUrl == null) {
      return "";
    }
    return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
  }

  private static String maskMobile(String mobile) {
    if (mobile == null || mobile.length() < 7) {
      return "***";
    }
    return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private static class CrmSmsResponse {
    private Integer code;
    private String msg;

    public Integer getCode() {
      return code;
    }

    public void setCode(Integer code) {
      this.code = code;
    }

    public String getMsg() {
      return msg;
    }

    public void setMsg(String msg) {
      this.msg = msg;
    }
  }
}
