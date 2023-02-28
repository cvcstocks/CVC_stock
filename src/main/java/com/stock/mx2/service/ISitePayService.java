package com.stock.mx2.service;


import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.SitePay;

public interface ISitePayService {
  ServerResponse add(SitePay paramSitePay);

  ServerResponse listByAdmin(String paramString, int paramInt1, int paramInt2);

  ServerResponse update(SitePay paramSitePay);

  ServerResponse del(Integer paramInteger);

  ServerResponse getPayInfo();

  ServerResponse getPayInfoById(Integer paramInteger);
}
