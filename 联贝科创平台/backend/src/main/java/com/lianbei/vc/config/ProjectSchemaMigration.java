package com.lianbei.vc.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianbei.vc.entity.Project;
import com.lianbei.vc.mapper.ProjectMapper;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 开发环境：项目库 V2 表结构迁移 + 从 project-detail-ext.json 导入种子数据。
 */
@Component
@Profile("dev")
public class ProjectSchemaMigration implements org.springframework.beans.factory.InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(ProjectSchemaMigration.class);

  private final JdbcTemplate jdbcTemplate;
  private final ObjectMapper objectMapper;
  private final ProjectMapper projectMapper;

  public ProjectSchemaMigration(
      JdbcTemplate jdbcTemplate, ObjectMapper objectMapper, ProjectMapper projectMapper) {
    this.jdbcTemplate = jdbcTemplate;
    this.objectMapper = objectMapper;
    this.projectMapper = projectMapper;
  }

  @Override
  public void afterPropertiesSet() {
    try {
      ensureSchema();
      backfillNewsProjectId();
      seedFromJsonIfNeeded();
      migrateTagsIfNeeded();
      alignProjectBusinessRecords();
    } catch (Exception ex) {
      log.warn("[schema] project v2 migration skipped: {}", ex.getMessage());
    }
  }

  private void ensureSchema() {
    if (!tableExists("project")) {
      return;
    }
    addColumnIfMissing(
        "project",
        "slug",
        "VARCHAR(128) DEFAULT NULL COMMENT '稳定标识' AFTER name");
    addColumnIfMissing(
        "project",
        "intro",
        "TEXT DEFAULT NULL COMMENT '详情介绍' AFTER company_desc");
    addColumnIfMissing(
        "project",
        "is_certified",
        "TINYINT NOT NULL DEFAULT 0 COMMENT '是否认证' AFTER is_hot");
    addColumnIfMissing(
        "project",
        "status",
        "TINYINT NOT NULL DEFAULT 1 COMMENT '0草稿 1已发布 2下架' AFTER is_certified");
    addColumnIfMissing(
        "project",
        "latest_round",
        "VARCHAR(64) DEFAULT NULL COMMENT '最新轮次' AFTER status");
    addColumnIfMissing(
        "project",
        "latest_amount",
        "VARCHAR(64) DEFAULT NULL COMMENT '最新融资金额' AFTER latest_round");
    addColumnIfMissing(
        "project",
        "latest_financing_date",
        "DATE DEFAULT NULL COMMENT '最新融资日期' AFTER latest_amount");
    addIndexIfMissing("project", "uk_project_slug", "UNIQUE KEY uk_project_slug (slug)");

    createTableIfMissing(
        """
        CREATE TABLE project_business (
          id BIGINT PRIMARY KEY,
          project_id BIGINT NOT NULL,
          full_name VARCHAR(255) DEFAULT NULL,
          english_name VARCHAR(255) DEFAULT NULL,
          legal_person VARCHAR(64) DEFAULT NULL,
          registered_address VARCHAR(512) DEFAULT NULL,
          establish_date DATE DEFAULT NULL,
          unified_social_credit_code VARCHAR(32) DEFAULT NULL,
          data_source VARCHAR(32) DEFAULT 'import',
          verified_at DATETIME DEFAULT NULL,
          create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
          update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
          UNIQUE KEY uk_business_project (project_id),
          KEY idx_business_full_name (full_name)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        """);

    createTableIfMissing(
        """
        CREATE TABLE project_financing (
          id BIGINT PRIMARY KEY AUTO_INCREMENT,
          project_id BIGINT NOT NULL,
          financing_date DATE DEFAULT NULL,
          round VARCHAR(64) NOT NULL,
          amount VARCHAR(64) DEFAULT NULL,
          amount_currency VARCHAR(16) DEFAULT 'CNY',
          is_latest TINYINT NOT NULL DEFAULT 0,
          sort_order INT NOT NULL DEFAULT 0,
          create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
          KEY idx_financing_project (project_id, sort_order)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        """);

    createTableIfMissing(
        """
        CREATE TABLE project_financing_investor (
          id BIGINT PRIMARY KEY AUTO_INCREMENT,
          financing_id BIGINT NOT NULL,
          institution_id BIGINT DEFAULT NULL,
          investor_name VARCHAR(128) NOT NULL,
          KEY idx_pfi_financing (financing_id)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        """);

    createTableIfMissing(
        """
        CREATE TABLE project_shareholder (
          id BIGINT PRIMARY KEY AUTO_INCREMENT,
          project_id BIGINT NOT NULL,
          shareholder_name VARCHAR(128) NOT NULL,
          ratio VARCHAR(32) DEFAULT NULL,
          capital VARCHAR(64) DEFAULT NULL,
          capital_date VARCHAR(32) DEFAULT NULL,
          sort_order INT NOT NULL DEFAULT 0,
          KEY idx_shareholder_project (project_id, sort_order)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        """);

    createTableIfMissing(
        """
        CREATE TABLE project_team_member (
          id BIGINT PRIMARY KEY AUTO_INCREMENT,
          project_id BIGINT NOT NULL,
          member_name VARCHAR(64) NOT NULL,
          title VARCHAR(128) DEFAULT NULL,
          avatar_url VARCHAR(512) DEFAULT NULL,
          bio TEXT DEFAULT NULL,
          sort_order INT NOT NULL DEFAULT 0,
          KEY idx_team_project (project_id, sort_order)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        """);

    createTableIfMissing(
        """
        CREATE TABLE project_tag (
          id BIGINT PRIMARY KEY AUTO_INCREMENT,
          name VARCHAR(64) NOT NULL,
          UNIQUE KEY uk_tag_name (name)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        """);

    createTableIfMissing(
        """
        CREATE TABLE project_tag_rel (
          id BIGINT PRIMARY KEY AUTO_INCREMENT,
          project_id BIGINT NOT NULL,
          tag_id BIGINT NOT NULL,
          UNIQUE KEY uk_project_tag (project_id, tag_id),
          KEY idx_tag_rel_tag (tag_id)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        """);
    ensureProjectTagRelIdColumn();

    if (tableExists("news")) {
      addColumnIfMissing(
          "news",
          "project_id",
          "BIGINT DEFAULT NULL COMMENT '关联 project.id' AFTER project_name");
      addIndexIfMissing("news", "idx_news_project_id", "KEY idx_news_project_id (project_id)");
    }
  }

  private void backfillNewsProjectId() {
    if (!tableExists("news") || !columnExists("news", "project_id")) {
      return;
    }
    jdbcTemplate.update(
        """
        UPDATE news n
        INNER JOIN project p ON n.project_name = p.name
        SET n.project_id = p.id
        WHERE n.project_id IS NULL AND n.project_name IS NOT NULL AND n.project_name != ''
        """);
  }

  private void seedFromJsonIfNeeded() throws IOException {
    if (!tableExists("project_business")) {
      return;
    }
    Map<String, JsonNode> ext = loadProjectExt();
    if (ext.isEmpty()) {
      return;
    }
    List<Project> projects = projectMapper.selectList(null);
    for (Project project : projects) {
      JsonNode node = ext.get(project.getName());
      if (node == null) {
        ensureSlug(project);
        continue;
      }
      if (hasBusiness(project.getId())) {
        ensureSlug(project);
        continue;
      }
      importProjectExt(project, node);
      log.info("[schema] imported project ext for {}", project.getName());
    }
  }

  private void importProjectExt(Project project, JsonNode ext) {
    Long projectId = project.getId();
    boolean isCertified = ext.path("isCertified").asBoolean(false);
    String intro = ext.path("intro").asText("");
    String slug = buildSlug(project);

    jdbcTemplate.update(
        """
        UPDATE project SET intro = ?, is_certified = ?, slug = COALESCE(slug, ?)
        WHERE id = ?
        """,
        StringUtils.hasText(intro) ? intro : project.getCompanyDesc(),
        isCertified ? 1 : 0,
        slug,
        projectId);

    if (ext.has("businessInfo")) {
      JsonNode b = ext.get("businessInfo");
      String fullName = b.path("fullName").asText("").trim();
      if (StringUtils.hasText(fullName)) {
        jdbcTemplate.update(
            """
            INSERT INTO project_business
              (id, project_id, full_name, english_name, legal_person, registered_address, establish_date, data_source)
            VALUES (?, ?, ?, ?, ?, ?, ?, 'import')
            """,
            projectId,
            projectId,
            fullName,
            nullIfDash(b.path("englishName").asText(null)),
            nullIfDash(b.path("legalPerson").asText(null)),
            nullIfBlank(b.path("address").asText(null)),
            parseDate(b.path("establishDate").asText(null)));
      }
    }

    if (ext.has("financingHistory") && ext.get("financingHistory").isArray()) {
      int sort = 0;
      JsonNode history = ext.get("financingHistory");
      for (JsonNode fin : history) {
        sort++;
        jdbcTemplate.update(
            """
            INSERT INTO project_financing
              (project_id, financing_date, round, amount, is_latest, sort_order)
            VALUES (?, ?, ?, ?, ?, ?)
            """,
            projectId,
            parseDate(fin.path("date").asText(null)),
            fin.path("round").asText(project.getRound()),
            nullIfBlank(fin.path("amount").asText(null)),
            sort == 1 ? 1 : 0,
            sort);
        Long financingId =
            jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        if (fin.has("investors") && fin.get("investors").isArray()) {
          for (JsonNode inv : fin.get("investors")) {
            String investorName = inv.asText("").trim();
            if (!StringUtils.hasText(investorName)) {
              continue;
            }
            Long institutionId = findInstitutionId(investorName);
            jdbcTemplate.update(
                """
                INSERT INTO project_financing_investor (financing_id, institution_id, investor_name)
                VALUES (?, ?, ?)
                """,
                financingId,
                institutionId,
                investorName);
          }
        }
        if (sort == 1) {
          jdbcTemplate.update(
              """
              UPDATE project SET latest_round = ?, latest_amount = ?, latest_financing_date = ?
              WHERE id = ?
              """,
              fin.path("round").asText(project.getRound()),
              nullIfBlank(fin.path("amount").asText(null)),
              parseDate(fin.path("date").asText(null)),
              projectId);
        }
      }
    }

    if (ext.has("shareholders") && ext.get("shareholders").isArray()) {
      int sort = 0;
      for (JsonNode s : ext.get("shareholders")) {
        sort++;
        jdbcTemplate.update(
            """
            INSERT INTO project_shareholder
              (project_id, shareholder_name, ratio, capital, capital_date, sort_order)
            VALUES (?, ?, ?, ?, ?, ?)
            """,
            projectId,
            s.path("name").asText(""),
            nullIfBlank(s.path("ratio").asText(null)),
            nullIfBlank(s.path("capital").asText(null)),
            nullIfDash(s.path("capitalDate").asText(null)),
            sort);
      }
    }

    if (ext.has("teamMembers") && ext.get("teamMembers").isArray()) {
      int sort = 0;
      for (JsonNode m : ext.get("teamMembers")) {
        sort++;
        jdbcTemplate.update(
            """
            INSERT INTO project_team_member
              (project_id, member_name, title, avatar_url, bio, sort_order)
            VALUES (?, ?, ?, ?, ?, ?)
            """,
            projectId,
            m.path("name").asText(""),
            nullIfBlank(m.path("title").asText(null)),
            nullIfBlank(m.path("avatar").asText(null)),
            nullIfBlank(m.path("bio").asText(null)),
            sort);
      }
    }
  }

  private void migrateTagsIfNeeded() {
    if (!tableExists("project_tag_rel")) {
      return;
    }
    List<Project> projects = projectMapper.selectList(null);
    for (Project project : projects) {
      Integer relCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM project_tag_rel WHERE project_id = ?",
              Integer.class,
              project.getId());
      if (relCount != null && relCount > 0) {
        continue;
      }
      if (!StringUtils.hasText(project.getTags())) {
        continue;
      }
      Arrays.stream(project.getTags().split("[,，]"))
          .map(String::trim)
          .filter(StringUtils::hasText)
          .forEach(tagName -> bindTag(project.getId(), tagName));
    }
  }

  private void bindTag(Long projectId, String tagName) {
    jdbcTemplate.update("INSERT IGNORE INTO project_tag (name) VALUES (?)", tagName);
    Long tagId =
        jdbcTemplate.queryForObject(
            "SELECT id FROM project_tag WHERE name = ? LIMIT 1", Long.class, tagName);
    if (tagId == null) {
      return;
    }
    jdbcTemplate.update(
        "INSERT IGNORE INTO project_tag_rel (project_id, tag_id) VALUES (?, ?)",
        projectId,
        tagId);
  }

  private void ensureSlug(Project project) {
    if (StringUtils.hasText(project.getSlug())) {
      return;
    }
    jdbcTemplate.update(
        "UPDATE project SET slug = ? WHERE id = ? AND (slug IS NULL OR slug = '')",
        buildSlug(project),
        project.getId());
  }

  private String buildSlug(Project project) {
    return "p-" + project.getId();
  }

  private boolean hasBusiness(Long projectId) {
    Integer count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM project_business WHERE project_id = ?",
            Integer.class,
            projectId);
    return count != null && count > 0;
  }

  /** 工商 id 与 project.id 对齐，并为每个项目补全工商记录 */
  private void alignProjectBusinessRecords() {
    if (!tableExists("project_business") || !tableExists("project")) {
      return;
    }
    try {
      jdbcTemplate.execute(
          "ALTER TABLE project_business MODIFY full_name VARCHAR(255) DEFAULT NULL");
    } catch (Exception ignored) {
      // 已调整或非 MySQL
    }
    realignMismatchedBusinessIds();
    backfillMissingBusinessRows();
    try {
      jdbcTemplate.execute("ALTER TABLE project_business MODIFY id BIGINT NOT NULL");
    } catch (Exception ignored) {
      // 已去除自增
    }
  }

  private void realignMismatchedBusinessIds() {
    List<Map<String, Object>> rows =
        jdbcTemplate.queryForList(
            """
            SELECT id, project_id, full_name, english_name, legal_person, registered_address,
                   establish_date, unified_social_credit_code, data_source
            FROM project_business
            WHERE id != project_id
            """);
    for (Map<String, Object> row : rows) {
      Long oldId = ((Number) row.get("id")).longValue();
      Long projectId = ((Number) row.get("project_id")).longValue();
      Integer exists =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM project_business WHERE id = ?", Integer.class, projectId);
      if (exists != null && exists > 0) {
        jdbcTemplate.update("DELETE FROM project_business WHERE id = ?", oldId);
        log.info("[schema] removed duplicate business row id={} for project {}", oldId, projectId);
        continue;
      }
      jdbcTemplate.update(
          """
          INSERT INTO project_business
            (id, project_id, full_name, english_name, legal_person, registered_address,
             establish_date, unified_social_credit_code, data_source)
          VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
          """,
          projectId,
          projectId,
          row.get("full_name"),
          row.get("english_name"),
          row.get("legal_person"),
          row.get("registered_address"),
          row.get("establish_date"),
          row.get("unified_social_credit_code"),
          row.get("data_source"));
      jdbcTemplate.update("DELETE FROM project_business WHERE id = ?", oldId);
      log.info("[schema] realigned business id {} -> {}", oldId, projectId);
    }
  }

  private void backfillMissingBusinessRows() {
    List<Project> projects = projectMapper.selectList(null);
    for (Project project : projects) {
      if (hasBusiness(project.getId())) {
        continue;
      }
      jdbcTemplate.update(
          """
          INSERT INTO project_business
            (id, project_id, full_name, registered_address, establish_date, data_source)
          VALUES (?, ?, '', ?, ?, 'auto')
          """,
          project.getId(),
          project.getId(),
          project.getLocation(),
          parseFoundingYear(project.getFoundingYear()));
      log.info("[schema] backfilled business for project {}", project.getName());
    }
  }

  private LocalDate parseFoundingYear(String foundingYear) {
    if (!StringUtils.hasText(foundingYear)) {
      return null;
    }
    String year = foundingYear.replace("年", "").trim();
    if (year.matches("\\d{4}")) {
      return LocalDate.parse(year + "-01-01");
    }
    return null;
  }

  private Long findInstitutionId(String name) {
    if (!tableExists("institution")) {
      return null;
    }
    List<Long> ids =
        jdbcTemplate.query(
            "SELECT id FROM institution WHERE name = ? LIMIT 1",
            (rs, rowNum) -> rs.getLong("id"),
            name);
    return ids.isEmpty() ? null : ids.get(0);
  }

  private Map<String, JsonNode> loadProjectExt() throws IOException {
    ClassPathResource resource = new ClassPathResource("config/project-detail-ext.json");
    Map<String, Object> raw =
        objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
    return raw.entrySet().stream()
        .collect(
            java.util.stream.Collectors.toMap(
                Map.Entry::getKey, e -> objectMapper.valueToTree(e.getValue())));
  }

  private LocalDate parseDate(String raw) {
    if (!StringUtils.hasText(raw) || "-".equals(raw.trim())) {
      return null;
    }
    String trimmed = raw.trim();
    if (trimmed.matches("\\d{4}-\\d{2}")) {
      return LocalDate.parse(trimmed + "-01");
    }
    if (trimmed.matches("\\d{4}-\\d{2}-\\d{2}")) {
      return LocalDate.parse(trimmed);
    }
    return null;
  }

  private String nullIfBlank(String value) {
    return StringUtils.hasText(value) ? value.trim() : null;
  }

  private String nullIfDash(String value) {
    if (!StringUtils.hasText(value)) {
      return null;
    }
    String trimmed = value.trim();
    return "-".equals(trimmed) ? null : trimmed;
  }

  private void addColumnIfMissing(String table, String column, String ddl) {
    if (!columnExists(table, column)) {
      jdbcTemplate.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + ddl);
      log.info("[schema] added {}.{}", table, column);
    }
  }

  private void addIndexIfMissing(String table, String indexName, String ddl) {
    if (!indexExists(table, indexName)) {
      jdbcTemplate.execute("ALTER TABLE " + table + " ADD " + ddl);
      log.info("[schema] added index {} on {}", indexName, table);
    }
  }

  private void ensureProjectTagRelIdColumn() {
    if (!tableExists("project_tag_rel") || columnExists("project_tag_rel", "id")) {
      return;
    }
    try {
      jdbcTemplate.execute("ALTER TABLE project_tag_rel DROP PRIMARY KEY");
    } catch (Exception ignored) {
      // 可能已是 surrogate id 或无主键
    }
    jdbcTemplate.execute(
        "ALTER TABLE project_tag_rel ADD COLUMN id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST");
    addIndexIfMissing(
        "project_tag_rel", "uk_project_tag", "UNIQUE KEY uk_project_tag (project_id, tag_id)");
    log.info("[schema] added project_tag_rel.id");
  }

  private void createTableIfMissing(String ddl) {
    String sql = ddl.trim();
    if (!sql.toUpperCase().contains("IF NOT EXISTS")) {
      sql = sql.replaceFirst("CREATE TABLE ", "CREATE TABLE IF NOT EXISTS ");
    }
    jdbcTemplate.execute(sql);
  }

  private boolean tableExists(String table) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*) FROM information_schema.TABLES
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?
            """,
            Integer.class,
            table);
    return count != null && count > 0;
  }

  private boolean columnExists(String table, String column) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*) FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?
            """,
            Integer.class,
            table,
            column);
    return count != null && count > 0;
  }

  private boolean indexExists(String table, String indexName) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*) FROM information_schema.STATISTICS
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND INDEX_NAME = ?
            """,
            Integer.class,
            table,
            indexName);
    return count != null && count > 0;
  }
}
