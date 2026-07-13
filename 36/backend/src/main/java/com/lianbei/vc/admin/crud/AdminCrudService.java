package com.lianbei.vc.admin.crud;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianbei.vc.admin.dto.CrudFieldVO;
import com.lianbei.vc.admin.dto.CrudResourceVO;
import com.lianbei.vc.admin.service.AdminPermissionService;
import com.lianbei.vc.common.PageResult;
import com.lianbei.vc.exception.BusinessException;
import com.lianbei.vc.admin.dto.CrudFieldOptionsVO;
import com.lianbei.vc.entity.News;
import com.lianbei.vc.entity.Project;
import com.lianbei.vc.entity.ProjectBusiness;
import com.lianbei.vc.service.LibraryFilterService;
import com.lianbei.vc.service.NewsProjectLinkService;
import com.lianbei.vc.service.ProjectBusinessSyncService;
import com.lianbei.vc.service.ProjectTagSyncService;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AdminCrudService {

  private static final Set<String> HIDDEN_FIELDS = Set.of("password");
  private static final Set<String> MASK_FIELDS = Set.of("code");

  private final AdminResourceRegistry registry;
  private final AdminPermissionService permissionService;
  private final LibraryFilterService libraryFilterService;
  private final AdminCrudFieldOptionsResolver fieldOptionsResolver;
  private final ProjectTagSyncService projectTagSyncService;
  private final ProjectBusinessSyncService projectBusinessSyncService;
  private final NewsProjectLinkService newsProjectLinkService;
  private final ObjectMapper objectMapper;

  public AdminCrudService(
      AdminResourceRegistry registry,
      AdminPermissionService permissionService,
      LibraryFilterService libraryFilterService,
      AdminCrudFieldOptionsResolver fieldOptionsResolver,
      ProjectTagSyncService projectTagSyncService,
      ProjectBusinessSyncService projectBusinessSyncService,
      NewsProjectLinkService newsProjectLinkService,
      ObjectMapper objectMapper) {
    this.registry = registry;
    this.permissionService = permissionService;
    this.libraryFilterService = libraryFilterService;
    this.fieldOptionsResolver = fieldOptionsResolver;
    this.projectTagSyncService = projectTagSyncService;
    this.projectBusinessSyncService = projectBusinessSyncService;
    this.newsProjectLinkService = newsProjectLinkService;
    this.objectMapper = objectMapper;
  }

  public List<CrudResourceVO> listResources() {
    return registry.all().values().stream()
        .filter(meta -> permissionService.canAccessResource(meta.getKey()))
        .map(this::toResourceVO)
        .collect(Collectors.toList());
  }

  public CrudResourceVO getResourceMeta(String resource) {
    AdminResourceMeta meta = requireMeta(resource);
    permissionService.checkResource(resource, "list");
    return toResourceVO(meta);
  }

  public List<Map<String, Object>> listProjectTagOptions() {
    permissionService.checkResource("project", "list");
    return libraryFilterService.listProjectTagOptionGroups();
  }

  public Map<String, Object> getProjectBusiness(Long projectId) {
    permissionService.checkResource("project", "list");
    return projectBusinessSyncService.getForAdmin(projectId);
  }

  public PageResult<Map<String, Object>> page(
      String resource, int pageNum, int pageSize, String keyword) {
    AdminResourceMeta meta = requireMeta(resource);
    permissionService.checkResource(resource, "list");
    Page<Object> page = new Page<>(pageNum, pageSize);
    QueryWrapper<Object> wrapper = new QueryWrapper<>();
    if ("home_banner".equals(resource) || "project_collection".equals(resource)) {
      wrapper.orderByAsc("sort_order").orderByAsc("id");
    } else {
      wrapper.orderByDesc("id");
    }
    applyKeyword(wrapper, meta, keyword);
    @SuppressWarnings("unchecked")
    IPage<Object> result = ((BaseMapper<Object>) meta.getMapper()).selectPage(page, wrapper);
    List<Map<String, Object>> rows =
        result.getRecords().stream().map(row -> toMap(meta, row)).collect(Collectors.toList());
    return PageResult.of(result.getTotal(), rows);
  }

  public Map<String, Object> detail(String resource, Long id) {
    AdminResourceMeta meta = requireMeta(resource);
    permissionService.checkResource(resource, "list");
    Object row = meta.getMapper().selectById(id);
    if (row == null) {
      throw new BusinessException(404, "记录不存在");
    }
    return toMap(meta, row);
  }

  public Map<String, Object> create(String resource, Map<String, Object> body) {
    AdminResourceMeta meta = requireMeta(resource);
    permissionService.checkResource(resource, "create");
    ensureWritable(meta);
    Object entity = mapToEntity(meta, body, true);
    alignBusinessId(meta, entity);
    syncNewsProjectFields(meta, entity);
    insertEntity(meta, entity);
    afterProjectPersist(meta, entity, body);
    return toMap(meta, entity);
  }

  public Map<String, Object> update(String resource, Long id, Map<String, Object> body) {
    AdminResourceMeta meta = requireMeta(resource);
    permissionService.checkResource(resource, "update");
    ensureWritable(meta);
    Object existing = meta.getMapper().selectById(id);
    if (existing == null) {
      throw new BusinessException(404, "记录不存在");
    }
    Object entity = mapToEntity(meta, body, false);
    setFieldValue(entity, "id", id);
    alignBusinessId(meta, entity);
    syncNewsProjectFields(meta, entity);
    updateEntity(meta, entity);
    afterProjectPersist(meta, entity, body);
    Object updated = meta.getMapper().selectById(id);
    return toMap(meta, updated);
  }

  public void delete(String resource, Long id) {
    AdminResourceMeta meta = requireMeta(resource);
    permissionService.checkResource(resource, "delete");
    ensureWritable(meta);
    if (meta.getMapper().selectById(id) == null) {
      throw new BusinessException(404, "记录不存在");
    }
    if ("project".equals(resource)) {
      projectBusinessSyncService.deleteByProjectId(id);
    }
    meta.getMapper().deleteById(id);
  }

  private AdminResourceMeta requireMeta(String resource) {
    return registry
        .find(resource)
        .orElseThrow(() -> new BusinessException(404, "未知资源: " + resource));
  }

  private void ensureWritable(AdminResourceMeta meta) {
    if (meta.isReadOnly()) {
      throw new BusinessException(403, "该资源只读，不可修改");
    }
  }

  private void applyKeyword(QueryWrapper<Object> wrapper, AdminResourceMeta meta, String keyword) {
    if (!StringUtils.hasText(keyword)) {
      return;
    }
    String trimmed = keyword.trim();
    if (trimmed.matches("\\d+")) {
      wrapper.eq("id", Long.parseLong(trimmed));
      return;
    }
    for (Field field : meta.getEntityClass().getDeclaredFields()) {
      if (field.getType() == String.class) {
        wrapper.like(camelToSnake(field.getName()), trimmed);
        break;
      }
    }
  }

  private Object mapToEntity(AdminResourceMeta meta, Map<String, Object> body, boolean isCreate) {
    Map<String, Object> payload = new LinkedHashMap<>(body);
    payload.remove("business");
    if (isCreate) {
      payload.remove("id");
    }
    payload.remove("createTime");
    payload.remove("updateTime");
    for (String hidden : HIDDEN_FIELDS) {
      payload.remove(hidden);
    }
    return objectMapper.convertValue(payload, meta.getEntityClass());
  }

  private Map<String, Object> toMap(AdminResourceMeta meta, Object row) {
    @SuppressWarnings("unchecked")
    Map<String, Object> map = objectMapper.convertValue(row, Map.class);
    for (String field : MASK_FIELDS) {
      if (map.containsKey(field) && map.get(field) != null) {
        map.put(field, "******");
      }
    }
    for (String hidden : HIDDEN_FIELDS) {
      map.remove(hidden);
    }
    return map;
  }

  private CrudResourceVO toResourceVO(AdminResourceMeta meta) {
    return CrudResourceVO.builder()
        .key(meta.getKey())
        .label(meta.getLabel())
        .tableName(meta.getTableName())
        .readOnly(meta.isReadOnly())
        .fields(buildFields(meta))
        .build();
  }

  private List<CrudFieldVO> buildFields(AdminResourceMeta meta) {
    List<CrudFieldVO> fields = new ArrayList<>();
    for (Field field : meta.getEntityClass().getDeclaredFields()) {
      String name = field.getName();
      if (HIDDEN_FIELDS.contains(name)) {
        continue;
      }
      boolean hidden = MASK_FIELDS.contains(name);
      if ("news".equals(meta.getKey()) && "projectId".equals(name)) {
        hidden = true;
      }
      CrudFieldOptionsVO fieldOptions = fieldOptionsResolver.resolve(meta.getKey(), name);
      String label = AdminFieldLabel.get(meta.getKey(), name);
      if ("news".equals(meta.getKey()) && "projectName".equals(name)) {
        label = "关联项目";
        fieldOptions = new CrudFieldOptionsVO();
        fieldOptions.setInputType("project_lookup");
      }
      fields.add(
          CrudFieldVO.builder()
              .name(name)
              .label(label)
              .type(resolveType(field.getType()))
              .required(false)
              .readOnly("id".equals(name) || "createTime".equals(name) || "updateTime".equals(name))
              .hidden(hidden)
              .fieldOptions(fieldOptions)
              .build());
    }
    return fields;
  }

  private String resolveType(Class<?> type) {
    if (type == Integer.class || type == int.class || type == Long.class || type == long.class) {
      return "number";
    }
    if (type == LocalDateTime.class || type == LocalDate.class) {
      return "datetime";
    }
    if (type == Boolean.class || type == boolean.class) {
      return "boolean";
    }
    String name = type.getSimpleName().toLowerCase();
    if (name.contains("decimal") || name.contains("bigdecimal")) {
      return "number";
    }
    return "text";
  }

  private String camelToSnake(String camel) {
    return camel.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void insertEntity(AdminResourceMeta meta, Object entity) {
    ((BaseMapper) meta.getMapper()).insert(entity);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void updateEntity(AdminResourceMeta meta, Object entity) {
    ((BaseMapper) meta.getMapper()).updateById(entity);
  }

  private void alignBusinessId(AdminResourceMeta meta, Object entity) {
    if (!"project_business".equals(meta.getKey()) || !(entity instanceof ProjectBusiness business)) {
      return;
    }
    if (business.getProjectId() != null) {
      business.setId(business.getProjectId());
    }
  }

  private void syncNewsProjectFields(AdminResourceMeta meta, Object entity) {
    if (!"news".equals(meta.getKey()) || !(entity instanceof News news)) {
      return;
    }
    newsProjectLinkService.syncNewsFields(news);
  }

  private void afterProjectPersist(AdminResourceMeta meta, Object entity, Map<String, Object> body) {
    if (!"project".equals(meta.getKey()) || !(entity instanceof Project project)) {
      return;
    }
    if (project.getId() == null) {
      return;
    }
    String tags = body.containsKey("tags") ? stringValue(body.get("tags")) : project.getTags();
    projectTagSyncService.syncFromTagsField(project.getId(), tags);
    @SuppressWarnings("unchecked")
    Map<String, Object> business = (Map<String, Object>) body.get("business");
    if (business != null) {
      projectBusinessSyncService.upsertFromAdmin(project.getId(), business);
    } else {
      projectBusinessSyncService.ensureForProject(project);
    }
  }

  private String stringValue(Object value) {
    return value == null ? null : String.valueOf(value);
  }

  private void setFieldValue(Object target, String fieldName, Object value) {
    try {
      Field field = target.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(target, value);
    } catch (ReflectiveOperationException ex) {
      throw new BusinessException("字段赋值失败: " + fieldName);
    }
  }
}
