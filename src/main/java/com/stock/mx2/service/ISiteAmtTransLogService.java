package com.stock.mx2.service;


import com.github.pagehelper.PageInfo;
import com.stock.mx2.common.ServerResponse;

public interface ISiteAmtTransLogService {
  ServerResponse<PageInfo> transList(Integer paramInteger, String paramString, int paramInt1, int paramInt2);
}
