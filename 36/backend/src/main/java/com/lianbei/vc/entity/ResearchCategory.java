package com.lianbei.vc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 研究院报告分类实体
 */
@Data
@TableName("research_category")
public class ResearchCategory {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 类型：report_type/industry/tag
     */
    private String categoryType;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 排序
     */
    private Integer sortOrder;
}
