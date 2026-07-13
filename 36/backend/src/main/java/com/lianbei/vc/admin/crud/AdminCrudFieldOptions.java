package com.lianbei.vc.admin.crud;

import com.lianbei.vc.common.constants.LibraryFilterKeys;
import java.util.HashMap;
import java.util.Map;

/** 后台 CRUD 表单字段：固定筛选项 → 下拉/多选 */
public final class AdminCrudFieldOptions {

  public enum InputType {
    SELECT,
    MULTISELECT
  }

  public enum SourceKind {
    FILTER,
    REGIONS,
    PROJECT_TAG_GROUPS,
    STATIC
  }

  public record FieldOptionRule(
      InputType inputType, SourceKind sourceKind, String scene, String filterKey) {

    static FieldOptionRule filter(InputType inputType, String scene, String filterKey) {
      return new FieldOptionRule(inputType, SourceKind.FILTER, scene, filterKey);
    }

    static FieldOptionRule regions(InputType inputType) {
      return new FieldOptionRule(inputType, SourceKind.REGIONS, null, null);
    }

    static FieldOptionRule projectTagGroups() {
      return new FieldOptionRule(InputType.MULTISELECT, SourceKind.PROJECT_TAG_GROUPS, null, null);
    }

    static FieldOptionRule staticOptions(InputType inputType, String key) {
      return new FieldOptionRule(inputType, SourceKind.STATIC, null, key);
    }
  }

  private static final Map<String, FieldOptionRule> GLOBAL = new HashMap<>();
  private static final Map<String, Map<String, FieldOptionRule>> RESOURCE = new HashMap<>();

  static {
    putGlobal("round", FieldOptionRule.filter(InputType.SELECT, LibraryFilterKeys.SCENE_PROJECT_LIBRARY, LibraryFilterKeys.ROUND));
    putGlobal("latestRound", FieldOptionRule.filter(InputType.SELECT, LibraryFilterKeys.SCENE_PROJECT_LIBRARY, LibraryFilterKeys.ROUND));
    putGlobal("statusLabel", FieldOptionRule.filter(InputType.SELECT, LibraryFilterKeys.SCENE_PROJECT_LIBRARY, LibraryFilterKeys.ROUND));
    putGlobal("region", FieldOptionRule.regions(InputType.SELECT));
    putGlobal("location", FieldOptionRule.regions(InputType.SELECT));
    putGlobal("category", FieldOptionRule.filter(InputType.MULTISELECT, LibraryFilterKeys.SCENE_SHARED, LibraryFilterKeys.INDUSTRY));
    putGlobal("industry", FieldOptionRule.filter(InputType.MULTISELECT, LibraryFilterKeys.SCENE_SHARED, LibraryFilterKeys.INDUSTRY));
    putGlobal("investmentField", FieldOptionRule.filter(InputType.MULTISELECT, LibraryFilterKeys.SCENE_SHARED, LibraryFilterKeys.INDUSTRY));
    putGlobal("instType", FieldOptionRule.filter(InputType.SELECT, LibraryFilterKeys.SCENE_INSTITUTION_LIBRARY, LibraryFilterKeys.INSTITUTION_TYPE));
    putGlobal("reportType", FieldOptionRule.filter(InputType.SELECT, LibraryFilterKeys.SCENE_RESEARCH_LIST, LibraryFilterKeys.REPORT_TYPE));
    putGlobal("foundingYear", FieldOptionRule.filter(InputType.SELECT, LibraryFilterKeys.SCENE_PROJECT_LIBRARY, LibraryFilterKeys.FOUNDED_YEAR));
    putGlobal("foundedYear", FieldOptionRule.filter(InputType.SELECT, LibraryFilterKeys.SCENE_PROJECT_LIBRARY, LibraryFilterKeys.FOUNDED_YEAR));
    putGlobal("amountCurrency", FieldOptionRule.filter(InputType.SELECT, LibraryFilterKeys.SCENE_FINANCING_EVENTS, LibraryFilterKeys.CURRENCY));
    putGlobal("tag", FieldOptionRule.filter(InputType.MULTISELECT, LibraryFilterKeys.SCENE_SHARED, LibraryFilterKeys.INDUSTRY));
    putGlobal("newsType", FieldOptionRule.staticOptions(InputType.SELECT, "newsType"));

    putResource("project", "tags", FieldOptionRule.projectTagGroups());
    putResource("research_report", "tags", FieldOptionRule.filter(InputType.MULTISELECT, LibraryFilterKeys.SCENE_RESEARCH_LIST, LibraryFilterKeys.FEATURE_TAG));
    putResource("coverage_apply_record", "reportType", FieldOptionRule.staticOptions(InputType.SELECT, "coverageReportType"));
    putResource("activity", "activityType", FieldOptionRule.staticOptions(InputType.SELECT, "activityType"));
    putResource("ma_deal", "category", FieldOptionRule.staticOptions(InputType.SELECT, "maDealCategory"));
    putResource(
        "project_collection",
        "category",
        FieldOptionRule.staticOptions(InputType.SELECT, "projectCollectionCategory"));
  }

  private AdminCrudFieldOptions() {}

  public static FieldOptionRule resolve(String resource, String fieldName) {
    Map<String, FieldOptionRule> overrides = RESOURCE.get(resource);
    if (overrides != null && overrides.containsKey(fieldName)) {
      return overrides.get(fieldName);
    }
    return GLOBAL.get(fieldName);
  }

  private static void putGlobal(String fieldName, FieldOptionRule rule) {
    GLOBAL.put(fieldName, rule);
  }

  private static void putResource(String resource, String fieldName, FieldOptionRule rule) {
    RESOURCE.computeIfAbsent(resource, key -> new HashMap<>()).put(fieldName, rule);
  }
}
