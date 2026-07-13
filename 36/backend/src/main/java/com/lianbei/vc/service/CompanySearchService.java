package com.lianbei.vc.service;

import java.util.List;

/** 企业主体模糊搜索（入驻表单） */
public interface CompanySearchService {

  List<String> search(String keyword, int limit);
}
