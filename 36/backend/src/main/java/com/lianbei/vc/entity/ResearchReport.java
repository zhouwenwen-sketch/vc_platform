package com.lianbei.vc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 研究院报告实体
 */
@Data
@TableName("research_report")
public class ResearchReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 报告标题
     */
    private String title;

    /**
     * 简介/摘要
     */
    private String summary;

    /**
     * 报告类型
     */
    private String reportType;

    /**
     * 所属行业
     */
    private String industry;

    /**
     * 特色分类标签，逗号分隔
     */
    private String tags;

    /**
     * 发布日期
     */
    private LocalDate publishDate;

    /**
     * 报告正文
     */
    private String content;

    /**
     * 封面图
     */
    private String coverUrl;

    /**
     * 阅读量
     */
    private Integer viewCount;

    /**
     * 发布人
     */
    @TableField("publish_by")
    private String publishBy;

    /**
     * 状态：0草稿 1已发布
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
