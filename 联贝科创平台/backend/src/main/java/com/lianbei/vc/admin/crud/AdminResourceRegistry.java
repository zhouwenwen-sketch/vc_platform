package com.lianbei.vc.admin.crud;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lianbei.vc.entity.*;
import com.lianbei.vc.mapper.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class AdminResourceRegistry {

  private final Map<String, AdminResourceMeta> resources = new LinkedHashMap<>();

  public AdminResourceRegistry(
      NewsMapper newsMapper,
      ProjectMapper projectMapper,
      ProjectBusinessMapper projectBusinessMapper,
      ProjectFinancingMapper projectFinancingMapper,
      ProjectFinancingInvestorMapper projectFinancingInvestorMapper,
      ProjectShareholderMapper projectShareholderMapper,
      ProjectTeamMemberMapper projectTeamMemberMapper,
      ProjectTagRelMapper projectTagRelMapper,
      InstitutionMapper institutionMapper,
      ActivityMapper activityMapper,
      ActivityRegistrationMapper activityRegistrationMapper,
      UserMapper userMapper,
      UserAuthRecordMapper userAuthRecordMapper,
      ConnectionRecordMapper connectionRecordMapper,
      ResearchReportMapper researchReportMapper,
      ResearchCategoryMapper researchCategoryMapper,
      NewsCommentMapper newsCommentMapper,
      SmsCodeRecordMapper smsCodeRecordMapper,
      UserActivityRecordMapper userActivityRecordMapper,
      ProjectOnboardRecordMapper projectOnboardRecordMapper,
      UserProjectMapper userProjectMapper,
      ActivityPublishRecordMapper activityPublishRecordMapper,
      CoverageApplyRecordMapper coverageApplyRecordMapper,
      HomeBannerMapper homeBannerMapper,
      ProjectCollectionMapper projectCollectionMapper,
      MaDealMapper maDealMapper,
      InstitutionFundManagerMapper institutionFundManagerMapper,
      InstitutionTeamMemberMapper institutionTeamMemberMapper) {
    register("home_banner", "轮播图", HomeBanner.class, homeBannerMapper, false);
    register("project_collection", "项目集", ProjectCollection.class, projectCollectionMapper, false);
    register("news", "快讯资讯", News.class, newsMapper, false);
    register("project", "卡片信息", Project.class, projectMapper, false);
    register("project_business", "项目工商", ProjectBusiness.class, projectBusinessMapper, false);
    register("project_financing", "项目融资", ProjectFinancing.class, projectFinancingMapper, false);
    register(
        "project_financing_investor",
        "融资投资方",
        ProjectFinancingInvestor.class,
        projectFinancingInvestorMapper,
        false);
    register("project_shareholder", "股东信息", ProjectShareholder.class, projectShareholderMapper, false);
    register("project_team_member", "团队成员", ProjectTeamMember.class, projectTeamMemberMapper, false);
    register("project_tag_rel", "标签关联", ProjectTagRel.class, projectTagRelMapper, false);
    register("institution", "投资机构", Institution.class, institutionMapper, false);
    register(
        "institution_fund_manager",
        "基金管理人",
        InstitutionFundManager.class,
        institutionFundManagerMapper,
        false);
    register(
        "institution_team_member",
        "机构团队",
        InstitutionTeamMember.class,
        institutionTeamMemberMapper,
        false);
    register("activity", "活动", Activity.class, activityMapper, false);
    register("activity_registration", "活动报名", ActivityRegistration.class, activityRegistrationMapper, false);
    register("user", "C端用户", User.class, userMapper, false);
    register("user_auth_record", "用户认证", UserAuthRecord.class, userAuthRecordMapper, false);
    register("connection_record", "对接记录", ConnectionRecord.class, connectionRecordMapper, false);
    register("research_report", "研究报告", ResearchReport.class, researchReportMapper, false);
    register("research_category", "研究分类", ResearchCategory.class, researchCategoryMapper, false);
    register("ma_deal", "融资并购", MaDeal.class, maDealMapper, false);
    register("news_comment", "资讯评论", NewsComment.class, newsCommentMapper, false);
    register("coverage_apply_record", "寻求报道", CoverageApplyRecord.class, coverageApplyRecordMapper, false);
    register("sms_code_record", "短信记录", SmsCodeRecord.class, smsCodeRecordMapper, true);
    register("user_activity_record", "用户活动", UserActivityRecord.class, userActivityRecordMapper, false);
    register("project_onboard_record", "项目入驻", ProjectOnboardRecord.class, projectOnboardRecordMapper, false);
    register("user_project", "用户项目", UserProject.class, userProjectMapper, false);
    register(
        "activity_publish_request",
        "活动发布申请",
        ActivityPublishRecord.class,
        activityPublishRecordMapper,
        false);
  }

  private void register(
      String key, String label, Class<?> entityClass, BaseMapper<?> mapper, boolean readOnly) {
    String tableName = entityClass.getSimpleName();
    TableName tableNameAnno = entityClass.getAnnotation(TableName.class);
    if (tableNameAnno != null && !tableNameAnno.value().isBlank()) {
      tableName = tableNameAnno.value().replace("`", "");
    }
    resources.put(
        key,
        AdminResourceMeta.builder()
            .key(key)
            .label(label)
            .tableName(tableName)
            .entityClass(entityClass)
            .mapper(mapper)
            .readOnly(readOnly)
            .build());
  }

  public Optional<AdminResourceMeta> find(String key) {
    return Optional.ofNullable(resources.get(key));
  }

  public Map<String, AdminResourceMeta> all() {
    return resources;
  }
}
