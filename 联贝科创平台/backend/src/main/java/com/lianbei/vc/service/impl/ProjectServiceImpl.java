package com.lianbei.vc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lianbei.vc.common.ChinaRegions;
import com.lianbei.vc.common.FilterParamUtils;
import com.lianbei.vc.common.ProjectKeywordQuery;
import com.lianbei.vc.exception.BusinessException;
import com.lianbei.vc.common.PageResult;
import com.lianbei.vc.entity.Project;
import com.lianbei.vc.dto.response.FinancingEventVO;
import com.lianbei.vc.dto.response.ProjectDetailVO;
import com.lianbei.vc.dto.response.SearchProjectVO;
import com.lianbei.vc.mapper.ProjectMapper;
import com.lianbei.vc.service.NewsService;
import com.lianbei.vc.service.ProjectService;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project>
    implements ProjectService {

  private final NewsService newsService;
  private final ProjectDetailAssembler detailAssembler;

  public ProjectServiceImpl(NewsService newsService, ProjectDetailAssembler detailAssembler) {
    this.newsService = newsService;
    this.detailAssembler = detailAssembler;
  }

  @Override
  public PageResult<Project> pageProjects(
      int pageNum, int pageSize, String category, String round, String region) {
    LambdaQueryWrapper<Project> wrapper =
        new LambdaQueryWrapper<Project>()
            .eq(Project::getIsHot, 1)
            .eq(StringUtils.hasText(category), Project::getCategory, category)
            .eq(StringUtils.hasText(round), Project::getRound, round)
            .eq(StringUtils.hasText(region), Project::getRegion, region)
            .orderByDesc(Project::getCreateTime);
    Page<Project> page = page(new Page<>(pageNum, pageSize), wrapper);
    return PageResult.of(page);
  }

  @Override
  public PageResult<Project> pageLibrary(
      int pageNum,
      int pageSize,
      String keyword,
      String industry,
      String region,
      String regionScope,
      String round,
      String advantage,
      String foundedYear,
      String lbReport,
      String financing,
      String sortBy) {
    LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
    applyKeywordFilter(wrapper, keyword);
    applyIndustryFilter(wrapper, industry);
    applyRegionFilter(wrapper, region, regionScope);
    applyRoundFilter(wrapper, round);
    applyFoundedYearsFilter(wrapper, foundedYear);
    applyTagsFilter(wrapper, advantage);
    applyLbReportFilter(wrapper, lbReport);
    if ("yes".equals(financing)) {
      wrapper.eq(Project::getIsFinancing, 1);
    } else if ("no".equals(financing)) {
      wrapper.eq(Project::getIsFinancing, 0);
    }
    if ("hot".equals(sortBy)) {
      wrapper.last(
          "ORDER BY CASE round "
              + "WHEN '已上市' THEN 5 WHEN 'C轮' THEN 4 WHEN 'B++轮' THEN 3.5 WHEN 'B轮' THEN 3 "
              + "WHEN 'A轮' THEN 2 WHEN '天使轮' THEN 1 ELSE 0 END DESC, create_time DESC");
    } else if ("updated".equals(sortBy)) {
      wrapper
          .orderByDesc(Project::getUpdateTime)
          .orderByDesc(Project::getCreateTime)
          .orderByDesc(Project::getId);
    } else if ("newest".equals(sortBy)) {
      wrapper.orderByDesc(Project::getCreateTime).orderByDesc(Project::getId);
    } else {
      // recommend 或默认：推荐权重优先
      wrapper
          .orderByDesc(Project::getIsHot)
          .orderByDesc(Project::getIsCertified)
          .orderByDesc(Project::getCreateTime)
          .orderByDesc(Project::getId);
    }
    Page<Project> page = page(new Page<>(pageNum, pageSize), wrapper);
    return PageResult.of(page);
  }

  @Override
  public PageResult<SearchProjectVO> searchProjects(String keyword, int pageNum, int pageSize) {
    if (!StringUtils.hasText(keyword)) {
      return PageResult.of(0L, List.of());
    }
    LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
    applyKeywordFilter(wrapper, keyword);
    wrapper.orderByDesc(Project::getUpdateTime);
    Page<Project> page = page(new Page<>(pageNum, pageSize), wrapper);
    List<SearchProjectVO> list =
        page.getRecords().stream().map(this::toSearchProjectVO).collect(Collectors.toList());
    return PageResult.of(page.getTotal(), list);
  }

  @Override
  public boolean existsByName(String name) {
    if (!StringUtils.hasText(name)) {
      return false;
    }
    return count(new LambdaQueryWrapper<Project>().eq(Project::getName, name.trim())) > 0;
  }

  private SearchProjectVO toSearchProjectVO(Project project) {
    String round = project.getRound();
    if (!StringUtils.hasText(round)) {
      round = StringUtils.hasText(project.getStatusLabel()) ? project.getStatusLabel() : "未披露";
    }
    return SearchProjectVO.builder()
        .id(project.getId())
        .name(project.getName())
        .round(round)
        .companyDesc(project.getCompanyDesc())
        .logoUrl(project.getLogoUrl())
        .entityName(detailAssembler.resolveBusinessFullName(project))
        .build();
  }

  private void applyKeywordFilter(LambdaQueryWrapper<Project> wrapper, String keyword) {
    ProjectKeywordQuery.apply(wrapper, keyword);
  }

  @Override
  public List<Project> listFeaturedFinancing(int limit) {
    return list(
        new LambdaQueryWrapper<Project>()
            .eq(Project::getIsFinancing, 1)
            .orderByDesc(Project::getCreateTime)
            .last("LIMIT " + limit));
  }

  @Override
  public Project getDetail(Long id) {
    Project project = getById(id);
    if (project == null) {
      throw new BusinessException("项目不存在");
    }
    return project;
  }

  @Override
  public ProjectDetailVO getProjectDetail(Long id) {
    Project project = getDetail(id);
    return detailAssembler.assemble(
        project, newsService.listByProject(project.getId(), project.getName(), 50));
  }

  @Override
  public PageResult<FinancingEventVO> pageFinancingEvents(
      int pageNum,
      int pageSize,
      String industry,
      String region,
      String regionScope,
      String round,
      String financingYear,
      String currency) {
    LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
    applyIndustryFilter(wrapper, industry);
    applyRegionFilter(wrapper, region, regionScope);
    applyRoundFilter(wrapper, round);
    applyFinancingYearsFilter(wrapper, financingYear);
    applyCurrenciesFilter(wrapper, currency);
    wrapper.orderByDesc(Project::getCreateTime);

    Page<Project> page = page(new Page<>(pageNum, pageSize), wrapper);
    List<FinancingEventVO> list =
        page.getRecords().stream().map(this::toFinancingEventVO).collect(Collectors.toList());
    return PageResult.of(page.getTotal(), list);
  }

  private FinancingEventVO toFinancingEventVO(Project project) {
    return FinancingEventVO.builder()
        .id(project.getId())
        .projectId(project.getId())
        .companyName(project.getName())
        .logoUrl(project.getLogoUrl())
        .description(project.getCompanyDesc())
        .amount(detailAssembler.formatEventAmount(project))
        .round(project.getRound() != null ? project.getRound() : "")
        .investors(detailAssembler.formatInvestorsForList(project))
        .date(detailAssembler.formatEventDate(project))
        .build();
  }

  private void applyIndustryFilter(LambdaQueryWrapper<Project> wrapper, String industry) {
    applyTagsFilter(wrapper, industry);
  }

  private void applyRegionFilter(
      LambdaQueryWrapper<Project> wrapper, String region, String regionScope) {
    List<String> regions = FilterParamUtils.splitValues(region);
    if (!regions.isEmpty()) {
      applyOrFilter(
          wrapper,
          regions,
          value ->
              w ->
                  w.and(
                      inner ->
                          inner.eq(Project::getRegion, value).or().eq(Project::getLocation, value)));
      return;
    }
    if ("overseas".equals(regionScope)) {
      List<String> china = ChinaRegions.all();
      wrapper.and(
          w ->
              w.isNotNull(Project::getLocation)
                  .ne(Project::getLocation, "")
                  .notIn(Project::getLocation, china));
    }
  }

  private void applyRoundFilter(LambdaQueryWrapper<Project> wrapper, String round) {
    List<String> rounds = FilterParamUtils.splitValues(round);
    if (!rounds.isEmpty()) {
      wrapper.in(Project::getRound, rounds);
    }
  }

  private void applyFoundedYearsFilter(LambdaQueryWrapper<Project> wrapper, String foundedYear) {
    List<String> years =
        FilterParamUtils.splitValues(foundedYear).stream()
            .filter(this::isRecognizedFoundedYear)
            .collect(Collectors.toList());
    applyOrFilter(wrapper, years, value -> branch -> applySingleFoundedYear(branch, value));
  }

  private void applySingleFoundedYear(LambdaQueryWrapper<Project> wrapper, String foundedYear) {
    if ("2010年及以前".equals(foundedYear)) {
      wrapper.apply(
          "founding_year IS NOT NULL AND founding_year != '' "
              + "AND CAST(LEFT(founding_year, 4) AS UNSIGNED) <= 2010");
      return;
    }
    String year = foundedYear.replace("年", "").trim();
    if (year.matches("\\d{4}")) {
      wrapper.like(Project::getFoundingYear, year);
    }
  }

  /** 按项目标签匹配（project.tags 或 project_tag_rel） */
  private void applyTagsFilter(LambdaQueryWrapper<Project> wrapper, String tagsParam) {
    applyOrFilter(
        wrapper,
        FilterParamUtils.splitValues(tagsParam),
        value -> branch -> applySingleTagMatch(branch, value));
  }

  private void applySingleTagMatch(LambdaQueryWrapper<Project> wrapper, String tagName) {
    wrapper.and(
        w ->
            w.like(Project::getTags, tagName)
                .or()
                .apply(
                    "id IN (SELECT ptr.project_id FROM project_tag_rel ptr "
                        + "INNER JOIN project_tag pt ON pt.id = ptr.tag_id "
                        + "WHERE pt.name = {0})",
                    tagName));
  }

  /** 联贝科创报道：是/否 对应是否有关联资讯（news）事件，单选 */
  private void applyLbReportFilter(LambdaQueryWrapper<Project> wrapper, String lbReport) {
    String choice = resolveSingleChoice(lbReport);
    if ("是".equals(choice) || "yes".equalsIgnoreCase(choice)) {
      wrapper.apply(
          "EXISTS (SELECT 1 FROM news n WHERE n.project_id = id "
              + "OR (n.project_name IS NOT NULL AND n.project_name != '' AND n.project_name = name))");
      return;
    }
    if ("否".equals(choice) || "no".equalsIgnoreCase(choice)) {
      wrapper.apply(
          "NOT EXISTS (SELECT 1 FROM news n WHERE n.project_id = id "
              + "OR (n.project_name IS NOT NULL AND n.project_name != '' AND n.project_name = name))");
    }
  }

  private String resolveSingleChoice(String raw) {
    List<String> values = FilterParamUtils.splitValues(raw);
    if (values.isEmpty()) {
      return "";
    }
    return values.get(0);
  }

  private void applyFinancingYearsFilter(LambdaQueryWrapper<Project> wrapper, String financingYear) {
    List<String> years =
        FilterParamUtils.splitValues(financingYear).stream()
            .filter(this::isRecognizedFinancingYear)
            .collect(Collectors.toList());
    applyOrFilter(wrapper, years, value -> branch -> applySingleFinancingYear(branch, value));
  }

  private void applySingleFinancingYear(LambdaQueryWrapper<Project> wrapper, String financingYear) {
    if ("2010年及以前".equals(financingYear)) {
      wrapper.apply("YEAR(create_time) <= 2010");
      return;
    }
    String year = financingYear.replace("年", "").trim();
    if (year.matches("\\d{4}")) {
      wrapper.apply("YEAR(create_time) = {0}", year);
    }
  }

  private void applyCurrenciesFilter(LambdaQueryWrapper<Project> wrapper, String currency) {
    applyOrFilter(
        wrapper,
        FilterParamUtils.splitValues(currency),
        value -> w -> applySingleCurrency(w, value));
  }

  private void applySingleCurrency(LambdaQueryWrapper<Project> wrapper, String currency) {
    switch (currency) {
      case "美元" ->
          wrapper.and(
              w ->
                  w.like(Project::getInvestmentAmount, "美元")
                      .or()
                      .like(Project::getInvestmentAmount, "美金")
                      .or()
                      .like(Project::getInvestmentAmount, "$"));
      case "港元" ->
          wrapper.and(
              w ->
                  w.like(Project::getInvestmentAmount, "港元")
                      .or()
                      .like(Project::getInvestmentAmount, "港币"));
      case "欧元" -> wrapper.like(Project::getInvestmentAmount, "欧元");
      case "英镑" -> wrapper.like(Project::getInvestmentAmount, "英镑");
      case "卢比" -> wrapper.like(Project::getInvestmentAmount, "卢比");
      case "日元" -> wrapper.like(Project::getInvestmentAmount, "日元");
      case "人民币" ->
          wrapper.and(
              w ->
                  w.like(Project::getInvestmentAmount, "人民币")
                      .or()
                      .like(Project::getInvestmentAmount, "万")
                      .or()
                      .like(Project::getInvestmentAmount, "亿"));
      default -> wrapper.like(Project::getInvestmentAmount, currency);
    }
  }

  private void applyOrFilter(
      LambdaQueryWrapper<Project> wrapper,
      List<String> values,
      Function<String, Consumer<LambdaQueryWrapper<Project>>> branchFactory) {
    if (values == null || values.isEmpty()) {
      return;
    }
    List<String> effective =
        values.stream()
            .filter(StringUtils::hasText)
            .map(String::trim)
            .distinct()
            .collect(Collectors.toList());
    if (effective.isEmpty()) {
      return;
    }
    if (effective.size() == 1) {
      branchFactory.apply(effective.get(0)).accept(wrapper);
      return;
    }
    wrapper.and(
        outer -> {
          for (int i = 0; i < effective.size(); i++) {
            String value = effective.get(i);
            Consumer<LambdaQueryWrapper<Project>> branch = branchFactory.apply(value);
            if (i == 0) {
              branch.accept(outer);
            } else {
              outer.or(branch::accept);
            }
          }
        });
  }

  private boolean isRecognizedFoundedYear(String value) {
    if (!StringUtils.hasText(value)) {
      return false;
    }
    if ("2010年及以前".equals(value.trim())) {
      return true;
    }
    return value.replace("年", "").trim().matches("\\d{4}");
  }

  private boolean isRecognizedFinancingYear(String value) {
    if (!StringUtils.hasText(value)) {
      return false;
    }
    if ("2010年及以前".equals(value.trim())) {
      return true;
    }
    return value.replace("年", "").trim().matches("\\d{4}");
  }
}
