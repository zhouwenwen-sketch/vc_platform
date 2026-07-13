package com.lianbei.vc.service;

public interface SmsCodeService {

  void sendCode(String phone, String clientIp);

  void verifyCode(String phone, String code);

  boolean isMockMode();

  String getSendSuccessMessage();
}
