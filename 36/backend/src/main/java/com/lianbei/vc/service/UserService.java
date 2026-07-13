package com.lianbei.vc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lianbei.vc.dto.request.AuthApplyRequest;
import com.lianbei.vc.dto.response.LoginResponse;
import com.lianbei.vc.dto.response.UserInfoVO;
import com.lianbei.vc.entity.ConnectionRecord;
import com.lianbei.vc.entity.User;
import java.util.List;

public interface UserService extends IService<User> {

  void sendCode(String phone, String clientIp);

  LoginResponse login(String phone, String code);

  LoginResponse loginByWxPhone(String phoneCode);

  UserInfoVO getCurrentUserInfo(Long userId);

  void submitAuth(Long userId, AuthApplyRequest request);

  List<ConnectionRecord> listConnections(Long userId);

  void logout(String token);
}
