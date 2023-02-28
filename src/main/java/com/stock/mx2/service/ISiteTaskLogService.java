package com.stock.mx2.service;


import com.github.pagehelper.PageInfo;
import com.stock.mx2.common.ServerResponse;

import javax.servlet.http.HttpServletRequest;

public interface ISiteTaskLogService {
  ServerResponse<PageInfo> taskList(String keywork,String paramString, int paramInt1, int paramInt2);

  ServerResponse del(Integer id, HttpServletRequest request);
}
