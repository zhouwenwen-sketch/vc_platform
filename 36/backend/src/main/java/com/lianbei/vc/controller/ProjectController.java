package com.lianbei.vc.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianbei.vc.common.PageResult;
import com.lianbei.vc.common.Result;
import com.lianbei.vc.dto.response.ProjectDetailVO;
import com.lianbei.vc.dto.response.SearchProjectVO;
import com.lianbei.vc.entity.Project;
import com.lianbei.vc.service.ProjectCollectionService;
import com.lianbei.vc.service.ProjectService;
import com.lianbei.vc.service.LibraryFilterService;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 创投项目接口
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

  private final ProjectService projectService;
  private final ProjectCollectionService projectCollectionService;
  private final LibraryFilterService libraryFilterService;
  private final ObjectMapper objectMapper;

  public ProjectController(
      ProjectService projectService,
      ProjectCollectionService projectCollectionService,
      LibraryFilterService libraryFilterService,
      ObjectMapper objectMapper) {
    this.projectService = projectService;
    this.projectCollectionService = projectCollectionService;
    this.libraryFilterService = libraryFilterService;
    this.objectMapper = objectMapper;
  }

  /** 项目 Tab 页顶部配置 */
  @GetMapping("/init")
  @SuppressWarnings("unchecked")
  public Result<Map<String, Object>> init() throws IOException {
    ClassPathResource resource = new ClassPathResource("config/project-init.json");
    Map<String, Object> data =
        objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
    List<Map<String, Object>> cols = (List<Map<String, Object>>) data.get("projectCollections");
    if (cols != null) {
      for (Map<String, Object> c : cols) {
        Object idObj = c.get("id");
        if (idObj != null) {
          long id = idObj instanceof Number n ? n.longValue() : Long.parseLong(String.valueOf(idObj));
          c.put("projectCount", projectCollectionService.getProjectCount(id));
        }
      }
    }
    return Result.ok(data);
  }

  @GetMapping("/page")
  public Result<PageResult<Project>> page(
      @RequestParam(defaultValue = "1") int pageNum,
      @RequestParam(defaultValue = "10") int pageSize,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String round,
      @RequestParam(required = false) String region) {
    return Result.ok(projectService.pageProjects(pageNum, pageSize, category, round, region));
  }

  /** 首页在融项目，默认前 6 条 */
  @GetMapping("/featured")
  public Result<List<Project>> featured(
      @RequestParam(defaultValue = "6") int limit) {
    return Result.ok(projectService.listFeaturedFinancing(limit));
  }

  /** 项目集 Tab */
  @GetMapping("/collections/tabs")
  public Result<List<String>> collectionTabs() {
    return Result.ok(projectCollectionService.listTabs());
  }

  /** 项目集列表 */
  @GetMapping("/collections")
  public Result<PageResult<Map<String, Object>>> collections(
      @RequestParam(defaultValue = "1") int pageNum,
      @RequestParam(defaultValue = "10") int pageSize,
      @RequestParam(required = false) String tab) {
    return Result.ok(projectCollectionService.pageCollections(pageNum, pageSize, tab));
  }

  /** 项目集详情（合集内项目列表） */
  @GetMapping("/collections/{id}")
  public Result<Map<String, Object>> collectionDetail(
      @PathVariable Long id,
      @RequestParam(defaultValue = "1") int pageNum,
      @RequestParam(defaultValue = "20") int pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String round,
      @RequestParam(required = false) String industry) {
    try {
      return Result.ok(
          projectCollectionService.getCollectionDetail(
              id, pageNum, pageSize, keyword, round, industry));
    } catch (IllegalArgumentException e) {
      return Result.fail(e.getMessage());
    }
  }

  /** 企业项目库筛选项（行业/轮次/优势/成立时间） */
  @GetMapping("/library/filters")
  public Result<Map<String, List<String>>> libraryFilters() {
    return Result.ok(libraryFilterService.listProjectLibraryOptions());
  }

  /** 企业项目库列表（首页「项目库」） */
  @GetMapping("/collection/detail")
  public Result<PageResult<Project>> collectionDetail(
      @RequestParam(defaultValue = "1") int pageNum,
      @RequestParam(defaultValue = "10") int pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String industry,
      @RequestParam(required = false) String region,
      @RequestParam(required = false) String regionScope,
      @RequestParam(required = false) String round,
      @RequestParam(required = false) String advantage,
      @RequestParam(required = false) String foundedYear,
      @RequestParam(required = false) String lbReport,
      @RequestParam(required = false) String financing,
      @RequestParam(required = false) String sortBy) {
    return Result.ok(
        projectService.pageLibrary(
            pageNum,
            pageSize,
            keyword,
            industry,
            region,
            regionScope,
            round,
            advantage,
            foundedYear,
            lbReport,
            financing,
            sortBy));
  }

  /** 首页搜索 - 实时关键字匹配项目 */
  @GetMapping("/search")
  public Result<PageResult<SearchProjectVO>> search(
      @RequestParam String keyword,
      @RequestParam(defaultValue = "1") int pageNum,
      @RequestParam(defaultValue = "20") int pageSize) {
    return Result.ok(projectService.searchProjects(keyword, pageNum, pageSize));
  }

  /** 入驻申请 - 检查项目名称是否已被收录 */
  @GetMapping("/check-name")
  public Result<Map<String, Boolean>> checkName(@RequestParam String name) {
    return Result.ok(Map.of("exists", projectService.existsByName(name)));
  }

  /** 项目详情（含介绍、融资历史、工商、团队、行业资讯） */
  @GetMapping("/detail/{id}")
  public Result<ProjectDetailVO> detailFull(@PathVariable Long id) {
    return Result.ok(projectService.getProjectDetail(id));
  }

  @GetMapping("/{id:\\d+}")
  public Result<Project> detail(@PathVariable Long id) {
    return Result.ok(projectService.getDetail(id));
  }
}
