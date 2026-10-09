package com.lianbei.vc.common;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lianbei.vc.entity.Project;
import org.springframework.util.StringUtils;

/** 项目库关键字筛选（名称、简介、标签、行业、成立年份、工商全称） */
public final class ProjectKeywordQuery {

  private ProjectKeywordQuery() {}

  public static void apply(LambdaQueryWrapper<Project> wrapper, String keyword) {
    if (!StringUtils.hasText(keyword)) {
      return;
    }
    String kw = keyword.trim();
    wrapper.and(
        w ->
            w.like(Project::getName, kw)
                .or()
                .like(Project::getCompanyDesc, kw)
                .or()
                .like(Project::getTags, kw)
                .or()
                .like(Project::getCategory, kw)
                .or()
                .like(Project::getFoundingYear, kw)
                .or()
                .apply(
                    "EXISTS (SELECT 1 FROM project_business pb WHERE pb.project_id = project.id AND pb.full_name LIKE {0})",
                    "%" + kw + "%"));
  }
}
