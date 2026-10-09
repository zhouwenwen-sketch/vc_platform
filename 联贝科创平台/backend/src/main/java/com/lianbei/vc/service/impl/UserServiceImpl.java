package com.lianbei.vc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianbei.vc.config.TokenStore;
import com.lianbei.vc.dto.request.AuthApplyRequest;
import com.lianbei.vc.dto.response.LoginResponse;
import com.lianbei.vc.dto.response.UserInfoVO;
import com.lianbei.vc.exception.BusinessException;
import com.lianbei.vc.entity.ConnectionRecord;
import com.lianbei.vc.entity.User;
import com.lianbei.vc.entity.UserAuthRecord;
import com.lianbei.vc.mapper.ConnectionRecordMapper;
import com.lianbei.vc.mapper.UserAuthRecordMapper;
import com.lianbei.vc.mapper.UserMapper;
import com.lianbei.vc.integration.WeChatMiniProgramClient;
import com.lianbei.vc.service.SmsCodeService;
import com.lianbei.vc.service.UserService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

  private final TokenStore tokenStore;
  private final UserAuthRecordMapper userAuthRecordMapper;
  private final ConnectionRecordMapper connectionRecordMapper;
  private final ObjectMapper objectMapper;
  private final SmsCodeService smsCodeService;
  private final WeChatMiniProgramClient weChatMiniProgramClient;

  public UserServiceImpl(
      TokenStore tokenStore,
      UserAuthRecordMapper userAuthRecordMapper,
      ConnectionRecordMapper connectionRecordMapper,
      ObjectMapper objectMapper,
      SmsCodeService smsCodeService,
      WeChatMiniProgramClient weChatMiniProgramClient) {
    this.tokenStore = tokenStore;
    this.userAuthRecordMapper = userAuthRecordMapper;
    this.connectionRecordMapper = connectionRecordMapper;
    this.objectMapper = objectMapper;
    this.smsCodeService = smsCodeService;
    this.weChatMiniProgramClient = weChatMiniProgramClient;
  }

  @Override
  public void sendCode(String phone, String clientIp) {
    smsCodeService.sendCode(phone, clientIp);
  }

  @Override
  public LoginResponse login(String phone, String code) {
    if (!StringUtils.hasText(phone) || !StringUtils.hasText(code)) {
      throw new BusinessException("手机号和验证码不能为空");
    }
    smsCodeService.verifyCode(phone, code);
    User user = findOrCreateUserByPhone(phone);
    String token = tokenStore.createToken(user.getId());
    return LoginResponse.builder().token(token).userInfo(toUserInfo(user)).build();
  }

  @Override
  public LoginResponse loginByWxPhone(String phoneCode) {
    String phone = weChatMiniProgramClient.getPhoneNumber(phoneCode);
    User user = findOrCreateUserByPhone(phone);
    String token = tokenStore.createToken(user.getId());
    return LoginResponse.builder().token(token).userInfo(toUserInfo(user)).build();
  }

  private User findOrCreateUserByPhone(String phone) {
    User user =
        getOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone), false);
    if (user == null) {
      user =
          User.builder()
              .phone(phone)
              .nickname("用户" + phone.substring(phone.length() - 4))
              .authStatus("none")
              .build();
      save(user);
    }
    return user;
  }

  @Override
  public UserInfoVO getCurrentUserInfo(Long userId) {
    if (userId == null) {
      throw new BusinessException(401, "未登录，请先登录");
    }
    User user = getById(userId);
    if (user == null) {
      throw new BusinessException("用户不存在");
    }
    return toUserInfo(user);
  }

  @Override
  public void submitAuth(Long userId, AuthApplyRequest request) {
    if (userId == null) {
      throw new BusinessException(401, "未登录，请先登录");
    }
    if (request == null || !StringUtils.hasText(request.getType())) {
      throw new BusinessException("认证类型不能为空");
    }
    User user = getById(userId);
    if (user == null) {
      throw new BusinessException("用户不存在");
    }
    if ("pending".equals(user.getAuthStatus())) {
      throw new BusinessException("认证审核中，请勿重复提交");
    }
    if ("approved".equals(user.getAuthStatus())) {
      throw new BusinessException("您已完成认证");
    }
    try {
      Map<String, Object> data = new HashMap<>();
      data.put("name", request.getName());
      data.put("company", request.getCompany());
      data.put("companyEntity", request.getCompanyEntity());
      data.put("position", request.getPosition());
      data.put("idCard", request.getIdCard());
      data.put("phone", request.getPhone());
      data.put("email", request.getEmail());
      data.put("emailSubscribe", request.getEmailSubscribe());
      data.put("focusAreas", request.getFocusAreas());
      data.put("focusRounds", request.getFocusRounds());
      data.put("remark", request.getRemark());
      String applyJson = objectMapper.writeValueAsString(data);

      UserAuthRecord record =
          UserAuthRecord.builder()
              .userId(userId)
              .authType(request.getType())
              .status("pending")
              .applyData(applyJson)
              .build();
      userAuthRecordMapper.insert(record);

      user.setAuthStatus("pending");
      user.setRole(request.getType());
      updateById(user);
    } catch (Exception e) {
      throw new BusinessException("认证申请提交失败");
    }
  }

  @Override
  public List<ConnectionRecord> listConnections(Long userId) {
    if (userId == null) {
      throw new BusinessException(401, "未登录，请先登录");
    }
    return connectionRecordMapper.selectList(
        new LambdaQueryWrapper<ConnectionRecord>()
            .eq(ConnectionRecord::getUserId, userId)
            .orderByDesc(ConnectionRecord::getConnectionTime));
  }

  @Override
  public void logout(String token) {
    if (StringUtils.hasText(token)) {
      tokenStore.remove(token);
    }
  }

  private UserInfoVO toUserInfo(User user) {
    return UserInfoVO.builder()
        .id(user.getId())
        .phone(user.getPhone())
        .nickname(user.getNickname())
        .avatar(user.getAvatar())
        .role(user.getRole())
        .authStatus(user.getAuthStatus())
        .build();
  }
}
