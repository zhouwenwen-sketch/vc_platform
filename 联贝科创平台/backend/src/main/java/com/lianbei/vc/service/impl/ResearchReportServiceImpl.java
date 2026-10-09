package com.lianbei.vc.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lianbei.vc.common.FilterParamUtils;
import com.lianbei.vc.dto.response.ResearchReportDetailVO;
import com.lianbei.vc.entity.ResearchReport;
import com.lianbei.vc.exception.BusinessException;
import com.lianbei.vc.mapper.ResearchReportMapper;
import com.lianbei.vc.service.ResearchReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 研究院报告Service实现
 */
@Service
@RequiredArgsConstructor
public class ResearchReportServiceImpl implements ResearchReportService {

    private final ResearchReportMapper researchReportMapper;

    @Override
    public IPage<ResearchReport> getReportPage(
        int pageNum, int pageSize, String reportType, String industry, String year, String tag) {
        Page<ResearchReport> page = new Page<>(pageNum, pageSize);
        return researchReportMapper.selectReportPage(
            page,
            FilterParamUtils.splitValues(reportType),
            FilterParamUtils.splitValues(industry),
            FilterParamUtils.splitYears(year),
            FilterParamUtils.splitValues(tag));
    }

    @Override
    public ResearchReportDetailVO getReportDetail(Long id) {
        ResearchReport report = researchReportMapper.selectById(id);
        if (report == null || report.getStatus() == null || report.getStatus() != 1) {
            throw new BusinessException("报告不存在");
        }
        researchReportMapper.incrementViewCount(id);
        if (report.getViewCount() == null) {
            report.setViewCount(0);
        }
        report.setViewCount(report.getViewCount() + 1);
        return ResearchReportAssembler.toDetailVO(report);
    }

    @Override
    public long getTotalCount() {
        return researchReportMapper.selectCount(null);
    }
}
