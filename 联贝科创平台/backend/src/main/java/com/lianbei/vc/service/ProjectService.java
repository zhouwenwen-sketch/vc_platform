package com.lianbei.vc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lianbei.vc.common.PageResult;
import com.lianbei.vc.dto.response.FinancingEventVO;
import com.lianbei.vc.dto.response.SearchProjectVO;
import com.lianbei.vc.entity.Project;
import java.util.List;

public interface ProjectService extends IService<Project> {

  PageResult<Project> pageProjects(
      int pageNum, int pageSize, String category, String round, String region);

  /** 企业项目库（支持关键字与多维筛选） */
  PageResult<Project> pageLibrary(
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
      String sortBy);

  List<Project> listFeaturedFinancing(int limit);

  Project getDetail(Long id);

  com.lianbei.vc.dto.response.ProjectDetailVO getProjectDetail(Long id);

  /** 首页搜索 - 按关键字匹配项目 */
  PageResult<SearchProjectVO> searchProjects(String keyword, int pageNum, int pageSize);

  /** 入驻申请 - 项目名称是否已存在于项目库（精确匹配） */
  boolean existsByName(String name);

  /** 融资事件列表（每条对应一个 project） */
  PageResult<FinancingEventVO> pageFinancingEvents(
      int pageNum,
      int pageSize,
      String industry,
      String region,
      String regionScope,
      String round,
      String financingYear,
      String currency);
}
