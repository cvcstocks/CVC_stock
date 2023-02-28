package com.stock.mx2.service;


import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.SiteFuturesSetting;

public interface ISiteFuturesSettingService {
  SiteFuturesSetting getSetting();

  ServerResponse update(SiteFuturesSetting paramSiteFuturesSetting);
}
