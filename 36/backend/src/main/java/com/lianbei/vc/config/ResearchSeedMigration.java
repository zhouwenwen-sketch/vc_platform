package com.lianbei.vc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 开发环境：研究院报告种子数据（表为空时导入，UTF-8 安全）
 */
@Component
@Profile("dev")
public class ResearchSeedMigration implements org.springframework.beans.factory.InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(ResearchSeedMigration.class);

  private final JdbcTemplate jdbcTemplate;

  public ResearchSeedMigration(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void afterPropertiesSet() {
    try {
      ensureTables();
      if (!tableExists("research_report")) {
        log.warn("[schema] research_report table missing after ensureTables");
        return;
      }
      if (needsReseed()) {
        log.warn("[schema] research data corrupted or empty, reseeding...");
        jdbcTemplate.execute("DELETE FROM research_report");
        jdbcTemplate.execute("DELETE FROM research_category");
        seedCategories();
        seedReports();
        log.info("[schema] research seed imported (8 reports)");
      } else {
        log.info("[schema] research tables ready");
      }
    } catch (Exception ex) {
      log.warn("[schema] research seed skipped: {}", ex.getMessage());
    }
  }

  /** 标题已变成 ??? 说明 PowerShell 导入乱码，需重写 */
  private boolean needsReseed() {
    Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM research_report", Long.class);
    if (count == null || count == 0) {
      return true;
    }
    Integer bad =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM research_report WHERE title LIKE '?%' OR title LIKE '??%'",
            Integer.class);
    return bad != null && bad > 0;
  }

  private boolean tableExists(String table) {
    Integer n =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
            Integer.class,
            table);
    return n != null && n > 0;
  }

  private void ensureTables() {
    jdbcTemplate.execute(
        """
        CREATE TABLE IF NOT EXISTS research_report (
          id BIGINT PRIMARY KEY AUTO_INCREMENT,
          title VARCHAR(512) NOT NULL,
          summary VARCHAR(2048) DEFAULT NULL,
          report_type VARCHAR(64) DEFAULT NULL,
          industry VARCHAR(64) DEFAULT NULL,
          tags VARCHAR(512) DEFAULT NULL,
          publish_date DATE NOT NULL,
          content TEXT,
          cover_url VARCHAR(512) DEFAULT NULL,
          view_count INT NOT NULL DEFAULT 0,
          publish_by VARCHAR(64) NOT NULL DEFAULT '大学生创投研究院',
          status TINYINT NOT NULL DEFAULT 1,
          create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
          update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
          KEY idx_report_publish_date (publish_date)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
        """);
    jdbcTemplate.execute(
        """
        CREATE TABLE IF NOT EXISTS research_category (
          id BIGINT PRIMARY KEY AUTO_INCREMENT,
          category_type VARCHAR(32) NOT NULL,
          name VARCHAR(64) NOT NULL,
          sort_order INT NOT NULL DEFAULT 0,
          KEY idx_category_type (category_type)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
        """);
  }

  private void seedCategories() {
    String sql =
        "INSERT INTO research_category (category_type, name, sort_order) VALUES (?, ?, ?)";
    Object[][] rows = {
      {"report_type", "行业研究", 1},
      {"report_type", "专题报告", 2},
      {"report_type", "数据洞察", 3},
      {"report_type", "深度分析", 4},
      {"industry", "人工智能", 1},
      {"industry", "医疗健康", 2},
      {"industry", "先进制造", 3},
      {"industry", "新能源", 4},
      {"industry", "消费电商", 5},
      {"industry", "企业服务", 6},
      {"industry", "文化娱乐", 7},
      {"tag", "热门赛道", 1},
      {"tag", "产业洞察", 2},
      {"tag", "前沿技术", 3},
      {"tag", "短篇洞察", 4},
      {"tag", "其他", 5}
    };
    for (Object[] row : rows) {
      jdbcTemplate.update(sql, row[0], row[1], row[2]);
    }
  }

  private void seedReports() {
    String sql =
        "INSERT INTO research_report (title, summary, content, report_type, industry, tags, publish_date, view_count, publish_by, status) VALUES (?,?,?,?,?,?,?,?,?,1)";
    insert(
        sql,
        "中国AI应用出海：算力筑基，场景聚力——《2025年中国AI应用出海企业发展需求洞察报告》发布！",
        "GPU云夯实算力底座，为AI应用出海注入核心动能",
        """
        近年来，全球AI市场迎来爆发式增长，贝恩数据显示，2023年全球人工智能软硬件市场规模已达1,850亿美元，并正以40%-55%的年增速扩张，预计2027年将突破7,800亿-9,900亿美元，其中AI应用市场规模将超4,070亿美元。在这一浪潮下，中国AI应用企业凭借技术代际突破、国内场景创新经验及政策支持，加速向海外市场扩张，成为全球AI生态的重要参与者。

        本报告基于对120余家中国AI出海企业的深度调研，从算力基础设施、场景落地、合规路径与融资需求等维度，系统梳理AI应用出海的发展现状与核心挑战。""",
        "行业研究",
        "人工智能",
        "热门赛道,产业洞察,前沿技术",
        "2025-07-31",
        12580);
    insert(
        sql,
        "2023年中国专精特新系列之优质中小企业进阶路径洞察报告",
        "深入分析专精特新中小企业的发展现状、面临挑战及进阶路径。",
        """
        专精特新中小企业是产业链供应链的关键节点，也是制造强国战略的重要支撑。本报告从政策环境、融资能力、数字化水平与全球化布局四个维度，梳理优质中小企业进阶的关键路径。

        报告认为，下一阶段专精特新企业的核心竞争力将更多体现在「硬科技+细分场景+全球供应链嵌入」的综合能力上。""",
        "专题报告",
        "先进制造",
        "短篇洞察,产业洞察,其他",
        "2023-07-26",
        8920);
    insert(
        sql,
        "2023年中国各省区市专精特新\"小巨人\"企业发展洞察报告",
        "分析全国各省市专精特新小巨人企业分布情况、发展特点及区域差异。",
        """
        报告对全国31个省级行政区的专精特新「小巨人」企业进行统计分析，呈现东部沿海、长三角、珠三角与中西部产业集群的差异化特征。

        从行业分布看，先进制造、新材料、工业软件与医疗器械是「小巨人」企业最为集中的赛道。""",
        "数据洞察",
        "先进制造",
        "短篇洞察,产业洞察,其他",
        "2023-07-07",
        6540);
    insert(
        sql,
        "2025年中国大模型产业发展白皮书",
        "深度剖析大模型技术发展趋势、产业应用场景及商业化路径。",
        "大模型产业正从「参数规模竞赛」转向「行业深度落地」。白皮书覆盖基础模型、推理成本、Agent架构、垂直行业应用与监管合规等关键议题。",
        "深度分析",
        "人工智能",
        "热门赛道,前沿技术",
        "2025-06-15",
        18900);
    insert(
        sql,
        "医疗AI应用场景与商业化路径研究报告",
        "探讨医疗AI在诊断、药物研发、健康管理等场景的应用现状与发展前景。",
        "医疗AI在影像辅助诊断、病理分析与临床决策支持等场景加速落地。本报告梳理头部厂商商业化进展与支付模式创新。",
        "行业研究",
        "医疗健康",
        "前沿技术,产业洞察",
        "2025-05-28",
        11200);
    insert(
        sql,
        "新能源汽车产业链投资价值分析报告",
        "分析新能源汽车产业链各环节投资机会、风险及发展趋势。",
        "从电池、电机、电控到智能驾驶与充换电网络，报告拆解新能源汽车产业链投资地图与关键风险因素。",
        "专题报告",
        "新能源",
        "热门赛道,产业洞察",
        "2025-05-12",
        9850);
    insert(
        sql,
        "2025年消费电商趋势报告：直播电商与内容电商融合发展",
        "研究直播电商与内容电商的融合趋势、商业模式创新及用户行为变化。",
        "直播电商进入「内容化、品牌化、精细化运营」阶段。报告基于50+品牌案例，总结内容种草、私域转化与跨境直播的共性打法。",
        "行业研究",
        "消费电商",
        "热门赛道,产业洞察",
        "2025-04-25",
        14300);
    insert(
        sql,
        "AIGC内容创作行业发展报告",
        "分析AIGC技术在内容创作领域的应用现状、商业模式及未来趋势。",
        "AIGC正在重塑营销文案、短视频脚本、游戏资产生成与设计工作流。报告对比国内外主流工具在中文语境下的可用性与版权边界。",
        "数据洞察",
        "文化娱乐",
        "前沿技术,热门赛道",
        "2025-03-20",
        16500);
  }

  private void insert(String sql, String title, String summary, String content,
                      String reportType, String industry, String tags,
                      String publishDate, int viewCount) {
    jdbcTemplate.update(sql, title, summary, content.trim(), reportType, industry, tags,
        publishDate, viewCount, "大学生创投研究院");
  }
}
