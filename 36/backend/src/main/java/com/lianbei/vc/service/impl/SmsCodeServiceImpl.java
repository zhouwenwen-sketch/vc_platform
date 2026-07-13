package com.lianbei.vc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lianbei.vc.config.SmsProperties;
import com.lianbei.vc.entity.SmsCodeRecord;
import com.lianbei.vc.exception.BusinessException;
import com.lianbei.vc.integration.CrmSmsClient;
import com.lianbei.vc.mapper.SmsCodeRecordMapper;
import com.lianbei.vc.service.SmsCodeService;
import com.lianbei.vc.utils.PhoneUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SmsCodeServiceImpl implements SmsCodeService {

  private static final String STATUS_SENT = "sent";
  private static final String CRM_CODE_PLACEHOLDER = "CRM";

  private final SmsCodeRecordMapper smsCodeRecordMapper;
  private final SmsProperties smsProperties;
  private final CrmSmsClient crmSmsClient;

  public SmsCodeServiceImpl(
      SmsCodeRecordMapper smsCodeRecordMapper,
      SmsProperties smsProperties,
      CrmSmsClient crmSmsClient) {
    this.smsCodeRecordMapper = smsCodeRecordMapper;
    this.smsProperties = smsProperties;
    this.crmSmsClient = crmSmsClient;
  }

  @Override
  public void sendCode(String mobile, String clientIp) {
    validateMobile(mobile);
    if (!smsProperties.isEnabled()) {
      return;
    }

    assertSendInterval(mobile);
    assertDailyLimit(mobile);
    crmSmsClient.sendLoginCode(mobile);
    recordSend(mobile, clientIp);
  }

  @Override
  public void verifyCode(String mobile, String captcha) {
    validateMobile(mobile);
    if (!StringUtils.hasText(captcha)) {
      throw new BusinessException("验证码不能为空");
    }

    if (!smsProperties.isEnabled()) {
      if (smsProperties.getMockCode().equals(captcha)) {
        return;
      }
      throw new BusinessException("验证码错误，测试请使用 " + smsProperties.getMockCode());
    }

    crmSmsClient.verifyLoginCode(mobile, captcha);
  }

  @Override
  public boolean isMockMode() {
    return !smsProperties.isEnabled();
  }

  @Override
  public String getSendSuccessMessage() {
    if (isMockMode()) {
      return "验证码已发送（测试环境请使用固定验证码 " + smsProperties.getMockCode() + "）";
    }
    return "验证码已发送，请注意查收短信";
  }

  private void recordSend(String mobile, String clientIp) {
    LocalDateTime now = LocalDateTime.now();
    SmsCodeRecord record =
        SmsCodeRecord.builder()
            .phone(mobile)
            .code(CRM_CODE_PLACEHOLDER)
            .scene(smsProperties.getScene())
            .status(STATUS_SENT)
            .failCount(0)
            .expireTime(now.plusMinutes(5))
            .sendIp(clientIp)
            .createTime(now)
            .build();
    smsCodeRecordMapper.insert(record);
  }

  private void validateMobile(String mobile) {
    if (!StringUtils.hasText(mobile)) {
      throw new BusinessException("手机号不能为空");
    }
    if (!PhoneUtils.isValid(mobile)) {
      throw new BusinessException("请输入正确手机号");
    }
  }

  private void assertSendInterval(String mobile) {
    SmsCodeRecord last =
        smsCodeRecordMapper.selectOne(
            new LambdaQueryWrapper<SmsCodeRecord>()
                .eq(SmsCodeRecord::getPhone, mobile)
                .orderByDesc(SmsCodeRecord::getId)
                .last("LIMIT 1"));
    if (last == null) {
      return;
    }
    LocalDateTime nextAllowed =
        last.getCreateTime().plusSeconds(smsProperties.getSendIntervalSeconds());
    if (nextAllowed.isAfter(LocalDateTime.now())) {
      throw new BusinessException("发送过于频繁，请稍后再试");
    }
  }

  private void assertDailyLimit(String mobile) {
    LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
    Long count =
        smsCodeRecordMapper.selectCount(
            new LambdaQueryWrapper<SmsCodeRecord>()
                .eq(SmsCodeRecord::getPhone, mobile)
                .ge(SmsCodeRecord::getCreateTime, startOfDay));
    if (count != null && count >= smsProperties.getDailyLimit()) {
      throw new BusinessException("今日发送次数已达上限，请明天再试");
    }
  }
}
