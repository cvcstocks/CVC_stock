package com.stock.mx2.service;


import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.SiteSmsLog;

import javax.servlet.http.HttpServletRequest;

public interface ISiteSmsLogService {
  ServerResponse smsList(String paramString, int paramInt1, int paramInt2);

  void addData(SiteSmsLog siteSmsLog);

  ServerResponse del(Integer id, HttpServletRequest request);

}
