package com.stock.mx2.service;


import com.github.pagehelper.PageInfo;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.User;

import javax.servlet.http.HttpServletRequest;

public interface ISiteLoginLogService {
  ServerResponse saveLog(User paramUser, HttpServletRequest paramHttpServletRequest);

  ServerResponse<PageInfo> loginList(Integer paramInteger, int paramInt1, int paramInt2);

  int deleteByUserId(Integer paramInteger);

  ServerResponse del(Integer id, HttpServletRequest request);
}
