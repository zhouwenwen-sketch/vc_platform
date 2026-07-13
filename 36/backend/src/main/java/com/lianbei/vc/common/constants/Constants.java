package com.lianbei.vc.common.constants;

/**
 * 全局业务常量
 */
public final class Constants {

  private Constants() {}

  /** 统一成功响应码 */
  public static final int SUCCESS_CODE = 200;

  /** 默认分页 */
  public static final int DEFAULT_PAGE_NUM = 1;
  public static final int DEFAULT_PAGE_SIZE = 10;

  /** 首页推荐条数 */
  public static final int FEATURED_LIMIT = 6;

  /** 认证类型 */
  public static final String AUTH_TYPE_INVESTOR = "investor";
  public static final String AUTH_TYPE_ENTREPRENEUR = "entrepreneur";

  /** 活动类型 */
  public static final String ACTIVITY_TYPE_EVENT = "event";
  public static final String ACTIVITY_TYPE_ROADSHOW = "roadshow";

  /** 活动状态 */
  public static final String STATUS_REGISTERING = "registering";
  public static final String STATUS_ONGOING = "ongoing";
  public static final String STATUS_ENDED = "ended";

  /** 用户参与活动状态 */
  public static final String ACTIVITY_JOIN_RESERVED = "reserved";
  public static final String ACTIVITY_JOIN_SIGNED_UP = "signed_up";
  public static final String ACTIVITY_JOIN_ATTENDED = "attended";

  /** 研究院固定发布方 */
  public static final String RESEARCH_PUBLISHER_NAME = "联贝科创研究院";
  public static final String RESEARCH_PUBLISHER_AVATAR =
      "https://img.lianbeicdn.com/lianbei/ad/202010/20201028105942_386.png";
}
