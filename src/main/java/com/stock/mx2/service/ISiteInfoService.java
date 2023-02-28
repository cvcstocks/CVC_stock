package com.stock.mx2.service;


import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.SiteInfo;

public interface ISiteInfoService {
  ServerResponse get();

  ServerResponse add(SiteInfo paramSiteInfo);

  ServerResponse update(SiteInfo paramSiteInfo);

  ServerResponse getInfo();
}
