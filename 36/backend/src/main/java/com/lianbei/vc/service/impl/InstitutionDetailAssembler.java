package com.lianbei.vc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianbei.vc.dto.response.InstitutionDetailVO;
import com.lianbei.vc.entity.Institution;
import com.lianbei.vc.entity.InstitutionFundManager;
import com.lianbei.vc.entity.InstitutionTeamMember;
import com.lianbei.vc.entity.Project;
import com.lianbei.vc.entity.ProjectFinancing;
import com.lianbei.vc.entity.ProjectFinancingInvestor;
import com.lianbei.vc.mapper.InstitutionFundManagerMapper;
import com.lianbei.vc.mapper.InstitutionTeamMemberMapper;
import com.lianbei.vc.mapper.ProjectFinancingInvestorMapper;
import com.lianbei.vc.mapper.ProjectFinancingMapper;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class InstitutionDetailAssembler {

  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

  private final ObjectMapper objectMapper;
  private final InstitutionTeamMemberMapper teamMemberMapper;
  private final InstitutionFundManagerMapper fundManagerMapper;
  private final ProjectFinancingMapper financingMapper;
  private final ProjectFinancingInvestorMapper financingInvestorMapper;
  private final InstitutionInvestmentCounter investmentCounter;
  private Map<String, JsonNode> institutionExtByName;
  private Map<String, JsonNode> projectExtByName;

  public InstitutionDetailAssembler(
      ObjectMapper objectMapper,
      InstitutionTeamMemberMapper teamMemberMapper,
      InstitutionFundManagerMapper fundManagerMapper,
      ProjectFinancingMapper financingMapper,
      ProjectFinancingInvestorMapper financingInvestorMapper,
      InstitutionInvestmentCounter investmentCounter) {
    this.objectMapper = objectMapper;
    this.teamMemberMapper = teamMemberMapper;
    this.fundManagerMapper = fundManagerMapper;
    this.financingMapper = financingMapper;
    this.financingInvestorMapper = financingInvestorMapper;
    this.investmentCounter = investmentCounter;
  }

  public InstitutionDetailVO assemble(Institution institution, List<Project> allProjects) {
    JsonNode ext = loadInstitutionExt().get(institution.getName());
    List<String> instTypes = parseInstTypes(institution, ext);
    List<String> fields = parseInvestmentFields(institution, ext);
    List<InstitutionDetailVO.InvestmentEventVO> events =
        buildInvestmentEvents(institution, ext, allProjects);
    List<InstitutionDetailVO.TeamMemberVO> teamMembers = listTeamMembers(institution, ext);

    int eventCount = investmentCounter.countDistinctProjects(institution);

    return InstitutionDetailVO.builder()
        .id(institution.getId())
        .name(institution.getName())
        .entityName(institution.getEntityName())
        .logoUrl(institution.getLogoUrl())
        .instTypes(instTypes)
        .foundedYear(institution.getFoundedYear())
        .region(text(ext, "region", ""))
        .eventCount(eventCount)
        .manageScale(
            StringUtils.hasText(institution.getManageScale())
                ? institution.getManageScale()
                : text(ext, "manageScale", "-"))
        .teamCount(text(ext, "teamCount", "-"))
        .intro(
            StringUtils.hasText(institution.getIntro())
                ? institution.getIntro()
                : text(ext, "intro", defaultIntro(institution)))
        .investmentFields(fields)
        .investmentEvents(events)
        .businessInfo(parseBusinessInfo(institution, ext))
        .fundManagers(listFundManagers(institution, ext))
        .teamMembers(teamMembers)
        .contact(parseContact(institution, ext))
        .build();
  }

  private List<InstitutionDetailVO.TeamMemberVO> listTeamMembers(
      Institution institution, JsonNode ext) {
    List<InstitutionTeamMember> rows =
        teamMemberMapper.selectList(
            new LambdaQueryWrapper<InstitutionTeamMember>()
                .eq(InstitutionTeamMember::getInstitutionId, institution.getId())
                .orderByAsc(InstitutionTeamMember::getSortOrder)
                .orderByAsc(InstitutionTeamMember::getId));
    if (!rows.isEmpty()) {
      List<InstitutionDetailVO.TeamMemberVO> list = new ArrayList<>();
      for (InstitutionTeamMember row : rows) {
        list.add(
            InstitutionDetailVO.TeamMemberVO.builder()
                .name(row.getMemberName())
                .title(StringUtils.hasText(row.getTitle()) ? row.getTitle() : "")
                .avatar(row.getAvatar() != null ? row.getAvatar() : "")
                .bio(row.getBio() != null ? row.getBio() : "")
                .build());
      }
      return list;
    }
    return parseTeamMembers(ext);
  }

  private List<InstitutionDetailVO.InvestmentEventVO> buildInvestmentEvents(
      Institution institution, JsonNode ext, List<Project> allProjects) {
    Map<String, InstitutionDetailVO.InvestmentEventVO> eventMap = new LinkedHashMap<>();
    Set<String> matchNames = buildMatchNames(institution, ext);
    Map<Long, Project> projectById =
        allProjects.stream()
            .collect(Collectors.toMap(Project::getId, p -> p, (a, b) -> a, LinkedHashMap::new));

    appendDbInvestmentEvents(institution.getId(), matchNames, projectById, eventMap);

    for (Project project : allProjects) {
      JsonNode projectExt = loadProjectExt().get(project.getName());
      if (projectExt != null
          && projectExt.has("financingHistory")
          && projectExt.get("financingHistory").isArray()) {
        for (JsonNode node : projectExt.get("financingHistory")) {
          if (!investorMatched(node, matchNames)) {
            continue;
          }
          String round = node.path("round").asText(project.getRound());
          String date = node.path("date").asText(formatProjectDate(project));
          String key = eventDedupeKey(project.getId(), round, date);
          if (eventMap.containsKey(key)) {
            continue;
          }
          eventMap.put(
              key,
              InstitutionDetailVO.InvestmentEventVO.builder()
                  .projectId(project.getId())
                  .companyName(project.getName())
                  .logoUrl(project.getLogoUrl())
                  .round(round)
                  .industry(resolveIndustry(project))
                  .description(project.getCompanyDesc())
                  .date(date)
                  .amount(
                      node.path("amount").asText(
                          StringUtils.hasText(project.getInvestmentAmount())
                              ? project.getInvestmentAmount()
                              : "未透露"))
                  .build());
        }
      }
    }

    appendRecentInvestmentFallback(institution, allProjects, eventMap);

    List<InstitutionDetailVO.InvestmentEventVO> events = new ArrayList<>(eventMap.values());
    events.sort(Comparator.comparing(InstitutionDetailVO.InvestmentEventVO::getDate).reversed());
    return events;
  }

  /** 从 project_financing + project_financing_investor 组装投资事件（Excel 导入数据走此路径） */
  private void appendDbInvestmentEvents(
      Long institutionId,
      Set<String> matchNames,
      Map<Long, Project> projectById,
      Map<String, InstitutionDetailVO.InvestmentEventVO> eventMap) {
    Set<Long> financingIds = findFinancingIdsForInstitution(institutionId, matchNames);
    for (Long financingId : financingIds) {
      ProjectFinancing financing = financingMapper.selectById(financingId);
      if (financing == null) {
        continue;
      }
      Project project = projectById.get(financing.getProjectId());
      if (project == null) {
        continue;
      }
      String round =
          StringUtils.hasText(financing.getRound()) ? financing.getRound() : project.getRound();
      String date = formatFinancingEventDate(financing.getFinancingDate(), project);
      String key = eventDedupeKey(project.getId(), round, date);
      if (eventMap.containsKey(key)) {
        continue;
      }
      eventMap.put(
          key,
          InstitutionDetailVO.InvestmentEventVO.builder()
              .projectId(project.getId())
              .companyName(project.getName())
              .logoUrl(project.getLogoUrl())
              .round(round != null ? round : "")
              .industry(resolveIndustry(project))
              .description(project.getCompanyDesc())
              .date(date)
              .amount(
                  StringUtils.hasText(financing.getAmount())
                      ? financing.getAmount()
                      : (StringUtils.hasText(project.getInvestmentAmount())
                          ? project.getInvestmentAmount()
                          : "未透露"))
              .build());
    }
  }

  private Set<Long> findFinancingIdsForInstitution(Long institutionId, Set<String> matchNames) {
    Set<Long> financingIds = new LinkedHashSet<>();
    if (institutionId != null) {
      financingInvestorMapper
          .selectList(
              new LambdaQueryWrapper<ProjectFinancingInvestor>()
                  .eq(ProjectFinancingInvestor::getInstitutionId, institutionId))
          .forEach(row -> financingIds.add(row.getFinancingId()));
    }
    for (String name : matchNames) {
      if (!StringUtils.hasText(name)) {
        continue;
      }
      String trimmed = name.trim();
      financingInvestorMapper
          .selectList(
              new LambdaQueryWrapper<ProjectFinancingInvestor>()
                  .eq(ProjectFinancingInvestor::getInvestorName, trimmed))
          .forEach(row -> financingIds.add(row.getFinancingId()));
    }
    return financingIds;
  }

  private String eventDedupeKey(Long projectId, String round, String date) {
    return projectId
        + "|"
        + (round != null ? round : "")
        + "|"
        + (date != null ? date : "");
  }

  private String resolveIndustry(Project project) {
    return StringUtils.hasText(project.getCategory())
        ? project.getCategory()
        : firstTag(project.getTags());
  }

  private String formatFinancingEventDate(LocalDate financingDate, Project project) {
    if (financingDate != null) {
      return DATE_FMT.format(financingDate);
    }
    return formatProjectDate(project);
  }

  private void appendRecentInvestmentFallback(
      Institution institution,
      List<Project> allProjects,
      Map<String, InstitutionDetailVO.InvestmentEventVO> eventMap) {
    if (!StringUtils.hasText(institution.getRecentInvestment())) {
      return;
    }
    for (Project project : allProjects) {
      if (!institution.getRecentInvestment().equals(project.getName())) {
        continue;
      }
      boolean exists =
          eventMap.values().stream()
              .anyMatch(e -> project.getId().equals(e.getProjectId()));
      if (exists) {
        return;
      }
      String key = eventDedupeKey(project.getId(), project.getRound(), formatProjectDate(project));
      if (eventMap.containsKey(key)) {
        return;
      }
      eventMap.put(
          key,
          InstitutionDetailVO.InvestmentEventVO.builder()
              .projectId(project.getId())
              .companyName(project.getName())
              .logoUrl(project.getLogoUrl())
              .round(project.getRound())
              .industry(resolveIndustry(project))
              .description(project.getCompanyDesc())
              .date(formatProjectDate(project))
              .amount(
                  StringUtils.hasText(project.getInvestmentAmount())
                      ? project.getInvestmentAmount()
                      : "未透露")
              .build());
      return;
    }
  }

  private boolean investorMatched(JsonNode financingNode, Set<String> matchNames) {
    if (!financingNode.has("investors") || !financingNode.get("investors").isArray()) {
      return false;
    }
    for (JsonNode inv : financingNode.get("investors")) {
      String investor = inv.asText("");
      if (!StringUtils.hasText(investor)) {
        continue;
      }
      for (String name : matchNames) {
        if (investor.equals(name)
            || investor.contains(name)
            || name.contains(investor)) {
          return true;
        }
      }
    }
    return false;
  }

  private Set<String> buildMatchNames(Institution institution, JsonNode ext) {
    Set<String> names = new HashSet<>();
    if (StringUtils.hasText(institution.getName())) {
      names.add(institution.getName().trim());
    }
    if (StringUtils.hasText(institution.getEntityName())) {
      names.add(institution.getEntityName().trim());
    }
    if (ext != null && ext.has("aliases") && ext.get("aliases").isArray()) {
      ext.get("aliases").forEach(n -> names.add(n.asText().trim()));
    }
    return names;
  }

  private List<String> parseInstTypes(Institution institution, JsonNode ext) {
    if (ext != null && ext.has("instTypes") && ext.get("instTypes").isArray()) {
      List<String> list = new ArrayList<>();
      ext.get("instTypes").forEach(n -> list.add(n.asText()));
      if (!list.isEmpty()) {
        return list;
      }
    }
    if (StringUtils.hasText(institution.getInstType())) {
      return List.of(institution.getInstType());
    }
    return List.of();
  }

  private List<String> parseInvestmentFields(Institution institution, JsonNode ext) {
    if (StringUtils.hasText(institution.getInvestmentFields())) {
      List<String> list = new ArrayList<>();
      for (String part : institution.getInvestmentFields().split("[,，]")) {
        if (StringUtils.hasText(part)) {
          list.add(part.trim());
        }
      }
      if (!list.isEmpty()) {
        return list;
      }
    }
    if (ext != null && ext.has("investmentFields") && ext.get("investmentFields").isArray()) {
      List<String> list = new ArrayList<>();
      ext.get("investmentFields").forEach(n -> list.add(n.asText()));
      if (!list.isEmpty()) {
        return list;
      }
    }
    if (StringUtils.hasText(institution.getInvestmentField())) {
      return List.of(institution.getInvestmentField());
    }
    return List.of();
  }

  private InstitutionDetailVO.BusinessInfoVO parseBusinessInfo(
      Institution institution, JsonNode ext) {
    if (ext != null && ext.has("businessInfo")) {
      JsonNode b = ext.get("businessInfo");
      return InstitutionDetailVO.BusinessInfoVO.builder()
          .fullName(b.path("fullName").asText(institution.getEntityName()))
          .legalPerson(b.path("legalPerson").asText("-"))
          .establishDate(b.path("establishDate").asText(institution.getFoundedYear()))
          .address(b.path("address").asText("-"))
          .build();
    }
    if (StringUtils.hasText(institution.getEntityName())) {
      return InstitutionDetailVO.BusinessInfoVO.builder()
          .fullName(institution.getEntityName())
          .legalPerson("-")
          .establishDate(institution.getFoundedYear())
          .address("-")
          .build();
    }
    return null;
  }

  private List<InstitutionDetailVO.FundManagerVO> listFundManagers(
      Institution institution, JsonNode ext) {
    List<InstitutionFundManager> rows =
        fundManagerMapper.selectList(
            new LambdaQueryWrapper<InstitutionFundManager>()
                .eq(InstitutionFundManager::getInstitutionId, institution.getId())
                .orderByAsc(InstitutionFundManager::getSortOrder)
                .orderByAsc(InstitutionFundManager::getId));
    if (!rows.isEmpty()) {
      List<InstitutionDetailVO.FundManagerVO> list = new ArrayList<>();
      for (InstitutionFundManager row : rows) {
        list.add(toFundManagerVO(row));
      }
      return list;
    }
    return parseFundManagers(ext);
  }

  private InstitutionDetailVO.FundManagerVO toFundManagerVO(InstitutionFundManager row) {
    String fullName = textOrEmpty(row.getFullName());
    return InstitutionDetailVO.FundManagerVO.builder()
        .name(fullName)
        .entityName(fullName)
        .fullName(fullName)
        .legalPerson(textOrEmpty(row.getLegalPerson()))
        .instType(textOrEmpty(row.getInstType()))
        .officeAddress(textOrEmpty(row.getOfficeAddress()))
        .registeredCapital(textOrEmpty(row.getRegisteredCapital()))
        .paidInCapital(textOrEmpty(row.getPaidInCapital()))
        .paidInRatio(textOrEmpty(row.getPaidInRatio()))
        .registrationNo(textOrEmpty(row.getRegistrationNo()))
        .establishDate(textOrEmpty(row.getEstablishDate()))
        .registerDate(textOrEmpty(row.getRegisterDate()))
        .build();
  }

  private List<InstitutionDetailVO.FundManagerVO> parseFundManagers(JsonNode ext) {
    if (ext == null || !ext.has("fundManagers") || !ext.get("fundManagers").isArray()) {
      return List.of();
    }
    List<InstitutionDetailVO.FundManagerVO> list = new ArrayList<>();
    ext.get("fundManagers")
        .forEach(
            n -> {
              String fullName =
                  firstText(n, "fullName", "full_name", "name", "entityName", "entity_name");
              list.add(
                  InstitutionDetailVO.FundManagerVO.builder()
                      .name(fullName)
                      .entityName(firstText(n, "entityName", "entity_name", "fullName", "full_name", "name"))
                      .fullName(fullName)
                      .legalPerson(firstText(n, "legalPerson", "legal_person"))
                      .instType(firstText(n, "instType", "inst_type"))
                      .officeAddress(firstText(n, "officeAddress", "office_address", "address"))
                      .registeredCapital(firstText(n, "registeredCapital", "registered_capital"))
                      .paidInCapital(firstText(n, "paidInCapital", "paid_in_capital"))
                      .paidInRatio(firstText(n, "paidInRatio", "paid_in_ratio"))
                      .registrationNo(firstText(n, "registrationNo", "registration_no"))
                      .establishDate(firstText(n, "establishDate", "establish_date"))
                      .registerDate(firstText(n, "registerDate", "register_date"))
                      .build());
            });
    return list;
  }

  private String firstText(JsonNode node, String... fields) {
    for (String field : fields) {
      if (node.has(field) && !node.get(field).isNull()) {
        String val = node.get(field).asText("").trim();
        if (StringUtils.hasText(val)) {
          return val;
        }
      }
    }
    return "";
  }

  private String textOrEmpty(String value) {
    return StringUtils.hasText(value) ? value.trim() : "";
  }

  private List<InstitutionDetailVO.TeamMemberVO> parseTeamMembers(JsonNode ext) {
    if (ext == null || !ext.has("teamMembers") || !ext.get("teamMembers").isArray()) {
      return List.of();
    }
    List<InstitutionDetailVO.TeamMemberVO> list = new ArrayList<>();
    ext.get("teamMembers")
        .forEach(
            n ->
                list.add(
                    InstitutionDetailVO.TeamMemberVO.builder()
                        .name(n.path("name").asText(""))
                        .title(n.path("title").asText(""))
                        .avatar(n.path("avatar").asText(""))
                        .bio(n.path("bio").asText(""))
                        .build()));
    return list;
  }

  private InstitutionDetailVO.ContactVO parseContact(Institution institution, JsonNode ext) {
    String website =
        StringUtils.hasText(institution.getWebsite())
            ? institution.getWebsite()
            : (ext != null && ext.has("contact")
                ? ext.get("contact").path("website").asText("-")
                : "-");
    if (ext == null || !ext.has("contact")) {
      return InstitutionDetailVO.ContactVO.builder()
          .website(website)
          .phone("-")
          .email("-")
          .address("-")
          .build();
    }
    JsonNode c = ext.get("contact");
    return InstitutionDetailVO.ContactVO.builder()
        .website(website)
        .phone(c.path("phone").asText("-"))
        .email(c.path("email").asText("-"))
        .address(c.path("address").asText("-"))
        .build();
  }

  private String formatProjectDate(Project project) {
    if (project.getCreateTime() != null) {
      return DATE_FMT.format(project.getCreateTime());
    }
    return "";
  }

  private String firstTag(String tags) {
    if (!StringUtils.hasText(tags)) {
      return "";
    }
    return tags.split("[,，]")[0].trim();
  }

  private int safeCount(Integer count) {
    return count != null ? count : 0;
  }

  private String defaultIntro(Institution institution) {
    return String.format(
        "%s是创投平台收录的投资机构，专注于%s领域，持续发掘优质创业项目。",
        institution.getName(),
        StringUtils.hasText(institution.getInvestmentField())
            ? institution.getInvestmentField()
            : "多赛道");
  }

  private String text(JsonNode ext, String field, String fallback) {
    if (ext != null && ext.has(field) && !ext.get(field).isNull()) {
      String val = ext.get(field).asText();
      if (StringUtils.hasText(val)) {
        return val;
      }
    }
    return fallback != null ? fallback : "";
  }

  private Map<String, JsonNode> loadInstitutionExt() {
    if (institutionExtByName != null) {
      return institutionExtByName;
    }
    institutionExtByName = loadJsonConfig("config/institution-detail-ext.json");
    return institutionExtByName;
  }

  private Map<String, JsonNode> loadProjectExt() {
    if (projectExtByName != null) {
      return projectExtByName;
    }
    projectExtByName = loadJsonConfig("config/project-detail-ext.json");
    return projectExtByName;
  }

  private Map<String, JsonNode> loadJsonConfig(String path) {
    try {
      ClassPathResource resource = new ClassPathResource(path);
      Map<String, Object> raw =
          objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
      return raw.entrySet().stream()
          .collect(Collectors.toMap(Map.Entry::getKey, e -> objectMapper.valueToTree(e.getValue())));
    } catch (IOException e) {
      return Collections.emptyMap();
    }
  }
}
