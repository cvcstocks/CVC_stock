package com.stock.mx2.service;

import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.SiteIndexSetting;

public interface ISiteIndexSettingService {
  SiteIndexSetting getSiteIndexSetting();
  
  ServerResponse update(SiteIndexSetting paramSiteIndexSetting);
}
