package com.lianbei.vc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lianbei.vc.entity.ResearchCategory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 研究院报告分类Mapper
 */
public interface ResearchCategoryMapper extends BaseMapper<ResearchCategory> {

    /**
     * 根据类型查询分类列表
     */
    List<ResearchCategory> selectByType(@Param("categoryType") String categoryType);
}
