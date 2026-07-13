package com.lianbei.vc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lianbei.vc.entity.Institution;
import com.lianbei.vc.entity.ProjectFinancing;
import com.lianbei.vc.entity.ProjectFinancingInvestor;
import com.lianbei.vc.mapper.InstitutionMapper;
import com.lianbei.vc.mapper.ProjectFinancingInvestorMapper;
import com.lianbei.vc.mapper.ProjectFinancingMapper;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** 按 project_financing_investor 统计机构关联的去重项目数（投资事件数） */
@Service
public class InstitutionInvestmentCounter {

  private static final Logger log = LoggerFactory.getLogger(InstitutionInvestmentCounter.class);

  private final InstitutionMapper institutionMapper;
  private final ProjectFinancingInvestorMapper investorMapper;
  private final ProjectFinancingMapper financingMapper;

  public InstitutionInvestmentCounter(
      InstitutionMapper institutionMapper,
      ProjectFinancingInvestorMapper investorMapper,
      ProjectFinancingMapper financingMapper) {
    this.institutionMapper = institutionMapper;
    this.investorMapper = investorMapper;
    this.financingMapper = financingMapper;
  }

  public int countDistinctProjects(Institution institution) {
    if (institution == null || institution.getId() == null) {
      return 0;
    }
    return countForInstitutions(List.of(institution)).getOrDefault(institution.getId(), 0);
  }

  public Map<Long, Integer> countForInstitutions(List<Institution> institutions) {
    if (institutions == null || institutions.isEmpty()) {
      return Map.of();
    }

    Map<Long, Set<Long>> financingIdsByInstId = new HashMap<>();
    Map<Long, Set<String>> namesByInstId = new HashMap<>();
    Set<String> allNames = new LinkedHashSet<>();

    for (Institution inst : institutions) {
      if (inst.getId() == null) {
        continue;
      }
      financingIdsByInstId.put(inst.getId(), new LinkedHashSet<>());
      Set<String> names = matchNames(inst);
      namesByInstId.put(inst.getId(), names);
      allNames.addAll(names);
    }

    Set<Long> instIds = financingIdsByInstId.keySet();
    if (!instIds.isEmpty()) {
      investorMapper
          .selectList(
              new LambdaQueryWrapper<ProjectFinancingInvestor>()
                  .in(ProjectFinancingInvestor::getInstitutionId, instIds)
                  .select(
                      ProjectFinancingInvestor::getInstitutionId,
                      ProjectFinancingInvestor::getFinancingId))
          .forEach(
              row -> {
                if (row.getInstitutionId() != null && row.getFinancingId() != null) {
                  financingIdsByInstId.get(row.getInstitutionId()).add(row.getFinancingId());
                }
              });
    }

    if (!allNames.isEmpty()) {
      Map<String, Set<Long>> instIdsByName = new HashMap<>();
      for (Map.Entry<Long, Set<String>> entry : namesByInstId.entrySet()) {
        for (String name : entry.getValue()) {
          instIdsByName.computeIfAbsent(name, ignored -> new LinkedHashSet<>()).add(entry.getKey());
        }
      }

      investorMapper
          .selectList(
              new LambdaQueryWrapper<ProjectFinancingInvestor>()
                  .in(ProjectFinancingInvestor::getInvestorName, allNames)
                  .select(
                      ProjectFinancingInvestor::getInvestorName,
                      ProjectFinancingInvestor::getFinancingId))
          .forEach(
              row -> {
                if (row.getFinancingId() == null || !StringUtils.hasText(row.getInvestorName())) {
                  return;
                }
                Set<Long> linked = instIdsByName.get(row.getInvestorName().trim());
                if (linked == null) {
                  return;
                }
                for (Long instId : linked) {
                  financingIdsByInstId.get(instId).add(row.getFinancingId());
                }
              });
    }

    Set<Long> allFinancingIds =
        financingIdsByInstId.values().stream().flatMap(Set::stream).collect(Collectors.toSet());

    Map<Long, Long> projectIdByFinancingId = new HashMap<>();
    if (!allFinancingIds.isEmpty()) {
      financingMapper
          .selectList(
              new LambdaQueryWrapper<ProjectFinancing>()
                  .in(ProjectFinancing::getId, allFinancingIds)
                  .select(ProjectFinancing::getId, ProjectFinancing::getProjectId))
          .forEach(
              financing -> {
                if (financing.getProjectId() != null) {
                  projectIdByFinancingId.put(financing.getId(), financing.getProjectId());
                }
              });
    }

    Map<Long, Integer> result = new HashMap<>();
    for (Long instId : financingIdsByInstId.keySet()) {
      Set<Long> projectIds = new LinkedHashSet<>();
      for (Long financingId : financingIdsByInstId.get(instId)) {
        Long projectId = projectIdByFinancingId.get(financingId);
        if (projectId != null) {
          projectIds.add(projectId);
        }
      }
      result.put(instId, projectIds.size());
    }
    return result;
  }

  /** 将 institution.event_count 与融资关联表对齐（用于排序与列表展示） */
  public void syncAllEventCounts() {
    long startedAt = System.currentTimeMillis();
    List<Institution> all =
        institutionMapper.selectList(
            new LambdaQueryWrapper<Institution>()
                .select(
                    Institution::getId,
                    Institution::getName,
                    Institution::getEntityName,
                    Institution::getEventCount));

    Map<Long, Integer> counts = countForInstitutions(all);
    int updated = 0;
    for (Institution inst : all) {
      int count = counts.getOrDefault(inst.getId(), 0);
      if (!Objects.equals(inst.getEventCount(), count)) {
        Institution patch = new Institution();
        patch.setId(inst.getId());
        patch.setEventCount(count);
        institutionMapper.updateById(patch);
        updated++;
      }
    }
    log.info(
        "[institution] synced event_count for {}/{} institutions in {} ms",
        updated,
        all.size(),
        System.currentTimeMillis() - startedAt);
  }

  private Set<String> matchNames(Institution institution) {
    Set<String> names = new LinkedHashSet<>();
    if (StringUtils.hasText(institution.getName())) {
      names.add(institution.getName().trim());
    }
    if (StringUtils.hasText(institution.getEntityName())) {
      names.add(institution.getEntityName().trim());
    }
    return names;
  }
}
