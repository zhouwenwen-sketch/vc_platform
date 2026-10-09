package com.lianbei.vc.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lianbei.vc.common.Result;
import com.lianbei.vc.dto.response.ResearchReportDetailVO;
import com.lianbei.vc.entity.ResearchCategory;
import com.lianbei.vc.entity.ResearchReport;
import com.lianbei.vc.mapper.ResearchCategoryMapper;
import com.lianbei.vc.service.ResearchReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 研究院报告控制器
 */
@RestController
@RequestMapping("/api/research")
@RequiredArgsConstructor
public class ResearchController {

    private final ResearchReportService researchReportService;
    private final ResearchCategoryMapper researchCategoryMapper;

    /**
     * 分页查询报告列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param reportType 报告类型（可选）
     * @param industry 所属行业（可选）
     * @param year 发布年份（可选）
     * @param tag 特色分类标签（可选）
     */
    @GetMapping("/reports")
    public Result<Map<String, Object>> getReportList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String tag) {

        IPage<ResearchReport> page = researchReportService.getReportPage(pageNum, pageSize, reportType, industry, year, tag);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", page.getRecords());
        result.put("total", page.getTotal());
        result.put("pageNum", page.getCurrent());
        result.put("pageSize", page.getSize());
        result.put("hasMore", page.getCurrent() * page.getSize() < page.getTotal());

        return Result.ok(result);
    }

    /**
     * 根据ID查询报告详情
     */
    @GetMapping("/reports/{id}")
    public Result<ResearchReportDetailVO> getReportById(@PathVariable Long id) {
        return Result.ok(researchReportService.getReportDetail(id));
    }

    /**
     * 获取所有分类选项
     */
    @GetMapping("/categories")
    public Result<Map<String, List<ResearchCategory>>> getCategories() {
        Map<String, List<ResearchCategory>> categories = new HashMap<>();
        categories.put("reportTypes", researchCategoryMapper.selectByType("report_type"));
        categories.put("industries", researchCategoryMapper.selectByType("industry"));
        categories.put("tags", researchCategoryMapper.selectByType("tag"));
        return Result.ok(categories);
    }

    /**
     * 获取报告总数
     */
    @GetMapping("/reports/count")
    public Result<Long> getReportCount() {
        return Result.ok(researchReportService.getTotalCount());
    }
}
