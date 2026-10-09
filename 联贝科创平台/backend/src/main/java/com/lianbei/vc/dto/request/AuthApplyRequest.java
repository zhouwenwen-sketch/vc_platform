package com.lianbei.vc.dto.request;

import lombok.Data;

/** 认证申请请求 */
@Data
public class AuthApplyRequest {
  /** investor / entrepreneur */
  private String type;
  private String name;
  /** 机构简称 */
  private String company;
  /** 公司工商主体 */
  private String companyEntity;
  private String position;
  private String idCard;
  private String phone;
  private String email;
  private Boolean emailSubscribe;
  /** 关注领域，逗号分隔 */
  private String focusAreas;
  /** 关注轮次，逗号分隔 */
  private String focusRounds;
  private String remark;
}
