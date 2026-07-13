package com.lianbei.vc.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lianbei.vc.dto.response.ResearchReportDetailVO;
import com.lianbei.vc.entity.ResearchReport;

/**
 * 研究院报告Service
 */
public interface ResearchReportService {

    /**
     * 分页查询报告列表
     */
    IPage<ResearchReport> getReportPage(
        int pageNum, int pageSize, String reportType, String industry, String year, String tag);

    /**
     * 报告详情（含阅读量 +1）
     */
    ResearchReportDetailVO getReportDetail(Long id);

    /**
     * 获取报告总数
     */
    long getTotalCount();
}
