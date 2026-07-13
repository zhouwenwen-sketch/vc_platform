package com.lianbei.vc.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lianbei.vc.admin.service.InstitutionImportService;
import com.lianbei.vc.dto.response.InstitutionImportResultVO;
import com.lianbei.vc.entity.Institution;
import com.lianbei.vc.entity.InstitutionTeamMember;
import com.lianbei.vc.exception.BusinessException;
import com.lianbei.vc.mapper.InstitutionMapper;
import com.lianbei.vc.mapper.InstitutionTeamMemberMapper;
import com.lianbei.vc.service.FileStorageService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
public class InstitutionImportServiceImpl implements InstitutionImportService {

  private static final DataFormatter FORMATTER = new DataFormatter();

  private final InstitutionMapper institutionMapper;
  private final InstitutionTeamMemberMapper teamMemberMapper;
  private final FileStorageService fileStorageService;

  public InstitutionImportServiceImpl(
      InstitutionMapper institutionMapper,
      InstitutionTeamMemberMapper teamMemberMapper,
      FileStorageService fileStorageService) {
    this.institutionMapper = institutionMapper;
    this.teamMemberMapper = teamMemberMapper;
    this.fileStorageService = fileStorageService;
  }

  @Override
  @Transactional
  public InstitutionImportResultVO importExcel(
      MultipartFile institutionsFile,
      MultipartFile basicInfoFile,
      MultipartFile membersFile,
      MultipartFile companiesFile,
      MultipartFile imagesZip,
      MultipartFile[] imageFiles) {
    if (isEmpty(institutionsFile)
        && isEmpty(basicInfoFile)
        && isEmpty(membersFile)
        && isEmpty(companiesFile)) {
      throw new BusinessException("请至少上传一个 Excel 文件");
    }

    List<String> warnings = new ArrayList<>();
    Map<String, byte[]> imageMap = loadImages(imagesZip, imageFiles, warnings);
    int[] imagesSaved = {0};

    int companiesInserted = 0;
    int companiesUpdated = 0;
    int membersInserted = 0;
    int membersUpdated = 0;
    int membersSkipped = 0;

    if (!isEmpty(institutionsFile)) {
      for (Map<String, String> row : parseSheet(institutionsFile, warnings)) {
        String name = institutionNameFromRow(row);
        if (!StringUtils.hasText(name)) {
          warnings.add("机构主表行缺少机构简称/名称，已跳过");
          continue;
        }
        if (upsertInstitutionCatalog(name.trim(), row)) {
          companiesUpdated++;
        } else {
          companiesInserted++;
        }
      }
    }

    MultipartFile legacyOrBasic = !isEmpty(basicInfoFile) ? basicInfoFile : companiesFile;
    if (!isEmpty(legacyOrBasic)) {
      for (Map<String, String> row : parseSheet(legacyOrBasic, warnings)) {
        if (isBasicInfoRow(row)) {
          String name = firstNonBlank(row, "机构名称", "机构简称");
          if (!StringUtils.hasText(name)) {
            warnings.add("基本信息行缺少机构名称，已跳过");
            continue;
          }
          if (mergeBasicInfo(name.trim(), row, imageMap, warnings, imagesSaved)) {
            companiesUpdated++;
          } else {
            companiesInserted++;
          }
          continue;
        }
        if (isLegacyCompanyRow(row)) {
          String name = firstNonBlank(row, "机构名称");
          if (!StringUtils.hasText(name)) {
            warnings.add("公司信息行缺少机构名称，已跳过");
            continue;
          }
          if (upsertLegacyCompany(name.trim(), row, imageMap, warnings, imagesSaved)) {
            companiesUpdated++;
          } else {
            companiesInserted++;
          }
        }
      }
    }

    if (!isEmpty(membersFile)) {
      Map<Long, Integer> sortCounter = new HashMap<>();
      for (Map<String, String> row : parseSheet(membersFile, warnings)) {
        String companyName = firstNonBlank(row, "机构名称", "公司名称");
        String memberName = cleanMemberName(firstNonBlank(row, "姓名"));
        if (!StringUtils.hasText(companyName) || !StringUtils.hasText(memberName)) {
          warnings.add("人员行缺少机构名称或姓名，已跳过");
          membersSkipped++;
          continue;
        }
        Institution institution = findInstitutionByName(companyName.trim());
        if (institution == null) {
          warnings.add("未找到机构「" + companyName.trim() + "」，跳过成员「" + memberName + "」");
          membersSkipped++;
          continue;
        }
        int sortOrder = sortCounter.getOrDefault(institution.getId(), 0);
        sortCounter.put(institution.getId(), sortOrder + 1);
        if (upsertMember(institution, memberName, row, imageMap, sortOrder, warnings, imagesSaved)) {
          membersUpdated++;
        } else {
          membersInserted++;
        }
      }
    }

    return InstitutionImportResultVO.builder()
        .companiesInserted(companiesInserted)
        .companiesUpdated(companiesUpdated)
        .membersInserted(membersInserted)
        .membersUpdated(membersUpdated)
        .membersSkipped(membersSkipped)
        .imagesSaved(imagesSaved[0])
        .warnings(warnings)
        .build();
  }

  private boolean upsertInstitutionCatalog(String name, Map<String, String> row) {
    Institution existing = findInstitutionByName(name);
    String fieldsRaw = normalizeIndustry(firstNonBlank(row, "主要投资行业", "投资领域"));
    String manageScale = resolveManageScale(row);

    Institution patch =
        Institution.builder()
            .name(name)
            .entityName(trim(firstNonBlank(row, "机构全称", "主体名称")))
            .intro(trim(firstNonBlank(row, "品牌介绍", "机构介绍")))
            .website(trim(firstNonBlank(row, "机构官网", "官网")))
            .investmentField(truncate(firstCsvValue(fieldsRaw), 64))
            .investmentFields(fieldsRaw)
            .foundedYear(extractYear(firstNonBlank(row, "成立时间")))
            .manageScale(manageScale)
            .instType(truncate(firstNonBlank(row, "资本类型", "机构类型"), 32))
            .build();

    if (existing == null) {
      institutionMapper.insert(patch);
      return false;
    }
    applyInstitutionPatch(existing, patch);
    institutionMapper.updateById(existing);
    return true;
  }

  private boolean mergeBasicInfo(
      String name,
      Map<String, String> row,
      Map<String, byte[]> imageMap,
      List<String> warnings,
      int[] imagesSaved) {
    Institution existing = findInstitutionByName(name);
    String logoUrl =
        resolveImageUrl(
            firstNonBlank(row, "机构头像", "头像名", "头像名字"), imageMap, warnings, imagesSaved);

    Institution patch =
        Institution.builder()
            .name(name)
            .intro(trim(firstNonBlank(row, "机构介绍", "品牌介绍")))
            .instType(truncate(firstNonBlank(row, "机构类型", "资本类型"), 32))
            .logoUrl(logoUrl)
            .build();

    if (existing == null) {
      institutionMapper.insert(patch);
      return false;
    }
    if (StringUtils.hasText(patch.getIntro())) {
      existing.setIntro(patch.getIntro());
    }
    if (StringUtils.hasText(patch.getInstType())) {
      existing.setInstType(patch.getInstType());
    }
    if (StringUtils.hasText(patch.getLogoUrl())) {
      existing.setLogoUrl(patch.getLogoUrl());
    }
    institutionMapper.updateById(existing);
    return true;
  }

  private boolean upsertLegacyCompany(
      String name,
      Map<String, String> row,
      Map<String, byte[]> imageMap,
      List<String> warnings,
      int[] imagesSaved) {
    Institution existing = findInstitutionByName(name);
    String fieldsRaw = row.getOrDefault("投资领域", "");
    String logoUrl = resolveImageUrl(row.get("头像名"), imageMap, warnings, imagesSaved);

    Institution patch =
        Institution.builder()
            .name(name)
            .investmentField(truncate(firstCsvValue(fieldsRaw), 64))
            .investmentFields(fieldsRaw.trim())
            .intro(trim(row.get("机构介绍")))
            .manageScale(trim(row.get("管理规模")))
            .website(trim(row.get("官网")))
            .logoUrl(logoUrl)
            .build();

    if (existing == null) {
      institutionMapper.insert(patch);
      return false;
    }
    applyInstitutionPatch(existing, patch);
    if (StringUtils.hasText(patch.getLogoUrl())) {
      existing.setLogoUrl(patch.getLogoUrl());
    }
    institutionMapper.updateById(existing);
    return true;
  }

  private void applyInstitutionPatch(Institution existing, Institution patch) {
    if (StringUtils.hasText(patch.getEntityName())) {
      existing.setEntityName(patch.getEntityName());
    }
    if (StringUtils.hasText(patch.getIntro())) {
      existing.setIntro(patch.getIntro());
    }
    if (StringUtils.hasText(patch.getWebsite())) {
      existing.setWebsite(patch.getWebsite());
    }
    if (StringUtils.hasText(patch.getInvestmentField())) {
      existing.setInvestmentField(patch.getInvestmentField());
    }
    if (StringUtils.hasText(patch.getInvestmentFields())) {
      existing.setInvestmentFields(patch.getInvestmentFields());
    }
    if (StringUtils.hasText(patch.getFoundedYear())) {
      existing.setFoundedYear(patch.getFoundedYear());
    }
    if (StringUtils.hasText(patch.getManageScale())) {
      existing.setManageScale(patch.getManageScale());
    }
    if (StringUtils.hasText(patch.getInstType())) {
      existing.setInstType(patch.getInstType());
    }
  }

  private boolean upsertMember(
      Institution institution,
      String memberName,
      Map<String, String> row,
      Map<String, byte[]> imageMap,
      int sortOrder,
      List<String> warnings,
      int[] imagesSaved) {
    InstitutionTeamMember existing =
        teamMemberMapper.selectOne(
            new LambdaQueryWrapper<InstitutionTeamMember>()
                .eq(InstitutionTeamMember::getInstitutionId, institution.getId())
                .eq(InstitutionTeamMember::getMemberName, memberName)
                .last("LIMIT 1"));

    String avatar =
        resolveImageUrl(
            firstNonBlank(row, "头像名字", "头像名", "机构头像"), imageMap, warnings, imagesSaved);
    String title = trim(firstNonBlank(row, "职位", "职务"));
    String bio = trim(firstNonBlank(row, "介绍", "人物简介"));

    InstitutionTeamMember entity =
        InstitutionTeamMember.builder()
            .institutionId(institution.getId())
            .memberName(memberName)
            .title(title)
            .bio(bio)
            .avatar(avatar)
            .sortOrder(sortOrder)
            .build();

    if (existing == null) {
      teamMemberMapper.insert(entity);
      return false;
    }
    existing.setTitle(entity.getTitle());
    existing.setBio(entity.getBio());
    existing.setSortOrder(entity.getSortOrder());
    if (StringUtils.hasText(entity.getAvatar())) {
      existing.setAvatar(entity.getAvatar());
    }
    teamMemberMapper.updateById(existing);
    return true;
  }

  private Institution findInstitutionByName(String name) {
    Institution byName =
        institutionMapper.selectOne(
            new LambdaQueryWrapper<Institution>().eq(Institution::getName, name).last("LIMIT 1"));
    if (byName != null) {
      return byName;
    }
    return institutionMapper.selectOne(
        new LambdaQueryWrapper<Institution>()
            .eq(Institution::getEntityName, name)
            .last("LIMIT 1"));
  }

  private String institutionNameFromRow(Map<String, String> row) {
    return firstNonBlank(row, "机构简称", "机构名称");
  }

  private boolean isBasicInfoRow(Map<String, String> row) {
    return row.containsKey("机构头像")
        || (row.containsKey("机构名称") && row.containsKey("机构类型") && !row.containsKey("投资领域"));
  }

  private boolean isLegacyCompanyRow(Map<String, String> row) {
    return row.containsKey("投资领域") || row.containsKey("管理规模");
  }

  private String resolveManageScale(Map<String, String> row) {
    String fundScale = trim(firstNonBlank(row, "基金管理规模"));
    if (StringUtils.hasText(fundScale) && !"-".equals(fundScale)) {
      return fundScale;
    }
    String legacy = trim(firstNonBlank(row, "管理规模"));
    if (StringUtils.hasText(legacy)) {
      return legacy;
    }
    String amount = trim(firstNonBlank(row, "管理规模(记录金额)"));
    if (!StringUtils.hasText(amount) || "-".equals(amount)) {
      return null;
    }
    String currency = firstNonBlank(row, "管理规模(记录币种)", "币种");
    if ("CNY".equalsIgnoreCase(currency) || "人民币".equals(currency)) {
      try {
        double value = Double.parseDouble(amount.replace(",", ""));
        if (value >= 100_000_000) {
          return String.format(Locale.ROOT, "%.0f亿人民币", value / 100_000_000);
        }
        if (value >= 10_000) {
          return String.format(Locale.ROOT, "%.0f万人民币", value / 10_000);
        }
      } catch (NumberFormatException ignored) {
        // keep raw
      }
    }
    return amount + (StringUtils.hasText(currency) ? " " + currency : "");
  }

  private String normalizeIndustry(String raw) {
    if (!StringUtils.hasText(raw)) {
      return "";
    }
    return raw.replace('、', ',').replace('，', ',').trim();
  }

  private String extractYear(String dateText) {
    if (!StringUtils.hasText(dateText)) {
      return null;
    }
    String trimmed = dateText.trim();
    if (trimmed.length() >= 4 && Character.isDigit(trimmed.charAt(0))) {
      return trimmed.substring(0, 4);
    }
    return trimmed.length() <= 16 ? trimmed : trimmed.substring(0, 16);
  }

  private Integer parseEventCount(String raw) {
    if (!StringUtils.hasText(raw) || "-".equals(raw.trim())) {
      return null;
    }
    try {
      return Integer.parseInt(raw.trim().replace(",", ""));
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  private String cleanMemberName(String raw) {
    if (!StringUtils.hasText(raw)) {
      return "";
    }
    String firstLine = raw.split("\\R")[0].trim();
    int newline = firstLine.indexOf('\n');
    if (newline > 0) {
      firstLine = firstLine.substring(0, newline).trim();
    }
    return firstLine;
  }

  private String resolveImageUrl(
      String filename,
      Map<String, byte[]> imageMap,
      List<String> warnings,
      int[] imagesSaved) {
    if (!StringUtils.hasText(filename)) {
      return null;
    }
    String trimmed = filename.trim();
    byte[] content = imageMap.get(trimmed.toLowerCase(Locale.ROOT));
    if (content == null) {
      content = imageMap.get(trimmed);
    }
    if (content == null) {
      warnings.add("未找到头像文件: " + trimmed);
      return null;
    }
    imagesSaved[0]++;
    return fileStorageService.storeImportedAsset(trimmed, content);
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
        if (base.startsWith(".")) {
          continue;
        }
        try {
          byte[] bytes = file.getBytes();
          if (bytes.length == 0) {
            continue;
          }
          map.put(base, bytes);
          map.put(base.toLowerCase(Locale.ROOT), bytes);
        } catch (IOException e) {
          warnings.add("读取图片失败: " + base);
        }
      }
    }
    if (map.isEmpty() && (!isEmpty(imagesZip) || (imageFiles != null && imageFiles.length > 0))) {
      if (!warnings.stream().anyMatch(w -> w.contains("RAR") || w.contains("ZIP"))) {
        warnings.add("未解析到任何图片，请检查压缩包格式或图片文件");
      }
    }
    return map;
  }

  private Map<String, byte[]> loadImagesZip(MultipartFile imagesZip, List<String> warnings) {
    Map<String, byte[]> map = new HashMap<>();
    if (isEmpty(imagesZip)) {
      return map;
    }
    try {
      byte[] header = imagesZip.getInputStream().readNBytes(4);
      if (header.length >= 4 && header[0] == 'R' && header[1] == 'a' && header[2] == 'r' && header[3] == '!') {
        warnings.add(
            "上传的压缩包实际是 RAR 格式（非 ZIP），Java 无法解析。请：① 在访达中选中「机构头像」文件夹 → 右键「压缩」生成 .zip；或 ② 直接多选 PNG 图片上传");
        return map;
      }
      if (header.length < 2 || header[0] != 'P' || header[1] != 'K') {
        warnings.add("压缩包不是有效的 ZIP 文件，请重新压缩后再上传");
        return map;
      }
    } catch (IOException e) {
      warnings.add("读取压缩包失败");
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
      throw new BusinessException("头像压缩包解析失败");
    }
    if (map.isEmpty()) {
      warnings.add("头像压缩包中未找到图片文件");
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
    short last = headerRow.getLastCellNum();
    for (int c = 0; c < last; c++) {
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

  private String firstCsvValue(String raw) {
    if (!StringUtils.hasText(raw)) {
      return "";
    }
    String[] parts = raw.split("[,，]");
    for (String part : parts) {
      if (StringUtils.hasText(part)) {
        return part.trim();
      }
    }
    return "";
  }

  private String truncate(String value, int max) {
    if (!StringUtils.hasText(value)) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.length() <= max ? trimmed : trimmed.substring(0, max);
  }

  private String trim(String value) {
    return StringUtils.hasText(value) ? value.trim() : null;
  }

  private boolean isEmpty(MultipartFile file) {
    return file == null || file.isEmpty();
  }
}
