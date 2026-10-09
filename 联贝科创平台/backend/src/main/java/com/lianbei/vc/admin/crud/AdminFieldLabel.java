package com.lianbei.vc.admin.crud;

import java.util.HashMap;
import java.util.Map;

/** 后台 CRUD 字段中文标签 */
public final class AdminFieldLabel {

  private static final Map<String, String> GLOBAL = new HashMap<>();
  private static final Map<String, Map<String, String>> RESOURCE_OVERRIDES = new HashMap<>();

  static {
    putGlobal("id", "ID");
    putGlobal("createTime", "创建时间");
    putGlobal("updateTime", "更新时间");

    putGlobal("title", "标题");
    putGlobal("summary", "摘要");
    putGlobal("content", "内容");
    putGlobal("source", "来源");
    putGlobal("tag", "标签");
    putGlobal("tags", "标签");
    putGlobal("region", "地区");
    putGlobal("name", "名称");
    putGlobal("nickname", "昵称");
    putGlobal("avatar", "头像");
    putGlobal("phone", "手机号");
    putGlobal("status", "状态");
    putGlobal("description", "描述");
    putGlobal("location", "地点");

    putGlobal("newsType", "资讯类型");
    putGlobal("projectId", "项目ID");
    putGlobal("projectName", "项目名称");
    putGlobal("userId", "用户ID");
    putGlobal("activityId", "活动ID");
    putGlobal("newsId", "资讯ID");
    putGlobal("roleId", "角色ID");
    putGlobal("tagId", "标签ID");
    putGlobal("financingId", "融资记录ID");
    putGlobal("institutionId", "机构ID");
    putGlobal("organizerId", "主办方ID");
    putGlobal("auditorId", "审核人ID");
    putGlobal("targetId", "目标ID");

    putGlobal("sourceUrl", "原文链接");
    putGlobal("coverUrl", "封面图");
    putGlobal("imageUrl", "轮播图片");
    putGlobal("subtitle", "副标题");
    putGlobal("linkUrl", "跳转链接");
    putGlobal("logoUrl", "品牌图标");
    putGlobal("avatarUrl", "头像");
    putGlobal("sourceAvatar", "来源头像");
    putGlobal("contentFormat", "内容格式");
    putGlobal("attribution", "版权声明");

    putGlobal("slug", "标识");
    putGlobal("round", "融资轮次");
    putGlobal("category", "行业分类");
    putGlobal("companyDesc", "公司描述");
    putGlobal("intro", "项目简介");
    putGlobal("investmentAmount", "融资金额");
    putGlobal("statusLabel", "状态标签");
    putGlobal("foundingYear", "成立年份");
    putGlobal("isFinancing", "正在融资");
    putGlobal("isHot", "热门");
    putGlobal("isCertified", "已认证");
    putGlobal("latestRound", "最新轮次");
    putGlobal("latestAmount", "最新金额");
    putGlobal("latestFinancingDate", "最新融资日期");

    putGlobal("fullName", "公司全称");
    putGlobal("englishName", "英文名称");
    putGlobal("legalPerson", "法人代表");
    putGlobal("registeredAddress", "注册地址");
    putGlobal("establishDate", "成立日期");
    putGlobal("unifiedSocialCreditCode", "统一社会信用代码");
    putGlobal("dataSource", "数据来源");
    putGlobal("verifiedAt", "核验时间");

    putGlobal("financingDate", "融资日期");
    putGlobal("amount", "金额");
    putGlobal("amountCurrency", "币种");
    putGlobal("isLatest", "是否最新");
    putGlobal("sortOrder", "排序");
    putGlobal("investorName", "投资方名称");

    putGlobal("shareholderName", "股东名称");
    putGlobal("ratio", "持股比例");
    putGlobal("capital", "认缴出资");
    putGlobal("capitalDate", "出资日期");

    putGlobal("memberName", "成员姓名");
    putGlobal("bio", "个人简介");

    putGlobal("entityName", "主体名称");
    putGlobal("instType", "机构类型");
    putGlobal("investmentField", "投资领域");
    putGlobal("recentInvestment", "近期投资");
    putGlobal("eventCount", "投资事件数");
    putGlobal("foundedYear", "成立年份");

    putGlobal("startTime", "开始时间");
    putGlobal("endTime", "结束时间");
    putGlobal("participantCount", "参与人数");
    putGlobal("activityType", "活动类型");
    putGlobal("bannerUrls", "轮播图");
    putGlobal("detailImages", "详情图片");
    putGlobal("price", "价格");
    putGlobal("priceText", "价格说明");
    putGlobal("organizerName", "主办方");
    putGlobal("likeCount", "点赞数");
    putGlobal("isRecommended", "推荐活动");

    putGlobal("orgName", "公司/机构");
    putGlobal("position", "职位");

    putGlobal("role", "角色");
    putGlobal("authStatus", "认证状态");
    putGlobal("authType", "认证类型");
    putGlobal("applyData", "申请数据");
    putGlobal("auditRemark", "审核备注");

    putGlobal("targetType", "目标类型");
    putGlobal("targetName", "目标名称");
    putGlobal("connectionTime", "对接时间");

    putGlobal("reportType", "报告类型");
    putGlobal("industry", "行业");
    putGlobal("publishDate", "发布日期");
    putGlobal("viewCount", "阅读量");
    putGlobal("publishBy", "发布方");

    putGlobal("categoryType", "分类类型");

    putGlobal("joinStatus", "参与状态");
    putGlobal("jobType", "职位类型");
    putGlobal("isCertifier", "认证人");

    putGlobal("code", "验证码");
    putGlobal("scene", "场景");
    putGlobal("failCount", "失败次数");
    putGlobal("expireTime", "过期时间");
    putGlobal("usedTime", "使用时间");
    putGlobal("sendIp", "发送IP");

    putGlobal("contactName", "联系人");
    putGlobal("contactPhone", "联系电话");

    putGlobal("projectNo", "项目编号");
    putGlobal("brandName", "品牌名称");
    putGlobal("dealAmountText", "交易金额");
    putGlobal("mainBusiness", "主营业务");
    putGlobal("controllingStake", "实控股权");
    putGlobal("marketValue", "目前市值");
    putGlobal("revenueData", "营业收入");
    putGlobal("netProfitData", "年净利润");
    putGlobal("debtRatio", "负债率");
    putGlobal("totalAssets", "总资产");
    putGlobal("netAssets", "净资产");
    putGlobal("bookFunds", "账面资金");
    putGlobal("cooperationIntent", "合作意向");
    putGlobal("appointmentCount", "预约数");
    putGlobal("favoriteCount", "收藏数");
    putGlobal("shareCount", "转发数");
    putGlobal("publishTime", "发布时间");

    putResource("ma_deal", "category", "项目分类");
    putResource("ma_deal", "viewCount", "浏览量");

    putGlobal("reportType", "报道类型");
    putGlobal("applyData", "申请数据");
    putGlobal("newsId", "关联资讯ID");

    putResource("project", "name", "项目名称");
    putResource("institution", "name", "机构名称");
    putResource("institution_fund_manager", "fullName", "基金管理人全称");
    putResource("institution_fund_manager", "legalPerson", "法人代表");
    putResource("institution_fund_manager", "instType", "机构类型");
    putResource("institution_fund_manager", "officeAddress", "办公地址");
    putResource("institution_fund_manager", "registeredCapital", "注册资本");
    putResource("institution_fund_manager", "paidInCapital", "实缴资本");
    putResource("institution_fund_manager", "paidInRatio", "实缴比例");
    putResource("institution_fund_manager", "registrationNo", "登记编号");
    putResource("institution_fund_manager", "establishDate", "成立时间");
    putResource("institution_fund_manager", "registerDate", "登记时间");
    putResource("institution_team_member", "memberName", "成员姓名");
    putResource("activity_registration", "name", "姓名");
    putResource("research_category", "name", "分类名称");
    putResource("project_team_member", "title", "职位");
    putResource("news", "tag", "赛道标签");
    putResource("sms_code_record", "code", "验证码");

    putResource("project_collection", "category", "所属分类");
    putResource("project_collection", "badge", "角标");
    putResource("project_collection", "cover", "封面图");
    putResource("project_collection", "coverTitle", "封面叠字");
    putResource("project_collection", "collectionDate", "展示日期");
    putResource("project_collection", "projectsData", "项目数据(JSON)");
  }

  private AdminFieldLabel() {}

  public static String get(String resource, String fieldName) {
    Map<String, String> overrides = RESOURCE_OVERRIDES.get(resource);
    if (overrides != null && overrides.containsKey(fieldName)) {
      return overrides.get(fieldName);
    }
    return GLOBAL.getOrDefault(fieldName, fieldName);
  }

  private static void putGlobal(String field, String label) {
    GLOBAL.put(field, label);
  }

  private static void putResource(String resource, String field, String label) {
    RESOURCE_OVERRIDES.computeIfAbsent(resource, key -> new HashMap<>()).put(field, label);
  }
}
