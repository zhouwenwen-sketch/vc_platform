package com.lianbei.vc.admin.crud;

import com.lianbei.vc.admin.crud.AdminCrudFieldOptions.FieldOptionRule;
import com.lianbei.vc.admin.crud.AdminCrudFieldOptions.InputType;
import com.lianbei.vc.admin.crud.AdminCrudFieldOptions.SourceKind;
import com.lianbei.vc.admin.dto.CrudFieldOptionGroupVO;
import com.lianbei.vc.admin.dto.CrudFieldOptionsVO;
import com.lianbei.vc.common.ChinaRegions;
import com.lianbei.vc.common.constants.LibraryFilterKeys;
import com.lianbei.vc.service.LibraryFilterService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class AdminCrudFieldOptionsResolver {

  private static final List<String> OVERSEAS_REGIONS =
      List.of("美国", "新加坡", "日本", "英国", "德国", "其他海外");

  private static final Map<String, List<String>> STATIC_OPTIONS =
      Map.of(
          "coverageReportType",
              List.of("latest_financing", "no_financing"),
          "activityType",
              List.of("event", "roadshow"),
          "newsType",
              List.of("快讯", "文章"),
          "maDealCategory",
              List.of(
                  "listed_company",
                  "project_asset",
                  "fund_invest",
                  "enterprise_service"),
          "projectCollectionCategory",
              List.of("最受关注", "热门赛道", "大赛路演", "榜单名册", "政策支持"));

  private static final Map<String, Map<String, String>> STATIC_LABELS =
      Map.of(
          "coverageReportType",
              Map.of(
                  "latest_financing", "最新融资消息",
                  "no_financing", "没有新融资，但希望我们报道您的项目"),
          "activityType",
              Map.of("event", "活动", "roadshow", "路演"),
          "maDealCategory",
              Map.of(
                  "listed_company", "上市公司",
                  "project_asset", "项目资产",
                  "fund_invest", "基金产投",
                  "enterprise_service", "企服"));

  private final LibraryFilterService libraryFilterService;

  public AdminCrudFieldOptionsResolver(LibraryFilterService libraryFilterService) {
    this.libraryFilterService = libraryFilterService;
  }

  public CrudFieldOptionsVO resolve(String resource, String fieldName) {
    FieldOptionRule rule = AdminCrudFieldOptions.resolve(resource, fieldName);
    if (rule == null) {
      return null;
    }
    CrudFieldOptionsVO vo = new CrudFieldOptionsVO();
    vo.setInputType(rule.inputType().name().toLowerCase());
    switch (rule.sourceKind()) {
      case FILTER -> vo.setOptions(listFilterValues(rule.scene(), rule.filterKey()));
      case REGIONS -> vo.setOptions(listRegionValues());
      case PROJECT_TAG_GROUPS -> vo.setOptionGroups(listProjectTagGroups());
      case STATIC -> fillStaticOptions(vo, rule.filterKey());
      default -> vo.setOptions(List.of());
    }
    return vo;
  }

  private List<String> listFilterValues(String scene, String filterKey) {
    Map<String, List<String>> bundle = libraryFilterService.listBundle(mapFilterBundle(scene, filterKey));
    String outputKey = mapFilterOutputKey(scene, filterKey);
    return bundle.getOrDefault(outputKey, List.of());
  }

  private String mapFilterBundle(String scene, String filterKey) {
    if (LibraryFilterKeys.SCENE_FINANCING_EVENTS.equals(scene)) {
      return "financing-events";
    }
    if (LibraryFilterKeys.SCENE_INSTITUTION_LIBRARY.equals(scene)) {
      return "institution-library";
    }
    if (LibraryFilterKeys.SCENE_RESEARCH_LIST.equals(scene)) {
      return "research-list";
    }
    return "project-library";
  }

  private String mapFilterOutputKey(String scene, String filterKey) {
    if (LibraryFilterKeys.SCENE_SHARED.equals(scene) && LibraryFilterKeys.INDUSTRY.equals(filterKey)) {
      return LibraryFilterKeys.INDUSTRY;
    }
    if (LibraryFilterKeys.SCENE_INSTITUTION_LIBRARY.equals(scene)) {
      return LibraryFilterKeys.INSTITUTION_TYPE;
    }
    if (LibraryFilterKeys.SCENE_RESEARCH_LIST.equals(scene)) {
      if (LibraryFilterKeys.REPORT_TYPE.equals(filterKey)) {
        return LibraryFilterKeys.REPORT_TYPE;
      }
      return "tag";
    }
    if (LibraryFilterKeys.SCENE_FINANCING_EVENTS.equals(scene)) {
      if (LibraryFilterKeys.CURRENCY.equals(filterKey)) {
        return LibraryFilterKeys.CURRENCY;
      }
      return LibraryFilterKeys.ROUND;
    }
    if (LibraryFilterKeys.FOUNDED_YEAR.equals(filterKey)) {
      return LibraryFilterKeys.FOUNDED_YEAR;
    }
    return filterKey;
  }

  private List<String> listRegionValues() {
    List<String> values = new ArrayList<>(ChinaRegions.all());
    values.addAll(OVERSEAS_REGIONS);
    return values;
  }

  private List<CrudFieldOptionGroupVO> listProjectTagGroups() {
    List<Map<String, Object>> groups = libraryFilterService.listProjectTagOptionGroups();
    List<CrudFieldOptionGroupVO> result = new ArrayList<>();
    for (Map<String, Object> group : groups) {
      CrudFieldOptionGroupVO vo = new CrudFieldOptionGroupVO();
      vo.setLabel(String.valueOf(group.get("label")));
      @SuppressWarnings("unchecked")
      List<String> values = (List<String>) group.get("values");
      vo.setValues(values == null ? List.of() : values);
      result.add(vo);
    }
    return result;
  }

  private void fillStaticOptions(CrudFieldOptionsVO vo, String staticKey) {
    List<String> values = STATIC_OPTIONS.getOrDefault(staticKey, List.of());
    vo.setOptions(values);
    Map<String, String> labels = STATIC_LABELS.getOrDefault(staticKey, Map.of());
    if (!labels.isEmpty()) {
      List<Map<String, String>> labeled = new ArrayList<>();
      for (String value : values) {
        Map<String, String> item = new LinkedHashMap<>();
        item.put("value", value);
        item.put("label", labels.getOrDefault(value, value));
        labeled.add(item);
      }
      vo.setLabeledOptions(labeled);
    }
  }
}
