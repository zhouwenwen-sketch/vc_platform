package com.lianbei.vc.common.constants;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class LibraryFilterKeys {

  public static final String SCENE_SHARED = "shared";
  public static final String SCENE_PROJECT_LIBRARY = "project_library";
  public static final String SCENE_FINANCING_EVENTS = "financing_events";
  public static final String SCENE_INSTITUTION_LIBRARY = "institution_library";
  public static final String SCENE_RESEARCH_LIST = "research_list";

  public static final String INDUSTRY = "industry";
  public static final String ROUND = "round";
  public static final String ADVANTAGE = "advantage";
  public static final String FOUNDED_YEAR = "foundedYear";
  public static final String FINANCING_YEAR = "financingYear";
  public static final String CURRENCY = "currency";
  public static final String INSTITUTION_TYPE = "institutionType";
  public static final String REPORT_TYPE = "reportType";
  public static final String FEATURE_TAG = "featureTag";

  /** 后台可编辑的筛选项定义 */
  public static final List<FilterGroupDef> ADMIN_GROUPS =
      List.of(
          new FilterGroupDef(
              SCENE_SHARED,
              INDUSTRY,
              "所属行业",
              "项目库、机构库（投资领域）、研究院、项目集详情共用"),
          new FilterGroupDef(
              SCENE_PROJECT_LIBRARY,
              ROUND,
              "融资轮次",
              "项目库顶栏、项目集详情"),
          new FilterGroupDef(
              SCENE_PROJECT_LIBRARY, ADVANTAGE, "项目优势", "项目库侧栏"),
          new FilterGroupDef(
              SCENE_PROJECT_LIBRARY, FOUNDED_YEAR, "成立时间", "项目库侧栏"),
          new FilterGroupDef(
              SCENE_FINANCING_EVENTS, ROUND, "融资轮次", "融资事件侧栏"),
          new FilterGroupDef(
              SCENE_FINANCING_EVENTS, FINANCING_YEAR, "融资时间", "融资事件侧栏"),
          new FilterGroupDef(
              SCENE_FINANCING_EVENTS, CURRENCY, "货币币种", "融资事件侧栏"),
          new FilterGroupDef(
              SCENE_INSTITUTION_LIBRARY, INSTITUTION_TYPE, "机构类型", "机构库"),
          new FilterGroupDef(SCENE_RESEARCH_LIST, REPORT_TYPE, "报告类型", "研究院"),
          new FilterGroupDef(SCENE_RESEARCH_LIST, FEATURE_TAG, "特色分类", "研究院"));

  /** C 端 bundle：输出字段 -> scene:filterKey */
  private static final Map<String, LinkedHashMap<String, String>> BUNDLE_SOURCES =
      Map.of(
          "project-library",
              bundle(
                  entry(INDUSTRY, SCENE_SHARED, INDUSTRY),
                  entry(ROUND, SCENE_PROJECT_LIBRARY, ROUND),
                  entry(ADVANTAGE, SCENE_PROJECT_LIBRARY, ADVANTAGE),
                  entry(FOUNDED_YEAR, SCENE_PROJECT_LIBRARY, FOUNDED_YEAR)),
          "collection-detail",
              bundle(
                  entry(INDUSTRY, SCENE_SHARED, INDUSTRY),
                  entry(ROUND, SCENE_PROJECT_LIBRARY, ROUND)),
          "institution-library",
              bundle(
                  entry("investmentField", SCENE_SHARED, INDUSTRY),
                  entry(INSTITUTION_TYPE, SCENE_INSTITUTION_LIBRARY, INSTITUTION_TYPE)),
          "financing-events",
              bundle(
                  entry(INDUSTRY, SCENE_SHARED, INDUSTRY),
                  entry(ROUND, SCENE_FINANCING_EVENTS, ROUND),
                  entry(FINANCING_YEAR, SCENE_FINANCING_EVENTS, FINANCING_YEAR),
                  entry(CURRENCY, SCENE_FINANCING_EVENTS, CURRENCY)),
          "research-list",
              bundle(
                  entry(INDUSTRY, SCENE_SHARED, INDUSTRY),
                  entry(REPORT_TYPE, SCENE_RESEARCH_LIST, REPORT_TYPE),
                  entry("tag", SCENE_RESEARCH_LIST, FEATURE_TAG)));

  public record FilterGroupDef(String scene, String filterKey, String label, String remark) {
    public String id() {
      return scene + ":" + filterKey;
    }
  }

  private LibraryFilterKeys() {}

  public static Map<String, LinkedHashMap<String, String>> bundleSources() {
    return BUNDLE_SOURCES;
  }

  public static LinkedHashMap<String, String> bundleSource(String bundleName) {
    return BUNDLE_SOURCES.get(bundleName);
  }

  public static FilterGroupDef findGroup(String scene, String filterKey) {
    return ADMIN_GROUPS.stream()
        .filter(g -> g.scene().equals(scene) && g.filterKey().equals(filterKey))
        .findFirst()
        .orElse(null);
  }

  @SafeVarargs
  private static LinkedHashMap<String, String> bundle(Map.Entry<String, String>... entries) {
    LinkedHashMap<String, String> map = new LinkedHashMap<>();
    for (Map.Entry<String, String> entry : entries) {
      map.put(entry.getKey(), entry.getValue());
    }
    return map;
  }

  private static Map.Entry<String, String> entry(String outputKey, String scene, String filterKey) {
    return Map.entry(outputKey, scene + ":" + filterKey);
  }
}
