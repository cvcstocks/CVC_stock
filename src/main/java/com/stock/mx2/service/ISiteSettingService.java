package com.stock.mx2.service;


import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.SiteSetting;

public interface ISiteSettingService {
  SiteSetting getSiteSetting();

  ServerResponse update(SiteSetting paramSiteSetting);
}
