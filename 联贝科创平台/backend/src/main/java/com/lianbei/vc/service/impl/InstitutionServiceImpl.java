package com.lianbei.vc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lianbei.vc.common.FilterParamUtils;
import com.lianbei.vc.common.PageResult;
import com.lianbei.vc.dto.response.InstitutionDetailVO;
import com.lianbei.vc.entity.Institution;
import com.lianbei.vc.entity.Project;
import com.lianbei.vc.exception.BusinessException;
import com.lianbei.vc.mapper.InstitutionMapper;
import com.lianbei.vc.mapper.ProjectMapper;
import com.lianbei.vc.service.InstitutionService;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class InstitutionServiceImpl extends ServiceImpl<InstitutionMapper, Institution>
    implements InstitutionService {

  private final ProjectMapper projectMapper;
  private final InstitutionDetailAssembler detailAssembler;
  private final InstitutionInvestmentCounter investmentCounter;

  public InstitutionServiceImpl(
      ProjectMapper projectMapper,
      InstitutionDetailAssembler detailAssembler,
      InstitutionInvestmentCounter investmentCounter) {
    this.projectMapper = projectMapper;
    this.detailAssembler = detailAssembler;
    this.investmentCounter = investmentCounter;
  }

  @Override
  public InstitutionDetailVO getInstitutionDetail(Long id) {
    Institution institution = getById(id);
    if (institution == null) {
      throw new BusinessException("机构不存在");
    }
    List<Project> projects = projectMapper.selectList(null);
    return detailAssembler.assemble(institution, projects);
  }

  @Override
  public PageResult<Institution> pageLibrary(
      int pageNum,
      int pageSize,
      String keyword,
      String investmentField,
      String instType,
      String foundedYear) {
    LambdaQueryWrapper<Institution> wrapper = new LambdaQueryWrapper<>();
    if (StringUtils.hasText(keyword)) {
      wrapper.and(
          w ->
              w.like(Institution::getName, keyword)
                  .or()
                  .like(Institution::getEntityName, keyword));
    }
    List<String> fields = FilterParamUtils.splitValues(investmentField);
    if (!fields.isEmpty()) {
      wrapper.in(Institution::getInvestmentField, fields);
    }
    List<String> types = FilterParamUtils.splitValues(instType);
    if (!types.isEmpty()) {
      wrapper.in(Institution::getInstType, types);
    }
    List<String> years = FilterParamUtils.splitValues(foundedYear);
    if (!years.isEmpty()) {
      wrapper.in(Institution::getFoundedYear, years);
    }
    wrapper.orderByDesc(Institution::getEventCount).orderByDesc(Institution::getId);

    Page<Institution> page = page(new Page<>(pageNum, pageSize), wrapper);
    Map<Long, Integer> eventCounts = investmentCounter.countForInstitutions(page.getRecords());
    for (Institution inst : page.getRecords()) {
      inst.setEventCount(eventCounts.getOrDefault(inst.getId(), 0));
    }
    return PageResult.of(page);
  }
}
