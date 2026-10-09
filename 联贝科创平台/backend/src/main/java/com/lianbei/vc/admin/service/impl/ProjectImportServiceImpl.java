package com.lianbei.vc.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lianbei.vc.admin.service.ProjectImportService;
import com.lianbei.vc.dto.response.ProjectImportResultVO;
import com.lianbei.vc.entity.Institution;
import com.lianbei.vc.entity.Project;
import com.lianbei.vc.entity.ProjectDynamic;
import com.lianbei.vc.entity.ProjectFinancing;
import com.lianbei.vc.entity.ProjectFinancingInvestor;
import com.lianbei.vc.entity.ProjectTeamMember;
import com.lianbei.vc.exception.BusinessException;
import com.lianbei.vc.mapper.InstitutionMapper;
import com.lianbei.vc.mapper.ProjectDynamicMapper;
import com.lianbei.vc.mapper.ProjectFinancingInvestorMapper;
import com.lianbei.vc.mapper.ProjectFinancingMapper;
import com.lianbei.vc.mapper.ProjectMapper;
import com.lianbei.vc.mapper.ProjectTeamMemberMapper;
import com.lianbei.vc.service.FileStorageService;
import com.lianbei.vc.service.ProjectTagSyncService;
import com.lianbei.vc.service.impl.InstitutionInvestmentCounter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProjectImportServiceImpl implements ProjectImportService {

  private static final Logger log = LoggerFactory.getLogger(ProjectImportServiceImpl.class);
  private static final DataFormatter FORMATTER = new DataFormatter();
  private static final Pattern DATE_IN_TEXT =
      Pattern.compile("(20\\d{2})[年\\-/\\.](\\d{1,2})[月\\-/\\.](\\d{1,2})");

  private final ProjectMapper projectMapper;
  private final ProjectFinancingMapper financingMapper;
  private final ProjectFinancingInvestorMapper investorMapper;
  private final ProjectTeamMemberMapper teamMemberMapper;
  private final ProjectDynamicMapper dynamicMapper;
  private final InstitutionMapper institutionMapper;
  private final ProjectTagSyncService tagSyncService;
  private final FileStorageService fileStorageService;
  private final InstitutionInvestmentCounter investmentCounter;

  public ProjectImportServiceImpl(
      ProjectMapper projectMapper,
      ProjectFinancingMapper financingMapper,
      ProjectFinancingInvestorMapper investorMapper,
      ProjectTeamMemberMapper teamMemberMapper,
      ProjectDynamicMapper dynamicMapper,
      InstitutionMapper institutionMapper,
      ProjectTagSyncService tagSyncService,
      FileStorageService fileStorageService,
      InstitutionInvestmentCounter investmentCounter) {
    this.projectMapper = projectMapper;
    this.financingMapper = financingMapper;
    this.investorMapper = investorMapper;
    this.teamMemberMapper = teamMemberMapper;
    this.dynamicMapper = dynamicMapper;
    this.institutionMapper = institutionMapper;
    this.tagSyncService = tagSyncService;
    this.fileStorageService = fileStorageService;
    this.investmentCounter = investmentCounter;
  }

  @Override
  @Transactional(timeout = 600)
  public ProjectImportResultVO importExcel(
      MultipartFile projectsFile,
      MultipartFile portraitFile,
      MultipartFile membersFile,
      MultipartFile dynamicsFile,
      MultipartFile financingFile,
      MultipartFile imagesZip,
      MultipartFile[] imageFiles) {
    if (isEmpty(projectsFile)
        && isEmpty(portraitFile)
        && isEmpty(membersFile)
        && isEmpty(dynamicsFile)
        && isEmpty(financingFile)) {
      throw new BusinessException("请至少上传一个 Excel 文件");
    }

    List<String> warnings = new ArrayList<>();
    long startedAt = System.currentTimeMillis();
    log.info("[project-import] start");
    Map<String, byte[]> imageMap = loadImages(imagesZip, imageFiles, warnings);
    int[] imagesSaved = {0};

    int projectsInserted = 0;
    int projectsUpdated = 0;
    int portraitsUpdated = 0;
    int financingInserted = 0;
    int financingUpdated = 0;
    int membersInserted = 0;
    int membersUpdated = 0;
    int membersSkipped = 0;
    int dynamicsInserted = 0;
    int dynamicsSkipped = 0;

    Map<String, Project> projectByName = new HashMap<>();
    Map<String, String> nameAliases = new HashMap<>();
    Map<String, Long> financingKeyToId = new HashMap<>();
    Map<String, List<Long>> dateRoundToProjectIds = new HashMap<>();
    Set<Long> projectsNeedFinancingRefresh = new HashSet<>();
    Map<String, Long> institutionCache = new HashMap<>();
    Set<Long> projectsNeedTagSync = new HashSet<>();
    List<String> sortedMatchNames;
    List<String> sortedAliasKeys;

    if (!isEmpty(projectsFile)) {
      List<Map<String, String>> projectRows = parseSheet(projectsFile, warnings);
      log.info("[project-import] 项目信息 {} 行", projectRows.size());
      for (Map<String, String> row : projectRows) {
        String name = trim(projectKeyFromRow(row));
        if (!StringUtils.hasText(name)) {
          warnings.add("项目信息行缺少被投公司/项目简称，已跳过");
          continue;
        }
        String institutionName = trim(firstNonBlank(row, "机构名称"));
        Long institutionId = resolveInstitutionId(institutionName, warnings, institutionCache);

        boolean updated =
            upsertProjectFromInfo(
                name, row, imageMap, warnings, imagesSaved, projectByName, projectsNeedTagSync);
        if (updated) {
          projectsUpdated++;
        } else {
          projectsInserted++;
        }
        Project project = projectByName.get(name);
        if (project == null) {
          continue;
        }
        registerAliases(nameAliases, name);
        int[] finStats =
            upsertFinancingFromInfoRow(
                project,
                institutionId,
                row,
                financingKeyToId,
                dateRoundToProjectIds,
                projectsNeedFinancingRefresh);
        financingInserted += finStats[0];
        financingUpdated += finStats[1];
      }
    }

    refreshProjectIndex(projectByName, nameAliases);
    sortedMatchNames = buildSortedMatchNames(projectByName);
    sortedAliasKeys = buildSortedAliasKeys(nameAliases);
    if (!isEmpty(projectsFile) || !isEmpty(financingFile)) {
      log.info("[project-import] 索引已有融资记录…");
      indexExistingFinancings(projectByName.values(), financingKeyToId, dateRoundToProjectIds);
    }

    if (!isEmpty(portraitFile)) {
      List<Map<String, String>> portraitRows = parseSheet(portraitFile, warnings);
      log.info("[project-import] 项目画像 {} 行", portraitRows.size());
      for (Map<String, String> row : portraitRows) {
        String key = portraitProjectKey(row);
        if (!StringUtils.hasText(key)) {
          warnings.add("项目画像行缺少项目简称，已跳过");
          continue;
        }
        Project project = findProject(key, projectByName, nameAliases);
        if (project == null) {
          warnings.add("项目画像无法匹配项目「" + key + "」");
          continue;
        }
        if (applyPortrait(project, row, imageMap, warnings, imagesSaved, projectsNeedTagSync)) {
          portraitsUpdated++;
        }
      }
    }

    if (!isEmpty(financingFile)) {
      List<Map<String, String>> financingRows = parseSheet(financingFile, warnings);
      log.info("[project-import] 融资信息 {} 行", financingRows.size());
      for (Map<String, String> row : financingRows) {
        String projectKey = projectKeyFromRow(row);
        Project project = null;
        if (StringUtils.hasText(projectKey)) {
          project = findProject(projectKey, projectByName, nameAliases);
        }
        if (project == null) {
          project =
              matchProjectForFinancingRow(
                  row, projectByName, nameAliases, financingKeyToId, dateRoundToProjectIds);
        }
        if (project == null) {
          warnings.add(
              "融资信息无法匹配项目："
                  + (StringUtils.hasText(projectKey) ? projectKey + " " : "")
                  + firstNonBlank(row, "获投时间", "融资时间")
                  + " "
                  + firstNonBlank(row, "融资轮次")
                  + " "
                  + firstNonBlank(row, "融资金额"));
          continue;
        }
        if (upsertFinancingFromSheetRow(
            project,
            null,
            row,
            financingKeyToId,
            dateRoundToProjectIds,
            projectsNeedFinancingRefresh,
            warnings,
            institutionCache)) {
          financingUpdated++;
        } else {
          financingInserted++;
        }
      }
    }

    if (!isEmpty(membersFile)) {
      Map<Long, Integer> sortCounter = new HashMap<>();
      List<Map<String, String>> memberRows = parseSheet(membersFile, warnings);
      log.info("[project-import] 成员信息 {} 行", memberRows.size());
      for (Map<String, String> row : memberRows) {
        String memberName = cleanMemberName(firstNonBlank(row, "姓名"));
        if (!StringUtils.hasText(memberName)) {
          warnings.add("成员行缺少姓名，已跳过");
          membersSkipped++;
          continue;
        }
        String projectKey = projectKeyFromRow(row);
        Project project = null;
        if (StringUtils.hasText(projectKey)) {
          project = findProject(projectKey.trim(), projectByName, nameAliases);
        }
        if (project == null) {
          project = matchProjectFromBio(row, projectByName, nameAliases, sortedMatchNames, sortedAliasKeys);
        }
        if (project == null) {
          warnings.add(
              "成员「"
                  + memberName
                  + "」无法关联项目"
                  + (StringUtils.hasText(projectKey) ? "「" + projectKey + "」" : "")
                  + "，已跳过");
          membersSkipped++;
          continue;
        }
        int sortOrder = sortCounter.getOrDefault(project.getId(), 0);
        sortCounter.put(project.getId(), sortOrder + 1);
        if (upsertTeamMember(project, memberName, row, imageMap, sortOrder, warnings, imagesSaved)) {
          membersUpdated++;
        } else {
          membersInserted++;
        }
      }
    }

    if (!isEmpty(dynamicsFile)) {
      Map<Long, Integer> sortCounter = new HashMap<>();
      List<Map<String, String>> dynamicRows = parseSheet(dynamicsFile, warnings);
      log.info("[project-import] 动态信息 {} 行", dynamicRows.size());
      for (Map<String, String> row : dynamicRows) {
        String content = firstNonBlank(row, "事件", "动态", "内容");
        if (!StringUtils.hasText(content)) {
          dynamicsSkipped++;
          continue;
        }
        String projectKey = projectKeyFromRow(row);
        Project project = null;
        if (StringUtils.hasText(projectKey)) {
          project = findProject(projectKey.trim(), projectByName, nameAliases);
        }
        if (project == null) {
          project = matchProjectFromText(content, projectByName, nameAliases, sortedMatchNames, sortedAliasKeys);
        }
        if (project == null) {
          warnings.add(
              "动态无法匹配项目"
                  + (StringUtils.hasText(projectKey) ? "「" + projectKey + "」" : "")
                  + "，已跳过: "
                  + truncate(content, 40));
          dynamicsSkipped++;
          continue;
        }
        int sortOrder = sortCounter.getOrDefault(project.getId(), 0);
        sortCounter.put(project.getId(), sortOrder + 1);
        LocalDate eventDate = parseDate(firstNonBlank(row, "日期", "事件日期"));
        if (eventDate == null) {
          eventDate = parseDateFromText(content);
        }
        insertDynamic(project.getId(), eventDate, content, sortOrder);
        dynamicsInserted++;
      }
    }

    log.info("[project-import] 刷新最新融资 {} 个项目", projectsNeedFinancingRefresh.size());
    for (Long projectId : projectsNeedFinancingRefresh) {
      refreshLatestFinancing(projectId);
    }

    if (!imageMap.isEmpty()) {
      applyUnmatchedProjectLogos(projectByName.values(), imageMap, warnings, imagesSaved);
    }

    flushTagSync(projectsNeedTagSync);

    log.info("[project-import] syncing institution event counts...");
    investmentCounter.syncAllEventCounts();

    log.info(
        "[project-import] done in {} ms, projects +{}/~{}, dynamics {}",
        System.currentTimeMillis() - startedAt,
        projectsInserted,
        projectsUpdated,
        dynamicsInserted);

    return ProjectImportResultVO.builder()
        .projectsInserted(projectsInserted)
        .projectsUpdated(projectsUpdated)
        .portraitsUpdated(portraitsUpdated)
        .financingInserted(financingInserted)
        .financingUpdated(financingUpdated)
        .membersInserted(membersInserted)
        .membersUpdated(membersUpdated)
        .membersSkipped(membersSkipped)
        .dynamicsInserted(dynamicsInserted)
        .dynamicsSkipped(dynamicsSkipped)
        .imagesSaved(imagesSaved[0])
        .warnings(warnings)
        .build();
  }

  private boolean upsertProjectFromInfo(
      String name,
      Map<String, String> row,
      Map<String, byte[]> imageMap,
      List<String> warnings,
      int[] imagesSaved,
      Map<String, Project> projectByName,
      Set<Long> projectsNeedTagSync) {
    Project existing = projectByName.get(name);
    if (existing == null) {
      existing = findProjectByName(name);
      if (existing != null) {
        projectByName.put(name, existing);
      }
    }
    String logoUrl =
        resolveProjectLogoUrl(
            name,
            firstNonBlank(row, "Logo", "项目Logo", "头像", "logo"),
            imageMap,
            warnings,
            imagesSaved);
    LocalDate investDate = parseDate(firstNonBlank(row, "投资日期"));
    String round = trim(firstNonBlank(row, "投资轮次", "融资轮次"));
    String amount = normalizeAmount(firstNonBlank(row, "投资金额", "融资金额"));
    String tags = trim(firstNonBlank(row, "标签"));
    String desc = trim(firstNonBlank(row, "一句话简介", "公司简介"));
    String source = trim(firstNonBlank(row, "来源"));

    Project patch =
        Project.builder()
            .name(name)
            .tags(tags)
            .companyDesc(desc)
            .round(round)
            .investmentAmount(amount)
            .latestRound(round)
            .latestAmount(amount)
            .latestFinancingDate(investDate)
            .importSource(source)
            .statusLabel(round)
            .isHot(1)
            .isFinancing(0)
            .isCertified(0)
            .status(1)
            .build();
    if (StringUtils.hasText(logoUrl)) {
      patch.setLogoUrl(logoUrl);
    }

    if (existing == null) {
      projectMapper.insert(patch);
      patch.setSlug("p-" + patch.getId());
      projectMapper.updateById(patch);
      projectByName.put(name, patch);
      if (StringUtils.hasText(tags)) {
        projectsNeedTagSync.add(patch.getId());
      }
      return false;
    }

    applyProjectPatch(existing, patch);
    projectMapper.updateById(existing);
    projectByName.put(name, existing);
    if (StringUtils.hasText(tags)) {
      projectsNeedTagSync.add(existing.getId());
    }
    return true;
  }

  private void applyProjectPatch(Project existing, Project patch) {
    if (StringUtils.hasText(patch.getTags())) {
      existing.setTags(patch.getTags());
    }
    if (StringUtils.hasText(patch.getCompanyDesc())) {
      existing.setCompanyDesc(patch.getCompanyDesc());
    }
    if (StringUtils.hasText(patch.getRound())) {
      existing.setRound(patch.getRound());
    }
    if (StringUtils.hasText(patch.getInvestmentAmount())) {
      existing.setInvestmentAmount(patch.getInvestmentAmount());
    }
    if (StringUtils.hasText(patch.getLatestRound())) {
      existing.setLatestRound(patch.getLatestRound());
    }
    if (StringUtils.hasText(patch.getLatestAmount())) {
      existing.setLatestAmount(patch.getLatestAmount());
    }
    if (patch.getLatestFinancingDate() != null) {
      existing.setLatestFinancingDate(patch.getLatestFinancingDate());
    }
    if (StringUtils.hasText(patch.getImportSource())) {
      existing.setImportSource(patch.getImportSource());
    }
    if (StringUtils.hasText(patch.getStatusLabel())) {
      existing.setStatusLabel(patch.getStatusLabel());
    }
    if (StringUtils.hasText(patch.getLogoUrl())) {
      existing.setLogoUrl(patch.getLogoUrl());
    }
    existing.setStatus(1);
  }

  private int[] upsertFinancingFromInfoRow(
      Project project,
      Long institutionId,
      Map<String, String> row,
      Map<String, Long> financingKeyToId,
      Map<String, List<Long>> dateRoundToProjectIds,
      Set<Long> projectsNeedFinancingRefresh) {
    LocalDate date = parseDate(firstNonBlank(row, "投资日期"));
    String round = trim(firstNonBlank(row, "投资轮次", "融资轮次"));
    String amount = normalizeAmount(firstNonBlank(row, "投资金额", "融资金额"));
    String source = trim(firstNonBlank(row, "来源"));
    if (!StringUtils.hasText(round)) {
      return new int[] {0, 0};
    }
    return upsertFinancing(
        project,
        institutionId,
        date,
        round,
        amount,
        source,
        firstNonBlank(row, "机构名称"),
        financingKeyToId,
        dateRoundToProjectIds,
        projectsNeedFinancingRefresh);
  }

  private boolean upsertFinancingFromSheetRow(
      Project project,
      Long institutionId,
      Map<String, String> row,
      Map<String, Long> financingKeyToId,
      Map<String, List<Long>> dateRoundToProjectIds,
      Set<Long> projectsNeedFinancingRefresh,
      List<String> warnings,
      Map<String, Long> institutionCache) {
    LocalDate date = parseDate(firstNonBlank(row, "获投时间", "投资日期", "融资时间"));
    String round = trim(firstNonBlank(row, "融资轮次", "投资轮次"));
    String amount = normalizeAmount(firstNonBlank(row, "融资金额", "投资金额"));
    String source = trim(firstNonBlank(row, "来源"));
    String investorsRaw = firstNonBlank(row, "投资方");
    int[] stats =
        upsertFinancing(
            project,
            institutionId,
            date,
            round,
            amount,
            source,
            investorsRaw,
            financingKeyToId,
            dateRoundToProjectIds,
            projectsNeedFinancingRefresh);
    Long financingId =
        financingKeyToId.get(financingKey(project.getId(), date, round, amount));
    if (financingId != null && StringUtils.hasText(investorsRaw)) {
      replaceInvestors(financingId, investorsRaw, institutionId, warnings, institutionCache);
    }
    return stats[1] > 0;
  }

  private int[] upsertFinancing(
      Project project,
      Long institutionId,
      LocalDate date,
      String round,
      String amount,
      String source,
      String investorsOrInstName,
      Map<String, Long> financingKeyToId,
      Map<String, List<Long>> dateRoundToProjectIds,
      Set<Long> projectsNeedFinancingRefresh) {
    String key = financingKey(project.getId(), date, round, amount);
    Long existingId = financingKeyToId.get(key);
    ProjectFinancing existing =
        existingId != null ? financingMapper.selectById(existingId) : null;
    ProjectFinancing entity =
        ProjectFinancing.builder()
            .projectId(project.getId())
            .financingDate(date)
            .round(round)
            .amount(blankToNull(amount))
            .amountCurrency("CNY")
            .source(source)
            .sortOrder(0)
            .isLatest(0)
            .build();

    if (existing == null) {
      financingMapper.insert(entity);
      financingKeyToId.put(key, entity.getId());
      registerDateRound(dateRoundToProjectIds, project.getId(), date, round);
      if (institutionId != null) {
        insertInvestor(entity.getId(), institutionId, investorsOrInstName, "follow");
      }
      projectsNeedFinancingRefresh.add(project.getId());
      return new int[] {1, 0};
    }

    existing.setFinancingDate(date);
    existing.setRound(round);
    existing.setAmount(blankToNull(amount));
    if (StringUtils.hasText(source)) {
      existing.setSource(source);
    }
    financingMapper.updateById(existing);
    financingKeyToId.put(key, existing.getId());
    registerDateRound(dateRoundToProjectIds, project.getId(), date, round);
    projectsNeedFinancingRefresh.add(project.getId());
    return new int[] {0, 1};
  }

  private void registerDateRound(
      Map<String, List<Long>> index, Long projectId, LocalDate date, String round) {
    if (date == null || !StringUtils.hasText(round)) {
      return;
    }
    String key = dateRoundKey(date, round);
    index.computeIfAbsent(key, k -> new ArrayList<>());
    List<Long> ids = index.get(key);
    if (!ids.contains(projectId)) {
      ids.add(projectId);
    }
  }

  private String dateRoundKey(LocalDate date, String round) {
    return (date != null ? date : "") + "|" + (round != null ? round : "");
  }

  private void indexExistingFinancings(
      Collection<Project> projects,
      Map<String, Long> financingKeyToId,
      Map<String, List<Long>> dateRoundToProjectIds) {
    List<Long> projectIds =
        projects.stream()
            .map(Project::getId)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
    if (projectIds.isEmpty()) {
      return;
    }
    Map<Long, Project> projectById = new HashMap<>();
    for (Project project : projects) {
      if (project.getId() != null) {
        projectById.put(project.getId(), project);
      }
    }
    final int chunkSize = 500;
    for (int offset = 0; offset < projectIds.size(); offset += chunkSize) {
      List<Long> chunk =
          projectIds.subList(offset, Math.min(offset + chunkSize, projectIds.size()));
      List<ProjectFinancing> financings =
          financingMapper.selectList(
              new LambdaQueryWrapper<ProjectFinancing>().in(ProjectFinancing::getProjectId, chunk));
      for (ProjectFinancing financing : financings) {
        Project project = projectById.get(financing.getProjectId());
        if (project == null) {
          continue;
        }
        financingKeyToId.put(
            financingKey(
                project.getId(),
                financing.getFinancingDate(),
                financing.getRound(),
                financing.getAmount()),
            financing.getId());
        registerDateRound(
            dateRoundToProjectIds,
            project.getId(),
            financing.getFinancingDate(),
            financing.getRound());
      }
    }
  }

  private void replaceInvestors(
      Long financingId,
      String raw,
      Long defaultInstitutionId,
      List<String> warnings,
      Map<String, Long> institutionCache) {
    investorMapper.delete(
        new LambdaQueryWrapper<ProjectFinancingInvestor>()
            .eq(ProjectFinancingInvestor::getFinancingId, financingId));
    List<InvestorLine> lines = parseInvestors(raw);
    if (lines.isEmpty() && defaultInstitutionId != null) {
      Institution inst = institutionMapper.selectById(defaultInstitutionId);
      if (inst != null) {
        insertInvestor(financingId, defaultInstitutionId, inst.getName(), "follow");
      }
      return;
    }
    for (InvestorLine line : lines) {
      Long instId = findInstitutionId(line.name(), institutionCache);
      if (instId == null) {
        ProjectFinancingInvestor inv =
            ProjectFinancingInvestor.builder()
                .financingId(financingId)
                .investorName(line.name())
                .investorRole(line.role())
                .build();
        investorMapper.insert(inv);
      } else {
        insertInvestor(financingId, instId, line.name(), line.role());
      }
    }
  }

  private void insertInvestor(Long financingId, Long institutionId, String name, String role) {
    investorMapper.insert(
        ProjectFinancingInvestor.builder()
            .financingId(financingId)
            .institutionId(institutionId)
            .investorName(name)
            .investorRole(role)
            .build());
  }

  private record InvestorLine(String name, String role) {}

  private List<InvestorLine> parseInvestors(String raw) {
    List<InvestorLine> lines = new ArrayList<>();
    if (!StringUtils.hasText(raw)) {
      return lines;
    }
    String normalized = raw.replace('\r', '\n');
    String currentRole = "follow";
    for (String line : normalized.split("\n")) {
      String trimmed = line.trim();
      if (!StringUtils.hasText(trimmed) || "-".equals(trimmed)) {
        continue;
      }
      if (trimmed.contains("领投")) {
        currentRole = "lead";
        continue;
      }
      if (trimmed.contains("跟投")) {
        currentRole = "follow";
        continue;
      }
      for (String part : trimmed.split("[,，、]")) {
        String name = part.trim();
        if (StringUtils.hasText(name) && !"-".equals(name)) {
          lines.add(new InvestorLine(name, currentRole));
        }
      }
    }
    if (lines.isEmpty()) {
      for (String part : raw.split("[,，、]")) {
        String name = part.trim();
        if (StringUtils.hasText(name)) {
          lines.add(new InvestorLine(name, "follow"));
        }
      }
    }
    return lines;
  }

  private boolean applyPortrait(
      Project project,
      Map<String, String> row,
      Map<String, byte[]> imageMap,
      List<String> warnings,
      int[] imagesSaved,
      Set<Long> projectsNeedTagSync) {
    String region = trim(firstNonBlank(row, "地区", "区域"));
    String category = cleanIndustry(firstNonBlank(row, "行业分类"));
    String track = cleanIndustry(firstNonBlank(row, "赛道"));
    String mergedTags = mergeTags(project.getTags(), track);
    String logoFilename =
        firstNonBlank(row, "项目头像", "头像", "项目Logo", "Logo", "logo");

    project.setRegion(region);
    project.setLocation(region != null ? region.trim() : null);
    if (StringUtils.hasText(category)) {
      project.setCategory(category);
    }
    project.setNationalEconomyIndustry(cleanIndustry(firstNonBlank(row, "国民经济产业")));
    project.setStrategicEmergingIndustry(cleanIndustry(firstNonBlank(row, "战略新兴产业")));
    project.setHighPrecisionIndustry(cleanIndustry(firstNonBlank(row, "高精尖产业")));
    project.setListingBoard(blankDashToNull(firstNonBlank(row, "上市板块")));
    project.setListingDate(parseDate(firstNonBlank(row, "上市日期")));
    project.setEmployeeCount(blankDashToNull(firstNonBlank(row, "雇员人数")));
    project.setManagerCount(blankDashToNull(firstNonBlank(row, "管理人员人数")));
    if (StringUtils.hasText(mergedTags)) {
      project.setTags(mergedTags);
    }
    if (StringUtils.hasText(logoFilename) || !imageMap.isEmpty()) {
      String logoUrl =
          resolveProjectLogoUrl(project.getName(), logoFilename, imageMap, warnings, imagesSaved);
      if (StringUtils.hasText(logoUrl)) {
        project.setLogoUrl(logoUrl);
      }
    }
    projectMapper.updateById(project);
    projectsNeedTagSync.add(project.getId());
    return true;
  }

  private String projectKeyFromRow(Map<String, String> row) {
    return trim(firstNonBlank(row, "项目简称", "被投公司简称", "被投公司", "项目名称"));
  }

  private String portraitProjectKey(Map<String, String> row) {
    return projectKeyFromRow(row);
  }

  private boolean upsertTeamMember(
      Project project,
      String memberName,
      Map<String, String> row,
      Map<String, byte[]> imageMap,
      int sortOrder,
      List<String> warnings,
      int[] imagesSaved) {
    ProjectTeamMember existing =
        teamMemberMapper.selectOne(
            new LambdaQueryWrapper<ProjectTeamMember>()
                .eq(ProjectTeamMember::getProjectId, project.getId())
                .eq(ProjectTeamMember::getMemberName, memberName)
                .last("LIMIT 1"));
    String avatar =
        resolveImageUrl(
            firstNonBlank(row, "头像", "头像名", "Logo"), imageMap, warnings, imagesSaved);
    String title = trim(firstNonBlank(row, "职位", "职务"));
    String bio = trim(firstNonBlank(row, "介绍", "人物简介"));

    ProjectTeamMember entity =
        ProjectTeamMember.builder()
            .projectId(project.getId())
            .memberName(memberName)
            .title(title)
            .bio(bio)
            .avatarUrl(avatar)
            .sortOrder(sortOrder)
            .build();

    if (existing == null) {
      teamMemberMapper.insert(entity);
      return false;
    }
    existing.setTitle(entity.getTitle());
    existing.setBio(entity.getBio());
    existing.setSortOrder(entity.getSortOrder());
    if (StringUtils.hasText(entity.getAvatarUrl())) {
      existing.setAvatarUrl(entity.getAvatarUrl());
    }
    teamMemberMapper.updateById(existing);
    return true;
  }

  private void insertDynamic(Long projectId, LocalDate eventDate, String content, int sortOrder) {
    dynamicMapper.insert(
        ProjectDynamic.builder()
            .projectId(projectId)
            .eventDate(eventDate)
            .content(content.trim())
            .sortOrder(sortOrder)
            .build());
  }

  private Project matchProjectForFinancingRow(
      Map<String, String> row,
      Map<String, Project> projectByName,
      Map<String, String> nameAliases,
      Map<String, Long> financingKeyToId,
      Map<String, List<Long>> dateRoundToProjectIds) {
    String explicit = projectKeyFromRow(row);
    if (StringUtils.hasText(explicit)) {
      return findProject(explicit, projectByName, nameAliases);
    }
    LocalDate date = parseDate(firstNonBlank(row, "获投时间", "投资日期"));
    String round = trim(firstNonBlank(row, "融资轮次"));
    String amount = normalizeAmount(firstNonBlank(row, "融资金额"));
    for (Project project : projectByName.values()) {
      String key = financingKey(project.getId(), date, round, amount);
      if (financingKeyToId.containsKey(key)) {
        return project;
      }
    }
    List<Long> dateRoundHits = dateRoundToProjectIds.get(dateRoundKey(date, round));
    if (dateRoundHits != null && dateRoundHits.size() == 1) {
      Project project = projectByName.values().stream()
          .filter(p -> p.getId().equals(dateRoundHits.get(0)))
          .findFirst()
          .orElse(null);
      if (project != null) {
        return project;
      }
      return projectMapper.selectById(dateRoundHits.get(0));
    }
    return null;
  }

  private Project matchProjectFromBio(
      Map<String, String> row,
      Map<String, Project> projectByName,
      Map<String, String> aliases,
      List<String> sortedMatchNames,
      List<String> sortedAliasKeys) {
    String bio = firstNonBlank(row, "介绍", "人物简介");
    return matchProjectFromText(bio, projectByName, aliases, sortedMatchNames, sortedAliasKeys);
  }

  private Project matchProjectFromText(
      String text,
      Map<String, Project> projectByName,
      Map<String, String> aliases,
      List<String> sortedMatchNames,
      List<String> sortedAliasKeys) {
    if (!StringUtils.hasText(text)) {
      return null;
    }
    List<String> hits = new ArrayList<>();
    for (String name : sortedMatchNames) {
      if (text.contains(name)) {
        hits.add(name);
        break;
      }
    }
    if (hits.isEmpty()) {
      for (String alias : sortedAliasKeys) {
        if (text.contains(alias)) {
          String canonical = aliases.get(alias);
          if (!hits.contains(canonical)) {
            hits.add(canonical);
            break;
          }
        }
      }
    }
    if (hits.size() == 1) {
      return projectByName.get(hits.get(0));
    }
    return null;
  }

  private List<String> buildSortedMatchNames(Map<String, Project> projectByName) {
    return projectByName.keySet().stream()
        .filter(name -> name != null && name.length() >= 2)
        .sorted((a, b) -> Integer.compare(b.length(), a.length()))
        .collect(Collectors.toList());
  }

  private List<String> buildSortedAliasKeys(Map<String, String> aliases) {
    return aliases.keySet().stream()
        .filter(alias -> alias != null && alias.length() >= 2)
        .sorted((a, b) -> Integer.compare(b.length(), a.length()))
        .collect(Collectors.toList());
  }

  private void flushTagSync(Set<Long> projectIds) {
    if (projectIds.isEmpty()) {
      return;
    }
    log.info("[project-import] 同步标签 {} 个项目", projectIds.size());
    for (Long projectId : projectIds) {
      Project project = projectMapper.selectById(projectId);
      if (project != null) {
        tagSyncService.syncFromTagsField(projectId, project.getTags());
      }
    }
  }

  private void refreshProjectIndex(Map<String, Project> projectByName, Map<String, String> aliases) {
    List<Project> allProjects =
        projectMapper.selectList(
            new LambdaQueryWrapper<Project>().select(Project::getId, Project::getName));
    for (Project p : allProjects) {
      projectByName.putIfAbsent(p.getName(), p);
      registerAliases(aliases, p.getName());
    }
  }

  private void registerAliases(Map<String, String> aliases, String name) {
    aliases.put(name, name);
    for (String prefix : List.of("苏州", "北京", "上海", "赣州", "杭州", "无锡")) {
      if (name.startsWith(prefix) && name.length() > prefix.length()) {
        aliases.putIfAbsent(name.substring(prefix.length()), name);
      }
    }
    if (name.contains("机器人") && name.endsWith("机器人")) {
      String shortName = name.replace("机器人", "").trim();
      if (shortName.length() >= 2) {
        aliases.putIfAbsent(shortName, name);
      }
    }
    if (name.equals("大晓机器人")) {
      aliases.putIfAbsent("大晓", name);
      aliases.putIfAbsent("大晓无限", name);
    }
  }

  private Project findProject(String key, Map<String, Project> byName, Map<String, String> aliases) {
    Project direct = byName.get(key);
    if (direct != null) {
      return direct;
    }
    String canonical = aliases.get(key);
    if (canonical != null) {
      return byName.get(canonical);
    }
    Project fromDb = findProjectByName(key);
    if (fromDb != null) {
      byName.putIfAbsent(fromDb.getName(), fromDb);
      registerAliases(aliases, fromDb.getName());
    }
    return fromDb;
  }

  private Project findProjectByName(String name) {
    return projectMapper.selectOne(
        new LambdaQueryWrapper<Project>().eq(Project::getName, name).last("LIMIT 1"));
  }

  private void refreshLatestFinancing(Long projectId) {
    List<ProjectFinancing> list =
        financingMapper.selectList(
            new LambdaQueryWrapper<ProjectFinancing>()
                .eq(ProjectFinancing::getProjectId, projectId)
                .orderByDesc(ProjectFinancing::getFinancingDate)
                .orderByDesc(ProjectFinancing::getId));
    if (list.isEmpty()) {
      return;
    }
    ProjectFinancing latest = list.get(0);
    financingMapper.update(
        null,
        new LambdaUpdateWrapper<ProjectFinancing>()
            .eq(ProjectFinancing::getProjectId, projectId)
            .set(ProjectFinancing::getIsLatest, 0));
    financingMapper.update(
        null,
        new LambdaUpdateWrapper<ProjectFinancing>()
            .eq(ProjectFinancing::getId, latest.getId())
            .set(ProjectFinancing::getIsLatest, 1));
    Project project = projectMapper.selectById(projectId);
    if (project != null) {
      project.setLatestRound(latest.getRound());
      project.setLatestAmount(latest.getAmount());
      project.setLatestFinancingDate(latest.getFinancingDate());
      project.setRound(latest.getRound());
      project.setInvestmentAmount(latest.getAmount());
      projectMapper.updateById(project);
    }
  }

  private String financingKey(Long projectId, LocalDate date, String round, String amount) {
    return projectId
        + "|"
        + (date != null ? date : "")
        + "|"
        + (round != null ? round : "")
        + "|"
        + (amount != null ? amount : "");
  }

  private Long resolveInstitutionId(
      String name, List<String> warnings, Map<String, Long> institutionCache) {
    if (!StringUtils.hasText(name)) {
      return null;
    }
    Long id = findInstitutionId(name.trim(), institutionCache);
    if (id == null) {
      warnings.add("未找到投资机构「" + name.trim() + "」，相关融资投资方仅保存名称");
    }
    return id;
  }

  private Long findInstitutionId(String name, Map<String, Long> institutionCache) {
    if (!StringUtils.hasText(name)) {
      return null;
    }
    String key = name.trim();
    if (institutionCache.containsKey(key)) {
      return institutionCache.get(key);
    }
    Institution byName =
        institutionMapper.selectOne(
            new LambdaQueryWrapper<Institution>().eq(Institution::getName, key).last("LIMIT 1"));
    if (byName != null) {
      institutionCache.put(key, byName.getId());
      return byName.getId();
    }
    Institution byEntity =
        institutionMapper.selectOne(
            new LambdaQueryWrapper<Institution>()
                .eq(Institution::getEntityName, key)
                .last("LIMIT 1"));
    Long id = byEntity != null ? byEntity.getId() : null;
    institutionCache.put(key, id);
    return id;
  }

  private String cleanIndustry(String raw) {
    if (!StringUtils.hasText(raw) || "None".equalsIgnoreCase(raw.trim())) {
      return null;
    }
    String s = raw.trim();
    if ("-".equals(s)) {
      return null;
    }
    int nl = s.indexOf('\n');
    if (nl > 0) {
      String first = s.substring(0, nl).trim();
      if (first.startsWith("鲸准行业") || first.startsWith("赛道") || first.startsWith("国民经济产业")) {
        s = s.substring(nl + 1).trim();
      } else {
        s = first;
      }
    }
    nl = s.indexOf('\n');
    if (nl > 0) {
      s = s.substring(0, nl).trim();
    }
    if (s.contains(" / ")) {
      s = s.substring(0, s.indexOf(" / ")).trim();
    }
    return s;
  }

  private String mergeTags(String existing, String track) {
    Set<String> set = new LinkedHashSet<>();
    if (StringUtils.hasText(existing)) {
      for (String p : existing.split("[,，]")) {
        if (StringUtils.hasText(p)) {
          set.add(p.trim());
        }
      }
    }
    if (StringUtils.hasText(track)) {
      for (String line : track.split("\n")) {
        String t = line.trim();
        if (StringUtils.hasText(t) && !t.startsWith("赛道")) {
          set.add(t);
        }
      }
    }
    return set.isEmpty() ? existing : String.join(",", set.stream().limit(24).toList());
  }

  private LocalDate parseDate(String raw) {
    if (!StringUtils.hasText(raw) || "-".equals(raw.trim())) {
      return null;
    }
    String s = raw.trim().replace('/', '-').replace('.', '-');
    if (s.length() >= 10 && Character.isDigit(s.charAt(0))) {
      s = s.substring(0, 10);
    }
    try {
      return LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE);
    } catch (DateTimeParseException ignored) {
      // fall through
    }
    Matcher m = DATE_IN_TEXT.matcher(raw);
    if (m.find()) {
      return LocalDate.of(
          Integer.parseInt(m.group(1)),
          Integer.parseInt(m.group(2)),
          Integer.parseInt(m.group(3)));
    }
    return null;
  }

  private LocalDate parseDateFromText(String text) {
    Matcher m = DATE_IN_TEXT.matcher(text);
    if (m.find()) {
      return LocalDate.of(
          Integer.parseInt(m.group(1)),
          Integer.parseInt(m.group(2)),
          Integer.parseInt(m.group(3)));
    }
    return null;
  }

  private String normalizeAmount(String raw) {
    if (!StringUtils.hasText(raw) || "-".equals(raw.trim())) {
      return "";
    }
    return raw.trim().replace(",", "");
  }

  private String blankDashToNull(String raw) {
    if (!StringUtils.hasText(raw) || "-".equals(raw.trim()) || "None".equalsIgnoreCase(raw.trim())) {
      return null;
    }
    return raw.trim();
  }

  private String blankToNull(String raw) {
    return StringUtils.hasText(raw) ? raw : null;
  }

  private String resolveImageUrl(
      String filename, Map<String, byte[]> imageMap, List<String> warnings, int[] imagesSaved) {
    if (!StringUtils.hasText(filename)) {
      return null;
    }
    String trimmed = filename.trim();
    byte[] content = lookupImageBytes(trimmed, imageMap);
    if (content == null) {
      warnings.add("未找到项目 Logo: " + trimmed);
      return null;
    }
    imagesSaved[0]++;
    return fileStorageService.storeImportedAsset("project-import", trimmed, content);
  }

  private String resolveProjectLogoUrl(
      String projectName,
      String columnFilename,
      Map<String, byte[]> imageMap,
      List<String> warnings,
      int[] imagesSaved) {
    if (StringUtils.hasText(columnFilename)) {
      String trimmed = columnFilename.trim();
      if (trimmed.startsWith("/uploads/")
          || trimmed.startsWith("http://")
          || trimmed.startsWith("https://")) {
        return trimmed;
      }
      return resolveImageUrl(columnFilename, imageMap, warnings, imagesSaved);
    }
    if (!StringUtils.hasText(projectName) || imageMap.isEmpty()) {
      return null;
    }
    String matchedFilename = findImageFilenameForProject(projectName, imageMap);
    if (!StringUtils.hasText(matchedFilename)) {
      return null;
    }
    byte[] content = lookupImageBytes(matchedFilename, imageMap);
    if (content == null) {
      return null;
    }
    imagesSaved[0]++;
    return fileStorageService.storeImportedAsset("project-import", matchedFilename, content);
  }

  private void applyUnmatchedProjectLogos(
      Collection<Project> projects,
      Map<String, byte[]> imageMap,
      List<String> warnings,
      int[] imagesSaved) {
    for (Project project : projects) {
      if (project == null || project.getId() == null || StringUtils.hasText(project.getLogoUrl())) {
        continue;
      }
      String logoUrl =
          resolveProjectLogoUrl(project.getName(), null, imageMap, warnings, imagesSaved);
      if (!StringUtils.hasText(logoUrl)) {
        continue;
      }
      project.setLogoUrl(logoUrl);
      projectMapper.updateById(project);
    }
  }

  private byte[] lookupImageBytes(String filename, Map<String, byte[]> imageMap) {
    if (!StringUtils.hasText(filename)) {
      return null;
    }
    String trimmed = filename.trim();
    byte[] content = imageMap.get(trimmed);
    if (content == null) {
      content = imageMap.get(trimmed.toLowerCase(Locale.ROOT));
    }
    return content;
  }

  private String findImageFilenameForProject(String projectName, Map<String, byte[]> imageMap) {
    String trimmed = projectName.trim();
    for (String ext : List.of(".png", ".jpg", ".jpeg", ".webp", ".gif")) {
      for (String candidate : List.of(trimmed + ext, (trimmed + ext).toLowerCase(Locale.ROOT))) {
        if (lookupImageBytes(candidate, imageMap) != null) {
          return candidate;
        }
      }
    }
    Set<String> checked = new HashSet<>();
    for (String filename : imageMap.keySet()) {
      if (!checked.add(filename)) {
        continue;
      }
      int dot = filename.lastIndexOf('.');
      if (dot <= 0) {
        continue;
      }
      String stem = filename.substring(0, dot);
      if (trimmed.equals(stem) || trimmed.equalsIgnoreCase(stem)) {
        return filename;
      }
    }
    return null;
  }

  private Map<String, byte[]> loadImages(
      MultipartFile imagesZip, MultipartFile[] imageFiles, List<String> warnings) {
    Map<String, byte[]> map = new HashMap<>();
    if (!isEmpty(imagesZip)) {
      map.putAll(loadImagesZip(imagesZip, warnings));
    }
    if (imageFiles != null) {
      for (MultipartFile file : imageFiles) {
        if (file == null || file.isEmpty()) {
          continue;
        }
        String filename = file.getOriginalFilename();
        if (!StringUtils.hasText(filename)) {
          continue;
        }
        String base = filename.replace("\\", "/");
        int slash = base.lastIndexOf('/');
        if (slash >= 0) {
          base = base.substring(slash + 1);
        }
        try {
          byte[] bytes = file.getBytes();
          if (bytes.length > 0) {
            map.put(base, bytes);
            map.put(base.toLowerCase(Locale.ROOT), bytes);
          }
        } catch (IOException e) {
          warnings.add("读取图片失败: " + base);
        }
      }
    }
    return map;
  }

  private Map<String, byte[]> loadImagesZip(MultipartFile imagesZip, List<String> warnings) {
    Map<String, byte[]> map = new HashMap<>();
    if (isEmpty(imagesZip)) {
      return map;
    }
    try (ZipInputStream zis = new ZipInputStream(imagesZip.getInputStream())) {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        if (entry.isDirectory()) {
          continue;
        }
        String name = entry.getName().replace("\\", "/");
        int slash = name.lastIndexOf('/');
        String filename = slash >= 0 ? name.substring(slash + 1) : name;
        if (!StringUtils.hasText(filename) || filename.startsWith(".")) {
          continue;
        }
        byte[] bytes = readAll(zis);
        map.put(filename, bytes);
        map.put(filename.toLowerCase(Locale.ROOT), bytes);
      }
    } catch (IOException e) {
      throw new BusinessException("Logo 压缩包解析失败");
    }
    return map;
  }

  private List<Map<String, String>> parseSheet(MultipartFile file, List<String> warnings) {
    try (InputStream in = file.getInputStream();
        Workbook workbook = WorkbookFactory.create(in)) {
      Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
      if (sheet == null) {
        throw new BusinessException("Excel 无有效工作表: " + file.getOriginalFilename());
      }
      Row headerRow = sheet.getRow(sheet.getFirstRowNum());
      if (headerRow == null) {
        throw new BusinessException("Excel 表头为空: " + file.getOriginalFilename());
      }
      List<String> headers = readHeaders(headerRow);
      List<Map<String, String>> rows = new ArrayList<>();
      for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
        Row row = sheet.getRow(i);
        if (row == null || isRowEmpty(row, headers.size())) {
          continue;
        }
        Map<String, String> data = new LinkedHashMap<>();
        for (int c = 0; c < headers.size(); c++) {
          String header = headers.get(c);
          if (!StringUtils.hasText(header)) {
            continue;
          }
          data.put(header.trim(), cellText(row.getCell(c)));
        }
        rows.add(data);
      }
      return rows;
    } catch (BusinessException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new BusinessException("Excel 解析失败: " + file.getOriginalFilename());
    }
  }

  private List<String> readHeaders(Row headerRow) {
    List<String> headers = new ArrayList<>();
    for (int c = 0; c < headerRow.getLastCellNum(); c++) {
      headers.add(cellText(headerRow.getCell(c)));
    }
    return headers;
  }

  private boolean isRowEmpty(Row row, int colCount) {
    for (int c = 0; c < colCount; c++) {
      if (StringUtils.hasText(cellText(row.getCell(c)))) {
        return false;
      }
    }
    return true;
  }

  private String cellText(Cell cell) {
    if (cell == null) {
      return "";
    }
    return FORMATTER.formatCellValue(cell).trim();
  }

  private byte[] readAll(InputStream in) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buf = new byte[8192];
    int len;
    while ((len = in.read(buf)) != -1) {
      out.write(buf, 0, len);
    }
    return out.toByteArray();
  }

  private String firstNonBlank(Map<String, String> row, String... keys) {
    for (String key : keys) {
      String value = row.get(key);
      if (StringUtils.hasText(value)) {
        return value.trim();
      }
    }
    return "";
  }

  private String cleanMemberName(String raw) {
    if (!StringUtils.hasText(raw)) {
      return "";
    }
    String firstLine = raw.split("\\R")[0].trim();
    return firstLine;
  }

  private String truncate(String value, int max) {
    if (!StringUtils.hasText(value) || value.length() <= max) {
      return value;
    }
    return value.substring(0, max) + "…";
  }

  private String trim(String value) {
    return StringUtils.hasText(value) ? value.trim() : null;
  }

  private boolean isEmpty(MultipartFile file) {
    return file == null || file.isEmpty();
  }
}
