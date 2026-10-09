package com.lianbei.vc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianbei.vc.dto.request.CoverageApplySubmitRequest;
import com.lianbei.vc.dto.response.CoverageApplyCheckVO;
import com.lianbei.vc.dto.response.CoverageApplySubmitResponse;
import com.lianbei.vc.entity.CoverageApplyRecord;
import com.lianbei.vc.entity.Project;
import com.lianbei.vc.exception.BusinessException;
import com.lianbei.vc.mapper.CoverageApplyRecordMapper;
import com.lianbei.vc.mapper.ProjectMapper;
import com.lianbei.vc.service.CoverageApplyService;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class CoverageApplyServiceImpl implements CoverageApplyService {

  private final CoverageApplyRecordMapper coverageApplyRecordMapper;
  private final ProjectMapper projectMapper;
  private final ObjectMapper objectMapper;

  public CoverageApplyServiceImpl(
      CoverageApplyRecordMapper coverageApplyRecordMapper,
      ProjectMapper projectMapper,
      ObjectMapper objectMapper) {
    this.coverageApplyRecordMapper = coverageApplyRecordMapper;
    this.projectMapper = projectMapper;
    this.objectMapper = objectMapper;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public CoverageApplySubmitResponse submit(Long userId, CoverageApplySubmitRequest request) {
    if (userId == null) {
      throw new BusinessException(401, "未登录，请先登录");
    }
    validate(request);

    CoverageApplyRecord existing = findRecord(userId, request.getProjectId());
    if (existing != null && !"rejected".equals(existing.getStatus())) {
      throw new BusinessException("您已对该项目提交过报道申请，请勿重复提交");
    }

    Project project = projectMapper.selectById(request.getProjectId());
    if (project == null) {
      throw new BusinessException("项目不存在，请先完成项目入驻");
    }

    String projectName =
        StringUtils.hasText(request.getProjectName())
            ? request.getProjectName().trim()
            : project.getName();

    try {
      String applyJson = objectMapper.writeValueAsString(request);
      if (existing != null && "rejected".equals(existing.getStatus())) {
        existing.setProjectName(projectName);
        existing.setReportType(request.getReportType().trim());
        existing.setStatus("submitted");
        existing.setApplyData(applyJson);
        existing.setNewsId(null);
        existing.setAuditRemark(null);
        existing.setAuditorId(null);
        existing.setUpdateTime(LocalDateTime.now());
        coverageApplyRecordMapper.updateById(existing);
        return CoverageApplySubmitResponse.builder()
            .id(existing.getId())
            .message("报道申请已重新提交")
            .build();
      }

      CoverageApplyRecord record =
          CoverageApplyRecord.builder()
              .userId(userId)
              .projectId(request.getProjectId())
              .projectName(projectName)
              .reportType(request.getReportType().trim())
              .status("submitted")
              .applyData(applyJson)
              .createTime(LocalDateTime.now())
              .updateTime(LocalDateTime.now())
              .build();
      coverageApplyRecordMapper.insert(record);
      return CoverageApplySubmitResponse.builder()
          .id(record.getId())
          .message("报道申请已提交")
          .build();
    } catch (Exception ex) {
      throw new BusinessException("申请数据保存失败");
    }
  }

  @Override
  public CoverageApplyCheckVO checkApplied(Long userId, Long projectId) {
    if (userId == null || projectId == null) {
      return CoverageApplyCheckVO.builder().applied(false).build();
    }
    CoverageApplyRecord record = findRecord(userId, projectId);
    if (record == null || "rejected".equals(record.getStatus())) {
      return CoverageApplyCheckVO.builder().applied(false).build();
    }
    return CoverageApplyCheckVO.builder()
        .applied(true)
        .status(record.getStatus())
        .recordId(record.getId())
        .build();
  }

  private CoverageApplyRecord findRecord(Long userId, Long projectId) {
    return coverageApplyRecordMapper.selectOne(
        new LambdaQueryWrapper<CoverageApplyRecord>()
            .eq(CoverageApplyRecord::getUserId, userId)
            .eq(CoverageApplyRecord::getProjectId, projectId)
            .last("LIMIT 1"));
  }

  private void validate(CoverageApplySubmitRequest request) {
    if (request == null) {
      throw new BusinessException("申请数据不能为空");
    }
    if (request.getProjectId() == null) {
      throw new BusinessException("请选择要报道的项目");
    }
    if (!StringUtils.hasText(request.getReportType())) {
      throw new BusinessException("请选择报道类型");
    }
    if ("latest_financing".equals(request.getReportType())) {
      boolean hasRound =
          request.getFinancingList() != null
              && request.getFinancingList().stream()
                  .anyMatch(item -> item != null && StringUtils.hasText(item.getRound()));
      if (!hasRound) {
        throw new BusinessException("请填写融资轮次");
      }
    }
    if (!StringUtils.hasText(request.getReportContent())) {
      throw new BusinessException("请填写希望报道的内容");
    }
    if (!StringUtils.hasText(request.getCompetitiveness())) {
      throw new BusinessException("请填写核心竞争力");
    }
    if (!StringUtils.hasText(request.getDebutMedia())) {
      throw new BusinessException("请选择是否首发媒体");
    }
  }
}
