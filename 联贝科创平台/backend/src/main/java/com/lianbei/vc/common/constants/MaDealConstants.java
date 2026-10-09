package com.lianbei.vc.common.constants;

/** 融资并购分类 */
public final class MaDealConstants {

  public static final String CATEGORY_LISTED = "listed_company";
  public static final String CATEGORY_PROJECT_ASSET = "project_asset";
  public static final String CATEGORY_FUND_INVEST = "fund_invest";
  public static final String CATEGORY_ENTERPRISE_SERVICE = "enterprise_service";

  public static final String TARGET_TYPE = "ma_deal";
  public static final String CONNECTION_STATUS_PENDING = "pending";

  private MaDealConstants() {}

  public static String categoryLabel(String category) {
    if (category == null) {
      return "";
    }
    return switch (category) {
      case CATEGORY_LISTED -> "上市公司";
      case CATEGORY_PROJECT_ASSET -> "项目资产";
      case CATEGORY_FUND_INVEST -> "基金产投";
      case CATEGORY_ENTERPRISE_SERVICE -> "企服";
      default -> category;
    };
  }
}
