package com.lianbei.vc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lianbei.vc.entity.ResearchReport;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 研究院报告Mapper
 */
public interface ResearchReportMapper extends BaseMapper<ResearchReport> {

    /**
     * 分页查询报告列表（支持筛选）
     */
    IPage<ResearchReport> selectReportPage(
            Page<ResearchReport> page,
            @Param("reportTypes") List<String> reportTypes,
            @Param("industries") List<String> industries,
            @Param("years") List<Integer> years,
            @Param("tags") List<String> tags
    );

    /** 阅读量 +1 */
    @Update("UPDATE research_report SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(@Param("id") Long id);
}
