package com.lianbei.vc.config;

import com.lianbei.vc.service.impl.InstitutionInvestmentCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** 启动时按融资关联表刷新机构投资事件数，修正 Excel 导入的静态值 */
@Component
public class InstitutionEventCountMigration implements org.springframework.beans.factory.InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(InstitutionEventCountMigration.class);

  private final InstitutionInvestmentCounter investmentCounter;

  public InstitutionEventCountMigration(InstitutionInvestmentCounter investmentCounter) {
    this.investmentCounter = investmentCounter;
  }

  @Override
  public void afterPropertiesSet() {
    try {
      investmentCounter.syncAllEventCounts();
    } catch (Exception ex) {
      log.warn("[schema] institution event_count sync skipped: {}", ex.getMessage());
    }
  }
}
